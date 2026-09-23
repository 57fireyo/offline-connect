import React, { useEffect, useRef, useState } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalAvatar } from '../common/TacticalAvatar';
import { realP2PService } from '../../services/realP2PService';
import {
  Mic,
  MicOff,
  Video as VideoIcon,
  VideoOff,
  PhoneOff,
  Volume2,
  VolumeX,
  SwitchCamera,
  Radio,
  Lock,
  PhoneCall
} from 'lucide-react';

export const CallScreen: React.FC = () => {
  const {
    callSession,
    endCall,
    answerCall,
    toggleMute,
    toggleVideo,
    switchCamera,
    toggleSpeakerphone
  } = useApp();

  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRef = useRef<HTMLVideoElement>(null);
  const [remoteStream, setRemoteStream] = useState<MediaStream | null>(null);

  const {
    active,
    peer,
    callState,
    durationSeconds,
    isAudioMuted,
    isVideoEnabled,
    isSpeakerphoneOn,
    bitrateKbps,
    fps,
    isVoiceFallback
  } = callSession;

  // Listen for remote real video stream from other phone
  useEffect(() => {
    const unsub = realP2PService.onRemoteMedia((stream) => {
      setRemoteStream(stream);
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = stream;
      }
    });
    return () => unsub();
  }, []);

  // Bind local camera & microphone to local video element
  useEffect(() => {
    if (active && isVideoEnabled && !isVoiceFallback) {
      realP2PService.getLocalMedia(true, true)
        .then(stream => {
          if (localVideoRef.current) {
            localVideoRef.current.srcObject = stream;
          }
        })
        .catch(err => {
          console.warn('Local media binding fallback:', err);
        });
    }
  }, [active, isVideoEnabled, isVoiceFallback]);

  if (!active || !peer) {
    return null;
  }

  const formatDuration = (sec: number) => {
    const mins = Math.floor(sec / 60).toString().padStart(2, '0');
    const s = (sec % 60).toString().padStart(2, '0');
    return `${mins}:${s}`;
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#090D12] text-[#F9FAFB] flex flex-col justify-between select-none overflow-hidden font-mono-tactical">
      {/* Tactical HUD Corner Brackets */}
      <div className="pointer-events-none absolute inset-4 border border-[#26354A]/30">
        <div className="absolute -top-1 -left-1 w-6 h-6 border-t-2 border-l-2 border-[#10B981]" />
        <div className="absolute -top-1 -right-1 w-6 h-6 border-t-2 border-r-2 border-[#10B981]" />
        <div className="absolute -bottom-1 -left-1 w-6 h-6 border-b-2 border-l-2 border-[#10B981]" />
        <div className="absolute -bottom-1 -right-1 w-6 h-6 border-b-2 border-r-2 border-[#10B981]" />
      </div>

      {/* Top Telemetry Overlay */}
      <div className="relative z-10 bg-[#111822]/90 backdrop-blur-md border-b border-[#26354A] p-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <TacticalAvatar
            name={peer.displayName}
            callsign={peer.callsign}
            size={46}
            avatarColorIndex={peer.avatarColorIndex}
            isOnline={true}
          />

          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-[15px] text-[#F9FAFB] font-sans">
                {peer.displayName}
              </span>
              <span className="text-[10px] text-[#F59E0B] font-bold bg-[#F59E0B]/10 px-1.5 py-0.5 rounded-xs border border-[#F59E0B]/30">
                [{peer.callsign}]
              </span>
            </div>

            <div className="flex items-center gap-2 text-[11px] text-[#9CA3AF]">
              <span
                className={`font-bold ${
                  callState === 'CONNECTED'
                    ? 'text-[#10B981]'
                    : callState === 'INCOMING'
                    ? 'text-[#F59E0B] animate-pulse'
                    : 'text-[#06B6D4] animate-pulse'
                }`}
              >
                {callState === 'CONNECTED'
                  ? isVoiceFallback
                    ? 'DIRECT VOICE LINK'
                    : 'DIRECT P2P VIDEO STREAM'
                  : callState === 'INCOMING'
                  ? 'INCOMING CALL FROM PHONE...'
                  : 'CALLING PEER PHONE...'}
              </span>
              {callState === 'CONNECTED' && (
                <>
                  <span>•</span>
                  <span className="text-[#F9FAFB] font-bold">{formatDuration(durationSeconds)}</span>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Live Metrics */}
        <div className="flex flex-col items-end text-[10px] text-[#9CA3AF]">
          <div className="flex items-center gap-1.5 text-[#10B981]">
            <Lock className="w-3 h-3 text-[#10B981]" />
            <span>WEBRTC SECURE P2P</span>
          </div>
          <span className="text-[#06B6D4] font-bold">{bitrateKbps} kbps</span>
          <span>{fps > 0 ? `${fps} FPS` : 'AUDIO ONLY'}</span>
        </div>
      </div>

      {/* Center Video Area */}
      <div className="relative flex-1 flex items-center justify-center bg-[#0B141E] overflow-hidden">
        {/* Real Remote Video Feed */}
        {!isVoiceFallback && isVideoEnabled ? (
          <div className="relative w-full h-full flex items-center justify-center">
            {remoteStream ? (
              <video
                ref={remoteVideoRef}
                autoPlay
                playsInline
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="flex flex-col items-center gap-4 z-10">
                <div className="relative w-28 h-28 rounded-full bg-[#1B2636] border-2 border-[#10B981] flex items-center justify-center shadow-[0_0_30px_rgba(16,185,129,0.3)]">
                  <TacticalAvatar
                    name={peer.displayName}
                    callsign={peer.callsign}
                    size={100}
                    avatarColorIndex={peer.avatarColorIndex}
                    isOnline={true}
                  />
                </div>

                <div className="text-center">
                  <span className="text-[14px] font-bold text-[#F9FAFB] block font-sans">
                    {callState === 'CONNECTED' ? 'CONNECTED TO REAL PHONE CAMERA' : 'ESTABLISHING P2P VIDEO LINK...'}
                  </span>
                  <span className="text-[11px] text-[#06B6D4]">
                    Zero-server • Direct phone-to-phone media stream
                  </span>
                </div>
              </div>
            )}

            {/* Local Phone Camera Preview (Bottom Right PIP) */}
            <div className="absolute bottom-6 right-6 w-32 h-44 rounded-xl bg-[#111822] border-2 border-[#10B981] overflow-hidden shadow-2xl z-20 flex items-center justify-center">
              <video
                ref={localVideoRef}
                autoPlay
                playsInline
                muted
                className="w-full h-full object-cover"
              />
              <div className="absolute bottom-1 left-1.5 text-[8px] font-bold text-[#10B981] bg-black/80 px-1 rounded-xs">
                MY CAMERA
              </div>
            </div>
          </div>
        ) : (
          /* Voice Only Fallback Mode */
          <div className="flex flex-col items-center justify-center gap-6 z-10">
            <div className="relative flex items-center justify-center">
              <div className="absolute w-56 h-56 rounded-full border border-[#10B981]/20 animate-ping" />
              <div className="absolute w-44 h-44 rounded-full border border-[#10B981]/30 animate-pulse" />
              <div className="w-32 h-32 rounded-full bg-[#1B2636] border-2 border-[#10B981] flex items-center justify-center shadow-[0_0_35px_rgba(16,185,129,0.3)]">
                <Radio className="w-12 h-12 text-[#10B981] animate-pulse" />
              </div>
            </div>

            <div className="text-center flex flex-col gap-1">
              <span className="text-[14px] font-bold text-[#10B981]">
                DIRECT VOICE LINK ACTIVE
              </span>
              <span className="text-[11px] text-[#9CA3AF] font-sans">
                High definition audio with echo cancellation
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Control Dock */}
      <div className="relative z-10 bg-[#111822]/95 backdrop-blur-md border-t border-[#26354A] p-5 flex items-center justify-center gap-4">
        {callState === 'INCOMING' ? (
          <div className="flex items-center gap-8">
            <button
              type="button"
              onClick={endCall}
              className="w-16 h-16 rounded-full bg-[#EF4444] text-white flex items-center justify-center shadow-xl hover:bg-[#dc2626] transition-transform active:scale-95 cursor-pointer"
              title="Decline"
            >
              <PhoneOff className="w-7 h-7" />
            </button>
            <button
              type="button"
              onClick={answerCall}
              className="w-16 h-16 rounded-full bg-[#10B981] text-black flex items-center justify-center shadow-xl hover:bg-[#0ea372] transition-transform active:scale-95 animate-bounce cursor-pointer"
              title="Answer Call"
            >
              <PhoneCall className="w-7 h-7" />
            </button>
          </div>
        ) : (
          <>
            {/* Toggle Mute */}
            <button
              type="button"
              onClick={toggleMute}
              className={`w-13 h-13 rounded-full flex items-center justify-center border transition-all cursor-pointer ${
                isAudioMuted
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#243348] border-[#26354A] text-[#F9FAFB] hover:bg-[#2c3d56]'
              }`}
              title={isAudioMuted ? 'Unmute microphone' : 'Mute microphone'}
            >
              {isAudioMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
            </button>

            {/* Toggle Video */}
            <button
              type="button"
              onClick={toggleVideo}
              className={`w-13 h-13 rounded-full flex items-center justify-center border transition-all cursor-pointer ${
                !isVideoEnabled || isVoiceFallback
                  ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#EF4444]'
                  : 'bg-[#243348] border-[#26354A] text-[#F9FAFB] hover:bg-[#2c3d56]'
              }`}
              title={isVideoEnabled ? 'Disable camera' : 'Enable camera'}
            >
              {!isVideoEnabled || isVoiceFallback ? (
                <VideoOff className="w-5 h-5" />
              ) : (
                <VideoIcon className="w-5 h-5" />
              )}
            </button>

            {/* Switch Camera */}
            {!isVoiceFallback && isVideoEnabled && (
              <button
                type="button"
                onClick={switchCamera}
                className="w-13 h-13 rounded-full bg-[#243348] border border-[#26354A] text-[#F9FAFB] hover:bg-[#2c3d56] flex items-center justify-center transition-all cursor-pointer"
                title="Switch Camera (Front/Back)"
              >
                <SwitchCamera className="w-5 h-5" />
              </button>
            )}

            {/* Speakerphone Toggle */}
            <button
              type="button"
              onClick={toggleSpeakerphone}
              className={`w-13 h-13 rounded-full flex items-center justify-center border transition-all cursor-pointer ${
                isSpeakerphoneOn
                  ? 'bg-[#06B6D4]/20 border-[#06B6D4] text-[#06B6D4]'
                  : 'bg-[#243348] border-[#26354A] text-[#9CA3AF] hover:bg-[#2c3d56]'
              }`}
              title={isSpeakerphoneOn ? 'Speakerphone ON' : 'Earpiece Mode'}
            >
              {isSpeakerphoneOn ? <Volume2 className="w-5 h-5" /> : <VolumeX className="w-5 h-5" />}
            </button>

            {/* End Call Button */}
            <button
              type="button"
              onClick={endCall}
              className="w-14 h-14 rounded-full bg-[#EF4444] text-white flex items-center justify-center border border-[#EF4444] shadow-[0_0_20px_rgba(239,68,68,0.5)] hover:bg-[#dc2626] transition-all cursor-pointer"
              title="End Call"
            >
              <PhoneOff className="w-6 h-6" />
            </button>
          </>
        )}
      </div>
    </div>
  );
};
