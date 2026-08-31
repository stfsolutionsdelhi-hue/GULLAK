package com.example.ui.screens.member

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.UserEntity
import com.example.ui.components.AccountStatusBadge
import com.example.ui.components.CashPaymentModal
import com.example.ui.components.FinancialMetricCard
import com.example.ui.components.GullakTopBar
import com.example.ui.components.OnlinePaymentModal
import com.example.ui.components.PayNowComprehensiveDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupees
import com.example.ui.components.openWhatsApp
import com.example.ui.theme.GullakClay
import com.example.ui.theme.GullakDanger
import com.example.ui.theme.GullakGold
import com.example.ui.theme.GullakGoldContainer
import com.example.ui.theme.GullakGoldLight
import com.example.ui.theme.GullakNavyDark
import com.example.ui.theme.GullakPrimary
import com.example.ui.theme.GullakPrimaryLight
import com.example.ui.theme.GullakSuccess
import com.example.ui.theme.GullakSuccessContainer
import com.example.ui.theme.GullakWarning
import com.example.ui.viewmodel.GullakViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberMainScreen(
    user: UserEntity,
    viewModel: GullakViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showComprehensivePayDialog by remember { mutableStateOf(false) }
    var showPaymentChoiceDialog by remember { mutableStateOf(false) }
    var showCashModal by remember { mutableStateOf(false) }
    var showOnlineModal by remember { mutableStateOf(false) }

    val financials by viewModel.currentMemberFinancial.collectAsStateWithLifecycle()
    val payments by viewModel.currentMemberPayments.collectAsStateWithLifecycle()
    val notifications by viewModel.currentMemberNotifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.currentMemberUnreadCount.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userMsg by viewModel.userMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showMemberNotificationsPopup by remember { mutableStateOf(false) }
    var hasAutoShownNotificationsPopup by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Automatically trigger notification popup when member enters app ONLY if there are genuine UNREAD notifications
    LaunchedEffect(unreadCount) {
        if (!hasAutoShownNotificationsPopup && unreadCount > 0) {
            showMemberNotificationsPopup = true
            hasAutoShownNotificationsPopup = true
        }
    }

    LaunchedEffect(userMsg, errorMsg) {
        userMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        errorMsg?.let {
            snackbarHostState.showSnackbar("❌ $it")
            viewModel.clearMessages()
        }
    }

    val defaultRd = financials?.rdAmount ?: 400.0
    val currentRdDue = financials?.currentRdDue ?: 400.0
    val currentInterestDue = financials?.interestDue ?: 0.0
    val totalDue = financials?.totalDue ?: (currentRdDue + currentInterestDue)
    val loanOutstanding = financials?.loanOutstanding ?: 0.0
    val loanEligibility = financials?.loanEligibility ?: 50000.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GullakTopBar(
                title = "GULLAK CO OPRATIVE SOCIETY",
                subtitle = "नमस्ते, ${user.name} (${user.userId})",
                unreadNotificationCount = unreadCount,
                onNotificationClick = { showMemberNotificationsPopup = true },
                onLogoutClick = null
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = (selectedTab == 0),
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_member_home")
                )
                NavigationBarItem(
                    selected = (selectedTab == 1),
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_member_history")
                )
                NavigationBarItem(
                    selected = (selectedTab == 2),
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Loan") },
                    label = { Text("Loan", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_member_loan")
                )
                NavigationBarItem(
                    selected = (selectedTab == 3),
                    onClick = { selectedTab = 3 },
                    icon = {
                        Box {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GullakDanger)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    },
                    label = { Text("Notices", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_member_notif")
                )
                NavigationBarItem(
                    selected = (selectedTab == 4),
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_member_profile")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> MemberDashboardView(
                    user = user,
                    defaultRd = defaultRd,
                    currentRdDue = currentRdDue,
                    currentInterestDue = currentInterestDue,
                    totalDue = totalDue,
                    loanOutstanding = loanOutstanding,
                    recentPayments = payments.take(3),
                    onPayNowClick = { showComprehensivePayDialog = true },
                    onViewAllHistory = { selectedTab = 1 },
                    onViewLoanDetails = { selectedTab = 2 },
                    onChatWhatsApp = {
                        openWhatsApp(context, settings?.adminMobile ?: "9876543210", "Namaste Admin, mera Gullak app query:")
                    }
                )
                1 -> MemberHistoryView(payments = payments)
                2 -> MemberLoanView(
                    financials = financials,
                    onOpenWhatsApp = {
                        openWhatsApp(context, settings?.adminMobile ?: "9876543210", "Gullak Society Loan Jankari:")
                    },
                    onPayDuesClick = { showComprehensivePayDialog = true }
                )
                3 -> MemberNotificationsView(
                    notifications = notifications,
                    onMarkAsRead = { viewModel.markNotificationAsRead(it) },
                    onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                    onPayDuesClick = { showComprehensivePayDialog = true }
                )
                4 -> MemberProfileView(
                    user = user,
                    financials = financials,
                    adminMobile = settings?.adminMobile ?: "9876543210",
                    onPayDuesClick = { showComprehensivePayDialog = true },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }

    // Member App Open Popup Notifications Dialog
    if (showMemberNotificationsPopup) {
        MemberNotificationsPopupDialog(
            notifications = notifications,
            unreadCount = unreadCount,
            currentRdDue = currentRdDue,
            currentInterestDue = currentInterestDue,
            totalDue = totalDue,
            onDismiss = {
                if (unreadCount > 0) {
                    viewModel.markAllNotificationsAsRead()
                }
                showMemberNotificationsPopup = false
            },
            onMarkAsRead = { viewModel.markNotificationAsRead(it) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onPayDuesClick = {
                if (unreadCount > 0) {
                    viewModel.markAllNotificationsAsRead()
                }
                showMemberNotificationsPopup = false
                showComprehensivePayDialog = true
            },
            onViewAllNoticesClick = {
                if (unreadCount > 0) {
                    viewModel.markAllNotificationsAsRead()
                }
                showMemberNotificationsPopup = false
                selectedTab = 3
            }
        )
    }

    // 4-Column Comprehensive Member Payment Dialog
    if (showComprehensivePayDialog) {
        val livePenalty = financials?.calculateLivePenalty() ?: 0.0
        PayNowComprehensiveDialog(
            monthlyRdDefault = currentRdDue,
            interestDueDefault = currentInterestDue,
            penaltyDueDefault = livePenalty,
            loanOutstanding = loanOutstanding,
            upiId = settings?.upiId ?: "gullaksociety@okaxis",
            payeeName = settings?.upiPayeeName ?: "Gullak Co-operative Society",
            onDismiss = { showComprehensivePayDialog = false },
            onSubmitPayment = { totalAmount, rdAmount, interestAmount, penaltyAmount, loanReturnAmount, paymentType, paymentMode, remarks ->
                viewModel.submitPaymentWithBreakdown(
                    totalAmount = totalAmount,
                    rdAmount = rdAmount,
                    interestAmount = interestAmount,
                    penaltyAmount = penaltyAmount,
                    loanReturnAmount = loanReturnAmount,
                    paymentType = paymentType,
                    paymentMode = paymentMode,
                    remarks = remarks
                )
                showComprehensivePayDialog = false
            }
        )
    }

    // Payment Option Modal (Cash vs Pay Online)
    if (showPaymentChoiceDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPaymentChoiceDialog = false },
            title = {
                Text(
                    text = "Select Payment Mode / भुगतान का तरीका",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Total Due Amount: ${formatRupees(totalDue)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalDue > 0) GullakDanger else GullakSuccess
                        )
                    )

                    // Option 1: Cash Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPaymentChoiceDialog = false
                                showCashModal = true
                            }
                            .testTag("choice_cash_card"),
                        colors = CardDefaults.cardColors(containerColor = GullakGoldContainer),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, GullakGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GullakGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = "Cash",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Option 1: CASH / नकद",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GullakGoldLight
                                    )
                                )
                                Text(
                                    text = "सोसाइटी में Cash जमा करके Request भेजें",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GullakGoldLight.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }

                    // Option 2: Online UPI Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPaymentChoiceDialog = false
                                showOnlineModal = true
                            }
                            .testTag("choice_online_card"),
                        colors = CardDefaults.cardColors(containerColor = GullakPrimary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, GullakPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GullakPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Online UPI",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Option 2: PAY ONLINE / यूपीआई",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GullakPrimary
                                    )
                                )
                                Text(
                                    text = "UPI QR Code स्कैन करके तुरंत पेमेंट करें",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GullakTextSecondary)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showPaymentChoiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cash Payment Popup Form
    if (showCashModal) {
        CashPaymentModal(
            defaultAmount = if (totalDue > 0) totalDue else defaultRd,
            onDismiss = { showCashModal = false },
            onSubmit = { amount, month, remarks ->
                viewModel.submitPayment(amount, PaymentType.CASH, "$remarks (Month: $month)")
                showCashModal = false
            }
        )
    }

    // Online UPI Payment Popup Form
    if (showOnlineModal) {
        OnlinePaymentModal(
            defaultAmount = if (totalDue > 0) totalDue else defaultRd,
            upiId = settings?.upiId ?: "gullaksociety@okaxis",
            payeeName = settings?.upiPayeeName ?: "Gullak Co-operative Society",
            uploadedQrImage = settings?.uploadedQrCodeImage ?: "",
            onDismiss = { showOnlineModal = false },
            onSubmit = { amount, remarks ->
                viewModel.submitPayment(amount, PaymentType.ONLINE, remarks)
                showOnlineModal = false
            }
        )
    }
}

// 1. Member Dashboard View
@Composable
fun MemberDashboardView(
    user: UserEntity,
    defaultRd: Double,
    currentRdDue: Double,
    currentInterestDue: Double,
    totalDue: Double,
    loanOutstanding: Double,
    recentPayments: List<com.example.data.model.PaymentEntity>,
    onPayNowClick: () -> Unit,
    onViewAllHistory: () -> Unit,
    onViewLoanDetails: () -> Unit,
    onChatWhatsApp: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GullakPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "नमस्ते, ${user.name}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "User ID: ${user.userId} • Mobile: ${user.mobile}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                        AccountStatusBadge(status = user.status)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "इस महीने का कुल बकाया (Total Due)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f))
                                )
                                Text(
                                    text = formatRupees(totalDue),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (totalDue > 0) GullakGoldLight else Color.White
                                    )
                                )
                            }
                            if (totalDue == 0.0) {
                                Surface(
                                    color = GullakSuccess,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "PAID / चुकता ✅",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: इस महीने की स्थिति (Three Columns: RD, Interest, Loan)
        item {
            Text(
                text = "इस महीने की स्थिति (Current Month Status)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RD Column
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("RD", fontWeight = FontWeight.Bold, color = GullakPrimary, fontSize = 14.sp)
                        Text("Fixed", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatRupees(defaultRd),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GullakPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentRdDue > 0) "Due: ₹${currentRdDue.toInt()}" else "Paid ✅",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentRdDue > 0) GullakDanger else GullakSuccess
                        )
                    }
                }

                // Interest Column
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Interest", fontWeight = FontWeight.Bold, color = GullakClay, fontSize = 14.sp)
                        Text("1% Monthly", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatRupees(currentInterestDue),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (currentInterestDue > 0) GullakDanger else GullakTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (loanOutstanding > 0) "on ₹${loanOutstanding.toInt()}" else "No Loan",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Loan Column
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onViewLoanDetails() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Loan", fontWeight = FontWeight.Bold, color = GullakGold, fontSize = 14.sp)
                        Text("Outstanding", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatRupees(loanOutstanding),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GullakGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("View Details →", fontSize = 10.sp, color = GullakPrimaryLight, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Prominent Big PAY NOW Button
        item {
            Button(
                onClick = onPayNowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("member_pay_now_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAY NOW / अभी भुगतान करें",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Chat with Admin on WhatsApp Card (Moved from Profile to Home)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.2.dp, GullakSuccess.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GullakSuccess.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = GullakSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat with Admin on WhatsApp",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "सोसाइटी एडमिन से किसी भी सहायता या प्रश्न हेतु व्हाट्सएप पर बात करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onChatWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_chat_admin_whatsapp_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Chat With Admin (व्हाट्सएप संपर्क) 💬",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Recent Payments Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Payments / हाल के भुगतान",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onViewAllHistory) {
                    Text("View All (सभी देखें) →", color = GullakPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Abhi koi payment request nahi hai.",
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(recentPayments) { payment ->
                PaymentItemCard(payment = payment)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// 2. Member Payment History View
@Composable
fun MemberHistoryView(
    payments: List<com.example.data.model.PaymentEntity>
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Payment History / भुगतान का इतिहास",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "आपके द्वारा किए गए सभी Cash और Online भुगतानों की स्थिति",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Koi payment record nahi mila.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(payments) { payment ->
                    PaymentItemCard(payment = payment)
                }
            }
        }
    }
}

@Composable
fun PaymentItemCard(payment: com.example.data.model.PaymentEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val formattedDate = remember(payment.paymentDate) { dateFormat.format(Date(payment.paymentDate)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Member Name Header Row if available
            if (payment.userName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GullakPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = payment.userName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = GullakGoldLight,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = payment.userName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "ID: ${payment.userId} • 📱 ${payment.userMobile}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    StatusBadge(status = payment.status)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (payment.paymentType == PaymentType.ONLINE) GullakPrimary.copy(alpha = 0.1f) else GullakGoldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = payment.paymentType.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (payment.paymentType == PaymentType.ONLINE) GullakPrimary else GullakClay,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = payment.transactionId,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Gray
                    )
                }

                if (payment.userName.isBlank()) {
                    StatusBadge(status = payment.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = formatRupees(payment.approvedAmount ?: payment.amount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GullakGold
                        )
                    )
                    if (payment.approvedAmount != null && payment.approvedAmount != payment.amount) {
                        Text(
                            text = "Submitted: ${formatRupees(payment.amount)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                if (payment.remarks.isNotBlank()) {
                    Text(
                        text = payment.remarks,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            // 4-Column Breakdown details
            if (payment.rdAmount > 0 || payment.interestAmount > 0 || payment.penaltyAmount > 0 || payment.loanReturnAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RD: ₹${payment.rdAmount.toInt()}", fontSize = 11.sp, color = GullakGoldLight, fontWeight = FontWeight.SemiBold)
                        Text("Int: ₹${payment.interestAmount.toInt()}", fontSize = 11.sp, color = GullakGoldLight, fontWeight = FontWeight.SemiBold)
                        Text("Pen: ₹${payment.penaltyAmount.toInt()}", fontSize = 11.sp, color = if (payment.penaltyAmount > 0) GullakDanger else Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text("Loan: ₹${payment.loanReturnAmount.toInt()}", fontSize = 11.sp, color = if (payment.loanReturnAmount > 0) GullakSuccess else Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Show rejection reason if rejected
            if (payment.status == PaymentStatus.REJECTED && payment.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = GullakDanger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reason: ${payment.rejectionReason}",
                        color = GullakDanger,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// 3. Member Loan Details View
@Composable
fun MemberLoanView(
    financials: com.example.data.model.MemberFinancialEntity?,
    onOpenWhatsApp: () -> Unit,
    onPayDuesClick: () -> Unit
) {
    val loanOutstanding = financials?.loanOutstanding ?: 0.0
    val interestDue = financials?.interestDue ?: 0.0
    val totalLoanDues = loanOutstanding + interestDue
    val hasLoanDues = (loanOutstanding > 0.0 || interestDue > 0.0)
    val configuredEligibility = financials?.loanEligibility ?: 50000.0
    val effectiveEligibility = if (hasLoanDues) 0.0 else configuredEligibility
    val lastMonthEnd = financials?.lastMonthEndBalance ?: loanOutstanding

    var showLoanDuesAlert by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Loan Details / ऋण विवरण",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "सोसाइटी लोन पर 1% प्रति माह ब्याज लगता है (Month-End Outstanding आधार पर)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GullakPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Current Loan Outstanding (बकाया ऋण)", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupees(loanOutstanding),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monthly Interest Rate", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Text("1% प्रति माह", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Month Interest", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            Text(formatRupees(interestDue), color = GullakGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Important Loan Rules (नियम):", fontWeight = FontWeight.Bold)

                    Text(
                        text = "1. Interest calculation Month-End Outstanding Balance के आधार पर की जाती है।",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. उदाहरण: अगर 31 August को ₹40,000 Due है, तो September में ब्याज ₹40,000 × 1% = ₹400 होगा।",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Loan Eligibility Member Edit नहीं कर सकता। अगर पिछला कोई भी लोन बकाया है तो पात्रता स्वतः ₹0 हो जाती है।",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. Final Loan Approval Society के Offline VBA Ledger व नियमों के अनुसार होती है।",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasLoanDues) Color(0xFF2D1B1F) else GullakGoldContainer
                ),
                border = BorderStroke(1.dp, if (hasLoanDues) GullakDanger.copy(alpha = 0.6f) else GullakGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "आपकी Loan Eligibility: ${formatRupees(effectiveEligibility)}",
                            fontWeight = FontWeight.Bold,
                            color = if (hasLoanDues) GullakDanger else GullakGoldLight,
                            fontSize = 16.sp
                        )
                        if (hasLoanDues) {
                            Surface(
                                color = GullakDanger.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Dues Active",
                                    color = GullakDanger,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasLoanDues) {
                        Text(
                            text = "⚠️ सक्रिय लोन बकाया (Loan: ₹${loanOutstanding.toInt()}, Interest: ₹${interestDue.toInt()}) होने के कारण आपकी पात्रता स्वतः ₹0 है। नया लोन प्राप्त करने के लिए पहले बकाया लोन चुकता करें।",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFB4AB))
                        )
                    } else {
                        Text(
                            text = "✅ कोई सक्रिय लोन बकाया नहीं है। आप ₹${formatRupees(effectiveEligibility)} तक के नए लोन के लिए पात्र हैं। Final approval Admin द्वारा तय होगा।",
                            style = MaterialTheme.typography.bodySmall.copy(color = GullakGoldLight.copy(alpha = 0.85f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (hasLoanDues) {
                                showLoanDuesAlert = true
                            } else {
                                onOpenWhatsApp()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasLoanDues) GullakDanger else GullakSuccess
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (hasLoanDues) Icons.Default.Info else Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasLoanDues) "Check Loan Dues Alert (लोन बकाया स्थिति देखें)" else "Contact Admin on WhatsApp (लोन आवेदन)",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showLoanDuesAlert) {
        AlertDialog(
            onDismissRequest = { showLoanDuesAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GullakDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⚠️ Loan Eligibility Alert / पात्रता शून्य", fontWeight = FontWeight.Bold, color = GullakDanger)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Aapka pehle se loan balance baki hone ke karan aapki Loan Eligibility abhi ₹0 hai.",
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan Outstanding:", fontSize = 13.sp, color = Color.Gray)
                                Text("₹${loanOutstanding.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Interest Due:", fontSize = 13.sp, color = Color.Gray)
                                Text("₹${interestDue.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GullakDanger)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Loan Dues:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹${totalLoanDues.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GullakDanger)
                            }
                        }
                    }
                    Text(
                        text = "Naya loan tabhi approve ho sakega jab purana loan poora chukta ho jaye.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoanDuesAlert = false
                        onPayDuesClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess)
                ) {
                    Text("Pay Dues Now / बकाया जमा करें")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLoanDuesAlert = false }) {
                    Text("OK / समझ गया")
                }
            }
        )
    }
}

// 4. Member Notifications View
@Composable
fun MemberNotificationsView(
    notifications: List<com.example.data.model.NotificationEntity>,
    onMarkAsRead: (Long) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onPayDuesClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Notifications / सूचनाएँ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "सोसाइटी द्वारा भेजे गए संदेश, रिमाइंडर और सूचनाएँ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (unreadCount > 0) {
                Surface(
                    color = GullakDanger.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GullakDanger.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$unreadCount New",
                        color = GullakDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (unreadCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onMarkAllAsRead) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = GullakSuccessBright)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark All as Read / सभी पढ़े", fontSize = 12.sp, color = GullakSuccessBright)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Abhi koi notification nahi hai.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notif ->
                    val (icon, iconTint, borderClr, bgClr) = when (notif.type) {
                        com.example.data.model.NotificationType.PAYMENT_APPROVAL ->
                            listOf(Icons.Default.CheckCircle, GullakSuccess, GullakSuccess.copy(alpha = 0.3f), GullakSuccess.copy(alpha = 0.08f))
                        com.example.data.model.NotificationType.PAYMENT_REJECTION ->
                            listOf(Icons.Default.Error, GullakDanger, GullakDanger.copy(alpha = 0.3f), GullakDanger.copy(alpha = 0.08f))
                        com.example.data.model.NotificationType.DUES_REMINDER ->
                            listOf(Icons.Default.AccessTime, GullakWarning, GullakWarning.copy(alpha = 0.3f), GullakWarning.copy(alpha = 0.08f))
                        com.example.data.model.NotificationType.ANNOUNCEMENT ->
                            listOf(Icons.Default.Campaign, GullakPrimary, GullakPrimary.copy(alpha = 0.3f), GullakPrimary.copy(alpha = 0.08f))
                        else ->
                            listOf(Icons.Default.Info, GullakGold, GullakGold.copy(alpha = 0.3f), GullakGold.copy(alpha = 0.08f))
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) (bgClr as Color) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (!notif.isRead) (borderClr as Color) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!notif.isRead) onMarkAsRead(notif.id)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                                        contentDescription = null,
                                        tint = iconTint as Color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (!notif.isRead) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!notif.isRead) {
                                        Surface(
                                            color = GullakPrimary,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "NEW",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = dateFormat.format(Date(notif.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// Member App Open Popup Notifications Modal Dialog
@Composable
fun MemberNotificationsPopupDialog(
    notifications: List<com.example.data.model.NotificationEntity>,
    unreadCount: Int,
    currentRdDue: Double,
    currentInterestDue: Double,
    totalDue: Double,
    onDismiss: () -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onPayDuesClick: () -> Unit,
    onViewAllNoticesClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val displayNotifications = notifications.take(4)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GullakPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = GullakPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "सूचनाएँ व अपडेट्स",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Society Notices & Alerts",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                if (unreadCount > 0) {
                    Surface(
                        color = GullakDanger,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "$unreadCount NEW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 1: Active Dues Banner if any
                if (totalDue > 0.0) {
                    Surface(
                        color = Color(0xFF2D1B1F),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GullakDanger.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = GullakDanger,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "कुल बकाया: ${formatRupees(totalDue)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GullakDanger
                                    )
                                }
                                Text(
                                    text = "RD: ₹${currentRdDue.toInt()} • ब्याज: ₹${currentInterestDue.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                            Button(
                                onClick = onPayDuesClick,
                                colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 2: Recent / Unread Notifications
                if (displayNotifications.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GullakSuccess, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aapka account bilkul up-to-date hai!",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Koi naya pending notice ya alert nahi hai.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayNotifications) { notif ->
                            val (icon, iconTint, bgClr, borderClr) = when (notif.type) {
                                com.example.data.model.NotificationType.PAYMENT_APPROVAL ->
                                    listOf(Icons.Default.CheckCircle, GullakSuccess, GullakSuccess.copy(alpha = 0.08f), GullakSuccess.copy(alpha = 0.4f))
                                com.example.data.model.NotificationType.PAYMENT_REJECTION ->
                                    listOf(Icons.Default.Error, GullakDanger, GullakDanger.copy(alpha = 0.08f), GullakDanger.copy(alpha = 0.4f))
                                com.example.data.model.NotificationType.DUES_REMINDER ->
                                    listOf(Icons.Default.AccessTime, GullakWarning, GullakWarning.copy(alpha = 0.08f), GullakWarning.copy(alpha = 0.4f))
                                com.example.data.model.NotificationType.ANNOUNCEMENT ->
                                    listOf(Icons.Default.Campaign, GullakPrimary, GullakPrimary.copy(alpha = 0.08f), GullakPrimary.copy(alpha = 0.4f))
                                else ->
                                    listOf(Icons.Default.Info, GullakGold, GullakGold.copy(alpha = 0.08f), GullakGold.copy(alpha = 0.4f))
                            }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) (bgClr as Color) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = BorderStroke(1.dp, if (!notif.isRead) (borderClr as Color) else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!notif.isRead) onMarkAsRead(notif.id)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Icon(
                                                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                                                contentDescription = null,
                                                tint = iconTint as Color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = notif.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (!notif.isRead) {
                                            Surface(
                                                color = GullakPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "NEW",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateFormat.format(Date(notif.createdAt)),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (unreadCount > 0) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text("Mark Read", fontSize = 12.sp, color = GullakSuccessBright)
                    }
                }
                if (notifications.size > 2) {
                    TextButton(onClick = onViewAllNoticesClick) {
                        Text("All Notices", fontSize = 12.sp, color = GullakPrimary)
                    }
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK / समझ गया")
                }
            }
        },
        dismissButton = null
    )
}

// 5. Member Profile View
@Composable
fun MemberProfileView(
    user: UserEntity,
    financials: com.example.data.model.MemberFinancialEntity?,
    adminMobile: String,
    onPayDuesClick: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val joinDateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.US) }
    val formattedJoinDate = remember(user.createdAt) { joinDateFormat.format(Date(user.createdAt)) }

    val loanOutstanding = financials?.loanOutstanding ?: 0.0
    val interestDue = financials?.interestDue ?: 0.0
    val totalLoanDues = loanOutstanding + interestDue
    val hasLoanDues = (loanOutstanding > 0.0 || interestDue > 0.0)
    val configuredEligibility = financials?.loanEligibility ?: 50000.0
    val effectiveEligibility = if (hasLoanDues) 0.0 else configuredEligibility

    var showProfileLoanDuesAlert by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(GullakPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Member Account • ${user.userId}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileRow(label = "Member Name", value = user.name)
                    ProfileRow(label = "User ID", value = user.userId)
                    ProfileRow(label = "Mobile Number", value = user.mobile)
                    ProfileRow(label = "Account Status", value = user.status.name)
                    ProfileRow(label = "Joining Date", value = formattedJoinDate)
                    ProfileRow(label = "Security PIN", value = "•••• (${user.pin.length} digits)")
                }
            }
        }

        // Loan Eligibility Card in Profile
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasLoanDues) Color(0xFF2D1B1F) else GullakGoldContainer
                ),
                border = BorderStroke(1.2.dp, if (hasLoanDues) GullakDanger.copy(alpha = 0.6f) else GullakGold.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = if (hasLoanDues) GullakDanger else GullakGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Loan Eligibility (लोन पात्रता)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasLoanDues) GullakDanger else GullakGoldLight
                                )
                            )
                        }

                        Surface(
                            color = if (hasLoanDues) GullakDanger.copy(alpha = 0.2f) else GullakSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (hasLoanDues) "Dues: ₹${totalLoanDues.toInt()}" else "Eligible ✅",
                                color = if (hasLoanDues) GullakDanger else GullakSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formatRupees(effectiveEligibility),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasLoanDues) GullakDanger else GullakGoldLight
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (hasLoanDues) {
                        Text(
                            text = "⚠️ आपके खाते में पुराना लोन बकाया (₹${loanOutstanding.toInt()}) + ब्याज (₹${interestDue.toInt()}) होने के कारण लोन पात्रता स्वतः ₹0 है।",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFB4AB))
                        )
                    } else {
                        Text(
                            text = "✅ कोई सक्रिय लोन बकाया नहीं है। आप ₹${formatRupees(effectiveEligibility)} तक के नए लोन के लिए पात्र हैं। Final approval Admin द्वारा तय होगा।",
                            style = MaterialTheme.typography.bodySmall.copy(color = GullakGoldLight.copy(alpha = 0.85f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (hasLoanDues) {
                                showProfileLoanDuesAlert = true
                            } else {
                                openWhatsApp(context, adminMobile, "Namaste Admin, main ₹${effectiveEligibility.toInt()} tak ke naye loan ke liye apply karna chahta hoon.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasLoanDues) GullakDanger else GullakPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_loan_eligibility_action_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (hasLoanDues) Icons.Default.Info else Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasLoanDues) "Check Loan Dues Alert (बकाया विवरण देखें)" else "Apply for Loan / लोन आवेदन करें",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GullakGold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "सुरक्षा कारणों से संवेदनशील वित्तीय जानकारी व लोन पात्रता केवल Admin द्वारा अपडेट की जा सकती है।",
                        style = MaterialTheme.typography.bodySmall.copy(color = GullakTextSecondary)
                    )
                }
            }
        }

        // Section: Advance Settings (Includes Secure Logout)
        item {
            var showMemberLogoutConfirm by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, GullakDanger.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GullakGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚙️ Advance Settings (एडवांस सेटिंग्स)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GullakGoldLight
                        )
                    }

                    Text(
                        text = "Member account session security. App se logout karne ke liye neeche diye gaye button ka upyog karein.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showMemberLogoutConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GullakDanger),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_advance_logout_btn")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOGOUT / सुरक्षित लॉगआउट करें 🔒", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (showMemberLogoutConfirm) {
                AlertDialog(
                    onDismissRequest = { showMemberLogoutConfirm = false },
                    title = { Text("Logout Confirmation 🔒", fontWeight = FontWeight.Bold) },
                    text = { Text("Kya aap Gullak account se logout karna chahte hain?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showMemberLogoutConfirm = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GullakDanger)
                        ) {
                            Text("Haan, Logout Karein")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showMemberLogoutConfirm = false }) {
                            Text("Nahi")
                        }
                    }
                )
            }
        }
    }

    if (showProfileLoanDuesAlert) {
        AlertDialog(
            onDismissRequest = { showProfileLoanDuesAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GullakDanger)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⚠️ Loan Eligibility Alert / पात्रता शून्य (₹0)", fontWeight = FontWeight.Bold, color = GullakDanger)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Aapka pehle se loan balance baki hone ke karan aapki Loan Eligibility abhi ₹0 hai.",
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan Outstanding:", fontSize = 13.sp, color = Color.Gray)
                                Text("₹${loanOutstanding.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Interest Due:", fontSize = 13.sp, color = Color.Gray)
                                Text("₹${interestDue.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GullakDanger)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Loan Dues:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹${totalLoanDues.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GullakDanger)
                            }
                        }
                    }
                    Text(
                        text = "Naya loan tabhi approve ho sakega jab purana loan poora chukta ho jaye.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileLoanDuesAlert = false
                        onPayDuesClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess)
                ) {
                    Text("Pay Dues Now / बकाया जमा करें")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showProfileLoanDuesAlert = false }) {
                    Text("OK / समझ गया")
                }
            }
        )
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
