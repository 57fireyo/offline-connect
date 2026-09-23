import React from 'react';

interface SignalStrengthMeterProps {
  rssi: number;
  distanceMeters: number;
  className?: string;
}

export const SignalStrengthMeter: React.FC<SignalStrengthMeterProps> = ({
  rssi,
  distanceMeters,
  className = ''
}) => {
  // RSSI ranges from -100 (poor) to -35 (excellent)
  const bars =
    rssi >= -55 ? 4 :
    rssi >= -70 ? 3 :
    rssi >= -85 ? 2 : 1;

  const barColor =
    bars >= 3 ? '#10B981' :
    bars === 2 ? '#F59E0B' : '#EF4444';

  return (
    <div className={`flex items-center gap-1.5 ${className}`}>
      {/* 4-bar indicator */}
      <div className="flex items-end gap-0.5 h-3.5">
        {[1, 2, 3, 4].map(i => {
          const height = i * 3.5;
          const isActive = i <= bars;
          return (
            <div
              key={i}
              className="w-[3px] rounded-xs transition-all duration-300"
              style={{
                height: `${height}px`,
                backgroundColor: isActive ? barColor : '#26354A'
              }}
            />
          );
        })}
      </div>

      <span className="font-mono-tactical text-[11px] text-[#9CA3AF] font-medium tracking-tight">
        {rssi}dBm • ~{distanceMeters.toFixed(1)}m
      </span>
    </div>
  );
};
