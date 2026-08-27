package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UserEntity
import com.example.data.repository.MemberWithFinancials
import com.example.ui.components.AccountStatusBadge
import com.example.ui.components.ApproveWithEditDialog
import com.example.ui.components.GullakConfirmationDialog
import com.example.ui.components.GullakTopBar
import com.example.ui.components.RejectPaymentDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupees
import com.example.ui.components.openWhatsApp
import com.example.ui.theme.GullakClay
import com.example.ui.theme.GullakDanger
import com.example.ui.theme.GullakDangerContainer
import com.example.ui.theme.GullakGold
import com.example.ui.theme.GullakGoldContainer
import com.example.ui.theme.GullakInfo
import com.example.ui.theme.GullakInfoContainer
import com.example.ui.theme.GullakNavyDark
import com.example.ui.theme.GullakPrimary
import com.example.ui.theme.GullakPrimaryLight
import com.example.ui.theme.GullakSuccess
import com.example.ui.theme.GullakSuccessContainer
import com.example.ui.theme.GullakWarning
import com.example.ui.theme.GullakWarningContainer
import com.example.ui.viewmodel.GullakViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminMainScreen(
    adminUser: UserEntity,
    viewModel: GullakViewModel,
    modifier: Modifier = Modifier
) {
    var selectedNavTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingPayments by viewModel.pendingPayments.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val membersWithFinancials by viewModel.membersWithFinancials.collectAsStateWithLifecycle()
    val totalMembers by viewModel.totalMembers.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeMemberCount.collectAsStateWithLifecycle()
    val inactiveCount by viewModel.inactiveMemberCount.collectAsStateWithLifecycle()
    val todayCollection by viewModel.todayCollection.collectAsStateWithLifecycle()
    val monthlyCollection by viewModel.monthlyCollection.collectAsStateWithLifecycle()
    val totalLoanOutstanding by viewModel.totalLoanOutstanding.collectAsStateWithLifecycle()
    val totalDueAmount by viewModel.totalDueAmount.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val importSummary by viewModel.importSummary.collectAsStateWithLifecycle()

    val userMsg by viewModel.userMessage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Dialog States
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<MemberWithFinancials?>(null) }
    var memberToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var memberForPinReset by remember { mutableStateOf<UserEntity?>(null) }
    var paymentToApprove by remember { mutableStateOf<PaymentEntity?>(null) }
    var paymentToReject by remember { mutableStateOf<PaymentEntity?>(null) }
    var paymentToApproveEdit by remember { mutableStateOf<PaymentEntity?>(null) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GullakTopBar(
                title = "GULLAK ADMIN PANEL",
                subtitle = "Administrator • ${adminUser.name}",
                onLogoutClick = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = (selectedNavTab == 0),
                    onClick = { selectedNavTab = 0 },
                    icon = {
                        Box {
                            Icon(Icons.Default.Home, contentDescription = "Dashboard")
                            if (pendingPayments.isNotEmpty()) {
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
                    label = { Text("Tasks / होम") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_admin_tasks")
                )
                NavigationBarItem(
                    selected = (selectedNavTab == 1),
                    onClick = { selectedNavTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Members") },
                    label = { Text("Members") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_admin_members")
                )
                NavigationBarItem(
                    selected = (selectedNavTab == 2),
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Payments") },
                    label = { Text("Payments") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_admin_payments")
                )
                NavigationBarItem(
                    selected = (selectedNavTab == 3),
                    onClick = { selectedNavTab = 3 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Remind") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_admin_notif")
                )
                NavigationBarItem(
                    selected = (selectedNavTab == 4),
                    onClick = { selectedNavTab = 4 },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Excel & Settings") },
                    label = { Text("Excel/Set") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GullakPrimary,
                        selectedTextColor = GullakPrimary,
                        indicatorColor = GullakPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_admin_excel")
                )
            }
        },
        floatingActionButton = {
            if (selectedNavTab == 1) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = GullakPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_member_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedNavTab) {
                0 -> AdminDashboardTab(
                    pendingPayments = pendingPayments,
                    totalMembers = totalMembers,
                    activeCount = activeCount,
                    inactiveCount = inactiveCount,
                    todayCollection = todayCollection ?: 0.0,
                    monthlyCollection = monthlyCollection ?: 0.0,
                    totalLoanOutstanding = totalLoanOutstanding ?: 0.0,
                    totalDueAmount = totalDueAmount ?: 0.0,
                    onApproveClick = { paymentToApprove = it },
                    onRejectClick = { paymentToReject = it },
                    onApproveEditClick = { paymentToApproveEdit = it },
                    onNavigateToMembers = { selectedNavTab = 1 },
                    onNavigateToPayments = { selectedNavTab = 2 }
                )
                1 -> AdminMembersTab(
                    membersWithFinancials = membersWithFinancials,
                    onAddMemberClick = { showAddMemberDialog = true },
                    onEditMemberClick = { memberToEdit = it },
                    onDeleteMemberClick = { memberToDelete = it.user },
                    onResetPinClick = { memberForPinReset = it.user },
                    onToggleStatusClick = { member ->
                        viewModel.toggleMemberStatus(member.user.userId, member.user.status)
                    }
                )
                2 -> AdminPaymentsTab(
                    allPayments = allPayments,
                    onApproveClick = { paymentToApprove = it },
                    onRejectClick = { paymentToReject = it },
                    onApproveEditClick = { paymentToApproveEdit = it }
                )
                3 -> AdminNotificationsTab(
                    settings = settings,
                    onSendToAll = { title, msg -> viewModel.sendNotificationToAll(title, msg) },
                    onTriggerReminders = { template -> viewModel.triggerDuesReminders(template) }
                )
                4 -> AdminExcelAndSettingsTab(
                    viewModel = viewModel,
                    settings = settings,
                    allAuditLogs = allAuditLogs,
                    onUpdateSettings = { newSettings -> viewModel.updateSocietySettings(newSettings) },
                    onApplyYearEndBonus = { viewModel.applyYearEndBonusAdjustment() }
                )
            }
        }
    }

    // Modal Dialogs for Admin Actions
    if (paymentToApprove != null) {
        GullakConfirmationDialog(
            title = "Approve Payment? / भुगतान स्वीकृत करें?",
            message = "Member: ${paymentToApprove!!.userName}\nAmount: ${formatRupees(paymentToApprove!!.amount)}\nType: ${paymentToApprove!!.paymentType.name}\n\nक्या आप इस Payment को Approve करना चाहते हैं?",
            confirmButtonText = "APPROVE / स्वीकृत करें",
            onConfirm = {
                viewModel.approvePayment(paymentToApprove!!.id)
                paymentToApprove = null
            },
            onDismiss = { paymentToApprove = null }
        )
    }

    if (paymentToReject != null) {
        RejectPaymentDialog(
            memberName = paymentToReject!!.userName,
            amount = paymentToReject!!.amount,
            onDismiss = { paymentToReject = null },
            onConfirmReject = { reason ->
                viewModel.rejectPayment(paymentToReject!!.id, reason)
                paymentToReject = null
            }
        )
    }

    if (paymentToApproveEdit != null) {
        ApproveWithEditDialog(
            memberName = paymentToApproveEdit!!.userName,
            originalAmount = paymentToApproveEdit!!.amount,
            onDismiss = { paymentToApproveEdit = null },
            onConfirmApproveEdit = { editedAmount ->
                viewModel.approvePaymentWithEdit(paymentToApproveEdit!!.id, editedAmount)
                paymentToApproveEdit = null
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirmAdd = { name, mobile, pin, initialRd, loan, eligibility, status, remarks ->
                viewModel.addMember(name, mobile, pin, initialRd, loan, eligibility, status, remarks)
                showAddMemberDialog = false
            }
        )
    }

    if (memberToEdit != null) {
        EditMemberDialog(
            member = memberToEdit!!,
            onDismiss = { memberToEdit = null },
            onConfirmSave = { name, mobile, rd, loan, eligibility, status, remarks ->
                viewModel.updateMember(memberToEdit!!.user.userId, name, mobile, rd, loan, eligibility, status, remarks)
                memberToEdit = null
            }
        )
    }

    if (memberToDelete != null) {
        GullakConfirmationDialog(
            title = "Permanent Delete Member? / सदस्य हटाएं?",
            message = "क्या आप '${memberToDelete!!.name}' (${memberToDelete!!.userId}) को Permanently Delete करना चाहते हैं? यह Action वापस नहीं किया जा सकता।",
            confirmButtonText = "Delete Permanently",
            isDanger = true,
            onConfirm = {
                viewModel.deleteMemberPermanently(memberToDelete!!.userId)
                memberToDelete = null
            },
            onDismiss = { memberToDelete = null }
        )
    }

    if (memberForPinReset != null) {
        ResetPinDialog(
            member = memberForPinReset!!,
            onDismiss = { memberForPinReset = null },
            onConfirmPin = { newPin ->
                viewModel.resetMemberPin(memberForPinReset!!.userId, newPin)
                memberForPinReset = null
            }
        )
    }
}

// 1. Admin Dashboard Tab (Today's Tasks & Metrics)
@Composable
fun AdminDashboardTab(
    pendingPayments: List<PaymentEntity>,
    totalMembers: Int,
    activeCount: Int,
    inactiveCount: Int,
    todayCollection: Double,
    monthlyCollection: Double,
    totalLoanOutstanding: Double,
    totalDueAmount: Double,
    onApproveClick: (PaymentEntity) -> Unit,
    onRejectClick: (PaymentEntity) -> Unit,
    onApproveEditClick: (PaymentEntity) -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToPayments: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Today's Tasks
        item {
            Text(
                text = "TODAY'S TASKS / आज के मुख्य कार्य",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = GullakPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = if (pendingPayments.isNotEmpty()) GullakWarningContainer else GullakSuccessContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (pendingPayments.isNotEmpty()) "${pendingPayments.size} Payments Pending Approval" else "All Payments Processed! ✅",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (pendingPayments.isNotEmpty()) Color(0xFF78350F) else GullakSuccess
                            )
                        )
                        Text(
                            text = "$activeCount Active Members • Total Dues: ${formatRupees(totalDueAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                        )
                    }
                    if (pendingPayments.isNotEmpty()) {
                        Surface(
                            color = GullakDanger,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${pendingPayments.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Pending Payments Queue
        if (pendingPayments.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Approvals Queue (${pendingPayments.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(pendingPayments) { payment ->
                AdminPendingPaymentCard(
                    payment = payment,
                    onApprove = { onApproveClick(payment) },
                    onReject = { onRejectClick(payment) },
                    onApproveEdit = { onApproveEditClick(payment) }
                )
            }
        }

        // Section: Financial & Society Metrics Overview Cards
        item {
            Text(
                text = "Society Overview & Collections",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today Collection
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Today's Collection", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupees(todayCollection),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = GullakSuccess
                            )
                        }
                    }

                    // Monthly Collection
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Monthly Collection", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupees(monthlyCollection),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = GullakPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Loan Outstanding
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Loan Outstanding", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = formatRupees(totalLoanOutstanding),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = GullakNavyDark
                            )
                        }
                    }

                    // Total Members
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToMembers() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Members", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "$totalMembers ($activeCount Active)",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = GullakClay
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AdminPendingPaymentCard(
    payment: PaymentEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onApproveEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, GullakWarning),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.userName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "User ID: ${payment.userId} • Mobile: ${payment.userMobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = if (payment.paymentType == PaymentType.ONLINE) GullakPrimary.copy(alpha = 0.1f) else GullakGoldContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = payment.paymentType.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (payment.paymentType == PaymentType.ONLINE) GullakPrimary else GullakClay,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatRupees(payment.amount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GullakPrimary
                        )
                    )
                    Text(
                        text = dateFormat.format(Date(payment.paymentDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (payment.remarks.isNotBlank()) {
                    Text(
                        text = "Remarks: ${payment.remarks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Three Required Master Buttons: APPROVE, REJECT, APPROVE WITH EDIT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("admin_approve_btn_${payment.id}")
                ) {
                    Text("APPROVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onApproveEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GullakInfo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_edit_approve_btn_${payment.id}")
                ) {
                    Text("EDIT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GullakDanger),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_reject_btn_${payment.id}")
                ) {
                    Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// 2. Admin Members Management Tab
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminMembersTab(
    membersWithFinancials: List<MemberWithFinancials>,
    onAddMemberClick: () -> Unit,
    onEditMemberClick: (MemberWithFinancials) -> Unit,
    onDeleteMemberClick: (MemberWithFinancials) -> Unit,
    onResetPinClick: (MemberWithFinancials) -> Unit,
    onToggleStatusClick: (MemberWithFinancials) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(membersWithFinancials, searchQuery, selectedFilter) {
        membersWithFinancials.filter { item ->
            val matchQuery = item.user.name.contains(searchQuery, ignoreCase = true) ||
                    item.user.mobile.contains(searchQuery) ||
                    item.user.userId.contains(searchQuery, ignoreCase = true)

            val matchFilter = when (selectedFilter) {
                "ACTIVE" -> item.user.status == AccountStatus.ACTIVE
                "INACTIVE" -> item.user.status == AccountStatus.INACTIVE
                "DUES" -> ((item.financials?.currentRdDue ?: 0.0) + (item.financials?.interestDue ?: 0.0)) > 0
                else -> true
            }
            matchQuery && matchFilter
        }
    }

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
                    text = "Member Directory (${membersWithFinancials.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "सोसाइटी के सभी सदस्य और उनका वित्तीय खाता",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onAddMemberClick,
                colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_member_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Member", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Name, Mobile, User ID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("member_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = (selectedFilter == "ALL"),
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${membersWithFinancials.size})") }
            )
            FilterChip(
                selected = (selectedFilter == "ACTIVE"),
                onClick = { selectedFilter = "ACTIVE" },
                label = { Text("Active") }
            )
            FilterChip(
                selected = (selectedFilter == "INACTIVE"),
                onClick = { selectedFilter = "INACTIVE" },
                label = { Text("Inactive") }
            )
            FilterChip(
                selected = (selectedFilter == "DUES"),
                onClick = { selectedFilter = "DUES" },
                label = { Text("Has Dues") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Koi member match nahi hua.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { item ->
                    AdminMemberCard(
                        member = item,
                        onEdit = { onEditMemberClick(item) },
                        onDelete = { onDeleteMemberClick(item) },
                        onResetPin = { onResetPinClick(item) },
                        onToggleStatus = { onToggleStatusClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMemberCard(
    member: MemberWithFinancials,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPin: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val user = member.user
    val fin = member.financials
    val totalDue = (fin?.currentRdDue ?: 0.0) + (fin?.interestDue ?: 0.0)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AccountStatusBadge(status = user.status)
                    }
                    Text(
                        text = "${user.userId} • Mobile: ${user.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Actions", tint = GullakPrimary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Member & Loan") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (user.status == AccountStatus.ACTIVE) "Mark Inactive (रिमाइंडर बंद)" else "Activate Member") },
                            onClick = {
                                showMenu = false
                                onToggleStatus()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset PIN (${user.pin})") },
                            onClick = {
                                showMenu = false
                                onResetPin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Permanent Delete", color = GullakDanger) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Quick Row: RD Due, Interest Due, Loan Outstanding, Loan Eligibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("RD Due", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        formatRupees(fin?.currentRdDue ?: 0.0),
                        fontWeight = FontWeight.Bold,
                        color = if ((fin?.currentRdDue ?: 0.0) > 0) GullakDanger else GullakSuccess
                    )
                }
                Column {
                    Text("Interest Due", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        formatRupees(fin?.interestDue ?: 0.0),
                        fontWeight = FontWeight.Bold,
                        color = if ((fin?.interestDue ?: 0.0) > 0) GullakDanger else GullakNavyDark
                    )
                }
                Column {
                    Text("Loan Out", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        formatRupees(fin?.loanOutstanding ?: 0.0),
                        fontWeight = FontWeight.Bold,
                        color = GullakNavyDark
                    )
                }
                Column {
                    Text("Eligibility", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        formatRupees(fin?.loanEligibility ?: 50000.0),
                        fontWeight = FontWeight.Bold,
                        color = GullakGold
                    )
                }
            }
        }
    }
}

// 3. Admin Payments Tab
@Composable
fun AdminPaymentsTab(
    allPayments: List<PaymentEntity>,
    onApproveClick: (PaymentEntity) -> Unit,
    onRejectClick: (PaymentEntity) -> Unit,
    onApproveEditClick: (PaymentEntity) -> Unit
) {
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredPayments = remember(allPayments, filterStatus) {
        if (filterStatus == "ALL") allPayments else allPayments.filter { it.status.name == filterStatus }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Payment Records / भुगतान प्रबंधन",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "सभी सदस्यों के Cash और UPI Online भुगतानों का लेखा-जोखा",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        ScrollableTabRow(
            selectedTabIndex = when (filterStatus) {
                "PENDING" -> 1
                "APPROVED" -> 2
                "REJECTED" -> 3
                else -> 0
            },
            edgePadding = 0.dp
        ) {
            Tab(selected = filterStatus == "ALL", onClick = { filterStatus = "ALL" }, text = { Text("All (${allPayments.size})") })
            Tab(selected = filterStatus == "PENDING", onClick = { filterStatus = "PENDING" }, text = { Text("Pending") })
            Tab(selected = filterStatus == "APPROVED", onClick = { filterStatus = "APPROVED" }, text = { Text("Approved") })
            Tab(selected = filterStatus == "REJECTED", onClick = { filterStatus = "REJECTED" }, text = { Text("Rejected") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredPayments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Koi payment nahi mili.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPayments) { payment ->
                    if (payment.status == PaymentStatus.PENDING) {
                        AdminPendingPaymentCard(
                            payment = payment,
                            onApprove = { onApproveClick(payment) },
                            onReject = { onRejectClick(payment) },
                            onApproveEdit = { onApproveEditClick(payment) }
                        )
                    } else {
                        com.example.ui.screens.member.PaymentItemCard(payment = payment)
                    }
                }
            }
        }
    }
}

// 4. Admin Notifications & Reminders Tab
@Composable
fun AdminNotificationsTab(
    settings: SocietySettingsEntity?,
    onSendToAll: (title: String, message: String) -> Unit,
    onTriggerReminders: (customTemplate: String?) -> Unit
) {
    val template1 = settings?.template1 ?: "Aapka society ka amount dues hai. Kripya payment kar dein."
    val template2 = settings?.template2 ?: "Aapka monthly RD/payment pending hai. Kripya samay par payment karein."
    val template3 = settings?.template3 ?: "Reminder: Aapka society payment abhi pending hai."

    var customTitle by remember { mutableStateOf("Society Announcement / आवश्यक सूचना") }
    var messageText by remember { mutableStateOf(template1) }
    var selectedTemplateIndex by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Notifications & Reminders / सूचना व रिमाइंडर",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Payment reminder केवल Dues/Pending Active सदस्यों को जाएगा। Approved व Inactive को नहीं।",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Quick Trigger Reminders Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GullakGoldContainer),
                border = BorderStroke(1.dp, GullakGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⏰ Scheduled Reminder Engine",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Frequency: ${settings?.reminderFrequencyPerDay ?: 2} times/day (${settings?.reminderTimes ?: "09:00 AM, 06:00 PM"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F).copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onTriggerReminders(messageText) },
                        colors = ButtonDefaults.buttonColors(containerColor = GullakGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_reminders_btn")
                    ) {
                        Text("Trigger Payment Reminders to Pending Members Now 🔔")
                    }
                }
            }
        }

        // Notification Composer Card
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
                    Text("Select Template / संदेश चुनें:", fontWeight = FontWeight.Bold)

                    // Template 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateIndex = 1
                                messageText = template1
                            }
                    ) {
                        RadioButton(
                            selected = (selectedTemplateIndex == 1),
                            onClick = {
                                selectedTemplateIndex = 1
                                messageText = template1
                            }
                        )
                        Text(text = "Template 1: $template1", style = MaterialTheme.typography.bodySmall)
                    }

                    // Template 2
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateIndex = 2
                                messageText = template2
                            }
                    ) {
                        RadioButton(
                            selected = (selectedTemplateIndex == 2),
                            onClick = {
                                selectedTemplateIndex = 2
                                messageText = template2
                            }
                        )
                        Text(text = "Template 2: $template2", style = MaterialTheme.typography.bodySmall)
                    }

                    // Template 3
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplateIndex = 3
                                messageText = template3
                            }
                    ) {
                        RadioButton(
                            selected = (selectedTemplateIndex == 3),
                            onClick = {
                                selectedTemplateIndex = 3
                                messageText = template3
                            }
                        )
                        Text(text = "Template 3: $template3", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Notification Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                            selectedTemplateIndex = 0
                        },
                        label = { Text("Message / संदेश लिखें") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendToAll(customTitle, messageText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_to_all_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SEND TO ALL (सभी Active सदस्यों को भेजें)")
                    }
                }
            }
        }
    }
}

// 5. Admin Excel, Settings & Audit Tab
@Composable
fun AdminExcelAndSettingsTab(
    viewModel: GullakViewModel,
    settings: SocietySettingsEntity?,
    allAuditLogs: List<AuditLogEntity>,
    onUpdateSettings: (SocietySettingsEntity) -> Unit,
    onApplyYearEndBonus: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var csvInputText by remember { mutableStateOf("") }
    val importSummary by viewModel.importSummary.collectAsStateWithLifecycle()

    var societyName by remember(settings) { mutableStateOf(settings?.societyName ?: "GULLAK CO OPRATIVE SOCIETY") }
    var monthlyRd by remember(settings) { mutableStateOf((settings?.defaultMonthlyRd ?: 400.0).toInt().toString()) }
    var loanRate by remember(settings) { mutableStateOf((settings?.defaultLoanInterestRate ?: 1.0).toString()) }
    var upiId by remember(settings) { mutableStateOf(settings?.upiId ?: "gullaksociety@okaxis") }
    var payeeName by remember(settings) { mutableStateOf(settings?.upiPayeeName ?: "Gullak Co-operative Society") }
    var adminMobile by remember(settings) { mutableStateOf(settings?.adminMobile ?: "9876543210") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Excel Integration & Society Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Excel VBA Ledger Import/Export & Society Master Parameters",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Section: Excel VBA Integration
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📊 Excel VBA Ledger Integration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Columns: Name, Mobile Number, Payment Info, Amount, Date, Remarks, Transaction ID, Payment Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = GullakPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val csv = viewModel.repository.generateExcelCsvData()
                                    clipboardManager.setText(AnnotatedString(csv))
                                    Toast
                                        .makeText(context, "Excel CSV copied to clipboard!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_btn")
                        ) {
                            Text("Export Excel CSV 📋")
                        }

                        Button(
                            onClick = {
                                // Sample Ledger CSV Template to demo multi-import
                                csvInputText = """
Name,Mobile Number,Payment Info,Amount,Date,Remarks,Transaction ID,Payment Status
Kavita Singh,9810055555,CASH,400.0,27-08-2026 10:00,Monthly RD,TXN-20260827-890123,APPROVED
Manish Gupta,9810066666,ONLINE,1200.0,27-08-2026 11:30,RD + Loan,TXN-20260827-890124,APPROVED
Rahul Kumar,9810011111,CASH,400.0,27-08-2026 12:00,August RD,TXN-20260827-000001,APPROVED
""".trimIndent()
                                viewModel.previewCsvImport(csvInputText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GullakClay),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("load_sample_csv_btn")
                        ) {
                            Text("Load Sample CSV 📥")
                        }
                    }

                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = {
                            csvInputText = it
                            if (it.isNotBlank()) viewModel.previewCsvImport(it)
                        },
                        label = { Text("Paste Excel CSV Data here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("excel_csv_input"),
                        minLines = 3
                    )

                    // Import Summary Conflict Review Card
                    if (importSummary != null) {
                        Surface(
                            color = GullakInfoContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Import Summary Review:", fontWeight = FontWeight.Bold, color = GullakInfo)
                                Text("New Members: ${importSummary!!.newRecords}")
                                Text("Updated Members: ${importSummary!!.updatedRecords}")
                                Text("Duplicate Records (Skipped): ${importSummary!!.duplicateRecords}")
                                Text("Errors: ${importSummary!!.errors}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.executeCsvImport(csvInputText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Confirm & Execute Import ✅")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Society Settings Form
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚙️ Society Master Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = societyName,
                        onValueChange = { societyName = it },
                        label = { Text("Society Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = monthlyRd,
                            onValueChange = { monthlyRd = it },
                            label = { Text("Default RD (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = loanRate,
                            onValueChange = { loanRate = it },
                            label = { Text("Loan Rate (%/Mo)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Society UPI ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payeeName,
                        onValueChange = { payeeName = it },
                        label = { Text("UPI Payee Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adminMobile,
                        onValueChange = { adminMobile = it },
                        label = { Text("Admin WhatsApp / Support Mobile") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updated = (settings ?: SocietySettingsEntity()).copy(
                                societyName = societyName,
                                defaultMonthlyRd = monthlyRd.toDoubleOrNull() ?: 400.0,
                                defaultLoanInterestRate = loanRate.toDoubleOrNull() ?: 1.0,
                                upiId = upiId,
                                upiPayeeName = payeeName,
                                adminMobile = adminMobile
                            )
                            onUpdateSettings(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_society_settings_btn")
                    ) {
                        Text("Save Society Settings")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Year End Bonus Adjustment Action
                    OutlinedButton(
                        onClick = onApplyYearEndBonus,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GullakClay),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Year-End (31 Dec) 1% RD Bonus Adjustment 🎁")
                    }
                }
            }
        }

        // Section: Audit Logs
        item {
            Text(
                text = "System Audit Logs (${allAuditLogs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(allAuditLogs.take(15)) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GullakPrimary)
                        )
                        Text(
                            text = SimpleDateFormat("dd-MM HH:mm", Locale.US).format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Add Member Modal Dialog
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (
        name: String,
        mobile: String,
        pin: String,
        initialRd: Double,
        loan: Double,
        eligibility: Double,
        status: AccountStatus,
        remarks: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var initialRd by remember { mutableStateOf("400") }
    var loanOutstanding by remember { mutableStateOf("0") }
    var loanEligibility by remember { mutableStateOf("50000") }
    var remarks by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("+ Add Member / नया सदस्य जोड़ें", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Member Name (नाम)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_member_name_input")
                    )
                }
                item {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { if (it.length <= 10) mobile = it },
                        label = { Text("Mobile Number (मोबाइल)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_member_mobile_input")
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("Login PIN") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = initialRd,
                            onValueChange = { initialRd = it },
                            label = { Text("Monthly RD (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = loanOutstanding,
                            onValueChange = { loanOutstanding = it },
                            label = { Text("Loan (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = loanEligibility,
                            onValueChange = { loanEligibility = it },
                            label = { Text("Eligibility (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (विवरण)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMsg != null) {
                    item {
                        Text(text = errorMsg!!, color = GullakDanger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || mobile.length < 10) {
                        errorMsg = "Kripya valid name aur 10-digit mobile dalein"
                    } else {
                        onConfirmAdd(
                            name.trim(),
                            mobile.trim(),
                            pin.trim(),
                            initialRd.toDoubleOrNull() ?: 400.0,
                            loanOutstanding.toDoubleOrNull() ?: 0.0,
                            loanEligibility.toDoubleOrNull() ?: 50000.0,
                            AccountStatus.ACTIVE,
                            remarks.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                modifier = Modifier.testTag("add_member_submit_btn")
            ) {
                Text("Create Member")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Edit Member Modal Dialog
@Composable
fun EditMemberDialog(
    member: MemberWithFinancials,
    onDismiss: () -> Unit,
    onConfirmSave: (
        name: String,
        mobile: String,
        rd: Double,
        loan: Double,
        eligibility: Double,
        status: AccountStatus,
        remarks: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(member.user.name) }
    var mobile by remember { mutableStateOf(member.user.mobile) }
    var rd by remember { mutableStateOf((member.financials?.rdAmount ?: 400.0).toInt().toString()) }
    var loan by remember { mutableStateOf((member.financials?.loanOutstanding ?: 0.0).toInt().toString()) }
    var eligibility by remember { mutableStateOf((member.financials?.loanEligibility ?: 50000.0).toInt().toString()) }
    var status by remember { mutableStateOf(member.user.status) }
    var remarks by remember { mutableStateOf(member.user.remarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Member (${member.user.userId})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rd,
                            onValueChange = { rd = it },
                            label = { Text("RD (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = loan,
                            onValueChange = { loan = it },
                            label = { Text("Loan (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = eligibility,
                        onValueChange = { eligibility = it },
                        label = { Text("Loan Eligibility (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Status: ", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = (status == AccountStatus.ACTIVE),
                            onClick = { status = AccountStatus.ACTIVE },
                            label = { Text("Active") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = (status == AccountStatus.INACTIVE),
                            onClick = { status = AccountStatus.INACTIVE },
                            label = { Text("Inactive") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSave(
                        name.trim(),
                        mobile.trim(),
                        rd.toDoubleOrNull() ?: 400.0,
                        loan.toDoubleOrNull() ?: 0.0,
                        eligibility.toDoubleOrNull() ?: 50000.0,
                        status,
                        remarks.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Reset PIN Dialog
@Composable
fun ResetPinDialog(
    member: UserEntity,
    onDismiss: () -> Unit,
    onConfirmPin: (newPin: String) -> Unit
) {
    var pinText by remember { mutableStateOf(member.pin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset PIN for ${member.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Member ID: ${member.userId} • Mobile: ${member.mobile}")
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 6) pinText = it },
                    label = { Text("New 4-digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinText.isNotBlank()) onConfirmPin(pinText.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary)
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
