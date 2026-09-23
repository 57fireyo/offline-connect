import React from 'react';
import { MessageStatus } from '../../types';
import { Clock, Check, CheckCheck, AlertCircle } from 'lucide-react';

interface MessageStatusIconProps {
  status: MessageStatus;
  className?: string;
}

export const MessageStatusIcon: React.FC<MessageStatusIconProps> = ({ status, className = '' }) => {
  switch (status) {
    case 'QUEUED':
      return (
        <span title="Queued in store-and-forward" className={`inline-flex ${className}`}>
          <Clock className="w-3 h-3 text-[#F59E0B]" />
        </span>
      );
    case 'SENT':
      return (
        <span title="Sent" className={`inline-flex ${className}`}>
          <Check className="w-3 h-3 text-[#9CA3AF]" />
        </span>
      );
    case 'DELIVERED':
      return (
        <span title="Delivered to peer" className={`inline-flex ${className}`}>
          <CheckCheck className="w-3.5 h-3.5 text-[#9CA3AF]" />
        </span>
      );
    case 'SEEN':
      return (
        <span title="Read by peer" className={`inline-flex ${className}`}>
          <CheckCheck className="w-3.5 h-3.5 text-[#10B981]" />
        </span>
      );
    case 'FAILED':
      return (
        <span title="Failed to deliver" className={`inline-flex ${className}`}>
          <AlertCircle className="w-3 h-3 text-[#EF4444]" />
        </span>
      );
    default:
      return null;
  }
};
