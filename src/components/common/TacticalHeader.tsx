import React, { useState, useEffect } from 'react';
import { useApp } from '../../context/AppContext';
import { Radio, Lock, Battery } from 'lucide-react';

export const TacticalHeader: React.FC = () => {
  const { userProfile, activeTab, isDemoMode } = useApp();
  const [timeUtc, setTimeUtc] = useState<string>('');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const hours = now.getUTCHours().toString().padStart(2, '0');
      const mins = now.getUTCMinutes().toString().padStart(2, '0');
      const secs = now.getUTCSeconds().toString().padStart(2, '0');
      setTimeUtc(`${hours}:${mins}:${secs}Z`);
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  const tabTitle = {
    radar: 'RADAR DISCOVERY',
    chats: 'ENCRYPTED THREADS',
    sos: 'EMERGENCY BEACON',
    settings: 'OPERATOR CONFIG'
  }[activeTab];

  return (
    <header className="sticky top-0 z-40 w-full bg-[#111822]/95 backdrop-blur-md border-b border-[#26354A] px-4 py-2.5 shadow-sm">
      <div className="max-w-4xl mx-auto flex items-center justify-between">
        {/* Left: Tactical App Title & Callsign */}
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-[#1B2636] border border-[#26354A] flex items-center justify-center text-[#F59E0B]">
            <Radio className="w-4 h-4 text-[#F59E0B]" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-mono-tactical font-black text-[13px] tracking-wider text-[#F9FAFB]">
                OFFGRID CONNECT
              </span>
              {isDemoMode && (
                <span className="text-[9px] font-mono-tactical px-1.5 py-0.2 bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40 rounded-xs">
                  SIM
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 text-[10px] font-mono-tactical text-[#9CA3AF]">
              <span className="text-[#F59E0B] font-semibold">{userProfile.callsign}</span>
              <span>•</span>
              <span className="text-[#06B6D4]">{tabTitle}</span>
            </div>
          </div>
        </div>

        {/* Right: Security Badge & Zulu Clock */}
        <div className="flex items-center gap-2.5">
          <div className="hidden sm:flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-[#1B2636] border border-[#26354A] text-[10px] font-mono-tactical text-[#10B981]">
            <Lock className="w-3 h-3 text-[#10B981]" />
            <span>AIR-GAPPED E2EE</span>
          </div>

          <div className="flex items-center gap-1.5 px-2 py-1 rounded-md bg-[#090D12] border border-[#26354A] text-[11px] font-mono-tactical text-[#9CA3AF]">
            <span className="text-[#F59E0B] font-bold">{timeUtc}</span>
            <Battery className="w-3.5 h-3.5 text-[#10B981] hidden xs:block" />
          </div>
        </div>
      </div>
    </header>
  );
};
