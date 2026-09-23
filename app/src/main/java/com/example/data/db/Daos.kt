package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.SosAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatPeerId = :peerId ORDER BY timestamp ASC")
    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE status = 'QUEUED' AND chatPeerId = :peerId ORDER BY timestamp ASC")
    suspend fun getQueuedMessagesForPeer(peerId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE status = 'QUEUED' ORDER BY timestamp ASC")
    suspend fun getAllQueuedMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: MessageStatus)

    @Query("UPDATE messages SET status = 'SEEN' WHERE chatPeerId = :peerId AND senderId != :myId AND status != 'SEEN'")
    suspend fun markAllSeenForPeer(peerId: String, myId: String)

    @Query("DELETE FROM messages WHERE chatPeerId = :peerId")
    suspend fun clearChatHistory(peerId: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY lastSeenTimestamp DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE peerId = :peerId")
    suspend fun getContact(peerId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE endpointId = :endpointId")
    suspend fun getContactByEndpoint(endpointId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact: ContactEntity)

    @Query("UPDATE contacts SET connectionState = :state, endpointId = :endpointId WHERE peerId = :peerId")
    suspend fun updateConnectionState(peerId: String, state: ConnectionState, endpointId: String?)

    @Query("UPDATE contacts SET connectionState = :state")
    suspend fun updateAllConnectionStates(state: ConnectionState)

    @Query("UPDATE contacts SET rssi = :rssi, distanceEstimateMeters = :distance WHERE peerId = :peerId")
    suspend fun updateSignalMetrics(peerId: String, rssi: Int, distance: Float)

    @Query("UPDATE contacts SET isVerified = :isVerified WHERE peerId = :peerId")
    suspend fun updateVerification(peerId: String, isVerified: Boolean)

    @Query("DELETE FROM contacts WHERE peerId = :peerId")
    suspend fun deleteContact(peerId: String)
}

@Dao
interface SosDao {
    @Query("SELECT * FROM sos_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<SosAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SosAlertEntity)

    @Query("UPDATE sos_alerts SET acknowledged = 1 WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: String)

    @Query("DELETE FROM sos_alerts WHERE id = :alertId")
    suspend fun deleteAlert(alertId: String)
}
