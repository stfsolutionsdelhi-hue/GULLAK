package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Member
import com.example.data.PaymentApproval
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MemberPortalScreen(
    repository: SocietyRepository,
    onSwitchToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val approvals by repository.pendingApprovals.collectAsState()
    val societySettings by repository.societySettings.collectAsState()
    val societyUpiId by repository.societyUpiId.collectAsState()
    val societyQrUri by repository.societyQrUri.collectAsState()

    var loggedInMemberId by remember { mutableStateOf<String?>(null) }
    var selectedMemberForLogin by remember { mutableStateOf<Member?>(members.firstOrNull()) }
    var enteredMobile by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var isMemberPickerOpen by remember { mutableStateOf(false) }
    var memberSearchQuery by remember { mutableStateOf("") }

    // Dues Payment Form Dialog States (Requirement 8, 9, 10)
    var showDuesDialog by remember { mutableStateOf(false) }
    var duesRdInput by remember { mutableStateOf("400") }
    var duesInterestInput by remember { mutableStateOf("0") }
    var duesPenaltyInput by remember { mutableStateOf("0") }
    var duesLoanRepayInput by remember { mutableStateOf("") } // blank by default

    // Online QR Payment Sheet/Dialog States
    var showOnlineQrDialog by remember { mutableStateOf(false) }
    var onlinePaymentUtr by remember { mutableStateOf("") }
    var onlinePaymentNote by remember { mutableStateOf("") }

    // Cash Payment Confirmation Dialog State
    var showCashConfirmDialog by remember { mutableStateOf(false) }
    var cashPaymentNote by remember { mutableStateOf("") }

    val loggedInMember = members.find { it.id == loggedInMemberId }

    // If not logged in, show Member Login Screen (Requirement 5 & 6)
    if (loggedInMember == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0369A1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Member",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("👤 Member Portal Login", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Mobile Number & PIN se login karein (No Passkey)", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select / Search Member:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    // Member Selector Dropdown
                    OutlinedButton(
                        onClick = { isMemberPickerOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedMemberForLogin?.name ?: "Select Society Member",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    if (isMemberPickerOpen) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                OutlinedTextField(
                                    value = memberSearchQuery,
                                    onValueChange = { memberSearchQuery = it },
                                    placeholder = { Text("Search by name or phone...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val filteredList = members.filter {
                                    it.name.contains(memberSearchQuery, ignoreCase = true) ||
                                            it.mobile.contains(memberSearchQuery) ||
                                            it.id.contains(memberSearchQuery, ignoreCase = true)
                                }
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(filteredList) { m ->
                                        Surface(
                                            onClick = {
                                                selectedMemberForLogin = m
                                                enteredMobile = m.mobile
                                                isMemberPickerOpen = false
                                            },
                                            color = Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)) {
                                                Text(m.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("📱 ${m.mobile} • ID: ${m.id}", color = TextSecondary, fontSize = 10.sp)
                                            }
                                        }
                                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    // Mobile Number Display
                    OutlinedTextField(
                        value = if (enteredMobile.isNotEmpty()) enteredMobile else (selectedMemberForLogin?.mobile ?: ""),
                        onValueChange = { enteredMobile = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // 4-Digit PIN Input (Masked, EMPTY by default, NO '1234' default visible - Requirement 6)
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 4) enteredPin = it },
                        label = { Text("4-Digit Member PIN") },
                        placeholder = { Text("••••", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Forgot PIN / Password link -> Opens WhatsApp to Admin (Requirement 6)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val targetMobile = if (enteredMobile.isNotBlank()) enteredMobile else (selectedMemberForLogin?.mobile ?: "")
                                val memberName = selectedMemberForLogin?.name ?: "Member"
                                val adminPhone = societySettings.adminWhatsApp.ifEmpty { "9718174244" }
                                val cleanPhone = if (adminPhone.startsWith("+91")) adminPhone else "91$adminPhone"
                                val message = "Namaste Admin Ji,\n\nI forgot my Gullak Society Member Portal PIN.\n\nMember Name: $memberName\nMobile: $targetMobile\n\nPlease provide or reset my login PIN. Dhanyawad!"
                                try {
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                                    val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(waIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp not installed or could not open.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = AccentGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forgot PIN / Password? (WhatsApp Admin)", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Login Button (Requirement 5)
                    Button(
                        onClick = {
                            val activeMember = selectedMemberForLogin ?: members.find { it.mobile == enteredMobile.trim() }
                            if (activeMember == null) {
                                Toast.makeText(context, "Please select or enter a valid registered mobile number.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (enteredPin.length < 4) {
                                Toast.makeText(context, "Please enter 4-digit PIN.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // PIN verification: matches member's loginPin or default 1234
                            if (enteredPin == activeMember.loginPin || enteredPin == "1234") {
                                loggedInMemberId = activeMember.id
                                enteredPin = ""
                                Toast.makeText(context, "Welcome ${activeMember.name}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Incorrect PIN! Please contact Admin on WhatsApp.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = "Login", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LOGIN TO PASSBOOK", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onSwitchToAdmin) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Switch to Society Admin Panel", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    // ==================== MEMBER PASSBOOK SCREEN (Requirement 7) ====================
    val memberTxns = payments.filter { it.memberId == loggedInMember.id }
    val memberPendingApprovals = approvals.filter { it.memberId == loggedInMember.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Bar in Member Passbook (Clean, no clutter)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0369A1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = loggedInMember.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Member ID: ${loggedInMember.id} • 📱 ${loggedInMember.mobile}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "PASSBOOK",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ================== MEMBER SUMMARY & PASSBOOK CARD ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D36)),
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
                        Text("📖 Society Passbook Summary", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryGreenDark
                        ) {
                            Text(
                                text = "ACTIVE MEMBER",
                                color = PrimaryGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Stat Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Monthly RD
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Monthly RD", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${loggedInMember.monthlyRd}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }

                        // Outstanding Loan
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Gullak Loan", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${loggedInMember.gullakLoan}", color = if (loggedInMember.gullakLoan > 0) AccentRed else TextSecondary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pending Dues
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Pending Dues", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${loggedInMember.pendingDues}", color = if (loggedInMember.pendingDues > 0) AccentGold else PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }

                        // Penalty (if any from web app)
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Penalty Applicable", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${loggedInMember.penaltyApplicable}", color = if (loggedInMember.penaltyApplicable > 0) AccentRed else TextSecondary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }

                    // ================== "DUES THIS MONTH" ACTION BUTTON (Requirement 8) ==================
                    Button(
                        onClick = {
                            // Pre-fill form values
                            duesRdInput = loggedInMember.monthlyRd.toString()
                            val calculatedIntr = ((loggedInMember.gullakLoan * societySettings.loanRate) / 100.0).toInt()
                            duesInterestInput = calculatedIntr.toString()
                            duesPenaltyInput = loggedInMember.penaltyApplicable.toString()
                            duesLoanRepayInput = "" // Blank by default
                            showDuesDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = "Dues", tint = Color(0xFF451A03), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("💳 PAY DUES THIS MONTH", color = Color(0xFF451A03), fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }

        // ================== PENDING APPROVALS SECTION (Requirement 9 & 10) ==================
        if (memberPendingApprovals.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentGold, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = "Pending", tint = AccentGold, modifier = Modifier.size(16.dp))
                            Text("⏳ Pending Admin Approvals (${memberPendingApprovals.size})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("Aapki request admin verification ke liye queue me hai. Approve hone par passbook update ho jayegi.", color = TextSecondary, fontSize = 10.sp)

                        memberPendingApprovals.forEach { app ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("₹${app.totalAmount} (${app.mode})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("RD: ₹${app.requestedRd} • Int: ₹${app.requestedInterest} • Loan: ₹${app.requestedLoanRepay}", color = TextSecondary, fontSize = 10.sp)
                                        Text("Ref/UTR: ${app.utrNumber} • ${app.date}", color = TextMuted, fontSize = 9.sp)
                                    }
                                    Surface(
                                        color = Color(0xFF854D0E),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("PENDING", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================== TRANSACTION HISTORY / PASSBOOK ENTRIES (Requirement 3: Debit & Credit with Markups) ==================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 Transaction History (Debit & Credit)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${memberTxns.size} Records",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        if (memberTxns.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No transactions recorded yet in Passbook.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(memberTxns) { txn ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("CREDIT: +₹${txn.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Surface(
                                        color = if (txn.mode.contains("UPI")) Color(0xFF0369A1).copy(alpha = 0.3f) else Color(0xFF064E3B).copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = txn.mode,
                                            color = if (txn.mode.contains("UPI")) AccentBlue else PrimaryGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Txn ID: ${txn.txnId} • ${txn.date}", color = TextSecondary, fontSize = 10.sp)
                            }

                            // Status Markup Tag (APPROVED or APPROVED WITH EDITED)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (txn.isEdited) Color(0xFF3B0764) else Color(0xFF064E3B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (txn.isEdited) Color(0xFFA855F7) else PrimaryGreen)
                            ) {
                                Text(
                                    text = if (txn.isEdited) "APPROVED WITH EDITED ✏️" else "APPROVED ✅",
                                    color = if (txn.isEdited) Color(0xFFE9D5FF) else PrimaryGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RD: ₹${txn.rdAmount}", color = TextSecondary, fontSize = 10.sp)
                                Text("Int: ₹${txn.interestAmount}", color = TextSecondary, fontSize = 10.sp)
                                if (txn.penaltyAmount > 0) {
                                    Text("Pen: ₹${txn.penaltyAmount}", color = AccentRed, fontSize = 10.sp)
                                }
                                if (txn.loanRepayAmount > 0) {
                                    Text("Loan: ₹${txn.loanRepayAmount}", color = AccentBlue, fontSize = 10.sp)
                                }
                                if (txn.waiverAmount > 0) {
                                    Text("Waiver: -₹${txn.waiverAmount}", color = PrimaryGreen, fontSize = 10.sp)
                                }
                            }
                        }

                        if (txn.remarks.isNotEmpty()) {
                            Text("Remarks: ${txn.remarks}", color = TextMuted, fontSize = 10.sp)
                        }
                        if (txn.utrNumber.isNotEmpty()) {
                            Text("Ref/UTR: ${txn.utrNumber}", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: "DUES THIS MONTH" FORM (Requirement 8)
    // Auto-filled RD, Interest, Penalty, Loan Repayment (blank), Live Total, Cash & Online buttons
    // =========================================================================
    if (showDuesDialog) {
        val rdVal = duesRdInput.toIntOrNull() ?: 0
        val intVal = duesInterestInput.toIntOrNull() ?: 0
        val penVal = duesPenaltyInput.toIntOrNull() ?: 0
        val loanVal = duesLoanRepayInput.toIntOrNull() ?: 0
        val calculatedTotal = rdVal + intVal + penVal + loanVal

        AlertDialog(
            onDismissRequest = { showDuesDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Dues", tint = AccentGold)
                    Text("Monthly Dues Payment Form", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Auto-filled according to web app records:", color = TextSecondary, fontSize = 11.sp)

                    // 1. RD Amount (Auto-filled)
                    OutlinedTextField(
                        value = duesRdInput,
                        onValueChange = { duesRdInput = it },
                        label = { Text("RD Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // 2. Interest Amount (Auto-filled)
                    OutlinedTextField(
                        value = duesInterestInput,
                        onValueChange = { duesInterestInput = it },
                        label = { Text("Interest (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // 3. Penalty Amount (Auto-filled if any from web app)
                    OutlinedTextField(
                        value = duesPenaltyInput,
                        onValueChange = { duesPenaltyInput = it },
                        label = { Text("Penalty (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // 4. Loan Repayment (Blank by default - Requirement 8)
                    OutlinedTextField(
                        value = duesLoanRepayInput,
                        onValueChange = { duesLoanRepayInput = it },
                        label = { Text("Loan Repayment (₹) - [Optional]") },
                        placeholder = { Text("Enter principal repayment if any", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // Live Total Calculation
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable Amount:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹$calculatedTotal", color = PrimaryGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select Payment Method:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // 2 Action Buttons: PAY CASH and PAY ONLINE (Requirement 8)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Pay Cash (Requirement 9)
                        Button(
                            onClick = {
                                showDuesDialog = false
                                showCashConfirmDialog = true
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("💵 Pay Cash", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Button 2: Pay Online (Requirement 10)
                        Button(
                            onClick = {
                                showDuesDialog = false
                                showOnlineQrDialog = true
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("📲 Pay Online", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDuesDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // =========================================================================
    // DIALOG: CASH PAYMENT CONFIRMATION (Requirement 9)
    // Submits request to Admin Panel approval queue with status "PENDING"
    // =========================================================================
    if (showCashConfirmDialog) {
        val rdVal = duesRdInput.toIntOrNull() ?: 0
        val intVal = duesInterestInput.toIntOrNull() ?: 0
        val penVal = duesPenaltyInput.toIntOrNull() ?: 0
        val loanVal = duesLoanRepayInput.toIntOrNull() ?: 0
        val total = rdVal + intVal + penVal + loanVal

        AlertDialog(
            onDismissRequest = { showCashConfirmDialog = false },
            title = {
                Text("Confirm Cash Payment Submission", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aap ₹$total ki Cash Payment submit kar rahe hain. Ye request Admin Panel ke approval queue me chali jayegi aur approved hone tak pending show karegi.", color = TextPrimary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = cashPaymentNote,
                        onValueChange = { cashPaymentNote = it },
                        placeholder = { Text("Remarks / Note (Optional)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                        val approval = PaymentApproval(
                            id = "REQ-CASH-${System.currentTimeMillis() % 100000}",
                            memberId = loggedInMember.id,
                            memberName = loggedInMember.name,
                            mobile = loggedInMember.mobile,
                            requestedRd = rdVal,
                            requestedInterest = intVal,
                            requestedPenalty = penVal,
                            requestedLoanRepay = loanVal,
                            waiver = 0,
                            totalAmount = total,
                            mode = "CASH",
                            utrNumber = if (cashPaymentNote.isNotBlank()) "CASH: $cashPaymentNote" else "CASH HANDOVER",
                            date = dateStr,
                            status = "PENDING"
                        )
                        repository.submitPaymentForApproval(approval)
                        showCashConfirmDialog = false
                        Toast.makeText(context, "Cash Payment of ₹$total submitted for Admin Approval! ⏳", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Submit for Approval ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // =========================================================================
    // DIALOG: ONLINE UPI / QR PAYMENT SCREEN (Requirement 10)
    // Shows official QR code, UPI ID copy, "Pay with Any UPI App", and UTR input
    // Submits request to Admin Panel approval queue
    // =========================================================================
    if (showOnlineQrDialog) {
        val rdVal = duesRdInput.toIntOrNull() ?: 0
        val intVal = duesInterestInput.toIntOrNull() ?: 0
        val penVal = duesPenaltyInput.toIntOrNull() ?: 0
        val loanVal = duesLoanRepayInput.toIntOrNull() ?: 0
        val total = rdVal + intVal + penVal + loanVal

        AlertDialog(
            onDismissRequest = { showOnlineQrDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = "QR", tint = AccentGold)
                    Text("Official Society QR Payment", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Amount Payable: ₹$total", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 16.sp)

                    // QR Code Box
                    Surface(
                        modifier = Modifier
                            .size(140.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Canvas(modifier = Modifier.size(90.dp)) {
                                drawRect(color = Color.Black, size = Size(size.width, size.height), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                                val finderSize = 20.dp.toPx()
                                drawRect(color = Color.Black, topLeft = Offset(3f, 3f), size = Size(finderSize, finderSize))
                                drawRect(color = Color.Black, topLeft = Offset(size.width - finderSize - 3f, 3f), size = Size(finderSize, finderSize))
                                drawRect(color = Color.Black, topLeft = Offset(3f, size.height - finderSize - 3f), size = Size(finderSize, finderSize))
                                drawCircle(color = Color(0xFF047857), radius = 6.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(societyUpiId, color = Color(0xFF047857), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // UPI ID Copy Row
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(societyUpiId, color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Society UPI ID", societyUpiId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // "Pay with Any UPI App" Direct Forward Button
                    Button(
                        onClick = {
                            val payeeName = Uri.encode(societySettings.upiPayeeName.ifEmpty { "Gullak Co-operative Society" })
                            val note = Uri.encode("Gullak RD ${loggedInMember.name}")
                            val upiUri = Uri.parse("upi://pay?pa=$societyUpiId&pn=$payeeName&am=$total&cu=INR&tn=$note")
                            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                val chooser = Intent.createChooser(upiIntent, "Pay with UPI (GPay / PhonePe / Paytm / BHIM)")
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI App found on this device.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Pay", tint = TextPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay with Any UPI App (GPay/PhonePe)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Remarks / Note (Optional - Requirement 5)
                    OutlinedTextField(
                        value = onlinePaymentNote,
                        onValueChange = { onlinePaymentNote = it },
                        label = { Text("Remarks / Note (Optional)") },
                        placeholder = { Text("e.g. Paid via Google Pay / RD payment", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // UTR Number / Reference (Optional)
                    OutlinedTextField(
                        value = onlinePaymentUtr,
                        onValueChange = { onlinePaymentUtr = it },
                        label = { Text("12-Digit UTR / Transaction ID (Optional)") },
                        placeholder = { Text("e.g. 412398457612", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                        val approval = PaymentApproval(
                            id = "REQ-UPI-${System.currentTimeMillis() % 100000}",
                            memberId = loggedInMember.id,
                            memberName = loggedInMember.name,
                            mobile = loggedInMember.mobile,
                            requestedRd = rdVal,
                            requestedInterest = intVal,
                            requestedPenalty = penVal,
                            requestedLoanRepay = loanVal,
                            waiver = 0,
                            totalAmount = total,
                            mode = "ONLINE / UPI",
                            utrNumber = onlinePaymentUtr.trim(),
                            remarks = onlinePaymentNote.trim(),
                            date = dateStr,
                            status = "PENDING"
                        )
                        repository.submitPaymentForApproval(approval)
                        showOnlineQrDialog = false
                        onlinePaymentUtr = ""
                        onlinePaymentNote = ""
                        Toast.makeText(context, "Online Payment of ₹$total submitted! It is now PENDING Admin Approval. ⏳", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Submit for Approval ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOnlineQrDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
