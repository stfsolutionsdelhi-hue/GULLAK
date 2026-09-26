package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: SocietyRepository
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(5) }
    // Tab 0: Tasks, 1: Members, 2: Payments, 3: Reminders, 4: Settings, 5: Member Portal
    val isLiveSyncActive by repository.isLiveSyncActive.collectAsState()
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val isSessionLocked by repository.isSessionLocked.collectAsState()
    val loggedInMemberId by repository.loggedInMemberId.collectAsState()

    // User Request: App open hone par live sync automatically run kare
    LaunchedEffect(Unit) {
        if (repository.isLiveSyncActive.value) {
            repository.syncWithGoogleSheet()
        }
    }

    var showAccountSummaryDialog by remember { mutableStateOf(false) }
    var showLoanSummaryDialog by remember { mutableStateOf(false) }
    var showDrawerLogoutDialog by remember { mutableStateOf(false) }
    var showMemberLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var targetPaymentTxnId by remember { mutableStateOf<String?>(null) }

    val navItems = listOf(
        NavigationItem("Tasks", Icons.Default.Home),
        NavigationItem("Members", Icons.Default.People),
        NavigationItem("Payments", Icons.Default.CreditCard),
        NavigationItem("Reminders", Icons.Default.Notifications),
        NavigationItem("Settings", Icons.Default.Settings)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF090D16),
                drawerContentColor = TextPrimary,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .width(300.dp)
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Side Panel Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (selectedTab != 5) AccentGold else AccentBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (selectedTab != 5) "👑" else "👤", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = if (selectedTab != 5) "GULLAK CO-OPERATIVE" else "MEMBER PORTAL",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (selectedTab != 5) "Admin Control • Kakrola" else "Passbook & Online Pay",
                                color = if (selectedTab != 5) PrimaryGreen else AccentBlue,
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    if (selectedTab != 5) {
                        // ================== ADMIN SIDE DRAWER PANEL ==================
                        if (isSessionLocked) {
                            // When Admin Session is Locked / Logged Out: Do NOT expose confidential Admin menus
                            Text(
                                text = "🔒 ADMIN SESSION LOCKED",
                                color = AccentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = AccentGold) },
                                label = { Text("Unlock Admin Session 🔑", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                selected = selectedTab == 4,
                                onClick = {
                                    selectedTab = 4
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color(0xFF1E293B),
                                    selectedTextColor = AccentGold,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Member Passbook", tint = AccentBlue) },
                                label = { Text("Go to Member Passbook 👤", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    selectedTab = 5
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = AccentBlue,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.DownloadForOffline, contentDescription = "Download APK", tint = PrimaryGreen) },
                                label = { Text("Download / Update APK 📲", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    showDownloadDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Gavel, contentDescription = "Rules", tint = AccentGold) },
                                label = { Text("सोसाइटी नियम (Rules) 📜", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    showRulesDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        } else {
                            Text(
                                text = "👑 ADMIN TOOLS",
                                color = AccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = if (selectedTab == 0) PrimaryGreen else TextMuted) },
                                label = { Text("Society Tasks & Summary", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.People, contentDescription = "Members", tint = if (selectedTab == 1) PrimaryGreen else TextMuted) },
                                label = { Text("Members Directory (${members.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.CreditCard, contentDescription = "Payments", tint = if (selectedTab == 2) PrimaryGreen else TextMuted) },
                                label = { Text("RD & Loan Collection", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = selectedTab == 2,
                                onClick = {
                                    selectedTab = 2
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Reminders", tint = if (selectedTab == 3) PrimaryGreen else TextMuted) },
                                label = { Text("Reminders & Bulk SMS", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = selectedTab == 3,
                                onClick = {
                                    selectedTab = 3
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = if (selectedTab == 4) PrimaryGreen else TextMuted) },
                                label = { Text("Sync, QR & Settings", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = selectedTab == 4,
                                onClick = {
                                    selectedTab = 4
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = "📊 SOCIETY INSIGHTS",
                                color = PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Assessment, contentDescription = "Account Summary", tint = AccentGold) },
                                label = { Text("Account Summary", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    showAccountSummaryDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Gavel, contentDescription = "Rules", tint = AccentGold) },
                                label = { Text("सोसाइटी नियम (Rules) 📜", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    showRulesDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.DownloadForOffline, contentDescription = "Download APK", tint = AccentGold) },
                                label = { Text("Download / Update APK 📲", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                selected = false,
                                onClick = {
                                    showDownloadDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedTextColor = PrimaryGreen,
                                    unselectedTextColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom live sync status card for admins
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLiveSyncActive) PrimaryGreenDark else Color(0xFF451A03),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Live Google Sync", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (isLiveSyncActive) "🟢 ACTIVE" else "⏸ PAUSED",
                                        color = if (isLiveSyncActive) PrimaryGreen else AccentGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Admin Logout Button
                            Surface(
                                onClick = {
                                    showDrawerLogoutDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF3B0712).copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    Text("Logout / Lock Session", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                    } else {
                        // ================== MEMBER SIDE DRAWER PANEL ==================
                        Text(
                            text = "👤 MEMBER PASSBOOK TABS",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Passbook", tint = AccentBlue) },
                            label = { Text("My Passbook & Details", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            selected = true,
                            onClick = {
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF075985),
                                selectedTextColor = TextPrimary,
                                unselectedTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Gavel, contentDescription = "Rules", tint = AccentBlue) },
                            label = { Text("सोसाइटी नियम (Rules) 📜", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            selected = false,
                            onClick = {
                                showRulesDialog = true
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedTextColor = PrimaryGreen,
                                unselectedTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.DownloadForOffline, contentDescription = "Download APK", tint = AccentBlue) },
                            label = { Text("Download / Update APK 📲", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            selected = false,
                            onClick = {
                                showDownloadDialog = true
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedTextColor = PrimaryGreen,
                                unselectedTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // When member is logged in, show logout button; when not logged in, show Admin switch
                        if (loggedInMemberId != null) {
                            Surface(
                                onClick = {
                                    showMemberLogoutConfirmDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF3B0712).copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = "Logout Member", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    Text("Logout Passbook 🚪", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Surface(
                                onClick = {
                                    selectedTab = 0
                                    scope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Switch", tint = AccentGold, modifier = Modifier.size(16.dp))
                                    Text("Admin / Staff Login 🔑", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        onClick = {
                            showDownloadDialog = true
                            scope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(12.dp))
                                Text("Version ${com.example.data.APP_VERSION}", color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("APK Update 📥", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "🏦 Gullak Tasks"
                                    1 -> "👥 Society Members"
                                    2 -> "💳 RD & Loan Collection"
                                    3 -> "🔔 Auto Reminders"
                                    4 -> "⚙️ Society Settings"
                                    5 -> "👤 Member Passbook"
                                    else -> "Gullak Society"
                                },
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = AccentGold)
                        }
                    },
                    actions = {
                        if (selectedTab != 5 && !isSessionLocked) {
                            Surface(
                                onClick = {
                                    val state = repository.toggleLiveSync()
                                    val txt = if (state) "🟢 Live Sync Resumed" else "⏸️ Live Sync Paused"
                                    Toast.makeText(context, txt, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isLiveSyncActive) PrimaryGreenDark else Color(0xFF451A03),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveSyncActive) PrimaryGreen else AccentGold),
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isLiveSyncActive) PrimaryGreen else AccentGold,
                                        modifier = Modifier.size(6.dp)
                                    ) {}
                                    Text(
                                        text = if (isLiveSyncActive) "Live" else "Paused",
                                        color = if (isLiveSyncActive) PrimaryGreen else AccentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF070B14)
                    )
                )
            },
            bottomBar = {
                if (selectedTab != 5 && !isSessionLocked) {
                    NavigationBar(
                        containerColor = Color(0xFF060913),
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryGreen,
                                    selectedTextColor = PrimaryGreen,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = PrimaryGreenDark
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isSessionLocked && selectedTab != 5) {
                    var unlockPasscode by remember { mutableStateOf("") }
                    var showMainForgotDialog by remember { mutableStateOf(false) }
                    var forgotMobile by remember { mutableStateOf("") }
                    var forgotPin by remember { mutableStateOf("") }
                    var forgotNewPass by remember { mutableStateOf("") }
                    var forgotConfirmPass by remember { mutableStateOf("") }
                    val context = LocalContext.current
                    val societySettings by repository.societySettings.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgDark)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF450A0A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AccentRed, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Admin Panel Locked", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Enter admin passkey to unlock admin controls", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = unlockPasscode,
                            onValueChange = { unlockPasscode = it },
                            singleLine = true,
                            placeholder = { Text("Enter admin passkey", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (repository.unlockSession(unlockPasscode)) {
                                    selectedTab = 0 // User Request 1: Open Tasks tab upon admin login
                                    Toast.makeText(context, "Welcome Admin! Session Unlocked.", Toast.LENGTH_SHORT).show()
                                    unlockPasscode = ""
                                } else {
                                    Toast.makeText(context, "Invalid key! Enter correct admin passkey to unlock.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Unlock Admin Session 🔓", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                forgotMobile = societySettings.adminWhatsApp
                                forgotPin = ""
                                forgotNewPass = ""
                                forgotConfirmPass = ""
                                showMainForgotDialog = true
                            }
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Forgot", tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Forgot Passkey? / पासवर्ड भूल गए?", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { selectedTab = 5 }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Go back to Member Passbook", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showMainForgotDialog) {
                        val adminPhone = societySettings.adminWhatsApp
                        val maskedPhoneHint = if (adminPhone.length >= 10) "xxx${adminPhone.substring(3, 7)}xxx" else "xxx1817xxx"

                        AlertDialog(
                            onDismissRequest = { showMainForgotDialog = false },
                            containerColor = Color(0xFF0F172A),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = "Recovery", tint = AccentGold)
                                    Text("🔑 Admin Passkey Recovery", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Bina OTP ke Safe Recovery: Registered Admin Mobile number aur Recovery PIN daal kar naya passkey banayein.",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )

                                    OutlinedTextField(
                                        value = forgotMobile,
                                        onValueChange = { forgotMobile = it },
                                        label = { Text("Registered Admin Mobile (Hint: $maskedPhoneHint)") },
                                        placeholder = { Text("Enter 10-digit mobile number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentGold,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    OutlinedTextField(
                                        value = forgotPin,
                                        onValueChange = { forgotPin = it },
                                        label = { Text("Admin Recovery PIN") },
                                        placeholder = { Text("••••") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentGold,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    OutlinedTextField(
                                        value = forgotNewPass,
                                        onValueChange = { forgotNewPass = it },
                                        label = { Text("New Admin Passkey") },
                                        placeholder = { Text("Enter new passkey") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryGreen,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    OutlinedTextField(
                                        value = forgotConfirmPass,
                                        onValueChange = { forgotConfirmPass = it },
                                        label = { Text("Confirm New Passkey") },
                                        placeholder = { Text("Re-enter new passkey") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryGreen,
                                            unfocusedBorderColor = CardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (forgotMobile.trim().isEmpty()) {
                                            Toast.makeText(context, "Please enter your 10-digit registered admin mobile number!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (forgotPin.trim().isEmpty()) {
                                            Toast.makeText(context, "Please enter your Recovery PIN!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (forgotNewPass.trim().isEmpty()) {
                                            Toast.makeText(context, "New passkey cannot be empty!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (forgotNewPass.trim() != forgotConfirmPass.trim()) {
                                            Toast.makeText(context, "New passkeys do not match!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val (success, msg) = repository.resetAdminPasswordWithRecovery(
                                            adminMobile = forgotMobile,
                                            recoveryPin = forgotPin,
                                            newPass = forgotNewPass
                                        )
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (success) {
                                            showMainForgotDialog = false
                                            selectedTab = 0
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                                ) {
                                    Text("Reset & Unlock Admin 🔓", color = Color(0xFF451A03), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMainForgotDialog = false }) {
                                    Text("Cancel", color = TextMuted)
                                }
                            }
                        )
                    }
                } else {
                    when (selectedTab) {
                        0 -> TasksScreen(repository = repository, onNavigateToPayments = { selectedTab = 2 })
                        1 -> MembersScreen(
                            repository = repository,
                            onNavigateToPaymentDetail = { txnId ->
                                targetPaymentTxnId = txnId
                                selectedTab = 2
                            }
                        )
                        2 -> PaymentsScreen(
                            repository = repository,
                            highlightTxnId = targetPaymentTxnId,
                            onClearHighlight = { targetPaymentTxnId = null }
                        )
                        3 -> RemindersScreen(repository = repository)
                        4 -> SettingsScreen(repository = repository)
                        5 -> MemberPortalScreen(repository = repository, onSwitchToAdmin = {
                            repository.logoutAdmin()
                            selectedTab = 0
                        })
                    }
                }
            }
        }
    }

    // ================== DIALOG: ACCOUNT SUMMARY (Requirement 7) ==================
    if (showAccountSummaryDialog && !isSessionLocked) {
        val totalMembers = members.size
        val totalMonthlyRd = members.sumOf { it.monthlyRd }
        val totalCollected = payments.sumOf { it.totalAmount }
        val totalPendingDues = members.sumOf { it.pendingDues }
        val totalPenalties = members.sumOf { it.penaltyApplicable }

        AlertDialog(
            onDismissRequest = { showAccountSummaryDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = "Summary", tint = AccentGold)
                    Text("📊 Society Account Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Registered Members:", color = TextSecondary, fontSize = 12.sp)
                                Text("$totalMembers Members", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly RD Expected:", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalMonthlyRd", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Collected (This Mo):", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalCollected", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pending Dues:", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalPendingDues", color = if (totalPendingDues > 0) AccentGold else PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            if (totalPenalties > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Penalties Applicable:", color = TextSecondary, fontSize = 12.sp)
                                    Text("₹$totalPenalties", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Text(
                        "All accounts and balances are continuously in sync with the Google Sheets master database.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAccountSummaryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Close", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG: LOAN SUMMARY (Requirement 8) ==================
    if (showLoanSummaryDialog && !isSessionLocked) {
        val activeBorrowers = members.filter { it.gullakLoan > 0 }
        val totalOutstandingLoans = members.sumOf { it.gullakLoan }
        val totalLoanRepayments = payments.sumOf { it.loanRepayAmount }
        val totalInterestCollected = payments.sumOf { it.interestAmount }

        AlertDialog(
            onDismissRequest = { showLoanSummaryDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = "Loans", tint = AccentBlue)
                    Text("💰 Society Loan Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Active Borrowers:", color = TextSecondary, fontSize = 12.sp)
                                Text("${activeBorrowers.size} Members", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Outstanding Loans:", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalOutstandingLoans", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan Principal Recovered:", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalLoanRepayments", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Interest Collected (This Mo):", color = TextSecondary, fontSize = 12.sp)
                                Text("₹$totalInterestCollected", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (activeBorrowers.isNotEmpty()) {
                        Text("Active Borrowers Preview:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            activeBorrowers.take(4).forEach { b ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(b.name, color = TextPrimary, fontSize = 11.sp)
                                    Text("₹${b.gullakLoan}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLoanSummaryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Close", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG: SIDE DRAWER LOGOUT CONFIRMATION ==================
    if (showDrawerLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showDrawerLogoutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentRed)
                    Text("Confirm Admin Logout", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Kya aap Admin Session ko lock / logout karna chahte hain?",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        "Admin panel lock ho jayega. Login karne ke liye admin passkey ki zaroorat hogi.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.logoutAdmin()
                        showDrawerLogoutDialog = false
                        selectedTab = 5 // Switch directly to Member Passbook / Portal
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Yes, Logout 🔒", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDrawerLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG: RULES & REGULATIONS (FULL SCREEN & LARGE FONT) ==================
    if (showRulesDialog) {
        val rules by repository.rulesAndRegulations.collectAsState()
        Dialog(
            onDismissRequest = { showRulesDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark),
                color = BgDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showRulesDialog = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CardDark)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Close", tint = AccentGold)
                            }
                            Column {
                                Text(
                                    text = "📜 समाज के नियम और शर्तें",
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Gullak Co-operative Society • Official Guidelines",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showRulesDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("बंद करें (Close)", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚖️", fontSize = 24.sp)
                            Column {
                                Text(
                                    "सभी सदस्यों के लिए अनिवार्य नियम (Rules & Regulations)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "सोसाइटी की वित्तीय पारदर्शिता एवं सुचारू संचालन हेतु सभी नियम मान्य हैं:",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Rules List in Big Readable Font
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(rules) { index, rule ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Number Badge
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E3A8A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = AccentBlue,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }

                                    // Rule Content in Big Font
                                    Text(
                                        text = rule,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer Share Button
                    Button(
                        onClick = {
                            try {
                                val rulesText = StringBuilder("📜 *गुल्लक को-ऑपरेटिव सोसाइटी - नियम व विनियम (RULES & REGULATIONS)*\n\n")
                                rules.forEachIndexed { i, r -> rulesText.append("${i + 1}. $r\n\n") }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, rulesText.toString())
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Society Rules")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFFA7F3D0), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("नियम व्हाट्सएप पर भेजें 📲", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Member Logout Confirmation Dialog from Side Drawer
    if (showMemberLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMemberLogoutConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentRed)
                    Text("Confirm Member Logout", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Kya aap Gullak Member Passbook se logout karna chahte hain?",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🔔", fontSize = 14.sp)
                            Text(
                                "Logout ke baad bhi official society alerts aur passbook updates aapke device par aate rahenge.",
                                color = PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.logoutMember()
                        showMemberLogoutConfirmDialog = false
                        Toast.makeText(context, "Logged out. Society alerts will remain active 🔔", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Yes, Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemberLogoutConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // App Download & Update Dialog for Admin & Members
    if (showDownloadDialog) {
        val appDownloadUrl by repository.appDownloadUrl.collectAsState()
        var editUrlMode by remember { mutableStateOf(false) }
        var inputUrl by remember(appDownloadUrl) { mutableStateOf(appDownloadUrl) }

        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = "Download APK", tint = PrimaryGreen)
                    Text("Download & Update App APK", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Version info card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Installed Version", color = TextSecondary, fontSize = 10.sp)
                                Text("v${com.example.data.APP_VERSION}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Build Date", color = TextSecondary, fontSize = 10.sp)
                                Text(com.example.data.APP_BUILD_DATE, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Text(
                                "Purani app ko delete/uninstall karne ki bilkul zaroorat nahi hai. Download hone par seedha 'Update' par click karein.",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Download URL Display or Edit
                    if (editUrlMode) {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("APK Download / Drive URL") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { editUrlMode = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    if (inputUrl.isNotBlank()) {
                                        repository.updateAppDownloadUrl(inputUrl)
                                        editUrlMode = false
                                        Toast.makeText(context, "Download link updated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Save URL", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Current Download Link:", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = appDownloadUrl,
                                    color = AccentBlue,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Action Buttons: Direct Download & Open GitHub Repo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Checking GitHub repository for latest v8.0 update...", Toast.LENGTH_SHORT).show()
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(appDownloadUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to open update link: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color(0xFF064E3B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check Update 🔄", color = Color(0xFF064E3B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/stfsolutionsdelhi-hue/GULLAK"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to open GitHub: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GitHub Repo 🌐", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Copy Link & Share on WhatsApp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("App Download URL", appDownloadUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Download link copied! 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link 📋", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, "Namaste! Gullak Co-operative Society Android App ka latest update APK yahan se download karein:\n$appDownloadUrl\nGitHub Repo: https://github.com/stfsolutionsdelhi-hue/GULLAK\n(Version: v${com.example.data.APP_VERSION})")
                                            type = "text/plain"
                                        }
                                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share APK Download Link")
                                        context.startActivity(shareIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error sharing link: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp 📤", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Helpful Notice for 404
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ℹ️ Note: Agar GitHub direct APK par 404 error aaye to GitHub repository me ek baar 'Release' publish karein, ya 'Admin: Change APK Link ⚙️' se apna Google Drive / direct link set karein.",
                                color = TextMuted,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(6.dp),
                                lineHeight = 13.sp
                            )
                        }

                        // Admin Only: Edit Link Button
                        if (selectedTab != 5 || !isSessionLocked) {
                            TextButton(
                                onClick = { editUrlMode = true },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Admin: Change APK Link ⚙️", color = AccentGold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDownloadDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text("Close", color = TextPrimary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

