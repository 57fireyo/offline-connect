import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalAvatar } from '../common/TacticalAvatar';
import { MessageStatusIcon } from '../common/MessageStatusIcon';
import { Lock, Search, X, MessageSquare, Radar, Phone, Video } from 'lucide-react';

export const ChatsListScreen: React.FC = () => {
  const { conversations, openChat, startCall, setActiveTab } = useApp();
  const [searchQuery, setSearchQuery] = useState('');

  const filtered = conversations.filter(c => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      c.contact.displayName.toLowerCase().includes(q) ||
      c.contact.callsign.toLowerCase().includes(q) ||
      (c.lastMessage?.content.toLowerCase().includes(q) ?? false)
    );
  });

  const formatTime = (timestamp?: number) => {
    if (!timestamp) return '';
    const d = new Date(timestamp);
    const hrs = d.getHours().toString().padStart(2, '0');
    const mins = d.getMinutes().toString().padStart(2, '0');
    return `${hrs}:${mins}`;
  };

  return (
    <div className="w-full max-w-4xl mx-auto px-4 py-4 pb-28 flex flex-col gap-3.5">
      {/* 1. Top Encrypted Status Bar */}
      <div className="w-full rounded-xl bg-[#1B2636] border border-[#26354A] px-3.5 py-2.5 flex items-center justify-between shadow-xs">
        <div className="flex items-center gap-2 text-[#10B981]">
          <Lock className="w-3.5 h-3.5" />
          <span className="font-mono-tactical text-[11px] font-bold tracking-wider">
            AIR-GAPPED CHANNELS
          </span>
        </div>
        <span className="font-mono-tactical text-[10px] text-[#F59E0B] font-semibold">
          STORE-AND-FORWARD ON
        </span>
      </div>

      {/* 2. Search Filter Bar */}
      <div className="relative w-full">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#9CA3AF]" />
        <input
          type="text"
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          placeholder="Search peers, callsigns or transcripts..."
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

      {/* 3. Header Title */}
      <div className="flex items-center justify-between">
        <span className="font-mono-tactical text-[12px] font-bold text-[#9CA3AF] tracking-wider uppercase">
          ACTIVE SECURE THREADS ({filtered.length})
        </span>
        <span className="font-mono-tactical text-[10px] text-[#06B6D4]">
          ECDH P-256 / AES-256
        </span>
      </div>

      {/* 4. Conversations List */}
      {filtered.length === 0 ? (
        <div className="w-full rounded-2xl bg-[#1B2636] border border-[#26354A] p-8 text-center flex flex-col items-center justify-center gap-3">
          <MessageSquare className="w-9 h-9 text-[#F59E0B]" />
          <h4 className="font-bold text-[15px] text-[#F9FAFB]">
            {searchQuery ? 'No matching conversations' : 'No Active Encrypted Sessions'}
          </h4>
          <p className="text-[12px] text-[#9CA3AF] max-w-sm leading-relaxed">
            Discover peers in radio range via the Radar tab to open an offline direct encrypted channel.
          </p>
          <button
            type="button"
            onClick={() => setActiveTab('radar')}
            className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#F59E0B] text-black font-mono-tactical text-[12px] font-bold hover:bg-[#d98b08] cursor-pointer mt-1"
          >
            <Radar className="w-4 h-4" />
            <span>Scan Nearby Nodes</span>
          </button>
        </div>
      ) : (
        <div className="flex flex-col gap-2.5">
          {filtered.map(({ contact, lastMessage, unreadCount, queuedCount }) => {
            const isOnline = contact.connectionState === 'CONNECTED';
            const formattedTime = formatTime(lastMessage?.timestamp);

            return (
              <div
                key={contact.peerId}
                onClick={() => openChat(contact.peerId)}
                className={`w-full rounded-2xl bg-[#111822] border transition-all p-3.5 flex items-center justify-between gap-3 cursor-pointer hover:bg-[#1B2636]/60 ${
                  unreadCount > 0 ? 'border-[#F59E0B]/50' : 'border-[#26354A]'
                }`}
              >
                {/* Left: Avatar */}
                <TacticalAvatar
                  name={contact.displayName}
                  callsign={contact.callsign}
                  size={50}
                  avatarColorIndex={contact.avatarColorIndex}
                  isOnline={isOnline}
                />

                {/* Center: Info & Last Message Snippet */}
                <div className="flex-1 min-w-0 flex flex-col gap-1">
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-1.5 truncate">
                      <span className="font-bold text-[14px] text-[#F9FAFB] truncate">
                        {contact.displayName}
                      </span>
                      <span className="font-mono-tactical text-[11px] font-bold text-[#F59E0B] shrink-0">
                        [{contact.callsign}]
                      </span>
                    </div>

                    {formattedTime && (
                      <span
                        className={`font-mono-tactical text-[11px] shrink-0 ${
                          unreadCount > 0 ? 'text-[#F59E0B] font-bold' : 'text-[#9CA3AF]'
                        }`}
                      >
                        {formattedTime}
                      </span>
                    )}
                  </div>

                  {/* Message Snippet */}
                  <div className="flex items-center gap-1.5">
                    {lastMessage && <MessageStatusIcon status={lastMessage.status} />}
                    <p
                      className={`text-[13px] truncate flex-1 ${
                        unreadCount > 0
                          ? 'text-[#F9FAFB] font-semibold'
                          : 'text-[#9CA3AF]'
                      }`}
                    >
                      {lastMessage ? lastMessage.content : 'No messages exchanged yet. Tap to chat.'}
                    </p>

                    {/* Unread badge or Queued badge */}
                    {unreadCount > 0 ? (
                      <span className="px-1.5 py-0.5 rounded-full bg-[#F59E0B] text-black font-mono-tactical text-[10px] font-black shrink-0">
                        {unreadCount}
                      </span>
                    ) : queuedCount > 0 ? (
                      <span className="px-1.5 py-0.5 rounded-sm bg-[#06B6D4]/20 text-[#06B6D4] border border-[#06B6D4] font-mono-tactical text-[9px] font-bold shrink-0">
                        {queuedCount} QUEUED
                      </span>
                    ) : null}
                  </div>

                  {/* Connection metrics sub-strip */}
                  <div className="flex items-center gap-2 text-[10px] font-mono-tactical">
                    <span className={isOnline ? 'text-[#10B981]' : 'text-[#9CA3AF]'}>
                      {isOnline ? `LINKED • ${contact.rssi}dBm` : contact.connectionState}
                    </span>
                    {contact.isVerified && (
                      <span className="text-[#10B981] font-semibold">• VERIFIED KEY</span>
                    )}
                  </div>
                </div>

                {/* Right: Quick Call Actions */}
                <div
                  className="flex items-center gap-1 shrink-0"
                  onClick={e => e.stopPropagation()}
                >
                  <button
                    type="button"
                    onClick={() => startCall(contact, false)}
                    className="w-8 h-8 rounded-lg bg-[#243348] text-[#10B981] hover:bg-[#10B981]/20 flex items-center justify-center transition-colors cursor-pointer"
                    title="Voice Call"
                  >
                    <Phone className="w-4 h-4" />
                  </button>

                  <button
                    type="button"
                    onClick={() => startCall(contact, true)}
                    className="w-8 h-8 rounded-lg bg-[#243348] text-[#06B6D4] hover:bg-[#06B6D4]/20 flex items-center justify-center transition-colors cursor-pointer"
                    title="Video Call"
                  >
                    <Video className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
