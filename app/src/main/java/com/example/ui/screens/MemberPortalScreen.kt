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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Member
import com.example.data.sanitizeMobileNumber
import com.example.data.getLoanLimitDisplay
import com.example.data.getEffectiveLoanLimit
import com.example.data.getTotalRdDeposited
import com.example.data.PaymentApproval
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class CombinedTxn(
    val txnId: String,
    val date: String,
    val type: String, // CREDIT / DEBIT
    val totalAmount: Int,
    val rdAmount: Int,
    val interestAmount: Int,
    val penaltyAmount: Int,
    val loanRepayAmount: Int,
    val waiverAmount: Int,
    val mode: String,
    val remarks: String,
    val utrNumber: String,
    val status: String // "PENDING", "APPROVED", "APPROVED WITH EDITED"
)

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

    val loggedInMemberId by repository.loggedInMemberId.collectAsState()
    var enteredMobile by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var multiAccountSelectionList by remember { mutableStateOf<List<Member>>(emptyList()) }
    var showMultiAccountDialog by remember { mutableStateOf(false) }

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
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val loggedInMember = members.find { it.id == loggedInMemberId }

    // User Request: Member panel login hote hi automatically live sync hona chahiye
    LaunchedEffect(loggedInMemberId) {
        if (loggedInMemberId != null && repository.isLiveSyncActive.value) {
            try {
                repository.syncWithGoogleSheet()
            } catch (_: Exception) {}
        }
    }

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
                    // Mobile Number Input
                    OutlinedTextField(
                        value = enteredMobile,
                        onValueChange = { enteredMobile = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("Enter your registered mobile", color = TextMuted) },
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
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPinVisible) "Toggle PIN Visibility" else "Toggle PIN Visibility",
                                    tint = AccentBlue
                                )
                            }
                        },
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
                                val targetMobile = enteredMobile.trim()
                                val adminPhone = societySettings.adminWhatsApp.ifEmpty { "9718174244" }
                                val cleanPhone = if (adminPhone.startsWith("+91")) adminPhone else "91$adminPhone"
                                val message = "Namaste Admin Ji,\n\nI forgot my Gullak Society Member Portal PIN.\n\nMobile: $targetMobile\n\nPlease provide or reset my login PIN. Dhanyawad!"
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
                            val cleanMobile = sanitizeMobileNumber(enteredMobile)
                            if (cleanMobile.isEmpty()) {
                                Toast.makeText(context, "Please enter a valid registered mobile number.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (enteredPin.length < 4) {
                                Toast.makeText(context, "Please enter 4-digit PIN.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Find matching member accounts with comprehensive sanitization & substring matching
                            val digitsOnlyInput = cleanMobile.filter { it.isDigit() }.takeLast(10)
                            val matchingMembers = members.filter { m ->
                                val mSanitized = sanitizeMobileNumber(m.mobile)
                                val mDigits = mSanitized.filter { it.isDigit() }
                                val mRawDigits = m.mobile.filter { it.isDigit() }
                                val mAddressDigits = m.address.filter { it.isDigit() }
                                
                                mSanitized == cleanMobile ||
                                (digitsOnlyInput.length >= 10 && mDigits.endsWith(digitsOnlyInput)) ||
                                (digitsOnlyInput.length >= 10 && mRawDigits.contains(digitsOnlyInput)) ||
                                (digitsOnlyInput.length >= 10 && mAddressDigits.contains(digitsOnlyInput)) ||
                                (digitsOnlyInput.length >= 10 && m.name.contains(digitsOnlyInput))
                            }
                            if (matchingMembers.isEmpty()) {
                                Toast.makeText(context, "No registered member found with mobile $cleanMobile. Please tap 'Live Sync' in Admin panel or contact Admin.", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            // Strict PIN Verification (User Request 5: Single authoritative PIN only)
                            val correctMatches = matchingMembers.filter { m ->
                                m.loginPin.isNotBlank() && enteredPin.trim() == m.loginPin.trim()
                            }
                            if (correctMatches.isEmpty()) {
                                val hasUnsetPin = matchingMembers.any { it.loginPin.isBlank() }
                                if (hasUnsetPin) {
                                    Toast.makeText(context, "Your PIN is not set yet. Please tap 'Forgot PIN' to contact Admin.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Incorrect PIN! Please contact Admin on WhatsApp.", Toast.LENGTH_SHORT).show()
                                }
                                return@Button
                            }

                            if (correctMatches.size == 1) {
                                // Single matched account
                                val singleAccount = correctMatches.first()
                                repository.loginMember(singleAccount.id)
                                enteredPin = ""
                                enteredMobile = ""
                                showMultiAccountDialog = false
                                multiAccountSelectionList = emptyList()
                                Toast.makeText(context, "Welcome ${singleAccount.name}!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Multiple accounts matched under this phone number
                                multiAccountSelectionList = correctMatches
                                showMultiAccountDialog = true
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

            Spacer(modifier = Modifier.height(18.dp))
            // Small & discreet text link for Admin login so app looks 100% designed for members
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSwitchToAdmin,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Admin Switch",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Admin Panel",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryGreen,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text("App Version ${com.example.data.APP_VERSION}", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("•", color = TextMuted, fontSize = 10.sp)
                    Text("Live Sync Active", color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    } else {

    // ==================== MEMBER PASSBOOK SCREEN (Requirement 7) ====================
    val memberTxns = payments.filter { it.memberId == loggedInMember.id }
    val memberPendingApprovals = approvals.filter { it.memberId == loggedInMember.id }

    val combinedTxns = remember(memberTxns, memberPendingApprovals) {
        val list = mutableListOf<CombinedTxn>()
        // Map approved transactions
        memberTxns.forEach { t ->
            list.add(
                CombinedTxn(
                    txnId = t.txnId,
                    date = t.date,
                    type = t.type,
                    totalAmount = t.totalAmount,
                    rdAmount = t.rdAmount,
                    interestAmount = t.interestAmount,
                    penaltyAmount = t.penaltyAmount,
                    loanRepayAmount = t.loanRepayAmount,
                    waiverAmount = t.waiverAmount,
                    mode = t.mode,
                    remarks = t.remarks,
                    utrNumber = t.utrNumber,
                    status = if (t.isEdited) "APPROVED WITH EDITED" else "APPROVED"
                )
            )
        }
        // Map pending transactions
        memberPendingApprovals.forEach { p ->
            list.add(
                CombinedTxn(
                    txnId = p.id,
                    date = p.date,
                    type = "CREDIT",
                    totalAmount = p.totalAmount,
                    rdAmount = p.requestedRd,
                    interestAmount = p.requestedInterest,
                    penaltyAmount = p.requestedPenalty,
                    loanRepayAmount = p.requestedLoanRepay,
                    waiverAmount = p.waiver,
                    mode = p.mode,
                    remarks = p.remarks,
                    utrNumber = p.utrNumber,
                    status = "PENDING"
                )
            )
        }
        // Sort by date (newest first). If dates are matching or empty, fall back to comparing txnId.
        list.sortedWith { a, b ->
            val dateCompare = b.date.compareTo(a.date)
            if (dateCompare != 0) dateCompare else b.txnId.compareTo(a.txnId)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // Top Bar in Member Passbook (Clean, with prominent Logout & Switch)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
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
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Member ID: ${loggedInMember.id} • 📱 ${loggedInMember.mobile}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "MEMBER PORTAL",
                                color = AccentBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
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

                    // Stat Grid with Massive Fonts & Maximum Screen Real Estate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // RD Balance
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Savings, contentDescription = "RD", tint = AccentBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RD Balance", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${loggedInMember.openingRd}", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 26.sp)
                            }
                        }

                        // Monthly RD
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = "Monthly", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Monthly RD", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${loggedInMember.monthlyRd}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 26.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Loan Dues
                        val totalLoanDuesAmt = loggedInMember.gullakLoan + loggedInMember.emergencyLoan
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = "Loan", tint = if (totalLoanDuesAmt > 0) AccentRed else TextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Total Loan Dues", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹$totalLoanDuesAmt", color = if (totalLoanDuesAmt > 0) AccentRed else TextSecondary, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }
                        }

                        // Pending Dues
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payment, contentDescription = "Dues", tint = if (loggedInMember.pendingDues > 0) AccentGold else PrimaryGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pending Dues", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${loggedInMember.pendingDues}", color = if (loggedInMember.pendingDues > 0) AccentGold else PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }
                        }
                    }

                    if (loggedInMember.penaltyApplicable > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Gavel, contentDescription = "Penalty", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    Text("Penalty Applicable", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("₹${loggedInMember.penaltyApplicable}", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }
                        }
                    }

                    // ================== "DUES THIS MONTH" ACTION BUTTON (Requirement 8) ==================
                    Button(
                        onClick = {
                            // Pre-fill form values
                            duesRdInput = loggedInMember.monthlyRd.toString()
                            val calculatedIntr = (((loggedInMember.gullakLoan + loggedInMember.emergencyLoan) * societySettings.loanRate) / 100.0).toInt()
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

        // ================== MEMBER LOAN LIMIT & SUMMARY OF LOANS ==================
        item {
            val activeLoans = remember(loggedInMember, societySettings) {
                val list = mutableListOf<com.example.data.Loan>()
                val baseRate = societySettings.loanRate
                
                if (loggedInMember.gullakLoan > 0) {
                    list.add(
                        com.example.data.Loan(
                            loanId = "L-GUL-${loggedInMember.id}",
                            memberId = loggedInMember.id,
                            memberName = loggedInMember.name,
                            mobile = loggedInMember.mobile,
                            type = "Gullak Loan",
                            principal = loggedInMember.gullakLoan,
                            interestRate = baseRate,
                            outstanding = loggedInMember.gullakLoan,
                            issueDate = loggedInMember.joinDate.ifEmpty { "2026-03-10" },
                            status = "ACTIVE"
                        )
                    )
                }
                if (loggedInMember.emergencyLoan > 0) {
                    list.add(
                        com.example.data.Loan(
                            loanId = "L-EME-${loggedInMember.id}",
                            memberId = loggedInMember.id,
                            memberName = loggedInMember.name,
                            mobile = loggedInMember.mobile,
                            type = "Emergency Loan",
                            principal = loggedInMember.emergencyLoan,
                            interestRate = 2.0,
                            outstanding = loggedInMember.emergencyLoan,
                            issueDate = "2026-08-01",
                            status = "ACTIVE"
                        )
                    )
                }
                list
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Loan Limit Header & Clean Number Display
                    val loanLimitDisplay = loggedInMember.getLoanLimitDisplay(payments)
                    val effectiveLimit = loggedInMember.getEffectiveLoanLimit(payments)
                    val totalLoanOutstanding = loggedInMember.gullakLoan + loggedInMember.emergencyLoan
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Limit", tint = if (totalLoanOutstanding > 0) AccentGold else PrimaryGreen, modifier = Modifier.size(16.dp))
                            Text("🛡️ Loan Limit / Eligibility", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = loanLimitDisplay,
                            color = if (totalLoanOutstanding > 0) AccentGold else PrimaryGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    // Simple Clean Explanation Card (No graph / No credit-card progress bar)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (totalLoanOutstanding > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Text(
                                        "Active Loan Dues: ₹$totalLoanOutstanding chal rahe hain. Rule ke mutabiq jab tak purana loan clear nahi hota, nayi loan eligibility ₹0 rehti hai.",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 15.sp
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("✅", fontSize = 14.sp)
                                    Text(
                                        "Aapka koi active loan nahi hai. Vartaman loan eligibility limit ₹%,d hai.".format(java.util.Locale.ENGLISH, effectiveLimit),
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CardBorder, thickness = 1.dp)

                    // Active Loans Breakdown Section
                    Text("📊 Active Loans & Rates Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    if (activeLoans.isEmpty()) {
                        Text("No active loan records found. Maintain a good RD track to apply for a loan up to ₹${loggedInMember.loanLimit}!", color = TextMuted, fontSize = 11.sp)
                    } else {
                        activeLoans.forEach { loan ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(loan.type, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Loan ID: ${loan.loanId} • Issued: ${loan.issueDate}", color = TextMuted, fontSize = 9.sp)
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text("₹${loan.outstanding}", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF7F1D1D),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
                                        ) {
                                            Text(
                                                text = "${loan.interestRate}% Interest",
                                                color = Color(0xFFFECACA),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                    text = "${combinedTxns.size} Records",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        if (combinedTxns.isEmpty()) {
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
            itemsIndexed(combinedTxns, key = { index, txn -> "${txn.txnId}_$index" }) { _, txn ->
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
                                    val isDebit = txn.type == "DEBIT"
                                    val sign = if (isDebit) "-" else "+"
                                    val textCol = if (isDebit) AccentRed else PrimaryGreen
                                    Text("${txn.type}: ${sign}₹${txn.totalAmount}", color = textCol, fontWeight = FontWeight.Black, fontSize = 15.sp)
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

                            // Status Markup Tag (APPROVED, PENDING, or APPROVED WITH EDITED)
                            val statusBg = when (txn.status) {
                                "PENDING" -> Color(0xFF854D0E)
                                "APPROVED WITH EDITED" -> Color(0xFF3B0764)
                                else -> Color(0xFF064E3B)
                            }
                            val statusBorder = when (txn.status) {
                                "PENDING" -> AccentGold
                                "APPROVED WITH EDITED" -> Color(0xFFA855F7)
                                else -> PrimaryGreen
                            }
                            val statusText = when (txn.status) {
                                "PENDING" -> Color(0xFFFEF08A)
                                "APPROVED WITH EDITED" -> Color(0xFFE9D5FF)
                                else -> PrimaryGreen
                            }
                            val statusLabel = when (txn.status) {
                                "PENDING" -> "PENDING ⏳"
                                "APPROVED WITH EDITED" -> "APPROVED WITH EDITED ✏️"
                                else -> "APPROVED ✅"
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = statusBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder)
                            ) {
                                Text(
                                    text = statusLabel,
                                    color = statusText,
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

            // Version Footer in Member Passbook
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gullak Passbook", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("Version ${com.example.data.APP_VERSION}", color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                            if (societyQrUri != null) {
                                coil.compose.AsyncImage(
                                    model = societyQrUri,
                                    contentDescription = "Society QR Code",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(4.dp)
                                )
                            } else {
                                Canvas(modifier = Modifier.size(90.dp)) {
                                    drawRect(color = Color.Black, size = Size(size.width, size.height), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                                    val finderSize = 20.dp.toPx()
                                    drawRect(color = Color.Black, topLeft = Offset(3f, 3f), size = Size(finderSize, finderSize))
                                    drawRect(color = Color.Black, topLeft = Offset(size.width - finderSize - 3f, 3f), size = Size(finderSize, finderSize))
                                    drawRect(color = Color.Black, topLeft = Offset(3f, size.height - finderSize - 3f), size = Size(finderSize, finderSize))
                                    drawCircle(color = Color(0xFF047857), radius = 6.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                                }
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
                        label = { Text("Remarks / Payment Proof (Optional)") },
                        placeholder = { Text("e.g. Paid ₹$total via Google Pay / PhonePe", color = TextMuted) },
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
    } // Closes else { for Logged-In Passbook Screen

    // Secure Multi-Account Selection Popup (Rendered at root, always visible on login)
    if (showMultiAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showMultiAccountDialog = false
                multiAccountSelectionList = emptyList()
            },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = AccentBlue)
                    Text("Select Member Account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Multiple family member accounts registered with mobile ${enteredMobile.ifEmpty { "this number" }}. Tap your account to open passbook:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    multiAccountSelectionList.forEach { member ->
                        Surface(
                            onClick = {
                                repository.loginMember(member.id)
                                showMultiAccountDialog = false
                                multiAccountSelectionList = emptyList()
                                enteredPin = ""
                                enteredMobile = ""
                                Toast.makeText(context, "Welcome ${member.name}!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(member.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Member ID: ${member.id} • RD: ₹${member.monthlyRd}/mo", color = AccentGold, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "Open Passbook", tint = AccentBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMultiAccountDialog = false
                        multiAccountSelectionList = emptyList()
                    }
                ) {
                    Text("Back / Change Number", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Member Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
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
                        showLogoutConfirmDialog = false
                        Toast.makeText(context, "Logged out. Society alerts will remain active 🔔", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Yes, Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
