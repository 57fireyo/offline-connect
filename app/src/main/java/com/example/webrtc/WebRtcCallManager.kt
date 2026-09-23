package com.example.webrtc

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.data.model.CallState
import com.example.network.NearbyConnectionsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcCallManager(
    private val context: Context,
    private val nearbyManager: com.example.network.NearbyManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    companion object {
        private const val TAG = "WebRtcCallManager"
    }

    private var eglBase: EglBase? = null
    val eglContext: EglBase.Context?
        get() = eglBase?.eglBaseContext

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // State flows for UI binding
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _activePeerId = MutableStateFlow<String?>(null)
    val activePeerId: StateFlow<String?> = _activePeerId.asStateFlow()

    private val _activePeerName = MutableStateFlow("Remote Peer")
    val activePeerName: StateFlow<String> = _activePeerName.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isAudioMuted = MutableStateFlow(false)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    private val _isSpeakerphoneOn = MutableStateFlow(true)
    val isSpeakerphoneOn: StateFlow<Boolean> = _isSpeakerphoneOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    // Live Metrics for Adaptive Video & Audio quality
    private val _currentBitrateKbps = MutableStateFlow(1250)
    val currentBitrateKbps: StateFlow<Int> = _currentBitrateKbps.asStateFlow()

    private val _currentFps = MutableStateFlow(30)
    val currentFps: StateFlow<Int> = _currentFps.asStateFlow()

    private val _isVoiceFallbackActive = MutableStateFlow(false)
    val isVoiceFallbackActive: StateFlow<Boolean> = _isVoiceFallbackActive.asStateFlow()

    private var durationJob: Job? = null
    private var metricsJob: Job? = null

    init {
        observeIncomingSignaling()
    }

    private fun ensurePeerConnectionFactory(): PeerConnectionFactory? {
        if (peerConnectionFactory != null) return peerConnectionFactory
        return try {
            if (eglBase == null) {
                eglBase = EglBase.create()
            }
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            val eglCtx = eglBase?.eglBaseContext
            val encoderFactory = if (eglCtx != null) DefaultVideoEncoderFactory(eglCtx, true, true) else null
            val decoderFactory = if (eglCtx != null) DefaultVideoDecoderFactory(eglCtx) else null

            var builder = PeerConnectionFactory.builder()
            if (encoderFactory != null) builder = builder.setVideoEncoderFactory(encoderFactory)
            if (decoderFactory != null) builder = builder.setVideoDecoderFactory(decoderFactory)

            val factory = builder.createPeerConnectionFactory()
            peerConnectionFactory = factory
            factory
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize WebRTC PeerConnectionFactory lazily", e)
            null
        }
    }

    private fun observeIncomingSignaling() {
        scope.launch {
            nearbyManager.webRtcSignals.collect { (senderPeerId, signalJson) ->
                handleIncomingSignal(senderPeerId, signalJson)
            }
        }
    }

    /**
     * Start an Outgoing Call (Voice or Video)
     */
    fun startCall(peerId: String, peerName: String, isVideo: Boolean = true) {
        _activePeerId.value = peerId
        _activePeerName.value = peerName
        _isVideoEnabled.value = isVideo
        _callState.value = CallState.OUTGOING
        _isVoiceFallbackActive.value = false

        setupLocalTracks(isVideo)
        createPeerConnection()

        if (nearbyManager.isDemoMode.value && (peerId.startsWith("NODE-SAR") || peerId.startsWith("NODE-MEDIC"))) {
            // In demo mode, simulate peer answering after 2 seconds
            scope.launch {
                delay(2000)
                _callState.value = CallState.CONNECTED
                startCallMetrics()
            }
        } else {
            // Create SDP Offer and transmit over Nearby Connections
            createOffer(peerId)
        }
    }

    /**
     * Answer an Incoming Call
     */
    fun answerCall() {
        val peerId = _activePeerId.value ?: return
        _callState.value = CallState.CONNECTED
        setupLocalTracks(_isVideoEnabled.value)
        createAnswer(peerId)
        startCallMetrics()
    }

    /**
     * Decline or End Call
     */
    fun endCall() {
        val peerId = _activePeerId.value
        if (peerId != null) {
            val hangup = JSONObject().apply {
                put("signalType", "HANGUP")
            }
            nearbyManager.sendWebRtcSignal(peerId, hangup)
        }

        cleanupCall()
        _callState.value = CallState.ENDED

        scope.launch {
            delay(1000)
            _callState.value = CallState.IDLE
        }
    }

    /**
     * Toggle Mute Mic
     */
    fun toggleMute() {
        val newMute = !_isAudioMuted.value
        _isAudioMuted.value = newMute
        localAudioTrack?.setEnabled(!newMute)
    }

    /**
     * Toggle Video On/Off (Fallback to Voice-Only)
     */
    fun toggleVideo() {
        val newVideo = !_isVideoEnabled.value
        _isVideoEnabled.value = newVideo
        localVideoTrack?.setEnabled(newVideo)
        _isVoiceFallbackActive.value = !newVideo

        // Notify peer about voice fallback
        val peerId = _activePeerId.value
        if (peerId != null) {
            val signal = JSONObject().apply {
                put("signalType", if (newVideo) "VIDEO_RESUME" else "FALLBACK_VOICE")
            }
            nearbyManager.sendWebRtcSignal(peerId, signal)
        }
    }

    /**
     * Switch Front / Back Camera
     */
    fun switchCamera() {
        val newFront = !_isFrontCamera.value
        _isFrontCamera.value = newFront
        // cameraCapturer?.switchCamera(null)
    }

    /**
     * Toggle Speakerphone
     */
    fun toggleSpeakerphone() {
        val newSpeaker = !_isSpeakerphoneOn.value
        _isSpeakerphoneOn.value = newSpeaker
        audioManager.isSpeakerphoneOn = newSpeaker
    }

    private fun setupLocalTracks(enableVideo: Boolean) {
        val factory = ensurePeerConnectionFactory() ?: return
        try {
            // Audio Track
            val audioConstraints = MediaConstraints()
            audioSource = factory.createAudioSource(audioConstraints)
            localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource)

            // Video Track
            if (enableVideo) {
                videoCapturer = createCameraCapturer()
                val eglCtx = eglContext
                if (videoCapturer != null && eglCtx != null) {
                    surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglCtx)
                    videoSource = factory.createVideoSource(videoCapturer!!.isScreencast)
                    videoCapturer!!.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
                    videoCapturer!!.startCapture(640, 480, 30)
                    localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up local media tracks", e)
        }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        return try {
            val enumerator = Camera2Enumerator(context)
            val deviceNames = enumerator.deviceNames
            // Prefer front camera
            for (name in deviceNames) {
                if (enumerator.isFrontFacing(name)) {
                    val capturer = enumerator.createCapturer(name, null)
                    if (capturer != null) return capturer
                }
            }
            // Fallback to any camera
            for (name in deviceNames) {
                val capturer = enumerator.createCapturer(name, null)
                if (capturer != null) return capturer
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Camera capturer unavailable", e)
            null
        }
    }

    private fun createPeerConnection() {
        val factory = ensurePeerConnectionFactory() ?: return
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList()).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                val peerId = _activePeerId.value ?: return
                val json = JSONObject().apply {
                    put("signalType", "CANDIDATE")
                    put("sdpMid", candidate.sdpMid)
                    put("sdpMLineIndex", candidate.sdpMLineIndex)
                    put("sdp", candidate.sdp)
                }
                nearbyManager.sendWebRtcSignal(peerId, json)
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                Log.d(TAG, "WebRTC remote track received")
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                Log.d(TAG, "ICE Connection State: $state")
                if (state == PeerConnection.IceConnectionState.CONNECTED) {
                    _callState.value = CallState.CONNECTED
                    startCallMetrics()
                } else if (state == PeerConnection.IceConnectionState.DISCONNECTED ||
                    state == PeerConnection.IceConnectionState.FAILED) {
                    // Trigger adaptive voice-only fallback
                    _isVoiceFallbackActive.value = true
                }
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(dataChannel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
        })

        // Add local tracks
        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("ARDAMS")) }
        localVideoTrack?.let { peerConnection?.addTrack(it, listOf("ARDAMS")) }
    }

    private fun createOffer(peerId: String) {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(this, desc)
                val json = JSONObject().apply {
                    put("signalType", "OFFER")
                    put("sdp", desc.description)
                }
                nearbyManager.sendWebRtcSignal(peerId, json)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(err: String) {
                Log.e(TAG, "Create Offer failed: $err")
            }
            override fun onSetFailure(err: String) {}
        }, constraints)
    }

    private fun createAnswer(peerId: String) {
        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(this, desc)
                val json = JSONObject().apply {
                    put("signalType", "ANSWER")
                    put("sdp", desc.description)
                }
                nearbyManager.sendWebRtcSignal(peerId, json)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(err: String) {
                Log.e(TAG, "Create Answer failed: $err")
            }
            override fun onSetFailure(err: String) {}
        }, constraints)
    }

    private fun handleIncomingSignal(senderPeerId: String, signalJson: JSONObject) {
        val signalType = signalJson.optString("signalType")
        when (signalType) {
            "OFFER" -> {
                _activePeerId.value = senderPeerId
                _activePeerName.value = "Peer ($senderPeerId)"
                _callState.value = CallState.INCOMING

                createPeerConnection()
                val sdp = signalJson.optString("sdp")
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, SessionDescription(SessionDescription.Type.OFFER, sdp))
            }
            "ANSWER" -> {
                val sdp = signalJson.optString("sdp")
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        _callState.value = CallState.CONNECTED
                        startCallMetrics()
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, SessionDescription(SessionDescription.Type.ANSWER, sdp))
            }
            "CANDIDATE" -> {
                val sdpMid = signalJson.optString("sdpMid")
                val sdpMLineIndex = signalJson.optInt("sdpMLineIndex")
                val sdp = signalJson.optString("sdp")
                val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                peerConnection?.addIceCandidate(candidate)
            }
            "HANGUP" -> {
                endCall()
            }
            "FALLBACK_VOICE" -> {
                _isVoiceFallbackActive.value = true
            }
            "VIDEO_RESUME" -> {
                _isVoiceFallbackActive.value = false
            }
        }
    }

    private fun startCallMetrics() {
        durationJob?.cancel()
        _callDurationSeconds.value = 0
        durationJob = scope.launch {
            while (isActive && _callState.value == CallState.CONNECTED) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }

        metricsJob?.cancel()
        metricsJob = scope.launch {
            while (isActive && _callState.value == CallState.CONNECTED) {
                delay(2500)
                // Adaptive bitrate simulation based on P2P link
                if (_isVoiceFallbackActive.value) {
                    _currentBitrateKbps.value = (48..64).random()
                    _currentFps.value = 0
                } else {
                    _currentBitrateKbps.value = (980..1450).random()
                    _currentFps.value = (28..30).random()
                }
            }
        }
    }

    private fun cleanupCall() {
        durationJob?.cancel()
        metricsJob?.cancel()
        runCatching {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null

            localVideoTrack?.dispose()
            localVideoTrack = null

            videoSource?.dispose()
            videoSource = null

            localAudioTrack?.dispose()
            localAudioTrack = null

            audioSource?.dispose()
            audioSource = null

            peerConnection?.close()
            peerConnection = null
        }
        _activePeerId.value = null
    }
}
