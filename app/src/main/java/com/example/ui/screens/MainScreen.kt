package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: SocietyRepository
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    // Tab 0: Tasks, 1: Members, 2: Payments, 3: Reminders, 4: Settings, 5: Member Portal
    val isLiveSyncActive by repository.isLiveSyncActive.collectAsState()
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val isSessionLocked by repository.isSessionLocked.collectAsState()

    var showAccountSummaryDialog by remember { mutableStateOf(false) }
    var showLoanSummaryDialog by remember { mutableStateOf(false) }
    var showDrawerLogoutDialog by remember { mutableStateOf(false) }

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
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    .background(AccentGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏦", fontSize = 20.sp)
                            }
                            Column {
                                Text("GULLAK CO-OPERATIVE", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("Society Management • Kakrola", color = PrimaryGreen, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = CardBorder)

                        // Section 1: Admin Panel Tabs
                        Text("👑 ADMIN PANEL", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp, top = 4.dp))

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

                        // Section 2: Quick Insights (Requirements 7 & 8)
                        Text("📊 SOCIETY INSIGHTS", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))

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
                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Loan Summary", tint = AccentBlue) },
                            label = { Text("Loan Summary", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                            selected = false,
                            onClick = {
                                showLoanSummaryDialog = true
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedTextColor = PrimaryGreen,
                                unselectedTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))

                        // Section 3: Member Portal Login (Requirement 6)
                        Text("👤 MEMBER SECTION", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Member Portal", tint = if (selectedTab == 5) AccentBlue else TextMuted) },
                            label = { Text("Member Portal & Passbook", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            selected = selectedTab == 5,
                            onClick = {
                                selectedTab = 5
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF075985),
                                selectedTextColor = TextPrimary,
                                unselectedTextColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Bottom info & Logout in Side Panel (Requirement 6)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                        // Clean Drawer Logout Button (Requirement 6)
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

                        Text("Gullak Android Pro • V64 Master", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
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
                        // Quick switch to Member Portal or Admin
                        if (selectedTab != 5) {
                            TextButton(
                                onClick = { selectedTab = 5 },
                                colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Member", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Member", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextButton(
                                onClick = { selectedTab = 0 },
                                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF070B14)
                    )
                )
            },
            bottomBar = {
                if (selectedTab != 5) {
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
                when (selectedTab) {
                    0 -> TasksScreen(repository = repository, onNavigateToPayments = { selectedTab = 2 })
                    1 -> MembersScreen(repository = repository)
                    2 -> PaymentsScreen(repository = repository)
                    3 -> RemindersScreen(repository = repository)
                    4 -> SettingsScreen(repository = repository)
                    5 -> MemberPortalScreen(repository = repository, onSwitchToAdmin = { selectedTab = 0 })
                }
            }
        }
    }

    // ================== DIALOG: ACCOUNT SUMMARY (Requirement 7) ==================
    if (showAccountSummaryDialog) {
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
    if (showLoanSummaryDialog) {
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

    // ================== DIALOG: SIDE DRAWER LOGOUT CONFIRMATION (Requirement 6) ==================
    if (showDrawerLogoutDialog) {
        var drawerLogoutPasskey by remember { mutableStateOf("") }
        var isPassError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDrawerLogoutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Logout", tint = AccentRed)
                    Text("Confirm Session Lock / Logout", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Admin session ko lock karne ke liye Admin Passkey darj karein:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = drawerLogoutPasskey,
                        onValueChange = {
                            drawerLogoutPasskey = it
                            isPassError = false
                        },
                        placeholder = { Text("Enter admin passkey", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = isPassError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    if (isPassError) {
                        Text("Incorrect admin passkey! Please try again.", color = AccentRed, fontSize = 10.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repository.verifyAdminPassword(drawerLogoutPasskey)) {
                            repository.logoutAdmin()
                            showDrawerLogoutDialog = false
                            selectedTab = 4 // Navigate to settings which will show the locked admin screen
                        } else {
                            isPassError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Lock Admin Session 🔒", color = Color.White, fontWeight = FontWeight.Bold)
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
}

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

