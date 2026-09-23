import React from 'react';
import { ShieldCheck, Lock } from 'lucide-react';

interface SecurityVerificationBadgeProps {
  isVerified: boolean;
  onClick?: () => void;
  className?: string;
}

export const SecurityVerificationBadge: React.FC<SecurityVerificationBadgeProps> = ({
  isVerified,
  onClick,
  className = ''
}) => {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-mono-tactical font-bold transition-colors cursor-pointer ${
        isVerified
          ? 'bg-[#10B981]/15 text-[#10B981] border border-[#10B981]/50 hover:bg-[#10B981]/25'
          : 'bg-[#F59E0B]/15 text-[#F59E0B] border border-[#F59E0B]/50 hover:bg-[#F59E0B]/25'
      } ${className}`}
      title={isVerified ? 'Cryptographic keys verified' : 'Click to verify keys via SAS code'}
    >
      {isVerified ? (
        <ShieldCheck className="w-3 h-3 text-[#10B981]" />
      ) : (
        <Lock className="w-3 h-3 text-[#F59E0B]" />
      )}
      <span>{isVerified ? 'E2EE VERIFIED' : 'UNVERIFIED E2EE'}</span>
    </button>
  );
};
