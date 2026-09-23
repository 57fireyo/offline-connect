import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalAvatar } from '../common/TacticalAvatar';
import {
  Shield,
  Radio,
  BatteryCharging,
  Cpu,
  Check,
  Smartphone
} from 'lucide-react';

export const SettingsScreen: React.FC = () => {
  const {
    userProfile,
    updateProfile,
    isDemoMode,
    toggleDemoMode
  } = useApp();

  const [displayName, setDisplayName] = useState(userProfile.displayName);
  const [callsign, setCallsign] = useState(userProfile.callsign);
  const [avatarIndex, setAvatarIndex] = useState(userProfile.avatarColorIndex);
  const [meshRelay, setMeshRelay] = useState(userProfile.meshRelayEnabled);
  const [batterySaver, setBatterySaver] = useState(userProfile.batterySaverEnabled);
  const [isSaved, setIsSaved] = useState(false);

  const handleSave = () => {
    updateProfile(displayName, callsign, avatarIndex, meshRelay, batterySaver);
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 2500);
  };

  const avatarColorOptions = [
    { label: 'Tactical Green', color: '#10B981', bg: '#064E3B' },
    { label: 'Emergency Amber', color: '#F59E0B', bg: '#78350F' },
    { label: 'Radio Cyan', color: '#06B6D4', bg: '#164E63' },
    { label: 'Distress Red', color: '#EF4444', bg: '#7F1D1D' }
  ];

  return (
    <div className="w-full max-w-4xl mx-auto px-4 py-4 pb-28 flex flex-col gap-4">
      <span className="font-mono-tactical text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wider">
        OPERATOR PROFILE & SETTINGS
      </span>

      {/* 1. Operator Identity Card */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4.5 flex flex-col gap-4 shadow-md">
        <div className="flex items-center gap-3.5">
          <TacticalAvatar
            name={displayName || 'Operator'}
            callsign={callsign}
            size={56}
            avatarColorIndex={avatarIndex}
            isOnline={true}
          />

          <div>
            <span className="text-[14px] font-bold text-[#F9FAFB] block">
              Local Node Identity
            </span>
            <span className="font-mono-tactical text-[12px] text-[#10B981]">
              {userProfile.peerId}
            </span>
          </div>
        </div>

        {/* Name input */}
        <div className="flex flex-col gap-1">
          <label className="text-[11px] font-mono-tactical font-bold text-[#9CA3AF]">
            Operator Name
          </label>
          <input
            type="text"
            value={displayName}
            onChange={e => {
              setDisplayName(e.target.value);
              setIsSaved(false);
            }}
            className="w-full bg-[#1B2636] border border-[#26354A] rounded-xl px-3.5 py-2.5 text-[13px] text-[#F9FAFB] focus:outline-none focus:border-[#F59E0B] transition-colors"
          />
        </div>

        {/* Callsign input */}
        <div className="flex flex-col gap-1">
          <label className="text-[11px] font-mono-tactical font-bold text-[#9CA3AF]">
            Field Tactical Callsign
          </label>
          <input
            type="text"
            value={callsign}
            onChange={e => {
              setCallsign(e.target.value.toUpperCase());
              setIsSaved(false);
            }}
            className="w-full bg-[#1B2636] border border-[#26354A] rounded-xl px-3.5 py-2.5 text-[13px] font-mono-tactical text-[#F59E0B] focus:outline-none focus:border-[#F59E0B] transition-colors uppercase tracking-wider"
          />
        </div>

        {/* Avatar Color Selector */}
        <div className="flex flex-col gap-1.5">
          <label className="text-[11px] font-mono-tactical font-bold text-[#9CA3AF]">
            Tactical Color Scheme
          </label>
          <div className="flex items-center gap-2">
            {avatarColorOptions.map((opt, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => {
                  setAvatarIndex(idx);
                  setIsSaved(false);
                }}
                className={`w-8 h-8 rounded-full border-2 flex items-center justify-center transition-all cursor-pointer ${
                  avatarIndex === idx ? 'border-white scale-110' : 'border-transparent opacity-70 hover:opacity-100'
                }`}
                style={{ backgroundColor: opt.color }}
                title={opt.label}
              />
            ))}
          </div>
        </div>

        {/* Save Button */}
        <button
          type="button"
          onClick={handleSave}
          className="w-full py-2.5 rounded-xl bg-[#F59E0B] hover:bg-[#d98b08] text-black font-mono-tactical text-[12px] font-bold flex items-center justify-center gap-2 transition-all cursor-pointer"
        >
          {isSaved ? (
            <>
              <Check className="w-4 h-4" />
              <span>SAVED ✓</span>
            </>
          ) : (
            <span>SAVE PROFILE</span>
          )}
        </button>
      </div>

      {/* 2. Mesh Network Configuration Card */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4.5 flex flex-col gap-4 shadow-md">
        <div className="flex items-center gap-2 text-[#06B6D4] pb-1 border-b border-[#26354A]/60">
          <Radio className="w-4 h-4" />
          <span className="font-mono-tactical text-[11px] font-bold tracking-wider uppercase">
            MESH & RADIO PROTOCOLS
          </span>
        </div>

        {/* Multi-Hop Relay */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3">
            <Cpu className="w-5 h-5 text-[#06B6D4] shrink-0 mt-0.5" />
            <div>
              <span className="text-[13px] font-bold text-[#F9FAFB] block">
                Multi-Hop Mesh Relay
              </span>
              <span className="text-[11px] text-[#9CA3AF] leading-relaxed">
                Forward encrypted packets between out-of-range peers (A ➔ B ➔ C)
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={() => {
              const next = !meshRelay;
              setMeshRelay(next);
              updateProfile(displayName, callsign, avatarIndex, next, batterySaver);
            }}
            className={`w-12 h-6 rounded-full p-0.5 transition-colors cursor-pointer shrink-0 ${
              meshRelay ? 'bg-[#06B6D4]' : 'bg-[#243348]'
            }`}
          >
            <div
              className={`w-5 h-5 rounded-full bg-white transition-transform ${
                meshRelay ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

        {/* Battery Saver */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3">
            <BatteryCharging className="w-5 h-5 text-[#F59E0B] shrink-0 mt-0.5" />
            <div>
              <span className="text-[13px] font-bold text-[#F9FAFB] block">
                Battery-Aware Mode
              </span>
              <span className="text-[11px] text-[#9CA3AF] leading-relaxed">
                Throttle scanning frequency when device battery drops below 20%
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={() => {
              const next = !batterySaver;
              setBatterySaver(next);
              updateProfile(displayName, callsign, avatarIndex, meshRelay, next);
            }}
            className={`w-12 h-6 rounded-full p-0.5 transition-colors cursor-pointer shrink-0 ${
              batterySaver ? 'bg-[#F59E0B]' : 'bg-[#243348]'
            }`}
          >
            <div
              className={`w-5 h-5 rounded-full bg-white transition-transform ${
                batterySaver ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

        {/* Single-Device Demo Simulator */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3">
            <Smartphone className="w-5 h-5 text-[#10B981] shrink-0 mt-0.5" />
            <div>
              <span className="text-[13px] font-bold text-[#F9FAFB] block">
                Demo Mode Simulator
              </span>
              <span className="text-[11px] text-[#9CA3AF] leading-relaxed">
                Simulate peer responses (Ranger Sarah & Medic Dave) for testing
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={toggleDemoMode}
            className={`w-12 h-6 rounded-full p-0.5 transition-colors cursor-pointer shrink-0 ${
              isDemoMode ? 'bg-[#10B981]' : 'bg-[#243348]'
            }`}
          >
            <div
              className={`w-5 h-5 rounded-full bg-white transition-transform ${
                isDemoMode ? 'translate-x-6' : 'translate-x-0'
              }`}
            />
          </button>
        </div>
      </div>

      {/* 3. Hardware Keystore & Zero-Cloud Security Specs */}
      <div className="w-full rounded-2xl bg-[#1B2636] border border-[#26354A] p-4.5 flex flex-col gap-3 shadow-md">
        <div className="flex items-center gap-2 text-[#10B981] pb-1 border-b border-[#26354A]/60">
          <Shield className="w-4 h-4" />
          <span className="font-mono-tactical text-[11px] font-bold tracking-wider uppercase">
            AIR-GAPPED & ZERO-CLOUD ARCHITECTURE
          </span>
        </div>

        <ul className="text-[12px] text-[#9CA3AF] flex flex-col gap-1.5 list-disc list-inside leading-relaxed">
          <li>Keys generated inside hardware-backed WebCrypto / Android KeyStore</li>
          <li>E2EE via ECDH P-256 and AES-256-GCM authenticated cipher</li>
          <li>Audio/Video encrypted over SRTP with WebRTC P2P direct socket</li>
          <li>Zero cloud dependency — all packets remain strictly peer-to-peer</li>
        </ul>
      </div>
    </div>
  );
};
