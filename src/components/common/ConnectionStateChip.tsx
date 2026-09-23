import React from 'react';
import { ConnectionState } from '../../types';

interface ConnectionStateChipProps {
  state: ConnectionState;
  className?: string;
}

export const ConnectionStateChip: React.FC<ConnectionStateChipProps> = ({ state, className = '' }) => {
  const config = {
    CONNECTED: { label: 'CONNECTED (P2P)', color: '#10B981', bg: 'rgba(16, 185, 129, 0.15)', border: 'rgba(16, 185, 129, 0.4)' },
    CONNECTING: { label: 'LINKING...', color: '#F59E0B', bg: 'rgba(245, 158, 11, 0.15)', border: 'rgba(245, 158, 11, 0.4)' },
    SEARCHING: { label: 'SCANNING', color: '#06B6D4', bg: 'rgba(6, 182, 212, 0.15)', border: 'rgba(6, 182, 212, 0.4)' },
    OUT_OF_RANGE: { label: 'OUT OF RANGE', color: '#EF4444', bg: 'rgba(239, 68, 68, 0.15)', border: 'rgba(239, 68, 68, 0.4)' },
    DISCONNECTED: { label: 'OFFLINE', color: '#9CA3AF', bg: 'rgba(156, 163, 175, 0.15)', border: 'rgba(156, 163, 175, 0.3)' }
  }[state];

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-mono-tactical font-bold select-none ${className}`}
      style={{
        color: config.color,
        backgroundColor: config.bg,
        border: `1px solid ${config.border}`
      }}
    >
      <span
        className="w-1.5 h-1.5 rounded-full"
        style={{ backgroundColor: config.color }}
      />
      {config.label}
    </span>
  );
};
