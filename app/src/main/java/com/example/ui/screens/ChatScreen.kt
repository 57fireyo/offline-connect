package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.SafetyNumberInfo
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.components.MessageStatusIcon
import com.example.ui.components.TacticalAvatar
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: ContactEntity,
    messages: List<MessageEntity>,
    myPeerId: String,
    safetyNumber: SafetyNumberInfo?,
    isTyping: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String, String?, String?, Long) -> Unit,
    onTypingChanged: (Boolean) -> Unit,
    onStartCall: (Boolean) -> Unit,
    onOpenSecurityVerification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val tacticalShortcuts = listOf(
        "Status OK • Perimeter Secure",
        "Copy that • Coordinates locked",
        "Moving to rendezvous point",
        "Low bandwidth • Voice fallback ready"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Top Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TacticalAvatar(
                        name = contact.displayName,
                        callsign = contact.callsign,
                        size = 38.dp,
                        avatarColorIndex = contact.avatarColorIndex,
                        isOnline = contact.connectionState == ConnectionState.CONNECTED
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = contact.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "[${contact.callsign}]",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = EmergencyAmber
                            )
                        }
                        Text(
                            text = if (isTyping) "typing..." else "${contact.rssi} dBm • ${contact.connectionState.name}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isTyping) EmergencyAmber else if (contact.connectionState == ConnectionState.CONNECTED) TacticalGreen else TextSecondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { onStartCall(false) }, modifier = Modifier.testTag("chat_voice_call_btn")) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "P2P Voice Call",
                        tint = TacticalGreen
                    )
                }
                IconButton(onClick = { onStartCall(true) }, modifier = Modifier.testTag("chat_video_call_btn")) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "P2P Video Call",
                        tint = RadioCyan
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalDarkSurface)
        )

        // E2EE Security Verification Strip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSecurityVerification() }
                .testTag("chat_security_strip"),
            color = TacticalDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "E2EE",
                        tint = TacticalGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AES-256-GCM / ECDH • Safety SAS: ${safetyNumber?.numericCode ?: "---"}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Text(
                    text = if (contact.isVerified) "VERIFIED ✓" else "VERIFY >",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (contact.isVerified) TacticalGreen else EmergencyAmber
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                val isMe = message.senderId == myPeerId
                MessageBubble(message = message, isMe = isMe)
            }

            if (isTyping) {
                item {
                    TypingIndicatorBubble(senderName = contact.displayName)
                }
            }
        }

        // Tactical Shortcut Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(TacticalDarkSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tacticalShortcuts) { shortcut ->
                Surface(
                    onClick = {
                        onSendMessage(shortcut, null, null, 0)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = TacticalDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = shortcut,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TacticalDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tactical Image / File Attachment button
                IconButton(
                    onClick = {
                        // Send sample tactical map overlay attachment
                        onSendMessage(
                            "Tactical Grid Overlay - Sector 4",
                            "SAMPLE_GRID_IMAGE_BASE64",
                            "sector4_grid.jpg",
                            24576
                        )
                    },
                    modifier = Modifier.testTag("attach_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attach Map/Image",
                        tint = EmergencyAmber
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        onTypingChanged(it.isNotBlank())
                    },
                    placeholder = {
                        Text(
                            text = "P2P Offline message...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmergencyAmber,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = TacticalDarkSurfaceVariant,
                        unfocusedContainerColor = TacticalDarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput, null, null, 0)
                                textInput = ""
                                onTypingChanged(false)
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput, null, null, 0)
                            textInput = ""
                            onTypingChanged(false)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmergencyAmber)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val bubbleBg = if (isMe) TacticalDarkSurfaceHighlight else TacticalDarkSurfaceVariant
    val borderColor = if (isMe) EmergencyAmber.copy(alpha = 0.4f) else BorderSubtle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("message_${message.id}"),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
            ),
            color = bubbleBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // If message has attachment (Image/Map)
                if (message.attachmentData != null) {
                    TacticalAttachmentPreview(
                        fileName = message.fileName ?: "attachment.dat",
                        fileSize = message.fileSizeBytes
                    )
                }

                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )

                    if (isMe) {
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalAttachmentPreview(fileName: String, fileSize: Long) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0C141D),
        border = androidx.compose.foundation.BorderStroke(1.dp, RadioCyan.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Attachment",
                tint = RadioCyan,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = fileName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Encrypted Local Asset • ${fileSize / 1024} KB",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(senderName: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = TacticalDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(EmergencyAmber)
            )
            Text(
                text = "$senderName is typing...",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = EmergencyAmber
            )
        }
    }
}
