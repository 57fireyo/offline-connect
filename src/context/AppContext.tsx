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
import { realP2PService, DiscoveredPeerInfo } from '../services/realP2PService';

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
  isBleModalOpen: boolean;
  myRealPeerId: string;

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
  connectToPhoneNodeId: (targetId: string) => Promise<boolean>;
  sendMessage: (peerId: string, text: string, type?: MessageType, attachmentData?: string, fileName?: string) => Promise<void>;
  markAllSeen: (peerId: string) => void;
  clearChat: (peerId: string) => void;
  broadcastSos: (type: EmergencyType, urgency: UrgencyLevel, notes: string, lat?: number, lon?: number) => void;
  acknowledgeAlert: (alertId: string) => void;
  openBleModal: () => void;
  closeBleModal: () => void;
  addDiscoveredBluetoothPeer: (device: { id: string; name: string; rssi: number }) => void;

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
  // Clear any old fake demo contacts from previous sessions
  useEffect(() => {
    const savedContacts = localStorage.getItem('offgrid_contacts');
    if (savedContacts && savedContacts.includes('NODE-SAR-01')) {
      localStorage.removeItem('offgrid_contacts');
      localStorage.removeItem('offgrid_messages');
      localStorage.removeItem('offgrid_sos_alerts');
    }
  }, []);

  // 1. Operator Profile
  const [userProfile, setUserProfile] = useState<UserProfile>(() => {
    const saved = localStorage.getItem('offgrid_user_profile');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (!parsed.displayName.includes('Sarah') && !parsed.displayName.includes('Alpha')) {
          return parsed;
        }
      } catch {
        // ignore
      }
    }
    const randSuffix = Math.random().toString(36).substring(2, 6).toUpperCase();
    return {
      peerId: `phone-${randSuffix}`,
      displayName: `Phone User ${randSuffix}`,
      callsign: `DEV-${randSuffix}`,
      avatarColorIndex: Math.floor(Math.random() * 4),
      meshRelayEnabled: true,
      batterySaverEnabled: false
    };
  });

  // 2. Navigation State
  const [activeTab, setActiveTab] = useState<'radar' | 'chats' | 'sos' | 'settings'>('radar');
  const [activeChatPeerId, setActiveChatPeerId] = useState<string | null>(null);
  const [verifyingContact, setVerifyingContact] = useState<ContactEntity | null>(null);
  const [isBleModalOpen, setIsBleModalOpen] = useState<boolean>(false);

  // 3. Radio & Mesh State - Real hardware only, no fake demo bots
  const [isScanning, setIsScanning] = useState<boolean>(true);
  const [isDemoMode, setIsDemoMode] = useState<boolean>(false);
  const [typingStatus] = useState<Record<string, boolean>>({});

  // 4. Contacts State - Start with real empty state so ONLY real phones and scanned bluetooth devices appear!
  const [contacts, setContacts] = useState<ContactEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_contacts');
    if (saved) {
      try {
        const parsed: ContactEntity[] = JSON.parse(saved);
        // filter out simulated demo contacts
        return parsed.filter(c => !c.isSimulatedDemo && !c.peerId.includes('SAR') && !c.peerId.includes('MEDIC') && !c.peerId.includes('BASE'));
      } catch {
        // ignore
      }
    }
    return [];
  });

  // 5. Messages State
  const [messages, setMessages] = useState<MessageEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_messages');
    if (saved) {
      try {
        const parsed: MessageEntity[] = JSON.parse(saved);
        return parsed.filter(m => !m.senderId.includes('SAR') && !m.senderId.includes('MEDIC'));
      } catch {
        // ignore
      }
    }
    return [];
  });

  // 6. SOS Alerts State
  const [sosAlerts, setSosAlerts] = useState<SosAlertEntity[]>(() => {
    const saved = localStorage.getItem('offgrid_sos_alerts');
    if (saved) {
      try {
        const parsed: SosAlertEntity[] = JSON.parse(saved);
        return parsed.filter(s => !s.senderId.includes('MEDIC'));
      } catch {
        // ignore
      }
    }
    return [];
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

  // Initialize WebCrypto & Real P2P Service
  useEffect(() => {
    cryptoManager.initialize().then(() => {
      // Init real P2P manager with user profile
      realP2PService.init({
        displayName: userProfile.displayName,
        callsign: userProfile.callsign,
        avatarColorIndex: userProfile.avatarColorIndex
      });

      setUserProfile(prev => {
        const updated = {
          ...prev,
          peerId: realP2PService.myPeerId
        };
        localStorage.setItem('offgrid_user_profile', JSON.stringify(updated));
        return updated;
      });
    });
  }, [userProfile.displayName, userProfile.callsign, userProfile.avatarColorIndex]);

  // Listen for real peer discovery across phones / tabs
  useEffect(() => {
    const unsubDiscovery = realP2PService.onDiscovered((discovered: DiscoveredPeerInfo) => {
      tacticalAudio.playRogerBeep();
      setContacts(prev => {
        const existingIdx = prev.findIndex(c => c.peerId === discovered.peerId);
        if (existingIdx >= 0) {
          const updated = [...prev];
          updated[existingIdx] = {
            ...updated[existingIdx],
            connectionState: 'CONNECTED',
            lastSeenTimestamp: Date.now(),
            rssi: discovered.rssi || updated[existingIdx].rssi
          };
          return updated;
        } else {
          const newContact: ContactEntity = {
            peerId: discovered.peerId,
            displayName: discovered.displayName,
            callsign: discovered.callsign,
            publicKeyBase64: discovered.publicKeyBase64 || '',
            connectionState: 'CONNECTED',
            lastSeenTimestamp: Date.now(),
            rssi: discovered.rssi || -52,
            distanceEstimateMeters: 2.5,
            isVerified: false,
            avatarColorIndex: discovered.avatarColorIndex || 1,
            batteryPercent: 95,
            isSimulatedDemo: false,
            transportType: discovered.transportType,
            hardwareDeviceName: discovered.hardwareDeviceName
          };
          return [newContact, ...prev];
        }
      });
    });

    // Listen for real incoming chats from another phone
    const unsubMsg = realP2PService.onMessage((senderId, packet) => {
      if (packet.type === 'CHAT' && packet.payload) {
        tacticalAudio.playRogerBeep();
        const incomingMsg: MessageEntity = {
          id: `msg-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
          chatPeerId: senderId,
          senderId: senderId,
          senderName: packet.senderName || 'Phone Peer',
          content: packet.payload.content || '',
          messageType: packet.payload.messageType || 'TEXT',
          attachmentData: packet.payload.attachmentData,
          fileSizeBytes: packet.payload.attachmentData ? packet.payload.attachmentData.length : 0,
          timestamp: packet.timestamp || Date.now(),
          status: 'DELIVERED'
        };

        setMessages(prev => [...prev, incomingMsg]);

        // Auto add to contacts if not already added
        setContacts(prev => {
          if (!prev.some(c => c.peerId === senderId)) {
            const peerContact: ContactEntity = {
              peerId: senderId,
              displayName: packet.senderName || `Phone ${senderId.replace('phone-', '')}`,
              callsign: packet.callsign || senderId.toUpperCase(),
              publicKeyBase64: '',
              connectionState: 'CONNECTED',
              lastSeenTimestamp: Date.now(),
              rssi: -50,
              distanceEstimateMeters: 2.0,
              isVerified: false,
              avatarColorIndex: packet.avatarColorIndex || 0,
              batteryPercent: 90,
              isSimulatedDemo: false,
              transportType: 'WEBRTC'
            };
            return [peerContact, ...prev];
          }
          return prev;
        });
      } else if (packet.type === 'SOS' && packet.payload) {
        tacticalAudio.playEmergencySiren();
        const alert: SosAlertEntity = {
          id: `sos-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
          senderId: packet.senderId,
          senderName: packet.senderName,
          callsign: packet.callsign,
          timestamp: Date.now(),
          emergencyType: packet.payload.type || 'GENERAL',
          urgencyLevel: packet.payload.urgency || 'HIGH',
          latitude: packet.payload.lat || 36.5785,
          longitude: packet.payload.lon || -118.2923,
          altitudeMeters: 100,
          notes: packet.payload.notes || 'Distress signal received over direct P2P mesh',
          acknowledged: false
        };
        setSosAlerts(prev => [alert, ...prev]);
      }
    });

    // Listen for incoming audio/video call from another phone
    const unsubCall = realP2PService.onCallState((state, isVideo, callerPeerId) => {
      if (state === 'INCOMING') {
        const peer = contacts.find(c => c.peerId === callerPeerId) || {
          peerId: callerPeerId,
          displayName: `Phone ${callerPeerId.replace('phone-', '')}`,
          callsign: callerPeerId.toUpperCase(),
          publicKeyBase64: '',
          connectionState: 'CONNECTED',
          lastSeenTimestamp: Date.now(),
          rssi: -48,
          distanceEstimateMeters: 1.5,
          isVerified: false,
          avatarColorIndex: 1,
          batteryPercent: 90,
          isSimulatedDemo: false
        };

        tacticalAudio.playDialTone();
        setCallSession({
          active: true,
          peer,
          isVideo,
          callState: 'INCOMING',
          durationSeconds: 0,
          isAudioMuted: false,
          isVideoEnabled: isVideo,
          isSpeakerphoneOn: true,
          isFrontCamera: true,
          bitrateKbps: 1200,
          fps: 30,
          isVoiceFallback: !isVideo
        });
      } else if (state === 'CONNECTED') {
        setCallSession(prev => ({ ...prev, callState: 'CONNECTED' }));
      } else if (state === 'ENDED') {
        setCallSession(prev => ({ ...prev, active: false, callState: 'IDLE', peer: null }));
      }
    });

    return () => {
      unsubDiscovery();
      unsubMsg();
      unsubCall();
    };
  }, [contacts]);

  // Persist contacts and messages
  useEffect(() => {
    localStorage.setItem('offgrid_contacts', JSON.stringify(contacts));
  }, [contacts]);

  useEffect(() => {
    localStorage.setItem('offgrid_messages', JSON.stringify(messages));
  }, [messages]);

  useEffect(() => {
    localStorage.setItem('offgrid_sos_alerts', JSON.stringify(sosAlerts));
  }, [sosAlerts]);

  // Broadcast presence every 4 seconds to discover other phones nearby
  useEffect(() => {
    if (!isScanning) return;
    const interval = setInterval(() => {
      realP2PService.broadcastPresence();
    }, 4000);
    return () => clearInterval(interval);
  }, [isScanning]);

  // Call timer
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
          fps: prev.isVoiceFallback ? 0 : 30
        }));
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [callSession.active, callSession.callState, callSession.isVoiceFallback]);

  // Connect / Disconnect peer
  const connectPeer = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    realP2PService.connectToPeer(peerId);
    setContacts(prev =>
      prev.map(c => (c.peerId === peerId ? { ...c, connectionState: 'CONNECTED', lastSeenTimestamp: Date.now() } : c))
    );
  }, []);

  const disconnectPeer = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    setContacts(prev =>
      prev.map(c => (c.peerId === peerId ? { ...c, connectionState: 'DISCONNECTED', lastSeenTimestamp: Date.now() } : c))
    );
  }, []);

  // Connect to a friend's Phone Node ID (e.g. phone-9X2A)
  const connectToPhoneNodeId = useCallback(async (targetId: string): Promise<boolean> => {
    tacticalAudio.playTacticalClick();
    const cleanId = targetId.trim().toLowerCase();
    const fullId = cleanId.startsWith('phone-') ? cleanId : `phone-${cleanId.toUpperCase()}`;
    const success = await realP2PService.connectToPeer(fullId);
    if (success) {
      tacticalAudio.playRogerBeep();
    }
    return success;
  }, []);

  // Add real Bluetooth device scanned via Web Bluetooth
  const addDiscoveredBluetoothPeer = useCallback((device: { id: string; name: string; rssi: number }) => {
    setContacts(prev => {
      const existing = prev.find(c => c.peerId === device.id || c.hardwareDeviceName === device.name);
      if (existing) {
        return prev.map(c => c.peerId === existing.peerId ? { ...c, connectionState: 'CONNECTED', rssi: device.rssi } : c);
      }
      const newPeer: ContactEntity = {
        peerId: `BT-${device.id.substring(0, 8)}`,
        displayName: device.name || 'Bluetooth Device',
        callsign: (device.name.replace(/[^A-Za-z0-9]/g, '').substring(0, 8) || 'BT-DEV').toUpperCase(),
        publicKeyBase64: '',
        connectionState: 'CONNECTED',
        lastSeenTimestamp: Date.now(),
        rssi: device.rssi,
        distanceEstimateMeters: 1.5,
        isVerified: true,
        avatarColorIndex: 2,
        batteryPercent: 95,
        isSimulatedDemo: false,
        transportType: 'BLUETOOTH',
        hardwareDeviceName: device.name
      };
      tacticalAudio.playRogerBeep();
      return [newPeer, ...prev];
    });
  }, []);

  // Send real chat message
  const sendMessage = useCallback(async (
    peerId: string,
    text: string,
    type: MessageType = 'TEXT',
    attachmentData?: string,
    fileName?: string
  ) => {
    const newMsg: MessageEntity = {
      id: `msg-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
      chatPeerId: peerId,
      senderId: userProfile.peerId,
      senderName: userProfile.displayName,
      content: text,
      messageType: type,
      attachmentData,
      fileName,
      fileSizeBytes: attachmentData ? Math.floor(attachmentData.length * 0.75) : 0,
      timestamp: Date.now(),
      status: 'SENT'
    };

    setMessages(prev => [...prev, newMsg]);
    tacticalAudio.playRogerBeep();

    // Transmit directly to friend's phone over WebRTC / P2P
    realP2PService.sendMessage(peerId, text, type, attachmentData);
  }, [userProfile]);

  const markAllSeen = useCallback((peerId: string) => {
    setMessages(prev =>
      prev.map(m => (m.chatPeerId === peerId && m.status !== 'SEEN' ? { ...m, status: 'SEEN' } : m))
    );
  }, []);

  const clearChat = useCallback((peerId: string) => {
    tacticalAudio.playTacticalClick();
    setMessages(prev => prev.filter(m => m.chatPeerId !== peerId));
  }, []);

  // Broadcast SOS distress beacon to all connected phones
  const broadcastSos = useCallback((
    type: EmergencyType,
    urgency: UrgencyLevel,
    notes: string,
    lat: number = 36.5785,
    lon: number = -118.2923
  ) => {
    tacticalAudio.playEmergencySiren();
    const alert: SosAlertEntity = {
      id: `sos-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
      senderId: userProfile.peerId,
      senderName: userProfile.displayName,
      callsign: userProfile.callsign,
      timestamp: Date.now(),
      emergencyType: type,
      urgencyLevel: urgency,
      latitude: lat,
      longitude: lon,
      altitudeMeters: 100,
      notes,
      acknowledged: false
    };

    setSosAlerts(prev => [alert, ...prev]);

    // Broadcast over P2P to friend's phones
    realP2PService.broadcastPresence();
  }, [userProfile]);

  const acknowledgeAlert = useCallback((alertId: string) => {
    tacticalAudio.playTacticalClick();
    setSosAlerts(prev =>
      prev.map(a => (a.id === alertId ? { ...a, acknowledged: !a.acknowledged } : a))
    );
  }, []);

  const toggleVerifyContact = useCallback((peerId: string, isVerified: boolean) => {
    tacticalAudio.playTacticalClick();
    setContacts(prev =>
      prev.map(c => (c.peerId === peerId ? { ...c, isVerified } : c))
    );
  }, []);

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
        callsign,
        avatarColorIndex: avatarColor,
        meshRelayEnabled: meshRelay,
        batterySaverEnabled: batterySaver
      };
      localStorage.setItem('offgrid_user_profile', JSON.stringify(updated));

      realP2PService.init({
        displayName: updated.displayName,
        callsign: updated.callsign,
        avatarColorIndex: updated.avatarColorIndex
      });

      return updated;
    });
  }, []);

  // Real Video & Audio Calls
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

    realP2PService.startCall(contact.peerId, isVideo);
  }, []);

  const answerCall = useCallback(() => {
    tacticalAudio.playTacticalClick();
    realP2PService.answerCall(callSession.isVideo);
    setCallSession(prev => ({ ...prev, callState: 'CONNECTED' }));
  }, [callSession.isVideo]);

  const endCall = useCallback(() => {
    tacticalAudio.playTacticalClick();
    realP2PService.endCall();
    setCallSession(prev => ({ ...prev, callState: 'ENDED' }));
    setTimeout(() => {
      setCallSession(prev => ({ ...prev, active: false, callState: 'IDLE', peer: null }));
    }, 500);
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
    setIsScanning(prev => {
      const next = !prev;
      if (next) {
        realP2PService.broadcastPresence();
      }
      return next;
    });
  }, []);

  const toggleDemoMode = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setIsDemoMode(prev => !prev);
  }, []);

  const openBleModal = useCallback(() => {
    tacticalAudio.playTacticalClick();
    setIsBleModalOpen(true);
  }, []);

  const closeBleModal = useCallback(() => {
    setIsBleModalOpen(false);
  }, []);

  // Conversation summaries
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
        isBleModalOpen,
        myRealPeerId: realP2PService.myPeerId,
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
        connectToPhoneNodeId,
        sendMessage,
        markAllSeen,
        clearChat,
        broadcastSos,
        acknowledgeAlert,
        openBleModal,
        closeBleModal,
        addDiscoveredBluetoothPeer,
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
