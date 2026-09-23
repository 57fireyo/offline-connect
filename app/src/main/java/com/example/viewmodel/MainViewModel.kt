package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoManager
import com.example.crypto.SafetyNumberInfo
import com.example.data.model.CallState
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationSummary
import com.example.data.model.EmergencyType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SosAlertEntity
import com.example.data.model.UrgencyLevel
import com.example.data.model.UserProfile
import com.example.data.repository.OffGridRepository
import com.example.network.NearbyManager
import com.example.network.NearbyConnectionsManager
import com.example.webrtc.WebRtcCallManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val cryptoManager = CryptoManager()
    val repository = OffGridRepository(application, cryptoManager, viewModelScope)
    val nearbyManager = NearbyManager(application, repository, cryptoManager, viewModelScope)
    val webRtcManager = WebRtcCallManager(application, nearbyManager, viewModelScope)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val globalConnectionState: StateFlow<ConnectionState> = nearbyManager.globalConnectionState
    val isDiscovering: StateFlow<Boolean> = nearbyManager.isDiscovering
    val isDemoMode: StateFlow<Boolean> = nearbyManager.isDemoMode

    val nearbyContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationSummary>> = combine(
        repository.allContacts,
        repository.allMessages
    ) { contacts, messages ->
        contacts.map { contact ->
            val peerMessages = messages.filter { it.chatPeerId == contact.peerId }
            val lastMsg = peerMessages.maxByOrNull { it.timestamp }
            val unread = peerMessages.count { it.senderId != userProfile.value.peerId && it.status != MessageStatus.SEEN }
            val queued = peerMessages.count { it.status == MessageStatus.QUEUED }
            ConversationSummary(
                contact = contact,
                lastMessage = lastMsg,
                unreadCount = unread,
                queuedCount = queued
            )
        }.sortedWith(
            compareByDescending<ConversationSummary> { it.lastMessage?.timestamp ?: 0L }
                .thenByDescending { it.contact.connectionState == ConnectionState.CONNECTED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUnreadCount: StateFlow<Int> = conversations
        .combine(MutableStateFlow(0)) { list, _ ->
            list.sumOf { it.unreadCount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sosAlerts: StateFlow<List<SosAlertEntity>> = repository.allSosAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeContact = MutableStateFlow<ContactEntity?>(null)
    val activeContact: StateFlow<ContactEntity?> = _activeContact.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeContact
        .flatMapLatest { contact ->
            if (contact != null) {
                repository.getMessagesForPeer(contact.peerId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _safetyNumber = MutableStateFlow<SafetyNumberInfo?>(null)
    val safetyNumber: StateFlow<SafetyNumberInfo?> = _safetyNumber.asStateFlow()

    val typingStatus: StateFlow<Map<String, Boolean>> = nearbyManager.typingStatus

    // WebRTC call states
    val callState: StateFlow<CallState> = webRtcManager.callState
    val activeCallPeerName: StateFlow<String> = webRtcManager.activePeerName
    val isVideoCall: StateFlow<Boolean> = webRtcManager.isVideoEnabled
    val isAudioMuted: StateFlow<Boolean> = webRtcManager.isAudioMuted
    val isSpeakerphoneOn: StateFlow<Boolean> = webRtcManager.isSpeakerphoneOn
    val isFrontCamera: StateFlow<Boolean> = webRtcManager.isFrontCamera
    val callDurationSeconds: StateFlow<Int> = webRtcManager.callDurationSeconds
    val currentBitrateKbps: StateFlow<Int> = webRtcManager.currentBitrateKbps
    val currentFps: StateFlow<Int> = webRtcManager.currentFps
    val isVoiceFallbackActive: StateFlow<Boolean> = webRtcManager.isVoiceFallbackActive

    init {
        // We do not start hardware discovery synchronously in constructor
        // to avoid ungranted permissions IPC aborts on Android 12+ / emulators.
    }

    fun startNearbyMesh() {
        nearbyManager.startNearbyMesh()
    }

    fun stopNearbyMesh() {
        nearbyManager.stopNearbyMesh()
    }

    fun toggleDemoMode() {
        val next = !nearbyManager.isDemoMode.value
        nearbyManager.setDemoMode(next)
    }

    fun selectContact(contact: ContactEntity) {
        _activeContact.value = contact
        if (contact.publicKeyBase64.isNotEmpty()) {
            _safetyNumber.value = cryptoManager.calculateSafetyNumber(contact.publicKeyBase64)
        } else {
            _safetyNumber.value = cryptoManager.calculateSafetyNumber(cryptoManager.exportPublicKey())
        }
        viewModelScope.launch {
            repository.markAllSeen(contact.peerId)
        }
    }

    fun clearActiveContact() {
        _activeContact.value = null
        _safetyNumber.value = null
    }

    fun sendChatMessage(
        text: String,
        attachmentBase64: String? = null,
        fileName: String? = null,
        fileSizeBytes: Long = 0
    ) {
        val contact = _activeContact.value ?: return
        if (text.isBlank() && attachmentBase64 == null) return

        viewModelScope.launch {
            val messageType = when {
                attachmentBase64 != null && fileName?.endsWith(".jpg", ignoreCase = true) == true -> MessageType.IMAGE
                attachmentBase64 != null -> MessageType.FILE
                else -> MessageType.TEXT
            }

            val msg = repository.createOutgoingMessage(
                peerId = contact.peerId,
                content = text,
                type = messageType,
                attachmentData = attachmentBase64,
                fileName = fileName,
                fileSizeBytes = fileSizeBytes
            )
            nearbyManager.sendChatMessage(msg)
        }
    }

    fun sendTypingNotification(isTyping: Boolean) {
        val contact = _activeContact.value ?: return
        nearbyManager.sendTyping(contact.peerId, isTyping)
    }

    fun verifyContact(peerId: String, isVerified: Boolean) {
        viewModelScope.launch {
            repository.verifyContact(peerId, isVerified)
            val current = _activeContact.value
            if (current != null && current.peerId == peerId) {
                _activeContact.value = current.copy(isVerified = isVerified)
            }
        }
    }

    fun connectPeer(contact: ContactEntity) {
        contact.endpointId?.let { endpointId ->
            nearbyManager.connectToPeer(endpointId)
        }
    }

    fun disconnectPeer(contact: ContactEntity) {
        nearbyManager.disconnectPeer(contact.peerId)
    }

    fun clearChat(peerId: String) {
        viewModelScope.launch {
            repository.clearChat(peerId)
        }
    }

    // Call controls
    fun startCall(peerId: String, peerName: String, isVideo: Boolean) {
        webRtcManager.startCall(peerId, peerName, isVideo)
    }

    fun answerCall() {
        webRtcManager.answerCall()
    }

    fun endCall() {
        webRtcManager.endCall()
    }

    fun toggleMute() {
        webRtcManager.toggleMute()
    }

    fun toggleVideo() {
        webRtcManager.toggleVideo()
    }

    fun switchCamera() {
        webRtcManager.switchCamera()
    }

    fun toggleSpeakerphone() {
        webRtcManager.toggleSpeakerphone()
    }

    // SOS Broadcast
    fun broadcastSos(type: EmergencyType, urgency: UrgencyLevel, notes: String, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            val alert = repository.createAndBroadcastSos(
                type = type,
                urgency = urgency,
                lat = lat ?: 36.6002, // GPS coordinates
                lon = lon ?: -118.0583,
                notes = notes
            )
            nearbyManager.broadcastSos(alert)
        }
    }

    fun acknowledgeSos(alertId: String) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }

    fun updateProfile(name: String, callsign: String, avatar: Int, relay: Boolean, batterySaver: Boolean) {
        repository.updateProfile(name, callsign, avatar, relay, batterySaver)
    }

    override fun onCleared() {
        super.onCleared()
        nearbyManager.stopNearbyMesh()
    }
}
