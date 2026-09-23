import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalRadarBanner } from '../common/TacticalRadarBanner';
import { TacticalAvatar } from '../common/TacticalAvatar';
import { SignalStrengthMeter } from '../common/SignalStrengthMeter';
import { ConnectionStateChip } from '../common/ConnectionStateChip';
import { SecurityVerificationBadge } from '../common/SecurityVerificationBadge';
import { MessageSquare, Phone, Video, Search, X, Radio, Battery, Link2, Unlink } from 'lucide-react';

export const RadarScreen: React.FC = () => {
  const {
    contacts,
    isScanning,
    isDemoMode,
    toggleScan,
    toggleDemoMode,
    openChat,
    openVerifySecurity,
    connectPeer,
    disconnectPeer,
    startCall
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'CONNECTED' | 'IN_RANGE'>('ALL');

  const connectedCount = contacts.filter(c => c.connectionState === 'CONNECTED').length;
  const inRangeCount = contacts.filter(c => c.connectionState !== 'DISCONNECTED').length;

  const filteredContacts = contacts.filter(c => {
    const matchesSearch =
      c.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.callsign.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.peerId.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    if (filter === 'CONNECTED') return c.connectionState === 'CONNECTED';
    if (filter === 'IN_RANGE') return c.connectionState !== 'DISCONNECTED';
    return true;
  });

  return (
    <div className="w-full max-w-4xl mx-auto px-4 py-4 pb-28 flex flex-col gap-4">
      {/* 1. Tactical Radar Banner */}
      <TacticalRadarBanner
        isScanning={isScanning}
        connectedCount={connectedCount}
        discoveredCount={inRangeCount}
        onToggleScan={toggleScan}
      />

      {/* 2. Single-Device Demo Simulator Banner */}
      <div className="w-full rounded-xl bg-[#1B2636]/70 border border-[#26354A] p-3 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-md bg-[#10B981]/20 text-[#10B981] flex items-center justify-center shrink-0">
            <Radio className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono-tactical text-[12px] font-bold text-[#F9FAFB]">
                Field Demo Simulation Engine
              </span>
              <span className="text-[9px] font-mono-tactical px-1.5 py-0.2 rounded-xs bg-[#10B981]/20 text-[#10B981] font-bold">
                {isDemoMode ? 'SIMULATOR ON' : 'HARDWARE ONLY'}
              </span>
            </div>
            <p className="text-[11px] text-[#9CA3AF]">
              Simulates live radio peers (Ranger Sarah & Medic Dave) for interactive evaluation.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={toggleDemoMode}
          className={`px-3 py-1 rounded-md text-[11px] font-mono-tactical font-bold cursor-pointer transition-all ${
            isDemoMode
              ? 'bg-[#10B981] text-black hover:bg-[#0ea372]'
              : 'bg-[#243348] text-[#9CA3AF] border border-[#26354A] hover:bg-[#2c3d56]'
          }`}
        >
          {isDemoMode ? 'Enabled' : 'Disabled'}
        </button>
      </div>

      {/* 3. Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#9CA3AF]" />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Search peer callsign, node ID or operator..."
            className="w-full bg-[#111822] border border-[#26354A] rounded-xl pl-9 pr-8 py-2 text-[13px] text-[#F9FAFB] placeholder-[#6B7280] focus:outline-none focus:border-[#F59E0B] transition-colors"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#9CA3AF] hover:text-[#F9FAFB]"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
          {(['ALL', 'CONNECTED', 'IN_RANGE'] as const).map(tabKey => {
            const isSel = filter === tabKey;
            const labels = {
              ALL: `All (${contacts.length})`,
              CONNECTED: `Linked (${connectedCount})`,
              IN_RANGE: `In Range (${inRangeCount})`
            };
            return (
              <button
                key={tabKey}
                type="button"
                onClick={() => setFilter(tabKey)}
                className={`px-3 py-1.5 rounded-lg text-[11px] font-mono-tactical font-bold whitespace-nowrap cursor-pointer transition-colors ${
                  isSel
                    ? 'bg-[#F59E0B] text-black'
                    : 'bg-[#1B2636] text-[#9CA3AF] border border-[#26354A] hover:bg-[#243348]'
                }`}
              >
                {labels[tabKey]}
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Peers Header */}
      <div className="flex items-center justify-between">
        <span className="font-mono-tactical text-[11px] font-bold text-[#9CA3AF] tracking-wider uppercase">
          NEARBY RADIO NODES ({filteredContacts.length})
        </span>
        <span className="font-mono-tactical text-[10px] text-[#06B6D4]">
          AIR-GAPPED BLUETOOTH / WI-FI DIRECT
        </span>
      </div>

      {/* 5. Peer Cards List */}
      {filteredContacts.length === 0 ? (
        <div className="w-full rounded-2xl bg-[#1B2636]/60 border border-[#26354A] p-8 text-center flex flex-col items-center justify-center gap-3">
          <Radio className="w-8 h-8 text-[#F59E0B]" />
          <h4 className="font-bold text-[15px] text-[#F9FAFB]">No Nodes Found</h4>
          <p className="text-[12px] text-[#9CA3AF] max-w-sm">
            {searchQuery
              ? `No peers matched "${searchQuery}". Clear your search query.`
              : 'No peers found matching the selected filter. Activate Demo Mode or scan again.'}
          </p>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {filteredContacts.map(peer => {
            const isConnected = peer.connectionState === 'CONNECTED';

            return (
              <div
                key={peer.peerId}
                className={`w-full rounded-2xl bg-[#111822] border transition-all p-3.5 sm:p-4 flex flex-col gap-3 shadow-md ${
                  isConnected ? 'border-[#26354A] hover:border-[#10B981]/50' : 'border-[#26354A]/60 opacity-90'
                }`}
              >
                {/* Top Row: Avatar, Identity, Status */}
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <TacticalAvatar
                      name={peer.displayName}
                      callsign={peer.callsign}
                      size={48}
                      avatarColorIndex={peer.avatarColorIndex}
                      isOnline={isConnected}
                    />

                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-bold text-[15px] text-[#F9FAFB]">
                          {peer.displayName}
                        </span>
                        <span className="font-mono-tactical text-[11px] font-bold text-[#F59E0B] bg-[#F59E0B]/10 px-1.5 py-0.5 rounded-xs border border-[#F59E0B]/30">
                          [{peer.callsign}]
                        </span>
                      </div>

                      <div className="flex items-center gap-2 text-[11px] font-mono-tactical text-[#9CA3AF] mt-0.5">
                        <span>{peer.peerId}</span>
                        <span>•</span>
                        <div className="flex items-center gap-1 text-[#10B981]">
                          <Battery className="w-3 h-3" />
                          <span>{peer.batteryPercent}%</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Connection State Chip */}
                  <ConnectionStateChip state={peer.connectionState} />
                </div>

                {/* Middle Row: Signal strength & Verification badge */}
                <div className="flex items-center justify-between pt-1 border-t border-[#26354A]/50 flex-wrap gap-2">
                  <SignalStrengthMeter
                    rssi={peer.rssi}
                    distanceMeters={peer.distanceEstimateMeters}
                  />

                  <SecurityVerificationBadge
                    isVerified={peer.isVerified}
                    onClick={() => openVerifySecurity(peer)}
                  />
                </div>

                {/* Bottom Row: Tactical Action Buttons */}
                <div className="flex items-center justify-between gap-2 pt-1 border-t border-[#26354A]/30">
                  {/* Link / Unlink Button */}
                  <button
                    type="button"
                    onClick={() => {
                      if (isConnected) disconnectPeer(peer.peerId);
                      else connectPeer(peer.peerId);
                    }}
                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[11px] font-mono-tactical font-bold transition-colors cursor-pointer ${
                      isConnected
                        ? 'bg-[#1B2636] text-[#EF4444] border border-[#EF4444]/30 hover:bg-[#EF4444]/20'
                        : 'bg-[#10B981] text-black hover:bg-[#0ea372]'
                    }`}
                  >
                    {isConnected ? (
                      <>
                        <Unlink className="w-3.5 h-3.5" />
                        <span>Disconnect</span>
                      </>
                    ) : (
                      <>
                        <Link2 className="w-3.5 h-3.5" />
                        <span>Link Peer</span>
                      </>
                    )}
                  </button>

                  {/* Communications Buttons */}
                  <div className="flex items-center gap-1.5">
                    {/* Chat Button */}
                    <button
                      type="button"
                      onClick={() => openChat(peer.peerId)}
                      className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#243348] text-[#F9FAFB] hover:bg-[#2c3d56] border border-[#26354A] text-[11px] font-mono-tactical font-semibold transition-colors cursor-pointer"
                      title="Open End-to-End Encrypted Chat"
                    >
                      <MessageSquare className="w-3.5 h-3.5 text-[#F59E0B]" />
                      <span className="hidden xs:inline">Chat</span>
                    </button>

                    {/* Voice Call */}
                    <button
                      type="button"
                      onClick={() => startCall(peer, false)}
                      className="p-1.5 rounded-lg bg-[#243348] text-[#10B981] hover:bg-[#10B981]/20 border border-[#26354A] transition-colors cursor-pointer"
                      title="Start Offline Voice Call"
                    >
                      <Phone className="w-4 h-4" />
                    </button>

                    {/* Video Call */}
                    <button
                      type="button"
                      onClick={() => startCall(peer, true)}
                      className="p-1.5 rounded-lg bg-[#243348] text-[#06B6D4] hover:bg-[#06B6D4]/20 border border-[#26354A] transition-colors cursor-pointer"
                      title="Start Offline Video Call"
                    >
                      <Video className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
