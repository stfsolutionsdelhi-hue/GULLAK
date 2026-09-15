package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.data.PaymentApproval
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import java.net.URLEncoder

@Composable
fun TasksScreen(
    repository: SocietyRepository,
    onNavigateToPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val pendingApprovals by repository.pendingApprovals.collectAsState()
    val isLiveSyncActive by repository.isLiveSyncActive.collectAsState()

    // Member dialog state
    var selectedMemberForDetails by remember { mutableStateOf<Member?>(null) }

    // Approval dialogs state
    var editingApproval by remember { mutableStateOf<PaymentApproval?>(null) }
    var rejectingApproval by remember { mutableStateOf<PaymentApproval?>(null) }
    var showSimulateMemberDialog by remember { mutableStateOf(false) }

    // Dashboard Quick Overview Member Search
    var overviewSearchQuery by remember { mutableStateOf("") }

    val filteredOverviewMembers = remember(members, overviewSearchQuery) {
        val q = overviewSearchQuery.trim().lowercase()
        if (q.isBlank()) {
            members
        } else {
            val qDigits = q.filter { it.isDigit() }
            members.filter { m ->
                m.name.lowercase().contains(q) ||
                m.id.lowercase().contains(q) ||
                m.mobile.contains(q) ||
                (qDigits.isNotEmpty() && m.mobile.filter { it.isDigit() }.contains(qDigits)) ||
                m.address.lowercase().contains(q) ||
                m.nominee.lowercase().contains(q)
            }
        }
    }

    val totalRdCollected = payments.sumOf { it.rdAmount } + (members.size * 4800)
    val totalLoansOutstanding = members.sumOf { it.gullakLoan + it.emergencyLoan }
    val availableFund = totalRdCollected - totalLoansOutstanding + 45000

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header & Live Active / Pause Switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🏦 GULLAK SOCIETY", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Co-operative Society Accounting", color = TextSecondary, fontSize = 12.sp)
                }
                // Live Sync Status Toggle Button
                Surface(
                    onClick = {
                        val state = repository.toggleLiveSync()
                        val msg = if (state) "Live Sync is ACTIVE" else "Live Sync is PAUSED"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLiveSyncActive) PrimaryGreenDark else Color(0xFF451A03),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLiveSyncActive) PrimaryGreen else AccentGold
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveSyncActive) PrimaryGreen else AccentGold)
                        )
                        Text(
                            text = if (isLiveSyncActive) "LIVE ACTIVE" else "SYNC PAUSED",
                            color = if (isLiveSyncActive) PrimaryGreen else AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Summary Metric Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Members Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL MEMBERS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${members.size}", color = AccentGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Registered in Society", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Available Fund Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("AVAILABLE FUND", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹${availableFund / 1000}k", color = PrimaryGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Cash & Bank Liquid", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Section: Member Payment Approval System (Task Item 9 & 10)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "📥 Member Payment Approvals",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (pendingApprovals.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentRed)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${pendingApprovals.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { showSimulateMemberDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+ Member Pay", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (pendingApprovals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "All Done", tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                        Text(
                            "No pending payment approvals! All member submissions are up-to-date.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(pendingApprovals, key = { it.id }) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, if (req.mode.contains("UPI")) AccentBlue.copy(alpha = 0.5f) else AccentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.memberName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("📱 ${req.mobile} • ${req.date}", color = TextSecondary, fontSize = 11.sp)
                                if (req.utrNumber.isNotEmpty()) {
                                    Text("UTR: ${req.utrNumber}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${req.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (req.mode.contains("UPI")) Color(0xFF0369A1).copy(alpha = 0.3f) else Color(0xFF78350F).copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(req.mode, color = if (req.mode.contains("UPI")) AccentBlue else AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Breakdown row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0B1120),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RD: ₹${req.requestedRd}", color = TextSecondary, fontSize = 11.sp)
                                Text("Int: ₹${req.requestedInterest}", color = TextSecondary, fontSize = 11.sp)
                                if (req.requestedPenalty > 0) {
                                    Text("Pen: ₹${req.requestedPenalty}", color = AccentRed, fontSize = 11.sp)
                                }
                                if (req.requestedLoanRepay > 0) {
                                    Text("Loan Repay: ₹${req.requestedLoanRepay}", color = AccentBlue, fontSize = 11.sp)
                                }
                                if (req.waiver > 0) {
                                    Text("Waiver: -₹${req.waiver}", color = PrimaryGreen, fontSize = 11.sp)
                                }
                            }
                        }

                        // Approval Action Buttons (Approve, Edit & Approve, Reject)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Approve Button
                            Button(
                                onClick = {
                                    repository.approvePayment(req.id)
                                    Toast.makeText(context, "Payment from ${req.memberName} Approved!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF064E3B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // 2. Edit & Approve Button
                            OutlinedButton(
                                onClick = { editingApproval = req },
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Edit & OK", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // 3. Reject Button
                            OutlinedButton(
                                onClick = { rejectingApproval = req },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Reject", tint = AccentRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Reject", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryGreenDark, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("⚡ Quick RD / Loan Collection", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Receive cash or online payment immediately", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onNavigateToPayments,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Collect ₹", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section Title: Member Overview with Search Box (Requirement 1)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "👥 Society Members Quick Overview",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${filteredOverviewMembers.size} / ${members.size} Members",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Member Search Box
                OutlinedTextField(
                    value = overviewSearchQuery,
                    onValueChange = { overviewSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search member by name, mobile, address, ID...", color = TextMuted, fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (overviewSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { overviewSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )
                )
            }
        }

        if (filteredOverviewMembers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = "Not Found", tint = TextMuted, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "No member found matching '$overviewSearchQuery'",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(if (overviewSearchQuery.isBlank()) filteredOverviewMembers.take(20) else filteredOverviewMembers, key = { it.id }) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            // User Request: Tapping on member opens full details & action sheet!
                            selectedMemberForDetails = member
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CardDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1),
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text(member.name, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("📱 ${member.mobile} • Due: ${member.dueDay}", color = TextSecondary, fontSize = 11.sp)
                                if (member.address.isNotBlank()) {
                                    Text("📍 ${member.address}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("RD ₹${member.monthlyRd}/mo", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (member.gullakLoan > 0 || member.emergencyLoan > 0) {
                                Text("Loan ₹${member.gullakLoan + member.emergencyLoan}", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ================== DIALOGS ==================

    // 1. Member Full Profile & Actions Dialog (User Request 5)
    selectedMemberForDetails?.let { m ->
        AlertDialog(
            onDismissRequest = { selectedMemberForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(m.name.take(1), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(m.name, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("ID: ${m.id}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = CardBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mobile Number:", color = TextMuted, fontSize = 12.sp)
                        Text(m.mobile, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Address:", color = TextMuted, fontSize = 12.sp)
                        Text(m.address.ifEmpty { "Kakrola, Delhi" }, color = TextPrimary, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nominee:", color = TextMuted, fontSize = 12.sp)
                        Text(m.nominee.ifEmpty { "Self / Nominee" }, color = TextPrimary, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monthly RD:", color = TextMuted, fontSize = 12.sp)
                        Text("₹${m.monthlyRd} / month", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Opening RD Fund:", color = TextMuted, fontSize = 12.sp)
                        Text("₹${m.openingRd}", color = AccentBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gullak Loan Balance:", color = TextMuted, fontSize = 12.sp)
                        Text("₹${m.gullakLoan}", color = if (m.gullakLoan > 0) AccentRed else TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Emergency Loan Balance:", color = TextMuted, fontSize = 12.sp)
                        Text("₹${m.emergencyLoan}", color = if (m.emergencyLoan > 0) AccentRed else TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Due Day:", color = TextMuted, fontSize = 12.sp)
                        Text(m.dueDay, color = AccentGold, fontSize = 12.sp)
                    }
                    HorizontalDivider(color = CardBorder)

                    // Quick Actions Row
                    Text("Direct Member Contact:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Call
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${m.mobile}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("📞 Call", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // SMS
                        Button(
                            onClick = {
                                val smsText = "Namaste ${m.name} Ji, Gullak Society ki monthly RD (₹${m.monthlyRd}) ka payment reminder hai. Kripya samay par jama karein."
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${m.mobile}")).apply {
                                    putExtra("sms_body", smsText)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("💬 SMS", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // WhatsApp
                        Button(
                            onClick = {
                                val msg = "Namaste ${m.name} Ji, Gullak Society RD ₹${m.monthlyRd} due reminder."
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=91${m.mobile}&text=${URLEncoder.encode(msg, "UTF-8")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("🟢 WA", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedMemberForDetails = null
                        onNavigateToPayments()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Collect Payment ₹", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForDetails = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 2. Edit & Approve Dialog
    editingApproval?.let { req ->
        var editRd by remember { mutableStateOf(req.requestedRd.toString()) }
        var editIntr by remember { mutableStateOf(req.requestedInterest.toString()) }
        var editPen by remember { mutableStateOf(req.requestedPenalty.toString()) }
        var editLoanRepay by remember { mutableStateOf(req.requestedLoanRepay.toString()) }
        var editWaiver by remember { mutableStateOf(req.waiver.toString()) }

        val calcTotal = remember(editRd, editIntr, editPen, editLoanRepay, editWaiver) {
            val r = editRd.toIntOrNull() ?: 0
            val i = editIntr.toIntOrNull() ?: 0
            val p = editPen.toIntOrNull() ?: 0
            val l = editLoanRepay.toIntOrNull() ?: 0
            val w = editWaiver.toIntOrNull() ?: 0
            ((r + i + p + l) - w).coerceAtLeast(0)
        }

        AlertDialog(
            onDismissRequest = { editingApproval = null },
            title = {
                Text("Edit & Approve Payment", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Member: ${req.memberName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("UTR / Ref: ${req.utrNumber}", color = TextSecondary, fontSize = 11.sp)

                    OutlinedTextField(
                        value = editRd,
                        onValueChange = { editRd = it },
                        label = { Text("RD Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editIntr,
                        onValueChange = { editIntr = it },
                        label = { Text("Interest (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPen,
                        onValueChange = { editPen = it },
                        label = { Text("Penalty / Late Fee (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editLoanRepay,
                        onValueChange = { editLoanRepay = it },
                        label = { Text("Loan Repayment Principal (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editWaiver,
                        onValueChange = { editWaiver = it },
                        label = { Text("Waiver / Discount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0B1120),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable:", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("₹$calcTotal", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.approvePayment(
                            approvalId = req.id,
                            customizedRd = editRd.toIntOrNull() ?: 0,
                            customizedIntr = editIntr.toIntOrNull() ?: 0,
                            customizedPen = editPen.toIntOrNull() ?: 0,
                            customizedLoanRepay = editLoanRepay.toIntOrNull() ?: 0,
                            customizedWaiver = editWaiver.toIntOrNull() ?: 0
                        )
                        Toast.makeText(context, "Payment Edited & Approved Successfully!", Toast.LENGTH_SHORT).show()
                        editingApproval = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Confirm & Approve ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingApproval = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 3. Reject with Templates Dialog (User Request 10)
    rejectingApproval?.let { req ->
        val presetTemplates = listOf(
            "UTR / Transaction ID invalid or not received in society account.",
            "Incorrect payment amount transferred by member.",
            "Duplicate payment receipt submission.",
            "Late payment without required penalty fee.",
            "Member bank transaction failed/reversed."
        )
        var selectedTemplate by remember { mutableStateOf(presetTemplates[0]) }
        var customReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { rejectingApproval = null },
            title = {
                Text("Reject Payment Receipt", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Rejection Reason for ${req.memberName}:", color = TextSecondary, fontSize = 12.sp)

                    presetTemplates.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTemplate == t) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color.Transparent)
                                .clickable { selectedTemplate = t }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedTemplate == t,
                                onClick = { selectedTemplate = t },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                            )
                            Text(t, color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Or enter custom reason...", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (customReason.trim().isNotEmpty()) customReason.trim() else selectedTemplate
                        repository.rejectPayment(req.id, finalReason)
                        Toast.makeText(context, "Payment Rejected & Member Notified.", Toast.LENGTH_SHORT).show()
                        rejectingApproval = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Confirm Reject ❌", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingApproval = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 4. Simulate Member Online Payment Dialog
    if (showSimulateMemberDialog) {
        var simMember by remember { mutableStateOf(members.firstOrNull()) }
        var simRd by remember { mutableStateOf("400") }
        var simIntr by remember { mutableStateOf("0") }
        var simPen by remember { mutableStateOf("0") }
        var simLoan by remember { mutableStateOf("0") }
        var simUtr by remember { mutableStateOf("UPI/${System.currentTimeMillis() % 1000000}") }
        var simExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSimulateMemberDialog = false },
            title = {
                Text("Simulate Member Payment Submission", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Member Submitting Payment:", color = TextSecondary, fontSize = 12.sp)
                    Box {
                        OutlinedButton(
                            onClick = { simExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(simMember?.name ?: "Select Member", color = TextPrimary, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = simExpanded,
                            onDismissRequest = { simExpanded = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name, color = TextPrimary, fontSize = 12.sp) },
                                    onClick = {
                                        simMember = m
                                        simRd = m.monthlyRd.toString()
                                        simIntr = if (m.gullakLoan > 0 || m.emergencyLoan > 0) ((m.gullakLoan + m.emergencyLoan) * 0.01).toInt().toString() else "0"
                                        simExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = simRd, onValueChange = { simRd = it }, label = { Text("RD (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = simIntr, onValueChange = { simIntr = it }, label = { Text("Interest (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = simLoan, onValueChange = { simLoan = it }, label = { Text("Loan Repayment (₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = simUtr, onValueChange = { simUtr = it }, label = { Text("UTR / Transaction ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = simMember
                        if (m != null) {
                            repository.submitMemberPayment(
                                memberId = m.id,
                                memberName = m.name,
                                mobile = m.mobile,
                                rd = simRd.toIntOrNull() ?: 400,
                                interest = simIntr.toIntOrNull() ?: 0,
                                penalty = simPen.toIntOrNull() ?: 0,
                                loanRepay = simLoan.toIntOrNull() ?: 0,
                                waiver = 0,
                                mode = "ONLINE / UPI",
                                utr = simUtr
                            )
                            Toast.makeText(context, "Payment submitted for approval!", Toast.LENGTH_SHORT).show()
                            showSimulateMemberDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Submit for Approval 📲", color = Color(0xFF060913), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulateMemberDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
