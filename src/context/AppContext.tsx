import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import {
  ContactEntity,
  MessageEntity,
  SosAlertEntity,
  UserProfile,
  MessageType,
  EmergencyType,
  UrgencyLevel,
  CallState,
  ConversationSummary
} from '../types';
import { cryptoManager } from '../crypto/cryptoManager';
import { tacticalAudio } from '../services/audioService';

interface CallSessionState {
  active: boolean;
  peer: ContactEntity | null;
  isVideo: boolean;
  callState: CallState;
  durationSeconds: number;
  isAudioMuted: boolean;
  isVideoEnabled: boolean;
  isSpeakerphoneOn: boolean;
  isFrontCamera: boolean;
  bitrateKbps: number;
  fps: number;
  isVoiceFallback: boolean;
}

interface AppContextType {
  userProfile: UserProfile;
  contacts: ContactEntity[];
  messages: MessageEntity[];
  sosAlerts: SosAlertEntity[];
  activeTab: 'radar' | 'chats' | 'sos' | 'settings';
  activeChatPeerId: string | null;
  verifyingContact: ContactEntity | null;
  isScanning: boolean;
  isDemoMode: boolean;
  typingStatus: Record<string, boolean>;
  callSession: CallSessionState;
  conversations: ConversationSummary[];
  queuedCountTotal: number;

  // Actions
  setActiveTab: (tab: 'radar' | 'chats' | 'sos' | 'settings') => void;
  openChat: (peerId: string) => void;
  closeChat: () => void;
  openVerifySecurity: (contact: ContactEntity) => void;
  closeVerifySecurity: () => void;
  toggleVerifyContact: (peerId: string, isVerified: boolean) => void;
  toggleScan: () => void;
  toggleDemoMode: () => void;
  updateProfile: (name: string, callsign: string, avatarColor: number, meshRelay: boolean, batterySaver: boolean) => void;
  connectPeer: (peerId: string) => void;
  disconnectPeer: (peerId: string) => void;
  sendMessage: (peerId: string, text: string, type?: MessageType, attachmentData?: string, fileName?: string) => Promise<void>;
  markAllSeen: (peerId: string) => void;
  clearChat: (peerId: string) => void;
  broadcastSos: (type: EmergencyType, urgency: UrgencyLevel, notes: string, lat?: number, lon?: number) => void;
  acknowledgeAlert: (alertId: string) => void;

  // Call actions
  startCall: (contact: ContactEntity, isVideo: boolean) => void;
  endCall: () => void;
  answerCall: () => void;
  toggleMute: () => void;
  toggleVideo: () => void;
  switchCamera: () => void;
  toggleSpeakerphone: () => void;
}

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // 1. Operator Profile
  const [userProfile, setUserProfile] = useState<UserProfile>(() => {
    const saved = localStorage.getItem('offgrid_user_profile');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return {
      peerId: 'NODE-7A1F4B',
      displayName: 'Operator Alpha',
      callsign: 'ECHO-7',
      avatarColorIndex: 1,
      meshRelayEnabled: true,
      batterySaverEnabled: false
    };
  });

  // 2. Navigation State
  const [activeTab, setActiveTab] = useState<'radar' | 'chats' | 'sos' | 'settings'>('radar');
  const [activeChatPeerId, setActiveChatPeerId] = useState<string | null>(null);
  const [verifyingContact, setVerifyingContact] = useState<ContactEntity | null>(null);

  // 3. Radio & Mesh State
  const [isScanning, setIsScanning] = useState<boolean>(true);
  const [isDemoMode, setIsDemoMode] = useState<boolean>(true);
  const [typingStatus, setTypingStatus] = useState<Record<string, boolean>>({});

  // 4. Contacts State
  const [contacts, setContacts] = useState<ContactEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_contacts');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return [
      {
        peerId: 'NODE-SAR-01',
        displayName: 'Ranger Sarah',
        callsign: 'VALKYRIE-1',
        publicKeyBase64: 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f',
        connectionState: 'CONNECTED',
        lastSeenTimestamp: Date.now(),
        rssi: -54,
        distanceEstimateMeters: 8.5,
        isVerified: true,
        avatarColorIndex: 2,
        batteryPercent: 92,
        isSimulatedDemo: true
      },
      {
        peerId: 'NODE-MEDIC-03',
        displayName: 'Medic Dave',
        callsign: 'CADUCEUS-3',
        publicKeyBase64: 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f',
        connectionState: 'CONNECTED',
        lastSeenTimestamp: Date.now() - 45000,
        rssi: -68,
        distanceEstimateMeters: 18.0,
        isVerified: false,
        avatarColorIndex: 0,
        batteryPercent: 64,
        isSimulatedDemo: true
      },
      {
        peerId: 'NODE-BASE-09',
        displayName: 'Base Camp Echo',
        callsign: 'FORTRESS-MAIN',
        publicKeyBase64: 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a',
        connectionState: 'DISCONNECTED',
        lastSeenTimestamp: Date.now() - 360000,
        rssi: -86,
        distanceEstimateMeters: 72.0,
        isVerified: true,
        avatarColorIndex: 3,
        batteryPercent: 100,
        isSimulatedDemo: true
      }
    ];
  });

  // 5. Messages State
  const [messages, setMessages] = useState<MessageEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_messages');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return [
      {
        id: 'msg-init-1',
        chatPeerId: 'NODE-SAR-01',
        senderId: 'NODE-SAR-01',
        senderName: 'Ranger Sarah',
        content: 'OffGrid link active on channel 4. Signal strength high. We are establishing the valley search perimeter.',
        messageType: 'TEXT',
        fileSizeBytes: 0,
        timestamp: Date.now() - 120000,
        status: 'SEEN'
      },
      {
        id: 'msg-init-2',
        chatPeerId: 'NODE-SAR-01',
        senderId: 'NODE-7A1F4B',
        senderName: 'Operator Alpha',
        content: 'Copy that Valkyrie-1. End-to-end encryption keys verified. All peer packets routing offline.',
        messageType: 'TEXT',
        fileSizeBytes: 0,
        timestamp: Date.now() - 60000,
        status: 'SEEN'
      }
    ];
  });

  // 6. SOS Alerts State
  const [sosAlerts, setSosAlerts] = useState<SosAlertEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_sos_alerts');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // ignore
      }
    }
    return [
      {
        id: 'sos-init-1',
        senderId: 'NODE-MEDIC-03',
        senderName: 'Medic Dave',
        callsign: 'CADUCEUS-3',
        timestamp: Date.now() - 300000,
        emergencyType: 'MEDICAL',
        urgencyLevel: 'HIGH',
        latitude: 36.6002,
        longitude: -118.0583,
        altitudeMeters: 2450.0,
        notes: 'Injured hiker with ankle fracture at Ridge Trail mark 4. First aid administered. Need stretcher team.',
        acknowledged: false
      }
    ];
  });

  // 7. Call Session State
  const [callSession, setCallSession] = useState<CallSessionState>({
    active: false,
    peer: null,
    isVideo: true,
    callState: 'IDLE',
    durationSeconds: 0,
    isAudioMuted: false,
    isVideoEnabled: true,
    isSpeakerphoneOn: true,
    isFrontCamera: true,
    bitrateKbps: 1250,
    fps: 30,
    isVoiceFallback: false
  });

  // Initialize WebCrypto
  useEffect(() => {
    cryptoManager.initialize().then(() => {
      const fingerprint = cryptoManager.getDeviceFingerprint();
      setUserProfile(prev => {
        const updated = { ...prev, peerId: `NODE-${fingerprint}` };
        localStorage.setItem('offgrid_user_profile', JSON.stringify(updated));
        return updated;
      });
    });
  }, []);

  // Save changes to localStorage
  useEffect(() => {
    localStorage.setItem('offgrid_contacts', JSON.stringify(contacts));
  }, [contacts]);

  useEffect(() => {
    localStorage.setItem('offgrid_messages', JSON.stringify(messages));
  }, [messages]);

  useEffect(() => {
    localStorage.setItem('offgrid_sos_alerts', JSON.stringify(sosAlerts));
  }, [sosAlerts]);

  // Periodic Telemetry Heartbeat (fluctuating RSSI & distance)
  useEffect(() => {
    if (!isScanning) return;
    const interval = setInterval(() => {
      setContacts(prev =>
        prev.map(c => {
          if (c.connectionState === 'CONNECTED' && c.isSimulatedDemo) {
            const jitter = Math.floor(Math.random() * 5) - 2;
            const newRssi = Math.max(-95, Math.min(-42, c.rssi + jitter));
            const distJitter = (Math.random() * 0.4 - 0.2);
            const newDist = Math.max(1.0, +(c.distanceEstimateMeters + distJitter).toFixed(1));
            return {
              ...c,
              rssi: newRssi,
              distanceEstimateMeters: newDist,
              lastSeenTimestamp: Date.now()
            };
          }
          return c;
        })
      );
    }, 4500);

    return () => clearInterval(interval);
  }, [isScanning]);

  // Call timer & metrics updater
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  useEffect(() => {
    if (callSession.active && callSession.callState === 'CONNECTED') {
      timerRef.current = setInterval(() => {
        setCallSession(prev => ({
          ...prev,
          durationSeconds: prev.durationSeconds + 1,
          bitrateKbps: prev.isVoiceFallback
            ? Math.floor(48 + Math.random() * 16)
            : Math.floor(980 + Math.random() * 450),
          fps: prev.isVoiceFallback ? 0 : (Math.random() > 0.1 ? 30 : 28)
        }));
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [callSession.active, callSession.callState, callSession.isVoiceFallback]);

  // Store-and-Forward: Flush queued messages when a contact connects
  const flushQueuedMessages = useCallback((peerId: string) => {
    setMessages(prev => {
      const hasQueued = prev.some(m => m.chatPeerId === peerId && m.status === 'QUEUED');
      if (!hasQueued) return prev;

      return prev.map(m => {
        if (m.chatPeerId === peerId && m.status === 'QUEUED') {
          return { ...m, status: 'DELIVERED' };
        }
        return m;
      });
    });
  }, []);

  // Connect / Disconnect peer
  const connectPeer = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    setContacts(prev =>
      prev.map(c => {
        if (c.peerId === peerId) {
          const updated: ContactEntity = {
            ...c,
            connectionState: 'CONNECTED',
            lastSeenTimestamp: Date.now()
          };
          return updated;
        }
        return c;
      })
    );
    // Flush store-and-forward queue
    setTimeout(() => {
      flushQueuedMessages(peerId);
    }, 500);
  }, [flushQueuedMessages]);

  const disconnectPeer = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    setContacts(prev =>
      prev.map(c => {
        if (c.peerId === peerId) {
          return {
            ...c,
            connectionState: 'DISCONNECTED',
            lastSeenTimestamp: Date.now()
          };
        }
        return c;
      })
    );
  }, []);

  // Send Message (with real store-and-forward queueing & demo simulation)
  const sendMessage = useCallback(async (
    peerId: string,
    text: string,
    type: MessageType = 'TEXT',
    attachmentData?: string,
    fileName?: string
  ) => {
    const contact = contacts.find(c => c.peerId === peerId);
    const isOnline = contact?.connectionState === 'CONNECTED';

    const newMsg: MessageEntity = {
      id: 'msg-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7),
      chatPeerId: peerId,
      senderId: userProfile.peerId,
      senderName: userProfile.displayName,
      content: text,
      messageType: type,
      attachmentData,
      fileName,
      fileSizeBytes: attachmentData ? Math.floor(attachmentData.length * 0.75) : 0,
      timestamp: Date.now(),
      status: isOnline ? 'SENT' : 'QUEUED'
    };

    setMessages(prev => [...prev, newMsg]);
    tacticalAudio.playRogerBeep();

    if (!isOnline) {
      // Stored in queue, will auto-flush upon reconnection
      return;
    }

    // If online and in demo mode with simulated peers
    if (isDemoMode && contact?.isSimulatedDemo) {
      // Step 1: Delivered
      setTimeout(() => {
        setMessages(prev =>
          prev.map(m => (m.id === newMsg.id ? { ...m, status: 'DELIVERED' } : m))
        );
      }, 350);

      // Step 2: Read / Seen
      setTimeout(() => {
        setMessages(prev =>
          prev.map(m => (m.id === newMsg.id ? { ...m, status: 'SEEN' } : m))
        );
      }, 750);

      // Step 3: Peer starts typing
      setTimeout(() => {
        setTypingStatus(prev => ({ ...prev, [peerId]: true }));
      }, 1000);

      // Step 4: Peer reply arrives
      setTimeout(() => {
        setTypingStatus(prev => ({ ...prev, [peerId]: false }));

        let replyContent = `Valkyrie-1 received: "${text}". Signal RSSI: ${contact.rssi} dBm. Stored and relayed over offline mesh.`;
        const lower = text.toLowerCase();
        if (lower.includes('sos') || lower.includes('help') || lower.includes('emergency')) {
          replyContent = 'Ranger Sarah here. Distress message acknowledged. Drone reconnaissance has your grid. Keep radio open.';
        } else if (lower.includes('status') || lower.includes('check') || lower.includes('ping')) {
          replyContent = 'Base Camp Echo reports all 4 repeaters online. Mesh link latency 42ms. Wi-Fi Direct channel 6 clear.';
        } else if (lower.includes('call') || lower.includes('video') || lower.includes('voice')) {
          replyContent = 'Ready on peer link. Tap the Video Call icon in the top right to start the P2P stream.';
        } else if (attachmentData) {
          replyContent = 'Tactical attachment received. Image decrypted cleanly with AES-256 session key. Analyzing terrain markers.';
        }

        const replyMsg: MessageEntity = {
          id: 'msg-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7),
          chatPeerId: peerId,
          senderId: peerId,
          senderName: contact.displayName,
          content: replyContent,
          messageType: 'TEXT',
          fileSizeBytes: 0,
          timestamp: Date.now(),
          status: 'SEEN'
        };

        setMessages(prev => [...prev, replyMsg]);
        tacticalAudio.playRogerBeep();
      }, 2400);
    }
  }, [contacts, userProfile, isDemoMode]);

  // Mark all seen in peer conversation
  const markAllSeen = useCallback((peerId: string) => {
    setMessages(prev =>
      prev.map(m => {
        if (m.chatPeerId === peerId && m.senderId !== userProfile.peerId && m.status !== 'SEEN') {
          return { ...m, status: 'SEEN' };
        }
        return m;
      })
    );
  }, [userProfile.peerId]);

  // Clear chat history
  const clearChat = useCallback((peerId: string) => {
    setMessages(prev => prev.filter(m => m.chatPeerId !== peerId));
    tacticalAudio.playTacticalClick();
  }, []);

  // Broadcast SOS
  const broadcastSos = useCallback((
    type: EmergencyType,
    urgency: UrgencyLevel,
    notes: string,
    lat: number = 36.5785,
    lon: number = -118.2923
  ) => {
    tacticalAudio.playSosAlert();

    const alert: SosAlertEntity = {
      id: 'sos-' + Date.now(),
      senderId: userProfile.peerId,
      senderName: userProfile.displayName,
      callsign: userProfile.callsign,
      timestamp: Date.now(),
      emergencyType: type,
      urgencyLevel: urgency,
      latitude: lat,
      longitude: lon,
      altitudeMeters: 2450.0,
      notes: notes || 'Emergency distress signal broadcast over P2P mesh network.',
      acknowledged: true
    };

    setSosAlerts(prev => [alert, ...prev]);

    // In demo mode, simulate Search & Rescue automated acknowledgment
    if (isDemoMode) {
      setTimeout(() => {
        const sarReply: MessageEntity = {
          id: 'msg-sar-ack-' + Date.now(),
          chatPeerId: 'NODE-SAR-01',
          senderId: 'NODE-SAR-01',
          senderName: 'Ranger Sarah',
          content: `[SOS ACKNOWLEDGED] Dispatch team en route to coordinates ${lat.toFixed(4)}, ${lon.toFixed(4)}. Maintain position and conserve battery.`,
          messageType: 'TEXT',
          fileSizeBytes: 0,
          timestamp: Date.now(),
          status: 'DELIVERED'
        };
        setMessages(prev => [...prev, sarReply]);
        tacticalAudio.playRogerBeep();
      }, 1800);
    }
  }, [userProfile, isDemoMode]);

  const acknowledgeAlert = useCallback((alertId: string) => {
    tacticalAudio.playTacticalClick();
    setSosAlerts(prev =>
      prev.map(a => (a.id === alertId ? { ...a, acknowledged: !a.acknowledged } : a))
    );
  }, []);

  // Verify contact keys toggle
  const toggleVerifyContact = useCallback((peerId: string, isVerified: boolean) => {
    tacticalAudio.playTacticalClick();
    setContacts(prev =>
      prev.map(c => (c.peerId === peerId ? { ...c, isVerified } : c))
    );
    if (verifyingContact && verifyingContact.peerId === peerId) {
      setVerifyingContact(prev => (prev ? { ...prev, isVerified } : null));
    }
  }, [verifyingContact]);

  // Profile update
  const updateProfile = useCallback((
    name: string,
    callsign: string,
    avatarColor: number,
    meshRelay: boolean,
    batterySaver: boolean
  ) => {
    tacticalAudio.playTacticalClick();
    setUserProfile(prev => {
      const updated: UserProfile = {
        ...prev,
        displayName: name,
        callsign: callsign,
        avatarColorIndex: avatarColor,
        meshRelayEnabled: meshRelay,
        batterySaverEnabled: batterySaver
      };
      localStorage.setItem('offgrid_user_profile', JSON.stringify(updated));
      return updated;
    });
  }, []);

  // Call actions
  const startCall = useCallback((contact: ContactEntity, isVideo: boolean) => {
    tacticalAudio.playDialTone();
    setCallSession({
      active: true,
      peer: contact,
      isVideo,
      callState: 'OUTGOING',
      durationSeconds: 0,
      isAudioMuted: false,
      isVideoEnabled: isVideo,
      isSpeakerphoneOn: true,
      isFrontCamera: true,
      bitrateKbps: 1250,
      fps: 30,
      isVoiceFallback: !isVideo
    });

    // In demo mode or local P2P, connect after 2 seconds
    setTimeout(() => {
      setCallSession(prev => {
        if (prev.active && prev.callState === 'OUTGOING') {
          tacticalAudio.playRogerBeep();
          return { ...prev, callState: 'CONNECTED' };
        }
        return prev;
      });
    }, 2000);
  }, []);

  const endCall = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setCallSession(prev => ({ ...prev, callState: 'ENDED' }));
    setTimeout(() => {
      setCallSession(prev => ({ ...prev, active: false, callState: 'IDLE', peer: null }));
    }, 800);
  }, []);

  const answerCall = useCallback(() => {
    tacticalAudio.playRogerBeep();
    setCallSession(prev => ({ ...prev, callState: 'CONNECTED' }));
  }, []);

  const toggleMute = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setCallSession(prev => ({ ...prev, isAudioMuted: !prev.isAudioMuted }));
  }, []);

  const toggleVideo = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setCallSession(prev => {
      const nextVideo = !prev.isVideoEnabled;
      return {
        ...prev,
        isVideoEnabled: nextVideo,
        isVoiceFallback: !nextVideo
      };
    });
  }, []);

  const switchCamera = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setCallSession(prev => ({ ...prev, isFrontCamera: !prev.isFrontCamera }));
  }, []);

  const toggleSpeakerphone = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setCallSession(prev => ({ ...prev, isSpeakerphoneOn: !prev.isSpeakerphoneOn }));
  }, []);

  // UI Handlers
  const openChat = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    setActiveChatPeerId(peerId);
    markAllSeen(peerId);
  }, [markAllSeen]);

  const closeChat = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setActiveChatPeerId(null);
  }, []);

  const openVerifySecurity = useCallback((contact: ContactEntity) => {
    tacticalAudio.playTacticalClick();
    setVerifyingContact(contact);
  }, []);

  const closeVerifySecurity = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setVerifyingContact(null);
  }, []);

  const toggleScan = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setIsScanning(prev => !prev);
  }, []);

  const toggleDemoMode = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setIsDemoMode(prev => !prev);
  }, []);

  // Compute conversation summaries
  const conversations: ConversationSummary[] = contacts.map(contact => {
    const peerMsgs = messages.filter(m => m.chatPeerId === contact.peerId);
    const lastMessage = peerMsgs[peerMsgs.length - 1];
    const unreadCount = peerMsgs.filter(m => m.senderId !== userProfile.peerId && m.status !== 'SEEN').length;
    const queuedCount = peerMsgs.filter(m => m.status === 'QUEUED').length;

    return {
      contact,
      lastMessage,
      unreadCount,
      queuedCount
    };
  });

  const queuedCountTotal = messages.filter(m => m.status === 'QUEUED').length;

  return (
    <AppContext.Provider
      value={{
        userProfile,
        contacts,
        messages,
        sosAlerts,
        activeTab,
        activeChatPeerId,
        verifyingContact,
        isScanning,
        isDemoMode,
        typingStatus,
        callSession,
        conversations,
        queuedCountTotal,
        setActiveTab,
        openChat,
        closeChat,
        openVerifySecurity,
        closeVerifySecurity,
        toggleVerifyContact,
        toggleScan,
        toggleDemoMode,
        updateProfile,
        connectPeer,
        disconnectPeer,
        sendMessage,
        markAllSeen,
        clearChat,
        broadcastSos,
        acknowledgeAlert,
        startCall,
        endCall,
        answerCall,
        toggleMute,
        toggleVideo,
        switchCamera,
        toggleSpeakerphone
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = (): AppContextType => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
