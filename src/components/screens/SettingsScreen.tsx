import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import {
  User,
  Shield,
  Radio,
  Cpu,
  BatteryCharging,
  Check,
  Smartphone,
  Copy
} from 'lucide-react';

export const SettingsScreen: React.FC = () => {
  const { userProfile, updateProfile, myRealPeerId } = useApp();

  const [displayName, setDisplayName] = useState(userProfile.displayName);
  const [callsign, setCallsign] = useState(userProfile.callsign);
  const [avatarIndex, setAvatarIndex] = useState(userProfile.avatarColorIndex);
  const [meshRelay, setMeshRelay] = useState(userProfile.meshRelayEnabled);
  const [batterySaver, setBatterySaver] = useState(userProfile.batterySaverEnabled);
  const [isSaved, setIsSaved] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile(displayName, callsign, avatarIndex, meshRelay, batterySaver);
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 2500);
  };

  const copyMyCode = () => {
    navigator.clipboard.writeText(myRealPeerId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const avatarColorOptions = [
    { color: '#EF4444', label: 'Crimson' },
    { color: '#3B82F6', label: 'Cobalt' },
    { color: '#10B981', label: 'Emerald' },
    { color: '#F59E0B', label: 'Amber' }
  ];

  return (
    <div className="w-full max-w-2xl mx-auto px-4 py-4 pb-28 flex flex-col gap-4 font-mono-tactical">
      {/* 1. Device Node ID Card */}
      <div className="w-full rounded-2xl bg-gradient-to-r from-[#0E1A29] to-[#142336] border border-[#06B6D4]/40 p-4.5 flex items-center justify-between gap-3 shadow-md">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#06B6D4]/20 border border-[#06B6D4]/50 flex items-center justify-center text-[#06B6D4] shrink-0">
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] text-[#9CA3AF] uppercase block">This Phone's Direct P2P ID</span>
            <span className="text-[16px] font-black text-[#F59E0B] tracking-wider">{myRealPeerId}</span>
          </div>
        </div>

        <button
          type="button"
          onClick={copyMyCode}
          className="px-3.5 py-2 rounded-xl bg-[#1B2636] hover:bg-[#26354A] border border-[#26354A] text-[11px] font-bold text-[#F9FAFB] flex items-center gap-1.5 cursor-pointer"
        >
          {copied ? <Check className="w-4 h-4 text-[#10B981]" /> : <Copy className="w-4 h-4" />}
          <span>{copied ? 'Copied!' : 'Copy ID'}</span>
        </button>
      </div>

      {/* 2. Operator Identity Card */}
      <form onSubmit={handleSave} className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4.5 flex flex-col gap-4 shadow-md">
        <div className="flex items-center gap-2 text-[#F59E0B] pb-1 border-b border-[#26354A]/60">
          <User className="w-4 h-4" />
          <span className="text-[11px] font-bold tracking-wider uppercase">
            OPERATOR IDENTITY
          </span>
        </div>

        {/* Display Name Input */}
        <div className="flex flex-col gap-1.5">
          <label className="text-[11px] font-bold text-[#9CA3AF]">
            Phone Display Name
          </label>
          <input
            type="text"
            value={displayName}
            onChange={e => {
              setDisplayName(e.target.value);
              setIsSaved(false);
            }}
            placeholder="e.g. My Phone / Your Name"
            maxLength={32}
            className="w-full bg-[#1B2636] border border-[#26354A] rounded-xl px-3.5 py-2.5 text-[13px] text-[#F9FAFB] focus:outline-none focus:border-[#F59E0B] font-sans"
          />
        </div>

        {/* Callsign Input */}
        <div className="flex flex-col gap-1.5">
          <label className="text-[11px] font-bold text-[#9CA3AF]">
            Short Callsign Tag
          </label>
          <input
            type="text"
            value={callsign}
            onChange={e => {
              setCallsign(e.target.value.toUpperCase());
              setIsSaved(false);
            }}
            placeholder="e.g. ECHO-1"
            maxLength={12}
            className="w-full bg-[#1B2636] border border-[#26354A] rounded-xl px-3.5 py-2.5 text-[13px] text-[#F9FAFB] focus:outline-none focus:border-[#F59E0B]"
          />
        </div>

        {/* Avatar Color Selector */}
        <div className="flex flex-col gap-1.5">
          <label className="text-[11px] font-bold text-[#9CA3AF]">
            Avatar Color Scheme
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
          type="submit"
          className="w-full py-2.5 rounded-xl bg-[#F59E0B] hover:bg-[#d98b08] text-black text-[12px] font-bold flex items-center justify-center gap-2 transition-all cursor-pointer"
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
      </form>

      {/* 3. Hardware & Radio Protocols */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4.5 flex flex-col gap-4 shadow-md">
        <div className="flex items-center gap-2 text-[#06B6D4] pb-1 border-b border-[#26354A]/60">
          <Radio className="w-4 h-4" />
          <span className="text-[11px] font-bold tracking-wider uppercase">
            MESH & RADIO PROTOCOLS
          </span>
        </div>

        {/* Multi-Hop Relay */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3">
            <Cpu className="w-5 h-5 text-[#06B6D4] shrink-0 mt-0.5" />
            <div>
              <span className="text-[13px] font-bold text-[#F9FAFB] block">
                Direct P2P Relay
              </span>
              <span className="text-[11px] text-[#9CA3AF] leading-relaxed font-sans">
                Real-time WebRTC socket link between phones
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
                Battery Saver Mode
              </span>
              <span className="text-[11px] text-[#9CA3AF] leading-relaxed font-sans">
                Throttle background radio pinging when device battery is low
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
      </div>

      {/* 4. Hardware Keystore & Zero-Cloud Security Specs */}
      <div className="w-full rounded-2xl bg-[#1B2636] border border-[#26354A] p-4.5 flex flex-col gap-3 shadow-md">
        <div className="flex items-center gap-2 text-[#10B981] pb-1 border-b border-[#26354A]/60">
          <Shield className="w-4 h-4" />
          <span className="text-[11px] font-bold tracking-wider uppercase">
            ZERO-SERVER REAL PHONE-TO-PHONE PROTOCOL
          </span>
        </div>

        <ul className="text-[12px] text-[#9CA3AF] flex flex-col gap-1.5 list-disc list-inside leading-relaxed font-sans">
          <li>Browser Web Bluetooth API scans native BLE peripheral devices</li>
          <li>Direct WebRTC P2P DataChannel for WhatsApp-like instant text & photo sharing</li>
          <li>Real audio/video camera feed directly between two physical phones</li>
          <li>Zero chat logs stored on any cloud server — 100% peer-to-peer</li>
        </ul>
      </div>
    </div>
  );
};
