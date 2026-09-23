package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SosAlertEntity
import com.example.data.model.UrgencyLevel

class Converters {
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = runCatching { MessageType.valueOf(value) }.getOrDefault(MessageType.TEXT)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = runCatching { MessageStatus.valueOf(value) }.getOrDefault(MessageStatus.QUEUED)

    @TypeConverter
    fun fromConnectionState(value: ConnectionState): String = value.name

    @TypeConverter
    fun toConnectionState(value: String): ConnectionState = runCatching { ConnectionState.valueOf(value) }.getOrDefault(ConnectionState.DISCONNECTED)

    @TypeConverter
    fun fromEmergencyType(value: EmergencyType): String = value.name

    @TypeConverter
    fun toEmergencyType(value: String): EmergencyType = runCatching { EmergencyType.valueOf(value) }.getOrDefault(EmergencyType.MEDICAL)

    @TypeConverter
    fun fromUrgencyLevel(value: UrgencyLevel): String = value.name

    @TypeConverter
    fun toUrgencyLevel(value: String): UrgencyLevel = runCatching { UrgencyLevel.valueOf(value) }.getOrDefault(UrgencyLevel.CRITICAL)
}

@Database(
    entities = [MessageEntity::class, ContactEntity::class, SosAlertEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun sosDao(): SosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offgrid_connect.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
