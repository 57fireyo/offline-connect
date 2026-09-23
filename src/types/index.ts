export type ConnectionState =
  | 'DISCONNECTED'
  | 'SEARCHING'
  | 'CONNECTING'
  | 'CONNECTED'
  | 'OUT_OF_RANGE';

export type MessageStatus =
  | 'QUEUED'
  | 'SENT'
  | 'DELIVERED'
  | 'SEEN'
  | 'FAILED';

export type MessageType =
  | 'TEXT'
  | 'IMAGE'
  | 'LOCATION'
  | 'SYSTEM';

export type EmergencyType =
  | 'MEDICAL'
  | 'RESCUE'
  | 'HAZARD'
  | 'SUPPLY';

export type UrgencyLevel =
  | 'CRITICAL'
  | 'HIGH'
  | 'MEDIUM';

export type CallState =
  | 'IDLE'
  | 'OUTGOING'
  | 'INCOMING'
  | 'CONNECTED'
  | 'ENDED';

export interface ContactEntity {
  peerId: string;
  displayName: string;
  callsign: string;
  publicKeyBase64: string;
  endpointId?: string;
  connectionState: ConnectionState;
  lastSeenTimestamp: number;
  rssi: number;
  distanceEstimateMeters: number;
  isVerified: boolean;
  avatarColorIndex: number;
  batteryPercent: number;
  isSimulatedDemo: boolean;
}

export interface MessageEntity {
  id: string;
  chatPeerId: string;
  senderId: string;
  senderName: string;
  content: string;
  messageType: MessageType;
  attachmentData?: string;
  fileName?: string;
  fileSizeBytes: number;
  timestamp: number;
  status: MessageStatus;
}

export interface SosAlertEntity {
  id: string;
  senderId: string;
  senderName: string;
  callsign: string;
  timestamp: number;
  emergencyType: EmergencyType;
  urgencyLevel: UrgencyLevel;
  latitude?: number;
  longitude?: number;
  altitudeMeters?: number;
  notes: string;
  acknowledged: boolean;
}

export interface UserProfile {
  peerId: string;
  displayName: string;
  callsign: string;
  avatarColorIndex: number;
  meshRelayEnabled: boolean;
  batterySaverEnabled: boolean;
}

export interface SafetyNumberInfo {
  numericCode: string; // e.g. "849-201"
  hexFingerprint: string; // e.g. "7A1F 4B22 90C3 1E58"
  sharedKeyHex: string;
}

export interface ConversationSummary {
  contact: ContactEntity;
  lastMessage?: MessageEntity;
  unreadCount: number;
  queuedCount: number;
}
