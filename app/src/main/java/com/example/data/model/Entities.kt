package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ConnectionState {
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED,
    OUT_OF_RANGE
}

enum class MessageStatus {
    QUEUED,       // In store-and-forward queue awaiting peer reconnect
    SENT,         // Transmitted to nearby peer
    DELIVERED,    // Confirmed received by peer device
    SEEN,         // Read by peer
    FAILED
}

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    SOS,
    SYSTEM
}

enum class CallState {
    IDLE,
    OUTGOING,
    INCOMING,
    CONNECTED,
    ENDED
}

enum class EmergencyType {
    MEDICAL,
    RESCUE,
    HAZARD,
    SUPPLY
}

enum class UrgencyLevel {
    CRITICAL,
    HIGH,
    MEDIUM
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatPeerId: String,          // The recipient or chat partner ID
    val senderId: String,            // Who authored the message
    val senderName: String,          // Display name of sender
    val content: String,             // Plaintext (decrypted locally)
    val messageType: MessageType = MessageType.TEXT,
    val attachmentData: String? = null, // Base64 encoded image or file data
    val fileName: String? = null,
    val fileSizeBytes: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.QUEUED,
    val isRelayed: Boolean = false,
    val relayHops: Int = 0
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val peerId: String,       // Unique device public key thumbprint or ID
    val displayName: String,
    val callsign: String,
    val publicKeyBase64: String = "",
    val endpointId: String? = null,       // Current active Nearby Connection endpoint
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val rssi: Int = -70,                  // Signal strength in dBm (-100 to -30)
    val distanceEstimateMeters: Float = 5.0f,
    val isVerified: Boolean = false,      // Safety number confirmed by user
    val avatarColorIndex: Int = 0,
    val batteryPercent: Int = 85,
    val isSimulatedDemo: Boolean = false
)

@Entity(tableName = "sos_alerts")
data class SosAlertEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val callsign: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emergencyType: EmergencyType = EmergencyType.MEDICAL,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.CRITICAL,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val notes: String = "",
    val acknowledged: Boolean = false
)

data class UserProfile(
    val peerId: String,
    val displayName: String,
    val callsign: String,
    val avatarColorIndex: Int = 1,
    val meshRelayEnabled: Boolean = true,
    val batterySaverEnabled: Boolean = false
)

data class ConversationSummary(
    val contact: ContactEntity,
    val lastMessage: MessageEntity?,
    val unreadCount: Int = 0,
    val queuedCount: Int = 0
)

/**
 * High-level packet transmitted over Google Nearby Connections.
 * Serialized to JSON string / UTF-8 bytes for P2P delivery.
 */
data class NetworkPacket(
    val type: String,                   // HANDSHAKE, HANDSHAKE_ACK, CHAT, ACK, TYPING, SOS, WEBRTC, PING
    val sourceId: String,
    val sourceName: String,
    val targetId: String,               // Destination peer ID ("BROADCAST" for SOS/Discovery)
    val payload: String = "",           // Encrypted ciphertext or signaling JSON
    val iv: String = "",                // IV for AES-GCM
    val messageId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = 3                    // Multi-hop mesh relay Time-To-Live
)
