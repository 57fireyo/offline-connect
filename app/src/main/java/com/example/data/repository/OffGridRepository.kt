package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.crypto.CryptoManager
import com.example.data.db.AppDatabase
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SosAlertEntity
import com.example.data.model.UrgencyLevel
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class OffGridRepository(
    private val context: Context,
    val cryptoManager: CryptoManager = CryptoManager(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val database = AppDatabase.getDatabase(context)
    private val messageDao = database.messageDao()
    private val contactDao = database.contactDao()
    private val sosDao = database.sosDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("offgrid_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadOrCreateUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val allSosAlerts: Flow<List<SosAlertEntity>> = sosDao.getAllAlerts()
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private fun loadOrCreateUserProfile(): UserProfile {
        var peerId = prefs.getString("peer_id", null)
        if (peerId == null) {
            peerId = "NODE-" + cryptoManager.getDeviceFingerprint()
            prefs.edit().putString("peer_id", peerId).apply()
        }
        val name = prefs.getString("display_name", "Operator Alpha") ?: "Operator Alpha"
        val callsign = prefs.getString("callsign", "ECHO-7") ?: "ECHO-7"
        val avatar = prefs.getInt("avatar_color", 1)
        val relay = prefs.getBoolean("mesh_relay", true)
        val batterySaver = prefs.getBoolean("battery_saver", false)

        return UserProfile(
            peerId = peerId,
            displayName = name,
            callsign = callsign,
            avatarColorIndex = avatar,
            meshRelayEnabled = relay,
            batterySaverEnabled = batterySaver
        )
    }

    fun updateProfile(
        displayName: String,
        callsign: String,
        avatarIndex: Int,
        meshRelay: Boolean,
        batterySaver: Boolean
    ) {
        prefs.edit()
            .putString("display_name", displayName)
            .putString("callsign", callsign)
            .putInt("avatar_color", avatarIndex)
            .putBoolean("mesh_relay", meshRelay)
            .putBoolean("battery_saver", batterySaver)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            displayName = displayName,
            callsign = callsign,
            avatarColorIndex = avatarIndex,
            meshRelayEnabled = meshRelay,
            batterySaverEnabled = batterySaver
        )
    }

    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForPeer(peerId)
    }

    suspend fun getQueuedMessagesForPeer(peerId: String): List<MessageEntity> {
        return messageDao.getQueuedMessagesForPeer(peerId)
    }

    suspend fun createOutgoingMessage(
        peerId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        attachmentData: String? = null,
        fileName: String? = null,
        fileSizeBytes: Long = 0
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatPeerId = peerId,
            senderId = userProfile.value.peerId,
            senderName = userProfile.value.displayName,
            content = content,
            messageType = type,
            attachmentData = attachmentData,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.QUEUED
        )
        messageDao.insertMessage(message)
        return message
    }

    suspend fun saveReceivedMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status)
    }

    suspend fun markAllSeen(peerId: String) {
        messageDao.markAllSeenForPeer(peerId, userProfile.value.peerId)
    }

    suspend fun clearChat(peerId: String) {
        messageDao.clearChatHistory(peerId)
    }

    suspend fun getContact(peerId: String): ContactEntity? {
        return contactDao.getContact(peerId)
    }

    suspend fun saveOrUpdateContact(contact: ContactEntity) {
        contactDao.insertOrUpdate(contact)
    }

    suspend fun updatePeerConnectionState(peerId: String, state: ConnectionState, endpointId: String? = null) {
        contactDao.updateConnectionState(peerId, state, endpointId)
    }

    suspend fun updateSignalMetrics(peerId: String, rssi: Int, distance: Float) {
        contactDao.updateSignalMetrics(peerId, rssi, distance)
    }

    suspend fun verifyContact(peerId: String, isVerified: Boolean) {
        contactDao.updateVerification(peerId, isVerified)
    }

    suspend fun createAndBroadcastSos(
        type: EmergencyType,
        urgency: UrgencyLevel,
        lat: Double?,
        lon: Double?,
        notes: String
    ): SosAlertEntity {
        val alert = SosAlertEntity(
            id = UUID.randomUUID().toString(),
            senderId = userProfile.value.peerId,
            senderName = userProfile.value.displayName,
            callsign = userProfile.value.callsign,
            timestamp = System.currentTimeMillis(),
            emergencyType = type,
            urgencyLevel = urgency,
            latitude = lat,
            longitude = lon,
            notes = notes,
            acknowledged = true
        )
        sosDao.insertAlert(alert)
        return alert
    }

    suspend fun saveIncomingSosAlert(alert: SosAlertEntity) {
        sosDao.insertAlert(alert)
    }

    suspend fun acknowledgeAlert(alertId: String) {
        sosDao.acknowledgeAlert(alertId)
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existing = contactDao.getContact("NODE-SAR-01")
        if (existing == null) {
            // Seed realistic field contacts for immediate demo and testing
            val sarContact = ContactEntity(
                peerId = "NODE-SAR-01",
                displayName = "Ranger Sarah",
                callsign = "VALKYRIE-1",
                publicKeyBase64 = cryptoManager.exportPublicKey(), // uses valid key format
                connectionState = ConnectionState.CONNECTED,
                lastSeenTimestamp = System.currentTimeMillis(),
                rssi = -54,
                distanceEstimateMeters = 8.5f,
                isVerified = true,
                avatarColorIndex = 2,
                batteryPercent = 92,
                isSimulatedDemo = true
            )
            contactDao.insertOrUpdate(sarContact)

            val medicContact = ContactEntity(
                peerId = "NODE-MEDIC-03",
                displayName = "Medic Dave",
                callsign = "CADUCEUS-3",
                publicKeyBase64 = cryptoManager.exportPublicKey(),
                connectionState = ConnectionState.CONNECTED,
                lastSeenTimestamp = System.currentTimeMillis() - 45000,
                rssi = -68,
                distanceEstimateMeters = 18.0f,
                isVerified = false,
                avatarColorIndex = 0,
                batteryPercent = 64,
                isSimulatedDemo = true
            )
            contactDao.insertOrUpdate(medicContact)

            val outpostContact = ContactEntity(
                peerId = "NODE-BASE-09",
                displayName = "Base Camp Echo",
                callsign = "FORTRESS-MAIN",
                publicKeyBase64 = cryptoManager.exportPublicKey(),
                connectionState = ConnectionState.DISCONNECTED,
                lastSeenTimestamp = System.currentTimeMillis() - 360000,
                rssi = -86,
                distanceEstimateMeters = 72.0f,
                isVerified = true,
                avatarColorIndex = 3,
                batteryPercent = 100,
                isSimulatedDemo = true
            )
            contactDao.insertOrUpdate(outpostContact)

            // Seed initial welcome messages
            messageDao.insertMessage(
                MessageEntity(
                    id = "msg-init-1",
                    chatPeerId = "NODE-SAR-01",
                    senderId = "NODE-SAR-01",
                    senderName = "Ranger Sarah",
                    content = "OffGrid link active on channel 4. Signal strength high. We are establishing the valley search perimeter.",
                    timestamp = System.currentTimeMillis() - 120000,
                    status = MessageStatus.SEEN
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    id = "msg-init-2",
                    chatPeerId = "NODE-SAR-01",
                    senderId = userProfile.value.peerId,
                    senderName = userProfile.value.displayName,
                    content = "Copy that Valkyrie-1. End-to-end encryption keys verified. All peer packets routing offline.",
                    timestamp = System.currentTimeMillis() - 60000,
                    status = MessageStatus.SEEN
                )
            )

            // Seed sample SOS alert
            sosDao.insertAlert(
                SosAlertEntity(
                    id = "sos-init-1",
                    senderId = "NODE-MEDIC-03",
                    senderName = "Medic Dave",
                    callsign = "CADUCEUS-3",
                    timestamp = System.currentTimeMillis() - 300000,
                    emergencyType = EmergencyType.MEDICAL,
                    urgencyLevel = UrgencyLevel.HIGH,
                    latitude = 36.6002,
                    longitude = -118.0583,
                    altitudeMeters = 2450.0,
                    notes = "Injured hiker with ankle fracture at Ridge Trail mark 4. First aid administered. Need stretcher team.",
                    acknowledged = false
                )
            )
        }
    }
}
