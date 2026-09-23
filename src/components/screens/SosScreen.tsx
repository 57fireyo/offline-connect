import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { EmergencyType, UrgencyLevel } from '../../types';
import {
  AlertTriangle,
  MapPin,
  HeartPulse,
  LifeBuoy,
  Flame,
  Package,
  CheckCircle2,
  Clock
} from 'lucide-react';

export const SosScreen: React.FC = () => {
  const { sosAlerts, broadcastSos, acknowledgeAlert, userProfile, isDemoMode } = useApp();

  const [selectedType, setSelectedType] = useState<EmergencyType>('MEDICAL');
  const [selectedUrgency, setSelectedUrgency] = useState<UrgencyLevel>('CRITICAL');
  const [notes, setNotes] = useState('');
  const [coords] = useState({ lat: 36.5785, lon: -118.2923, alt: 2450 });
  const [broadcastedNotice, setBroadcastedNotice] = useState(false);

  const emergencyTypes: { type: EmergencyType; label: string; icon: typeof HeartPulse; color: string }[] = [
    { type: 'MEDICAL', label: 'Medical Emergency', icon: HeartPulse, color: '#EF4444' },
    { type: 'RESCUE', label: 'Search & Rescue', icon: LifeBuoy, color: '#F59E0B' },
    { type: 'HAZARD', label: 'Environmental Hazard', icon: Flame, color: '#F97316' },
    { type: 'SUPPLY', label: 'Supplies / Water', icon: Package, color: '#06B6D4' }
  ];

  const urgencyLevels: { level: UrgencyLevel; label: string; desc: string }[] = [
    { level: 'CRITICAL', label: 'CRITICAL', desc: 'Immediate threat to life' },
    { level: 'HIGH', label: 'HIGH PRIORITY', desc: 'Assistance needed ASAP' },
    { level: 'MEDIUM', label: 'MEDIUM', desc: 'Non-life threatening' }
  ];

  const handleBroadcast = () => {
    broadcastSos(selectedType, selectedUrgency, notes, coords.lat, coords.lon);
    setBroadcastedNotice(true);
    setTimeout(() => setBroadcastedNotice(false), 6000);
  };

  const formatTime = (ts: number) => {
    const d = new Date(ts);
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  };

  return (
    <div className="w-full max-w-4xl mx-auto px-4 py-4 pb-28 flex flex-col gap-4">
      {/* 1. Header Hero Banner */}
      <div className="w-full rounded-2xl bg-[#7F1D1D]/30 border border-[#EF4444]/40 p-4 flex items-center justify-between gap-3 shadow-md">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#EF4444]/20 border border-[#EF4444]/50 flex items-center justify-center text-[#EF4444] shrink-0 animate-pulse">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono-tactical text-[13px] font-black text-[#EF4444] tracking-wider uppercase">
                P2P EMERGENCY DISTRESS BEACON
              </span>
              <span className="w-2 h-2 rounded-full bg-[#EF4444] animate-ping" />
            </div>
            <p className="text-[11px] text-[#9CA3AF] mt-0.5">
              Propagates alerts across all offline Bluetooth & Wi-Fi Direct hops to nearby rescue teams.
            </p>
          </div>
        </div>

        <div className="hidden sm:flex flex-col items-end text-[10px] font-mono-tactical text-[#EF4444]">
          <span className="font-bold">CHANNEL 0 (EMERGENCY)</span>
          <span className="text-[#9CA3AF]">ALL HOPS RECEPTIVE</span>
        </div>
      </div>

      {/* Broadcast Success Notice */}
      {broadcastedNotice && (
        <div className="w-full rounded-xl bg-[#10B981]/20 border border-[#10B981] p-3.5 flex items-center gap-2.5 text-[#10B981] text-[12px] font-mono-tactical">
          <CheckCircle2 className="w-5 h-5 shrink-0" />
          <span>
            BEACON TRANSMITTED: SOS alert broadcasted to all {isDemoMode ? 'radio nodes & Search/Rescue' : 'local nodes'}!
          </span>
        </div>
      )}

      {/* 2. SOS Big Trigger Button */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-6 flex flex-col items-center justify-center gap-4 text-center shadow-lg">
        <button
          type="button"
          onClick={handleBroadcast}
          className="relative w-36 h-36 rounded-full bg-[#EF4444] text-white flex flex-col items-center justify-center gap-1 shadow-2xl transition-transform active:scale-95 cursor-pointer animate-sos-glow hover:bg-[#dc2626]"
          title="Hold or Tap to Broadcast Distress Beacon"
        >
          <AlertTriangle className="w-10 h-10 stroke-[2.5]" />
          <span className="font-mono-tactical font-black text-[18px] tracking-wider">
            BROADCAST
          </span>
          <span className="font-mono-tactical text-[10px] font-bold opacity-90">
            P2P BEACON
          </span>
        </button>

        <span className="font-mono-tactical text-[11px] text-[#9CA3AF]">
          Tap button above to immediately transmit your grid coordinates and distress payload.
        </span>
      </div>

      {/* 3. Emergency Triage Classification */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4 flex flex-col gap-3">
        <span className="font-mono-tactical text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wider">
          TRIAGE CLASSIFICATION
        </span>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
          {emergencyTypes.map(item => {
            const isSel = selectedType === item.type;
            const Icon = item.icon;
            return (
              <button
                key={item.type}
                type="button"
                onClick={() => setSelectedType(item.type)}
                className={`p-3 rounded-xl border flex flex-col items-center text-center gap-2 transition-all cursor-pointer ${
                  isSel
                    ? 'bg-[#1B2636] border-[#EF4444] text-[#F9FAFB] shadow-sm'
                    : 'bg-[#1B2636]/40 border-[#26354A] text-[#9CA3AF] hover:bg-[#1B2636]'
                }`}
              >
                <div
                  className="w-8 h-8 rounded-lg flex items-center justify-center"
                  style={{
                    backgroundColor: isSel ? `${item.color}25` : '#243348',
                    color: item.color
                  }}
                >
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-[11px] font-bold">{item.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Urgency Level Selector */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4 flex flex-col gap-3">
        <span className="font-mono-tactical text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wider">
          URGENCY LEVEL
        </span>

        <div className="grid grid-cols-3 gap-2">
          {urgencyLevels.map(lvl => {
            const isSel = selectedUrgency === lvl.level;
            return (
              <button
                key={lvl.level}
                type="button"
                onClick={() => setSelectedUrgency(lvl.level)}
                className={`p-3 rounded-xl border text-left flex flex-col gap-1 transition-all cursor-pointer ${
                  isSel
                    ? lvl.level === 'CRITICAL'
                      ? 'bg-[#EF4444]/20 border-[#EF4444] text-[#F9FAFB]'
                      : lvl.level === 'HIGH'
                      ? 'bg-[#F59E0B]/20 border-[#F59E0B] text-[#F9FAFB]'
                      : 'bg-[#06B6D4]/20 border-[#06B6D4] text-[#F9FAFB]'
                    : 'bg-[#1B2636]/40 border-[#26354A] text-[#9CA3AF] hover:bg-[#1B2636]'
                }`}
              >
                <span
                  className={`font-mono-tactical text-[12px] font-bold ${
                    lvl.level === 'CRITICAL'
                      ? 'text-[#EF4444]'
                      : lvl.level === 'HIGH'
                      ? 'text-[#F59E0B]'
                      : 'text-[#06B6D4]'
                  }`}
                >
                  {lvl.label}
                </span>
                <span className="text-[10px] text-[#9CA3AF] leading-tight">
                  {lvl.desc}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 5. GPS Grid Coordinates & Notes Input */}
      <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-4 flex flex-col gap-3.5">
        <div className="flex items-center justify-between pb-2 border-b border-[#26354A]/60">
          <div className="flex items-center gap-2 text-[#06B6D4]">
            <MapPin className="w-4 h-4" />
            <span className="font-mono-tactical text-[11px] font-bold tracking-wider">
              FIELD GPS COORDINATES
            </span>
          </div>
          <span className="text-[10px] font-mono-tactical text-[#10B981]">
            OFFLINE SATELLITE FIX ✓
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-[12px] font-mono-tactical">
          <div className="bg-[#1B2636] border border-[#26354A] p-2.5 rounded-lg">
            <span className="text-[10px] text-[#9CA3AF] block">Latitude / Longitude:</span>
            <span className="font-bold text-[#F9FAFB]">{coords.lat.toFixed(4)}° N, {coords.lon.toFixed(4)}° W</span>
          </div>
          <div className="bg-[#1B2636] border border-[#26354A] p-2.5 rounded-lg">
            <span className="text-[10px] text-[#9CA3AF] block">Elevation Above Sea:</span>
            <span className="font-bold text-[#F59E0B]">~{coords.alt}m MSL (Ridge Sector 4)</span>
          </div>
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="text-[11px] font-mono-tactical font-bold text-[#9CA3AF]">
            Distress Details & Critical Notes:
          </label>
          <textarea
            value={notes}
            onChange={e => setNotes(e.target.value)}
            placeholder="E.g. Injured party, medical supplies needed, landmarks or terrain hazards..."
            rows={2}
            className="w-full bg-[#1B2636] border border-[#26354A] rounded-xl p-3 text-[13px] text-[#F9FAFB] placeholder-[#6B7280] focus:outline-none focus:border-[#EF4444] transition-colors resize-none"
          />
        </div>
      </div>

      {/* 6. Active Distress Alerts Feed */}
      <div className="w-full flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="font-mono-tactical text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wider">
            MESH DISTRESS BEACONS IN RANGE ({sosAlerts.length})
          </span>
          <span className="font-mono-tactical text-[10px] text-[#EF4444]">
            MULTI-HOP FLOOD
          </span>
        </div>

        <div className="flex flex-col gap-2.5">
          {sosAlerts.map(alert => {
            const isMine = alert.senderId === userProfile.peerId;
            return (
              <div
                key={alert.id}
                className="w-full rounded-2xl bg-[#111822] border border-[#EF4444]/40 p-4 flex flex-col gap-2.5 shadow-md"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded-md bg-[#EF4444]/20 text-[#EF4444] border border-[#EF4444]/40 font-mono-tactical text-[10px] font-black">
                      {alert.emergencyType} • {alert.urgencyLevel}
                    </span>
                    <span className="font-mono-tactical text-[12px] font-bold text-[#F59E0B]">
                      [{alert.callsign}]
                    </span>
                    {isMine && (
                      <span className="font-mono-tactical text-[9px] px-1 bg-[#10B981]/20 text-[#10B981] rounded-xs">
                        LOCAL BEACON
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-1.5 text-[11px] font-mono-tactical text-[#9CA3AF]">
                    <Clock className="w-3.5 h-3.5 text-[#9CA3AF]" />
                    <span>{formatTime(alert.timestamp)}</span>
                  </div>
                </div>

                <p className="text-[13px] text-[#F9FAFB] leading-relaxed">
                  {alert.notes}
                </p>

                <div className="flex items-center justify-between pt-1 border-t border-[#26354A]/50 text-[11px] font-mono-tactical text-[#9CA3AF] flex-wrap gap-2">
                  <div className="flex items-center gap-1 text-[#06B6D4]">
                    <MapPin className="w-3.5 h-3.5" />
                    <span>
                      {alert.latitude?.toFixed(4)}° N, {alert.longitude?.toFixed(4)}° W
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={() => acknowledgeAlert(alert.id)}
                    className={`px-3 py-1 rounded-lg text-[11px] font-mono-tactical font-bold transition-all cursor-pointer ${
                      alert.acknowledged
                        ? 'bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40'
                        : 'bg-[#EF4444] text-white hover:bg-[#dc2626]'
                    }`}
                  >
                    {alert.acknowledged ? 'ACKNOWLEDGED ✓' : 'ACKNOWLEDGE ALERT'}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
