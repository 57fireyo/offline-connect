import React, { useState, useRef, useEffect } from 'react';
import { useApp } from '../../context/AppContext';
import { TacticalAvatar } from '../common/TacticalAvatar';
import { ConnectionStateChip } from '../common/ConnectionStateChip';
import { MessageStatusIcon } from '../common/MessageStatusIcon';
import {
  ArrowLeft,
  Phone,
  Video,
  Shield,
  Trash2,
  Lock,
  Send,
  Paperclip,
  Clock,
  Image as ImageIcon
} from 'lucide-react';

export const ChatScreen: React.FC = () => {
  const {
    activeChatPeerId,
    contacts,
    messages,
    userProfile,
    typingStatus,
    closeChat,
    openVerifySecurity,
    sendMessage,
    clearChat,
    startCall
  } = useApp();

  const [inputMessage, setInputMessage] = useState('');
  const [selectedAttachment, setSelectedAttachment] = useState<string | null>(null);
  const [selectedFileName, setSelectedFileName] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const contact = contacts.find(c => c.peerId === activeChatPeerId);
  const peerMessages = messages.filter(m => m.chatPeerId === activeChatPeerId);
  const isOnline = contact?.connectionState === 'CONNECTED';
  const isTyping = activeChatPeerId ? typingStatus[activeChatPeerId] : false;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [peerMessages.length, isTyping]);

  if (!contact) {
    return null;
  }

  const handleSend = async () => {
    if (!inputMessage.trim() && !selectedAttachment) return;
    const textToSend = inputMessage.trim() || (selectedAttachment ? 'Tactical image attachment' : '');
    const attachmentToSend = selectedAttachment || undefined;
    const fileNameToSend = selectedFileName || undefined;

    setInputMessage('');
    setSelectedAttachment(null);
    setSelectedFileName(null);

    await sendMessage(contact.peerId, textToSend, attachmentToSend ? 'IMAGE' : 'TEXT', attachmentToSend, fileNameToSend);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      setSelectedAttachment(reader.result as string);
      setSelectedFileName(file.name);
    };
    reader.readAsDataURL(file);
  };

  // Tactical quick replies
  const quickReplies = [
    'Status OK • Perimeter Secure',
    'Copy that • Coordinates locked',
    'Moving to rendezvous point',
    'Low bandwidth • Voice fallback ready'
  ];

  const formatMessageTime = (ts: number) => {
    const d = new Date(ts);
    const hrs = d.getHours().toString().padStart(2, '0');
    const mins = d.getMinutes().toString().padStart(2, '0');
    return `${hrs}:${mins}`;
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#090D12] flex flex-col max-w-4xl mx-auto">
      {/* Top Tactical Navigation Bar */}
      <div className="bg-[#111822] border-b border-[#26354A] px-3.5 py-2.5 flex items-center justify-between gap-2 shadow-md">
        <div className="flex items-center gap-2.5 min-w-0">
          <button
            type="button"
            onClick={closeChat}
            className="p-1.5 rounded-lg text-[#9CA3AF] hover:text-[#F9FAFB] hover:bg-[#1B2636] transition-colors cursor-pointer"
            title="Back to conversations"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>

          <TacticalAvatar
            name={contact.displayName}
            callsign={contact.callsign}
            size={40}
            avatarColorIndex={contact.avatarColorIndex}
            isOnline={isOnline}
          />

          <div className="min-w-0 flex flex-col">
            <div className="flex items-center gap-1.5 truncate">
              <span className="font-bold text-[14px] text-[#F9FAFB] truncate">
                {contact.displayName}
              </span>
              <span className="font-mono-tactical text-[11px] font-bold text-[#F59E0B] shrink-0">
                [{contact.callsign}]
              </span>
            </div>

            <div className="flex items-center gap-1.5">
              <ConnectionStateChip state={contact.connectionState} />
              {contact.isVerified && (
                <span className="text-[10px] font-mono-tactical text-[#10B981] font-semibold hidden xs:inline">
                  • KEY VERIFIED
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Action icons */}
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => startCall(contact, false)}
            className="p-2 rounded-lg bg-[#1B2636] text-[#10B981] hover:bg-[#10B981]/20 border border-[#26354A] transition-colors cursor-pointer"
            title="Voice Call"
          >
            <Phone className="w-4 h-4" />
          </button>

          <button
            type="button"
            onClick={() => startCall(contact, true)}
            className="p-2 rounded-lg bg-[#1B2636] text-[#06B6D4] hover:bg-[#06B6D4]/20 border border-[#26354A] transition-colors cursor-pointer"
            title="Video Call"
          >
            <Video className="w-4 h-4" />
          </button>

          <button
            type="button"
            onClick={() => openVerifySecurity(contact)}
            className={`p-2 rounded-lg border transition-colors cursor-pointer ${
              contact.isVerified
                ? 'bg-[#10B981]/15 text-[#10B981] border-[#10B981]/40 hover:bg-[#10B981]/25'
                : 'bg-[#F59E0B]/15 text-[#F59E0B] border-[#F59E0B]/40 hover:bg-[#F59E0B]/25'
            }`}
            title="Verify Peer Cryptographic Keys"
          >
            <Shield className="w-4 h-4" />
          </button>

          <button
            type="button"
            onClick={() => clearChat(contact.peerId)}
            className="p-2 rounded-lg text-[#9CA3AF] hover:text-[#EF4444] hover:bg-[#1B2636] transition-colors cursor-pointer"
            title="Clear Chat History"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Security Info Ribbon */}
      <div className="bg-[#1B2636]/70 border-b border-[#26354A] px-4 py-1.5 flex items-center justify-between text-[10px] font-mono-tactical">
        <div className="flex items-center gap-1.5 text-[#10B981]">
          <Lock className="w-3 h-3" />
          <span>E2EE • AES-256-GCM / ECDH P-256</span>
        </div>
        <button
          type="button"
          onClick={() => openVerifySecurity(contact)}
          className="text-[#F59E0B] hover:underline cursor-pointer"
        >
          {contact.isVerified ? 'VERIFIED ✓' : 'VERIFY SAS CODE'}
        </button>
      </div>

      {/* Store-and-Forward Offline Notice (if disconnected) */}
      {!isOnline && (
        <div className="bg-[#F59E0B]/15 border-b border-[#F59E0B]/30 px-4 py-2 flex items-center gap-2 text-[11px] font-mono-tactical text-[#F59E0B]">
          <Clock className="w-3.5 h-3.5 shrink-0" />
          <span>
            NODE OFFLINE: Messages will be queued locally and automatically delivered when peer enters radio range.
          </span>
        </div>
      )}

      {/* Quick Tactical Preset Chips */}
      <div className="bg-[#090D12] border-b border-[#26354A]/60 px-3 py-1.5 flex items-center gap-1.5 overflow-x-auto shrink-0">
        <span className="text-[10px] font-mono-tactical font-bold text-[#6B7280] uppercase tracking-wider shrink-0 mr-1">
          TACTICAL:
        </span>
        {quickReplies.map((reply, idx) => (
          <button
            key={idx}
            type="button"
            onClick={() => sendMessage(contact.peerId, reply)}
            className="px-2.5 py-1 rounded-md bg-[#1B2636] border border-[#26354A] text-[11px] font-mono-tactical text-[#9CA3AF] hover:text-[#F9FAFB] hover:border-[#F59E0B] hover:bg-[#243348] whitespace-nowrap transition-colors cursor-pointer"
          >
            {reply}
          </button>
        ))}
      </div>

      {/* Message List */}
      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
        {peerMessages.length === 0 ? (
          <div className="my-auto text-center flex flex-col items-center justify-center gap-2 text-[#9CA3AF]">
            <Lock className="w-8 h-8 text-[#10B981]/50" />
            <span className="font-mono-tactical text-[12px] font-semibold text-[#F9FAFB]">
              Direct Encrypted Radio Channel Established
            </span>
            <span className="text-[11px] max-w-xs">
              Messages are encrypted end-to-end with hardware-derived keys and relayed directly peer-to-peer.
            </span>
          </div>
        ) : (
          peerMessages.map(msg => {
            const isMe = msg.senderId === userProfile.peerId;

            return (
              <div
                key={msg.id}
                className={`flex flex-col max-w-[85%] sm:max-w-[70%] ${
                  isMe ? 'self-end items-end' : 'self-start items-start'
                }`}
              >
                {!isMe && (
                  <span className="text-[10px] font-mono-tactical font-semibold text-[#F59E0B] ml-2 mb-0.5">
                    {msg.senderName}
                  </span>
                )}

                <div
                  className={`rounded-2xl px-3.5 py-2.5 text-[13px] shadow-sm leading-relaxed ${
                    isMe
                      ? 'bg-[#243348] text-[#F9FAFB] border border-[#F59E0B]/30 rounded-br-xs'
                      : 'bg-[#1B2636] text-[#F9FAFB] border border-[#26354A] rounded-bl-xs'
                  }`}
                >
                  {/* Attachment preview */}
                  {msg.attachmentData && (
                    <div className="mb-2 rounded-lg overflow-hidden border border-[#26354A]">
                      <img
                        src={msg.attachmentData}
                        alt="Tactical attachment"
                        className="w-full max-h-56 object-cover"
                      />
                      {msg.fileName && (
                        <div className="bg-[#111822] px-2 py-1 text-[10px] font-mono-tactical text-[#9CA3AF] flex items-center gap-1">
                          <ImageIcon className="w-3 h-3 text-[#06B6D4]" />
                          <span className="truncate">{msg.fileName}</span>
                        </div>
                      )}
                    </div>
                  )}

                  <p className="whitespace-pre-wrap break-words">{msg.content}</p>

                  <div className="flex items-center justify-end gap-1.5 mt-1 text-[10px] font-mono-tactical text-[#9CA3AF]">
                    <span>{formatMessageTime(msg.timestamp)}</span>
                    {isMe && <MessageStatusIcon status={msg.status} />}
                  </div>
                </div>
              </div>
            );
          })
        )}

        {/* Typing indicator */}
        {isTyping && (
          <div className="self-start flex items-center gap-2 bg-[#1B2636] border border-[#26354A] px-3 py-1.5 rounded-full text-[11px] font-mono-tactical text-[#06B6D4]">
            <span className="w-1.5 h-1.5 rounded-full bg-[#06B6D4] animate-pulse" />
            <span>{contact.callsign} is transmitting...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Selected Attachment preview bar before sending */}
      {selectedAttachment && (
        <div className="bg-[#1B2636] border-t border-[#26354A] px-4 py-2 flex items-center justify-between gap-2">
          <div className="flex items-center gap-2 text-[12px] font-mono-tactical text-[#F9FAFB]">
            <ImageIcon className="w-4 h-4 text-[#06B6D4]" />
            <span className="truncate max-w-xs">{selectedFileName || 'image_payload.png'}</span>
          </div>
          <button
            type="button"
            onClick={() => {
              setSelectedAttachment(null);
              setSelectedFileName(null);
            }}
            className="text-[11px] font-mono-tactical text-[#EF4444] hover:underline cursor-pointer"
          >
            Remove
          </button>
        </div>
      )}

      {/* Bottom Input Field */}
      <div className="bg-[#111822] border-t border-[#26354A] p-3 flex items-center gap-2">
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          accept="image/*"
          className="hidden"
        />

        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="p-2.5 rounded-xl bg-[#1B2636] text-[#9CA3AF] hover:text-[#06B6D4] hover:bg-[#243348] border border-[#26354A] transition-colors cursor-pointer shrink-0"
          title="Attach field photo/map"
        >
          <Paperclip className="w-4 h-4" />
        </button>

        <input
          type="text"
          value={inputMessage}
          onChange={e => setInputMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={isOnline ? `Transmit encrypted packet to [${contact.callsign}]...` : 'Peer offline • Message will be queued...'}
          className="flex-1 bg-[#1B2636] border border-[#26354A] rounded-xl px-4 py-2.5 text-[13px] text-[#F9FAFB] placeholder-[#6B7280] focus:outline-none focus:border-[#F59E0B] transition-colors"
        />

        <button
          type="button"
          onClick={handleSend}
          disabled={!inputMessage.trim() && !selectedAttachment}
          className={`p-2.5 rounded-xl flex items-center justify-center transition-all cursor-pointer shrink-0 ${
            inputMessage.trim() || selectedAttachment
              ? 'bg-[#F59E0B] text-black hover:bg-[#d98b08]'
              : 'bg-[#243348] text-[#6B7280] cursor-not-allowed opacity-60'
          }`}
          title="Send packet"
        >
          <Send className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
