import React from 'react';
import { Shield } from 'lucide-react';

interface TacticalRadarBannerProps {
  isScanning: boolean;
  connectedCount: number;
  discoveredCount: number;
  onToggleScan: () => void;
  className?: string;
}

export const TacticalRadarBanner: React.FC<TacticalRadarBannerProps> = ({
  isScanning,
  connectedCount,
  discoveredCount,
  onToggleScan,
  className = ''
}) => {
  return (
    <div
      className={`w-full rounded-2xl bg-[#1B2636] border border-[#26354A] p-3.5 flex items-center justify-between gap-3 shadow-md ${className}`}
    >
      <div className="flex items-center gap-3.5">
        {/* Tactical Radar Mini View */}
        <div className="relative w-14 h-14 rounded-full bg-[#0B141E] border border-[#06B6D4]/40 flex items-center justify-center shrink-0 overflow-hidden">
          {/* Concentric rings */}
          <div className="absolute w-[35%] h-[35%] rounded-full border border-[#06B6D4]/20" />
          <div className="absolute w-[70%] h-[70%] rounded-full border border-[#06B6D4]/25" />
          <div className="absolute w-[100%] h-[100%] rounded-full border border-[#06B6D4]/35" />

          {/* Crosshairs */}
          <div className="absolute w-full h-[1px] bg-[#06B6D4]/15" />
          <div className="absolute h-full w-[1px] bg-[#06B6D4]/15" />

          {/* Radar Sweep Ray */}
          {isScanning && (
            <div
              className="absolute inset-0 animate-radar-sweep pointer-events-none origin-center"
              style={{
                background: 'conic-gradient(from 0deg, transparent 0deg, transparent 315deg, rgba(6, 182, 212, 0.45) 360deg)'
              }}
            />
          )}

          {/* Simulated peer blips on radar */}
          {connectedCount > 0 && (
            <div className="absolute w-1.5 h-1.5 rounded-full bg-[#10B981] top-[28%] right-[24%] shadow-[0_0_6px_#10B981]" />
          )}
          {discoveredCount > 1 && (
            <div className="absolute w-1.5 h-1.5 rounded-full bg-[#F59E0B] bottom-[30%] left-[26%] shadow-[0_0_6px_#F59E0B]" />
          )}

          {/* Center Tactical Icon */}
          <Shield className="w-4 h-4 text-[#06B6D4]/70 z-10" />
        </div>

        {/* Text info */}
        <div className="flex flex-col gap-0.5">
          <div className="flex items-center gap-2">
            <span
              className={`font-mono-tactical font-bold text-[13px] ${
                isScanning ? 'text-[#10B981]' : 'text-[#9CA3AF]'
              }`}
            >
              {isScanning ? 'P2P MESH ACTIVE' : 'MESH IDLE'}
            </span>
            <span
              className={`w-1.5 h-1.5 rounded-full ${
                isScanning ? 'bg-[#10B981] shadow-[0_0_4px_#10B981]' : 'bg-gray-500'
              }`}
            />
          </div>

          <span className="text-[11px] text-[#9CA3AF]">
            Bluetooth BLE + Wi-Fi Direct Transport
          </span>

          <span className="font-mono-tactical text-[12px] font-semibold text-[#F59E0B]">
            {connectedCount} Connected • {discoveredCount} In Range
          </span>
        </div>
      </div>

      {/* Action button */}
      <button
        type="button"
        onClick={onToggleScan}
        className={`px-3 py-1.5 rounded-lg font-mono-tactical text-[11px] font-bold tracking-wide transition-all cursor-pointer ${
          isScanning
            ? 'bg-[#243348] text-[#F59E0B] border border-[#F59E0B]/30 hover:bg-[#2c3d56]'
            : 'bg-[#F59E0B] text-black hover:bg-[#d98b08]'
        }`}
      >
        {isScanning ? 'Active' : 'Scan'}
      </button>
    </div>
  );
};
