package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CallState
import com.example.data.model.ConnectionState
import com.example.data.model.ContactEntity
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChatsListScreen
import com.example.ui.screens.NearbyUsersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SosScreen
import com.example.ui.screens.VerifySecurityScreen
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
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffGridApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isVerifyScreenOpen by remember { mutableStateOf(false) }
    var showPermissionBanner by remember { mutableStateOf(true) }

    val contacts by viewModel.nearbyContacts.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val totalUnreadCount by viewModel.totalUnreadCount.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeContact.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val sosAlerts by viewModel.sosAlerts.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val globalConnState by viewModel.globalConnectionState.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val safetyNumber by viewModel.safetyNumber.collectAsStateWithLifecycle()
    val typingMap by viewModel.typingStatus.collectAsStateWithLifecycle()

    // Call state
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val activeCallPeerName by viewModel.activeCallPeerName.collectAsStateWithLifecycle()
    val isVideoCall by viewModel.isVideoCall.collectAsStateWithLifecycle()
    val isMuted by viewModel.isAudioMuted.collectAsStateWithLifecycle()
    val isSpeakerphoneOn by viewModel.isSpeakerphoneOn.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val callDuration by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val currentBitrate by viewModel.currentBitrateKbps.collectAsStateWithLifecycle()
    val currentFps by viewModel.currentFps.collectAsStateWithLifecycle()
    val isVoiceFallback by viewModel.isVoiceFallbackActive.collectAsStateWithLifecycle()

    // Android runtime permissions request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        showPermissionBanner = !allGranted
        if (allGranted) {
            viewModel.startNearbyMesh()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Fullscreen WebRTC Call Overlay
    if (callState != CallState.IDLE) {
        CallScreen(
            peerName = activeCallPeerName,
            callState = callState,
            isVideo = isVideoCall,
            isMuted = isMuted,
            isSpeakerphoneOn = isSpeakerphoneOn,
            isFrontCamera = isFrontCamera,
            durationSeconds = callDuration,
            bitrateKbps = currentBitrate,
            fps = currentFps,
            isVoiceFallback = isVoiceFallback,
            onToggleMute = { viewModel.toggleMute() },
            onToggleVideo = { viewModel.toggleVideo() },
            onSwitchCamera = { viewModel.switchCamera() },
            onToggleSpeaker = { viewModel.toggleSpeakerphone() },
            onEndCall = { viewModel.endCall() },
            onAnswerCall = { viewModel.answerCall() }
        )
        return
    }

    // Security Verification Screen
    if (isVerifyScreenOpen && activeContact != null) {
        VerifySecurityScreen(
            contact = activeContact!!,
            safetyNumber = safetyNumber,
            myDeviceFingerprint = viewModel.cryptoManager.getDeviceFingerprint(),
            onBack = { isVerifyScreenOpen = false },
            onToggleVerified = { viewModel.verifyContact(activeContact!!.peerId, it) }
        )
        return
    }

    // Active Chat Screen
    if (activeContact != null) {
        val isPeerTyping = typingMap[activeContact!!.peerId] == true
        ChatScreen(
            contact = activeContact!!,
            messages = activeChatMessages,
            myPeerId = userProfile.peerId,
            safetyNumber = safetyNumber,
            isTyping = isPeerTyping,
            onBack = { viewModel.clearActiveContact() },
            onSendMessage = { text, attachment, fileName, size ->
                viewModel.sendChatMessage(text, attachment, fileName, size)
            },
            onTypingChanged = { viewModel.sendTypingNotification(it) },
            onStartCall = { isVideo ->
                viewModel.startCall(activeContact!!.peerId, activeContact!!.displayName, isVideo)
            },
            onOpenSecurityVerification = { isVerifyScreenOpen = true }
        )
        return
    }

    // Main Scaffold with Bottom Navigation
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "OffGrid",
                            tint = EmergencyAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "OFFGRID CONNECT",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    // P2P Offline Indicator
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TacticalGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "AIR-GAPPED",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                },
                actions = {
                    // Demo Mode indicator button
                    Surface(
                        onClick = { viewModel.toggleDemoMode() },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDemoMode) EmergencyAmber.copy(alpha = 0.2f) else TacticalDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDemoMode) EmergencyAmber else BorderSubtle
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("appbar_demo_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = "Demo Mode",
                                tint = if (isDemoMode) EmergencyAmber else TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isDemoMode) "DEMO ON" else "DEMO OFF",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isDemoMode) EmergencyAmber else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = TacticalDarkSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TacticalDarkSurface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Nearby
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(imageVector = Icons.Default.Radar, contentDescription = "Nearby")
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_nearby),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmergencyAmber,
                        selectedTextColor = EmergencyAmber,
                        indicatorColor = TacticalDarkSurfaceHighlight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_nearby")
                )

                // Tab 1: Chats
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        if (totalUnreadCount > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = EmergencyAmber) {
                                    Text(
                                        text = "$totalUnreadCount",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Forum, contentDescription = "Chats")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Forum, contentDescription = "Chats")
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_chats),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmergencyAmber,
                        selectedTextColor = EmergencyAmber,
                        indicatorColor = TacticalDarkSurfaceHighlight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_chats")
                )

                // Tab 2: SOS Broadcast
                val activeAlertCount = sosAlerts.count { !it.acknowledged }
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        if (activeAlertCount > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = DistressRed) {
                                    Text("$activeAlertCount")
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Emergency, contentDescription = "SOS")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Emergency, contentDescription = "SOS")
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_sos),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DistressRed,
                        selectedTextColor = DistressRed,
                        indicatorColor = TacticalDarkSurfaceHighlight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_sos")
                )

                // Tab 3: Settings
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_settings),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmergencyAmber,
                        selectedTextColor = EmergencyAmber,
                        indicatorColor = TacticalDarkSurfaceHighlight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Guidance Banner if permissions pending
            AnimatedVisibility(visible = showPermissionBanner) {
                Surface(
                    color = TacticalDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Permissions",
                                tint = EmergencyAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bluetooth, Wi-Fi & Camera permissions enabled for offline P2P transport.",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                showPermissionBanner = false
                                viewModel.startNearbyMesh()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmergencyAmber,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("OK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs Content
            when (selectedTab) {
                0 -> NearbyUsersScreen(
                    contacts = contacts,
                    isDiscovering = isDiscovering,
                    isDemoMode = isDemoMode,
                    onToggleScan = {
                        if (isDiscovering) viewModel.stopNearbyMesh() else viewModel.startNearbyMesh()
                    },
                    onToggleDemoMode = { viewModel.toggleDemoMode() },
                    onSelectContact = { viewModel.selectContact(it) },
                    onStartCall = { contact, isVideo ->
                        viewModel.startCall(contact.peerId, contact.displayName, isVideo)
                    },
                    onVerifyContact = {
                        viewModel.selectContact(it)
                        isVerifyScreenOpen = true
                    }
                )

                1 -> ChatsListScreen(
                    conversations = conversations,
                    onSelectContact = { viewModel.selectContact(it) },
                    onStartCall = { contact, isVideo ->
                        viewModel.startCall(contact.peerId, contact.displayName, isVideo)
                    },
                    onNavigateToRadar = { selectedTab = 0 }
                )

                2 -> SosScreen(
                    sosAlerts = sosAlerts,
                    onBroadcastSos = { type, urgency, notes, lat, lon ->
                        viewModel.broadcastSos(type, urgency, notes, lat, lon)
                    },
                    onAcknowledgeAlert = { viewModel.acknowledgeSos(it) }
                )

                3 -> SettingsScreen(
                    userProfile = userProfile,
                    isDemoMode = isDemoMode,
                    onToggleDemoMode = { viewModel.toggleDemoMode() },
                    onSaveProfile = { name, callsign, avatar, relay, batterySaver ->
                        viewModel.updateProfile(name, callsign, avatar, relay, batterySaver)
                    }
                )
            }
        }
    }
}
