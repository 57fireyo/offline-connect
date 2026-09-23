import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalRadarBanner } from '../common/TacticalRadarBanner';
import { TacticalAvatar } from '../common/TacticalAvatar';
import { SignalStrengthMeter } from '../common/SignalStrengthMeter';
import { ConnectionStateChip } from '../common/ConnectionStateChip';
import { SecurityVerificationBadge } from '../common/SecurityVerificationBadge';
import { BluetoothPairingModal } from '../common/BluetoothPairingModal';
import {
  MessageSquare,
  Phone,
  Video,
  Search,
  X,
  Battery,
  Link2,
  Unlink,
  Bluetooth,
  Smartphone,
  Copy,
  Check,
  UserPlus
} from 'lucide-react';

export const RadarScreen: React.FC = () => {
  const {
    contacts,
    isScanning,
    isBleModalOpen,
    myRealPeerId,
    openBleModal,
    closeBleModal,
    addDiscoveredBluetoothPeer,
    toggleScan,
    openChat,
    openVerifySecurity,
    connectPeer,
    disconnectPeer,
    connectToPhoneNodeId,
    startCall
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'CONNECTED' | 'IN_RANGE'>('ALL');
  const [friendCodeInput, setFriendCodeInput] = useState('');
  const [copied, setCopied] = useState(false);
  const [isLinking, setIsLinking] = useState(false);
  const [linkStatus, setLinkStatus] = useState<string | null>(null);

  const connectedCount = contacts.filter(c => c.connectionState === 'CONNECTED').length;
  const inRangeCount = contacts.filter(c => c.connectionState !== 'DISCONNECTED').length;

  const copyMyCode = () => {
    navigator.clipboard.writeText(myRealPeerId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleManualConnect = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!friendCodeInput.trim()) return;
    setIsLinking(true);
    setLinkStatus('Establishing direct WebRTC socket...');

    try {
      const res = await connectToPhoneNodeId(friendCodeInput.trim());
      if (res) {
        setLinkStatus('Connected to phone!');
        setFriendCodeInput('');
      } else {
        setLinkStatus('Connecting to peer node in range...');
      }
    } catch {
      setLinkStatus('Could not link to peer.');
    } finally {
      setIsLinking(false);
      setTimeout(() => setLinkStatus(null), 3000);
    }
  };

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
    <div className="w-full max-w-4xl mx-auto px-4 py-4 pb-28 flex flex-col gap-4 font-mono-tactical">
      {/* 1. Tactical Radar Banner */}
      <TacticalRadarBanner
        isScanning={isScanning}
        connectedCount={connectedCount}
        discoveredCount={inRangeCount}
        onToggleScan={toggleScan}
      />

      {/* 2. Real Phone-to-Phone Hardware Identity & Bluetooth Connect Banner */}
      <div className="w-full rounded-2xl bg-gradient-to-r from-[#0E1A29] via-[#112338] to-[#0A1624] border-2 border-[#06B6D4]/50 p-4 flex flex-col gap-4 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="w-11 h-11 rounded-xl bg-[#06B6D4]/20 border border-[#06B6D4]/50 flex items-center justify-center text-[#06B6D4] shrink-0 mt-0.5">
              <Bluetooth className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-bold text-[14px] text-[#F9FAFB] tracking-wide">
                  REAL BLUETOOTH & PHONE-TO-PHONE LINK
                </span>
                <span className="text-[9px] px-2 py-0.5 rounded-full bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/40 font-bold">
                  DIRECT P2P
                </span>
              </div>
              <p className="text-[12px] text-[#9CA3AF] mt-1 leading-relaxed font-sans">
                Scan nearby Bluetooth hardware devices or connect two phones directly over airwaves for WhatsApp-style chat and video call.
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={openBleModal}
            className="px-5 py-3 rounded-xl bg-gradient-to-r from-[#06B6D4] to-[#0284C7] hover:from-[#0891B2] hover:to-[#0369A1] text-black font-black text-[13px] flex items-center justify-center gap-2 shadow-lg transition-transform active:scale-95 cursor-pointer shrink-0"
          >
            <Bluetooth className="w-4 h-4" />
            <span>SCAN BLUETOOTH</span>
          </button>
        </div>

        {/* My Device Peer ID Box + Friend Code Connect */}
        <div className="pt-3 border-t border-[#26354A] flex flex-col md:flex-row items-center justify-between gap-3">
          {/* Left: My Device ID */}
          <div className="flex items-center justify-between w-full md:w-auto gap-3 bg-[#0B141E] border border-[#26354A] px-3.5 py-2 rounded-xl">
            <div className="flex items-center gap-2">
              <Smartphone className="w-4 h-4 text-[#10B981]" />
              <div className="flex flex-col">
                <span className="text-[10px] text-[#9CA3AF] uppercase">Your Phone Node ID:</span>
                <span className="text-[13px] font-bold text-[#F59E0B] tracking-wider">{myRealPeerId}</span>
              </div>
            </div>
            <button
              type="button"
              onClick={copyMyCode}
              className="p-1.5 rounded-lg bg-[#1B2636] hover:bg-[#243348] text-[#9CA3AF] hover:text-white transition-colors cursor-pointer"
              title="Copy ID to give to friend"
            >
              {copied ? <Check className="w-4 h-4 text-[#10B981]" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>

          {/* Right: Enter Friend's ID to instantly link */}
          <form onSubmit={handleManualConnect} className="flex items-center gap-2 w-full md:w-auto">
            <input
              type="text"
              value={friendCodeInput}
              onChange={e => setFriendCodeInput(e.target.value)}
              placeholder="Enter friend's Phone Node ID (e.g. phone-XXXX)..."
              className="w-full md:w-64 bg-[#0B141E] border border-[#26354A] rounded-xl px-3 py-2 text-[12px] text-[#F9FAFB] placeholder-[#6B7280] focus:outline-none focus:border-[#06B6D4]"
            />
            <button
              type="submit"
              disabled={isLinking || !friendCodeInput.trim()}
              className="px-3 py-2 rounded-xl bg-[#10B981] hover:bg-[#0ea372] disabled:opacity-50 text-black font-bold text-[12px] flex items-center gap-1.5 shrink-0 cursor-pointer"
            >
              <UserPlus className="w-4 h-4" />
              <span>Link</span>
            </button>
          </form>
        </div>

        {linkStatus && (
          <div className="text-[11px] text-[#06B6D4] font-bold bg-[#06B6D4]/10 border border-[#06B6D4]/30 px-3 py-1.5 rounded-lg">
            {linkStatus}
          </div>
        )}
      </div>

      {/* 3. Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#9CA3AF]" />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Search connected phone or Bluetooth device..."
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
                className={`px-3 py-2 rounded-xl text-[11px] font-bold whitespace-nowrap transition-colors cursor-pointer ${
                  isSel
                    ? 'bg-[#F59E0B] text-black'
                    : 'bg-[#1B2636] border border-[#26354A] text-[#9CA3AF] hover:text-[#F9FAFB]'
                }`}
              >
                {labels[tabKey]}
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Nodes List Header */}
      <div className="flex items-center justify-between px-1">
        <span className="text-[11px] font-bold text-[#9CA3AF] uppercase tracking-wider flex items-center gap-1.5">
          <span>CONNECTED REAL PHONES & BLUETOOTH DEVICES</span>
          <span className="text-[#06B6D4]">({filteredContacts.length})</span>
        </span>
        <span className="text-[10px] text-[#10B981] font-bold">
          E2EE ACTIVE
        </span>
      </div>

      {/* 5. Nodes Cards Grid/List */}
      {filteredContacts.length === 0 ? (
        <div className="w-full rounded-2xl bg-[#111822] border border-[#26354A] p-8 text-center flex flex-col items-center justify-center gap-3">
          <Bluetooth className="w-10 h-10 text-[#06B6D4] animate-pulse mb-1" />
          <h4 className="font-bold text-[16px] text-[#F9FAFB]">No Devices Connected Yet</h4>
          <p className="text-[12px] text-[#9CA3AF] max-w-md font-sans leading-relaxed">
            Dono phones me ye website open karein ya Bluetooth ON karein. "SCAN BLUETOOTH" dabakar apne friend ka device select karein, ya upar unka <strong>Phone Node ID</strong> dalkar connect karein!
          </p>
          <div className="flex items-center gap-3 mt-2">
            <button
              type="button"
              onClick={openBleModal}
              className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-[#06B6D4] to-[#0284C7] text-black font-bold text-[12px] flex items-center gap-2 cursor-pointer"
            >
              <Bluetooth className="w-4 h-4" />
              <span>Scan Bluetooth Now</span>
            </button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {filteredContacts.map(peer => {
            const isConnected = peer.connectionState === 'CONNECTED';

            return (
              <div
                key={peer.peerId}
                className={`w-full rounded-2xl bg-[#111822] border transition-all p-3.5 sm:p-4 flex flex-col gap-3 shadow-md ${
                  isConnected ? 'border-[#10B981]/50 hover:border-[#10B981]' : 'border-[#26354A]/60 opacity-90'
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
                        <span className="font-bold text-[15px] text-[#F9FAFB] font-sans">
                          {peer.displayName}
                        </span>
                        <span className="text-[11px] font-bold text-[#F59E0B] bg-[#F59E0B]/10 px-1.5 py-0.5 rounded-xs border border-[#F59E0B]/30">
                          [{peer.callsign}]
                        </span>
                        {peer.transportType === 'BLUETOOTH' ? (
                          <span className="text-[9px] px-1.5 py-0.5 rounded-xs bg-[#06B6D4]/20 text-[#06B6D4] border border-[#06B6D4]/30 flex items-center gap-1 font-bold">
                            <Bluetooth className="w-2.5 h-2.5" />
                            <span>BLUETOOTH</span>
                          </span>
                        ) : (
                          <span className="text-[9px] px-1.5 py-0.5 rounded-xs bg-[#10B981]/20 text-[#10B981] border border-[#10B981]/30 flex items-center gap-1 font-bold">
                            <Smartphone className="w-2.5 h-2.5" />
                            <span>PHONE P2P</span>
                          </span>
                        )}
                      </div>

                      <div className="flex items-center gap-2 text-[11px] text-[#9CA3AF] mt-0.5">
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

                {/* Bottom Row: Communication Action Buttons */}
                <div className="flex items-center justify-between gap-2 pt-1 border-t border-[#26354A]/30">
                  {/* Link / Unlink Button */}
                  <button
                    type="button"
                    onClick={() => {
                      if (isConnected) disconnectPeer(peer.peerId);
                      else connectPeer(peer.peerId);
                    }}
                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[11px] font-bold transition-colors cursor-pointer ${
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
                        <span>Link Phone</span>
                      </>
                    )}
                  </button>

                  {/* WhatsApp-Style Communications Buttons */}
                  <div className="flex items-center gap-1.5">
                    {/* Chat Button */}
                    <button
                      type="button"
                      onClick={() => openChat(peer.peerId)}
                      className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-[#06B6D4]/15 border border-[#06B6D4]/50 text-[#06B6D4] hover:bg-[#06B6D4]/25 text-[11px] font-bold transition-colors cursor-pointer"
                    >
                      <MessageSquare className="w-3.5 h-3.5" />
                      <span>Chat</span>
                    </button>

                    {/* Audio Call Button */}
                    <button
                      type="button"
                      onClick={() => startCall(peer, false)}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#1B2636] border border-[#26354A] text-[#F9FAFB] hover:border-[#F59E0B] text-[11px] font-bold transition-colors cursor-pointer"
                    >
                      <Phone className="w-3.5 h-3.5 text-[#F59E0B]" />
                      <span className="hidden xs:inline">Audio</span>
                    </button>

                    {/* Video Call Button */}
                    <button
                      type="button"
                      onClick={() => startCall(peer, true)}
                      className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-[#10B981] text-black hover:bg-[#0ea372] text-[11px] font-black transition-colors cursor-pointer shadow-md"
                    >
                      <Video className="w-3.5 h-3.5" />
                      <span>Video Call</span>
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Bluetooth Pairing Modal Dialog */}
      <BluetoothPairingModal
        isOpen={isBleModalOpen}
        onClose={closeBleModal}
        onDevicePaired={addDiscoveredBluetoothPeer}
      />
    </div>
  );
};
