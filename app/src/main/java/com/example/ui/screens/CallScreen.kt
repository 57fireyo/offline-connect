package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallState
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DistressRed
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkBackground
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CallScreen(
    peerName: String,
    callState: CallState,
    isVideo: Boolean,
    isMuted: Boolean,
    isSpeakerphoneOn: Boolean,
    isFrontCamera: Boolean,
    durationSeconds: Int,
    bitrateKbps: Int,
    fps: Int,
    isVoiceFallback: Boolean,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val durationText = "%02d:%02d".format(minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "call_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalDarkBackground)
            .testTag("call_screen")
    ) {
        // Main Video Stream / Tactical Acoustic Waveform Background
        if (isVideo && !isVoiceFallback && callState == CallState.CONNECTED) {
            // Simulated remote video feed with tactical grid overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
            ) {
                // Tactical target reticle & HUD lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid crosshair
                    drawLine(
                        RadioCyan.copy(alpha = 0.2f),
                        Offset(w / 2 - 40, h / 2),
                        Offset(w / 2 + 40, h / 2),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        RadioCyan.copy(alpha = 0.2f),
                        Offset(w / 2, h / 2 - 40),
                        Offset(w / 2, h / 2 + 40),
                        strokeWidth = 1.5f
                    )

                    // Corner brackets
                    val bracketLen = 30f
                    val pad = 40f
                    // Top-Left
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(pad, pad), Offset(pad + bracketLen, pad), 2f)
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(pad, pad), Offset(pad, pad + bracketLen), 2f)
                    // Top-Right
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), 2f)
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), 2f)
                    // Bottom-Left
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(pad, h - pad), Offset(pad + bracketLen, h - pad), 2f)
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(pad, h - pad), Offset(pad, h - pad - bracketLen), 2f)
                    // Bottom-Right
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(w - pad, h - pad), Offset(w - pad - bracketLen, h - pad), 2f)
                    drawLine(RadioCyan.copy(alpha = 0.4f), Offset(w - pad, h - pad), Offset(w - pad, h - pad - bracketLen), 2f)
                }

                // Remote Video tactical badge
                Text(
                    text = "REMOTE WEBRTC FEED • SRTP ENCRYPTED",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = RadioCyan.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Picture-in-Picture Local Camera Preview (PIP)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 20.dp)
                        .size(100.dp, 140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, EmergencyAmber, RoundedCornerShape(12.dp)),
                    color = TacticalDarkSurfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Local Camera",
                                tint = EmergencyAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFrontCamera) "FRONT CAM" else "BACK CAM",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Text(
                                text = "LOCAL PIP",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        } else {
            // Voice-Only / Fallback Acoustic Pulse Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Acoustic Rings
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val maxRadius = size.minDimension / 2
                        drawCircle(
                            color = TacticalGreen.copy(alpha = 0.15f),
                            radius = maxRadius * pulseScale,
                            center = center,
                            style = Stroke(2f)
                        )
                        drawCircle(
                            color = RadioCyan.copy(alpha = 0.25f),
                            radius = maxRadius * 0.75f,
                            center = center,
                            style = Stroke(2f)
                        )
                    }

                    // Avatar in center
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(TacticalDarkSurfaceVariant)
                            .border(2.dp, TacticalGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Call",
                            tint = TacticalGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = peerName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (callState == CallState.CONNECTED) durationText else callState.name,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = EmergencyAmber,
                    fontWeight = FontWeight.SemiBold
                )

                if (isVoiceFallback) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmergencyAmber.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyAmber)
                    ) {
                        Text(
                            text = "⚠ ADAPTIVE QUALITY: Fallback to Voice-Only (Bandwidth Saver)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EmergencyAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Top HUD Header (Encryption & Telemetry Metrics)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 28.dp, start = 16.dp, end = 16.dp),
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SRTP",
                        tint = TacticalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = peerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "WebRTC SRTP • Wi-Fi Direct P2P",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalGreen
                        )
                    }
                }

                // Bitrate & FPS Telemetry
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (callState == CallState.CONNECTED) durationText else callState.name,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyAmber
                    )
                    Text(
                        text = "$bitrateKbps kbps • $fps fps",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
        }

        // Bottom Controls Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (callState == CallState.INCOMING) {
                    // Answer Button
                    FloatingActionButton(
                        onClick = onAnswerCall,
                        containerColor = TacticalGreen,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.testTag("answer_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Answer")
                    }

                    // Decline Button
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = DistressRed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("decline_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Decline")
                    }
                } else {
                    // Mute Mic
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isMuted) DistressRed.copy(alpha = 0.2f) else TacticalDarkSurfaceVariant,
                                CircleShape
                            )
                            .testTag("call_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) DistressRed else TextPrimary
                        )
                    }

                    // Video On/Off Toggle
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (!isVideo || isVoiceFallback) EmergencyAmber.copy(alpha = 0.2f) else TacticalDarkSurfaceVariant,
                                CircleShape
                            )
                            .testTag("call_video_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isVideo && !isVoiceFallback) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle Video",
                            tint = if (isVideo && !isVoiceFallback) RadioCyan else EmergencyAmber
                        )
                    }

                    // Switch Camera (Front/Back)
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(48.dp)
                            .background(TacticalDarkSurfaceVariant, CircleShape)
                            .testTag("call_switch_camera_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = TextPrimary
                        )
                    }

                    // Speakerphone Toggle
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isSpeakerphoneOn) TacticalGreen.copy(alpha = 0.2f) else TacticalDarkSurfaceVariant,
                                CircleShape
                            )
                            .testTag("call_speaker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (isSpeakerphoneOn) TacticalGreen else TextSecondary
                        )
                    }

                    // End Call
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = DistressRed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("end_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
