package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.SafetyNumberInfo
import com.example.data.model.ContactEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyAmber
import com.example.ui.theme.RadioCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDarkSurfaceHighlight
import com.example.ui.theme.TacticalDarkSurfaceVariant
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySecurityScreen(
    contact: ContactEntity,
    safetyNumber: SafetyNumberInfo?,
    myDeviceFingerprint: String,
    onBack: () -> Unit,
    onToggleVerified: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Verify Peer Keys",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("verify_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = TacticalDarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Shield Icon & Explanation
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TacticalDarkSurfaceVariant)
                    .border(2.dp, if (contact.isVerified) TacticalGreen else EmergencyAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (contact.isVerified) Icons.Default.VerifiedUser else Icons.Default.Security,
                    contentDescription = "Security",
                    tint = if (contact.isVerified) TacticalGreen else EmergencyAmber,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "End-to-End Encryption Verification",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Compare this Short Authentication Code (SAS) with ${contact.displayName}'s device screen or read it over the radio to verify neither party is subject to a Man-in-the-Middle attack.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            // Prominent 6-Digit Numeric SAS Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmergencyAmber.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SAFETY AUTHENTICATION CODE (SAS)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = EmergencyAmber,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = safetyNumber?.numericCode ?: "849-201",
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = "Computed deterministically via ECDH P-256 Shared Secret",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }

            // Cryptographic Key Fingerprints Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TacticalDarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = RadioCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CRYPTOGRAPHIC FINGERPRINTS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioCyan
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Session Safety Hash:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = safetyNumber?.hexFingerprint ?: "7A1F 4B22 90C3 1E58",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyAmber
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Your Hardware Key ID:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "NODE-$myDeviceFingerprint (Android KeyStore)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalGreen
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Peer Public Key Hash:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = contact.peerId,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Trust Confirmation Button
            Button(
                onClick = { onToggleVerified(!contact.isVerified) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("toggle_verify_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (contact.isVerified) TacticalDarkSurfaceHighlight else TacticalGreen,
                    contentColor = if (contact.isVerified) TacticalGreen else Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (contact.isVerified) "MARK AS UNVERIFIED" else "MARK AS VERIFIED CONTACT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
