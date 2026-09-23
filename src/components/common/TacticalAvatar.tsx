import React from 'react';

interface TacticalAvatarProps {
  name: string;
  callsign?: string;
  size?: number; // size in px
  avatarColorIndex?: number;
  isOnline?: boolean;
  className?: string;
}

export const TacticalAvatar: React.FC<TacticalAvatarProps> = ({
  name,
  size = 48,
  avatarColorIndex = 0,
  isOnline = true,
  className = ''
}) => {
  const avatarColors = [
    { primary: '#10B981', bg: '#064E3B' }, // Tactical Green
    { primary: '#F59E0B', bg: '#78350F' }, // Emergency Amber
    { primary: '#06B6D4', bg: '#164E63' }, // Radio Cyan
    { primary: '#EF4444', bg: '#7F1D1D' }  // Distress Red
  ];

  const { primary, bg } = avatarColors[avatarColorIndex % avatarColors.length];

  const initials = name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map(p => p[0].toUpperCase())
    .join('') || 'OP';

  const dotSize = Math.max(9, Math.round(size * 0.28));

  return (
    <div
      className={`relative inline-flex items-center justify-center rounded-full shrink-0 select-none ${className}`}
      style={{
        width: `${size}px`,
        height: `${size}px`,
        backgroundColor: bg,
        border: `1.5px solid ${isOnline ? primary : '#26354A'}`
      }}
    >
      <span
        className="font-mono-tactical font-bold leading-none"
        style={{
          color: primary,
          fontSize: `${Math.round(size * 0.38)}px`
        }}
      >
        {initials}
      </span>

      {/* Status indicator dot */}
      <span
        className="absolute bottom-0 right-0 rounded-full border-2 border-[#090D12]"
        style={{
          width: `${dotSize}px`,
          height: `${dotSize}px`,
          backgroundColor: isOnline ? '#10B981' : '#6B7280'
        }}
      />
    </div>
  );
};
