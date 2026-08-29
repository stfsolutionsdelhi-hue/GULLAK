package com.example.ui.screens.member

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                onNotificationClick = { selectedTab = 3 },
                onLogoutClick = { viewModel.logout() }
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
                    loanEligibility = loanEligibility,
                    recentPayments = payments.take(3),
                    onPayNowClick = { showComprehensivePayDialog = true },
                    onViewAllHistory = { selectedTab = 1 },
                    onViewLoanDetails = { selectedTab = 2 }
                )
                1 -> MemberHistoryView(payments = payments)
                2 -> MemberLoanView(
                    financials = financials,
                    onOpenWhatsApp = {
                        openWhatsApp(context, settings?.adminMobile ?: "9876543210", "Gullak Society Loan Jankari:")
                    }
                )
                3 -> MemberNotificationsView(notifications = notifications)
                4 -> MemberProfileView(
                    user = user,
                    adminMobile = settings?.adminMobile ?: "9876543210",
                    onLogout = { viewModel.logout() }
                )
            }
        }
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
                                        color = Color(0xFF78350F)
                                    )
                                )
                                Text(
                                    text = "सोसाइटी में Cash जमा करके Request भेजें",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F).copy(alpha = 0.8f))
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
                                    style = MaterialTheme.typography.bodySmall.copy(color = GullakNavyDark.copy(alpha = 0.8f))
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
    loanEligibility: Double,
    recentPayments: List<com.example.data.model.PaymentEntity>,
    onPayNowClick: () -> Unit,
    onViewAllHistory: () -> Unit,
    onViewLoanDetails: () -> Unit
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
                            color = if (currentInterestDue > 0) GullakDanger else GullakNavyDark
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
                        Text("Loan", fontWeight = FontWeight.Bold, color = GullakNavyDark, fontSize = 14.sp)
                        Text("Outstanding", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatRupees(loanOutstanding),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GullakNavyDark
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

        // Loan Eligibility Banner Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GullakGoldContainer),
                border = BorderStroke(1.dp, GullakGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = GullakClay,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "आपकी Loan Eligibility: ${formatRupees(loanEligibility)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        )
                        Text(
                            text = "Final Loan Approval Society के नियमों के आधार पर Admin द्वारा तय होगा।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF78350F).copy(alpha = 0.85f)
                            )
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

                StatusBadge(status = payment.status)
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
                            color = GullakNavyDark
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
    onOpenWhatsApp: () -> Unit
) {
    val loanOutstanding = financials?.loanOutstanding ?: 0.0
    val interestDue = financials?.interestDue ?: 0.0
    val loanEligibility = financials?.loanEligibility ?: 50000.0
    val lastMonthEnd = financials?.lastMonthEndBalance ?: loanOutstanding

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
                        text = "3. Loan Eligibility Member Edit नहीं कर सकता। यह केवल Admin द्वारा सेट की जाती है।",
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
                colors = CardDefaults.cardColors(containerColor = GullakGoldContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "आपकी Loan Eligibility: ${formatRupees(loanEligibility)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "नया लोन प्राप्त करने के लिए कृपया Admin से WhatsApp या ऑफलाइन संपर्क करें।",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F).copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Contact Admin on WhatsApp")
                    }
                }
            }
        }
    }
}

// 4. Member Notifications View
@Composable
fun MemberNotificationsView(
    notifications: List<com.example.data.model.NotificationEntity>
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notifications / सूचनाएँ",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "सोसाइटी द्वारा भेजे गए संदेश, रिमाइंडर और सूचनाएँ",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Abhi koi notification nahi hai.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notif ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) GullakPrimary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GullakPrimary
                                )
                                Text(
                                    text = dateFormat.format(Date(notif.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
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

// 5. Member Profile View
@Composable
fun MemberProfileView(
    user: UserEntity,
    adminMobile: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val joinDateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.US) }
    val formattedJoinDate = remember(user.createdAt) { joinDateFormat.format(Date(user.createdAt)) }

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

        item {
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GullakPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "सुरक्षा कारणों से संवेदनशील वित्तीय जानकारी केवल Admin द्वारा बदली जा सकती है।",
                        style = MaterialTheme.typography.bodySmall.copy(color = GullakNavyDark)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    openWhatsApp(context, adminMobile, "Namaste Admin, mera Gullak app PIN help:")
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with Admin on WhatsApp")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GullakDanger),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LOGOUT / लॉगआउट करें", fontWeight = FontWeight.Bold)
            }
        }
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
