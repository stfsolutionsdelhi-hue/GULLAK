package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Member
import com.example.data.SocietyRepository
import com.example.ui.theme.*

@Composable
fun PaymentsScreen(
    repository: SocietyRepository,
    highlightTxnId: String? = null,
    onClearHighlight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val allApprovals by repository.pendingApprovals.collectAsState()
    val approvals = remember(allApprovals) {
        allApprovals.filter { it.status.equals("PENDING", ignoreCase = true) }
    }

    var showCollectDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<com.example.data.Payment?>(null) }
    var deletingPayment by remember { mutableStateOf<com.example.data.Payment?>(null) }

    LaunchedEffect(highlightTxnId, payments) {
        if (highlightTxnId != null) {
            val target = payments.find { it.txnId == highlightTxnId }
            if (target != null) {
                editingPayment = target
            }
            onClearHighlight()
        }
    }

    // Calculation Totals
    val totalCollectedThisMonth = payments.sumOf { it.totalAmount }
    val totalRd = payments.sumOf { it.rdAmount }
    val totalInterest = payments.sumOf { it.interestAmount }
    val totalPenalty = payments.sumOf { it.penaltyAmount }
    val totalLoanRepay = payments.sumOf { it.loanRepayAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("💳 RD & LOAN COLLECTION", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Auto-calculated breakdown with penalty & waiver", color = TextSecondary, fontSize = 12.sp)
                }
                Button(
                    onClick = { showCollectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Receive", tint = Color(0xFF064E3B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receive ₹", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Section: Pending Member Online Approvals (if any)
        if (approvals.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, AccentGold, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = "Approvals", tint = AccentGold, modifier = Modifier.size(18.dp))
                                Text("⏳ Pending Online Approvals (${approvals.size})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        approvals.forEach { app ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(app.memberName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("ID: ${app.memberId} • 📱 ${app.mobile}", color = TextSecondary, fontSize = 10.sp)
                                        }
                                        Text("₹${app.totalAmount}", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }

                                    Text("UTR: ${app.utrNumber} • ${app.date}", color = TextMuted, fontSize = 10.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                repository.approvePaymentRequest(app.id)
                                                Toast.makeText(context, "Payment of ₹${app.totalAmount} Approved for ${app.memberName}!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Approve ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                repository.rejectPaymentRequest(app.id)
                                                Toast.makeText(context, "Payment Rejected", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentRed))
                                        ) {
                                            Text("Reject ❌", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collection Stats Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryGreenDark, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL RECENT COLLECTION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹$totalCollectedThisMonth", color = PrimaryGreen, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0B1120))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("${payments.size} Receipts", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    // Breakdown Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("RD:", color = TextSecondary, fontSize = 11.sp)
                            Text("₹$totalRd", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Interest:", color = TextSecondary, fontSize = 11.sp)
                            Text("₹$totalInterest", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Penalty:", color = TextSecondary, fontSize = 11.sp)
                            Text("₹$totalPenalty", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Loan Repay:", color = TextSecondary, fontSize = 11.sp)
                            Text("₹$totalLoanRepay", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section Title: Payment Receipts
        item {
            Text(
                "🧾 Collection Receipts History (${payments.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (payments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = "Receipts", tint = TextMuted, modifier = Modifier.size(36.dp))
                        Text("No payment receipts recorded yet.", color = TextSecondary, fontSize = 13.sp)
                        Button(
                            onClick = { showCollectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Collect First Payment", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(payments, key = { it.txnId }) { pay ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pay.memberName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${pay.txnId} • ${pay.date}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${pay.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (pay.mode.contains("UPI")) Color(0xFF0369A1).copy(alpha = 0.3f) else Color(0xFF064E3B).copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(pay.mode, color = if (pay.mode.contains("UPI")) AccentBlue else PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Breakdown row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardDark,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RD: ₹${pay.rdAmount}", color = TextSecondary, fontSize = 11.sp)
                                Text("Int: ₹${pay.interestAmount}", color = TextSecondary, fontSize = 11.sp)
                                if (pay.penaltyAmount > 0) {
                                    Text("Pen: ₹${pay.penaltyAmount}", color = AccentRed, fontSize = 11.sp)
                                }
                                if (pay.loanRepayAmount > 0) {
                                    Text("Loan: ₹${pay.loanRepayAmount}", color = AccentBlue, fontSize = 11.sp)
                                }
                                if (pay.waiverAmount > 0) {
                                    Text("Waiver: -₹${pay.waiverAmount}", color = PrimaryGreen, fontSize = 11.sp)
                                }
                            }
                        }

                        if (pay.remarks.isNotEmpty()) {
                            Text("Note: ${pay.remarks}", color = TextMuted, fontSize = 11.sp)
                        }

                        // Markup Badge and Edit / Delete Actions (User Request 11)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Markup Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (pay.isEdited) Color(0xFF3B0764) else Color(0xFF064E3B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (pay.isEdited) Color(0xFFA855F7) else PrimaryGreen)
                            ) {
                                Text(
                                    text = if (pay.isEdited) "APPROVED WITH EDITED ✏️" else "APPROVED ✅",
                                    color = if (pay.isEdited) Color(0xFFE9D5FF) else PrimaryGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Edit & Delete Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { editingPayment = pay },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentBlue, modifier = Modifier.size(12.dp))
                                        Text("Edit", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    onClick = { deletingPayment = pay },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF3B0712).copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(12.dp))
                                        Text("Delete", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================== COLLECT PAYMENT DIALOG (User Request 3 & 7) ==================
    if (showCollectDialog) {
        var selectedMember by remember { mutableStateOf<Member?>(null) }
        var showMemberPhoneBookPicker by remember { mutableStateOf(false) }
        var memberSearchQuery by remember { mutableStateOf("") }

        // Form Fields (Blank / 0 until a member is chosen)
        var rdText by remember { mutableStateOf("0") }
        var interestText by remember { mutableStateOf("0") }
        var penaltyText by remember { mutableStateOf("0") }
        var loanRepayText by remember { mutableStateOf("0") }
        var waiverText by remember { mutableStateOf("0") }
        var paymentMode by remember { mutableStateOf("CASH") }
        var remarksText by remember { mutableStateOf("") }
        var utrText by remember { mutableStateOf("") }

        // Function to update auto-fill when member changes
        fun updateMemberSelection(m: Member) {
            selectedMember = m
            rdText = m.monthlyRd.toString()
            val activeLoan = m.gullakLoan + m.emergencyLoan
            interestText = if (activeLoan > 0) (activeLoan * 0.01).toInt().toString() else "0"
            // Directly from Web App database (NO local counting)
            penaltyText = m.penaltyApplicable.toString()
            loanRepayText = "0"
            waiverText = "0"
        }

        val totalCalculated = remember(rdText, interestText, penaltyText, loanRepayText, waiverText) {
            val r = rdText.toIntOrNull() ?: 0
            val i = interestText.toIntOrNull() ?: 0
            val p = penaltyText.toIntOrNull() ?: 0
            val l = loanRepayText.toIntOrNull() ?: 0
            val w = waiverText.toIntOrNull() ?: 0
            ((r + i + p + l) - w).coerceAtLeast(0)
        }

        // ================= MEMBER BOOK SEARCH PICKER DIALOG (User Request 2) =================
        if (showMemberPhoneBookPicker) {
            val filteredPhoneBook = remember(members, memberSearchQuery) {
                if (memberSearchQuery.isBlank()) {
                    members.sortedBy { it.name.lowercase() }
                } else {
                    members.filter {
                        it.name.contains(memberSearchQuery, ignoreCase = true) ||
                        it.mobile.contains(memberSearchQuery) ||
                        it.id.contains(memberSearchQuery, ignoreCase = true)
                    }.sortedBy { it.name.lowercase() }
                }
            }

            AlertDialog(
                onDismissRequest = { showMemberPhoneBookPicker = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Contacts, contentDescription = "Member Book", tint = AccentGold)
                        Text("Member Book 📇", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = memberSearchQuery,
                            onValueChange = { memberSearchQuery = it },
                            placeholder = { Text("Search name, mobile, or ID...", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = AccentGold, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (memberSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { memberSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text(
                            "Total ${filteredPhoneBook.size} members found • Tap to select:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredPhoneBook, key = { it.id }) { m ->
                                val initials = m.name.trim().split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .joinToString("")
                                    .uppercase()

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            updateMemberSelection(m)
                                            showMemberPhoneBookPicker = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedMember?.id == m.id) Color(0xFF1E293B) else Color(0xFF0F172A)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selectedMember?.id == m.id) PrimaryGreen else CardBorder
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Phonebook Avatar Circle
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (selectedMember?.id == m.id) PrimaryGreen else Color(0xFF334155)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initials.ifEmpty { "G" },
                                                color = if (selectedMember?.id == m.id) Color(0xFF064E3B) else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(m.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("📱 ${m.mobile} • ID: ${m.id}", color = TextSecondary, fontSize = 11.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("RD: ₹${m.monthlyRd}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            val activeLoan = m.gullakLoan + m.emergencyLoan
                                            if (activeLoan > 0) {
                                                Text("Loan: ₹$activeLoan", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMemberPhoneBookPicker = false }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = CardDark,
                shape = RoundedCornerShape(12.dp)
            )
        }

        AlertDialog(
            onDismissRequest = { showCollectDialog = false },
            title = {
                Text("Receive RD & Loan Payment", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Society Member:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    // Phone Book Selection Card (Phone book style search button)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                memberSearchQuery = ""
                                showMemberPhoneBookPicker = true
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (selectedMember != null) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedMember!!.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "📱 ${selectedMember!!.mobile} • ID: ${selectedMember!!.id}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                Text("Choose Member...", color = TextMuted, fontSize = 12.sp)
                            }

                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Contacts, contentDescription = "Search", tint = AccentGold, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (selectedMember == null) "Select 📇" else "Change 📇",
                                        color = AccentGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Auto-fill Indicator
                    Text("⚡ Auto-filled Columns (Penalty, Waiver, RD, Interest)", color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

                    // RD Input
                    OutlinedTextField(
                        value = rdText,
                        onValueChange = { rdText = it },
                        label = { Text("RD Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Interest Input
                    OutlinedTextField(
                        value = interestText,
                        onValueChange = { interestText = it },
                        label = { Text("Interest Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Penalty Input
                    OutlinedTextField(
                        value = penaltyText,
                        onValueChange = { penaltyText = it },
                        label = { Text("Penalty Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Loan Repay Input
                    OutlinedTextField(
                        value = loanRepayText,
                        onValueChange = { loanRepayText = it },
                        label = { Text("Loan Repay Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Waiver Input
                    OutlinedTextField(
                        value = waiverText,
                        onValueChange = { waiverText = it },
                        label = { Text("Waiver / Discount Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Payment Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mode:", color = TextSecondary, fontSize = 12.sp)
                        listOf("CASH", "ONLINE / UPI").forEach { mode ->
                            FilterChip(
                                selected = paymentMode == mode,
                                onClick = { paymentMode = mode },
                                label = { Text(mode, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (paymentMode.contains("UPI")) {
                        OutlinedTextField(
                            value = utrText,
                            onValueChange = { utrText = it },
                            label = { Text("UTR / UPI Ref Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Remarks (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Total Calculation Preview Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0B1120),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreenDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount Due:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₹$totalCalculated", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = selectedMember
                        if (m != null) {
                            repository.recordPayment(
                                memberId = m.id,
                                memberName = m.name,
                                mobile = m.mobile,
                                rdAmount = rdText.toIntOrNull() ?: 400,
                                interestAmount = interestText.toIntOrNull() ?: 0,
                                penaltyAmount = penaltyText.toIntOrNull() ?: 0,
                                loanRepayAmount = loanRepayText.toIntOrNull() ?: 0,
                                waiverAmount = waiverText.toIntOrNull() ?: 0,
                                mode = paymentMode,
                                remarks = remarksText,
                                utrNumber = utrText
                            )
                            Toast.makeText(context, "Payment of ₹$totalCalculated received for ${m.name}!", Toast.LENGTH_SHORT).show()
                            showCollectDialog = false
                        } else {
                            Toast.makeText(context, "Please select a member first from Member Book.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save & Issue Receipt 🧾", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== EDIT PAYMENT DIALOG (User Request 11) ==================
    editingPayment?.let { pay ->
        var editRd by remember(pay) { mutableStateOf(pay.rdAmount.toString()) }
        var editInterest by remember(pay) { mutableStateOf(pay.interestAmount.toString()) }
        var editPenalty by remember(pay) { mutableStateOf(pay.penaltyAmount.toString()) }
        var editLoanRepay by remember(pay) { mutableStateOf(pay.loanRepayAmount.toString()) }
        var editWaiver by remember(pay) { mutableStateOf(pay.waiverAmount.toString()) }
        var editMode by remember(pay) { mutableStateOf(pay.mode) }
        var editRemarks by remember(pay) { mutableStateOf(pay.remarks) }
        var editUtr by remember(pay) { mutableStateOf(pay.utrNumber) }

        val editTotal = remember(editRd, editInterest, editPenalty, editLoanRepay, editWaiver) {
            val r = editRd.toIntOrNull() ?: 0
            val i = editInterest.toIntOrNull() ?: 0
            val p = editPenalty.toIntOrNull() ?: 0
            val l = editLoanRepay.toIntOrNull() ?: 0
            val w = editWaiver.toIntOrNull() ?: 0
            ((r + i + p + l) - w).coerceAtLeast(0)
        }

        AlertDialog(
            onDismissRequest = { editingPayment = null },
            title = {
                Text("✏️ Edit Receipt (${pay.txnId})", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Member: ${pay.memberName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editRd,
                            onValueChange = { editRd = it },
                            label = { Text("RD (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editInterest,
                            onValueChange = { editInterest = it },
                            label = { Text("Interest (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editPenalty,
                            onValueChange = { editPenalty = it },
                            label = { Text("Penalty (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editLoanRepay,
                            onValueChange = { editLoanRepay = it },
                            label = { Text("Loan Repay (₹)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = editWaiver,
                        onValueChange = { editWaiver = it },
                        label = { Text("Waiver (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mode:", color = TextSecondary, fontSize = 11.sp)
                        listOf("CASH", "ONLINE / UPI").forEach { m ->
                            FilterChip(
                                selected = editMode == m,
                                onClick = { editMode = m },
                                label = { Text(m, fontSize = 10.sp) }
                            )
                        }
                    }

                    if (editMode.contains("UPI")) {
                        OutlinedTextField(
                            value = editUtr,
                            onValueChange = { editUtr = it },
                            label = { Text("UTR Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = editRemarks,
                        onValueChange = { editRemarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0B1120),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Updated Total:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("₹$editTotal", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.editPayment(
                            txnId = pay.txnId,
                            newRd = editRd.toIntOrNull() ?: 0,
                            newInterest = editInterest.toIntOrNull() ?: 0,
                            newPenalty = editPenalty.toIntOrNull() ?: 0,
                            newLoanRepay = editLoanRepay.toIntOrNull() ?: 0,
                            newWaiver = editWaiver.toIntOrNull() ?: 0,
                            newMode = editMode,
                            newRemarks = editRemarks,
                            newUtr = editUtr
                        )
                        editingPayment = null
                        Toast.makeText(context, "Receipt ${pay.txnId} Updated Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Save Changes", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPayment = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DELETE PAYMENT CONFIRMATION DIALOG (User Request 11) ==================
    deletingPayment?.let { pay ->
        AlertDialog(
            onDismissRequest = { deletingPayment = null },
            title = {
                Text("🗑️ Delete Payment Receipt?", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Aap ${pay.memberName} ki receipt (${pay.txnId}) delete karne ja rahe hain.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF181512),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Amount: ₹${pay.totalAmount} (${pay.mode})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Date: ${pay.date}", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                    Text(
                        "⚠️ Note: Isse member ke pending dues aur loan balances restore ho jayenge.",
                        color = AccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deletePayment(pay.txnId)
                        deletingPayment = null
                        Toast.makeText(context, "Receipt ${pay.txnId} Deleted & Balances Restored!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPayment = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
