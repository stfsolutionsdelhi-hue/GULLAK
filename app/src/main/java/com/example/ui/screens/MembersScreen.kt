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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Member
import com.example.data.Payment
import com.example.data.SocietyRepository
import com.example.data.getEffectiveLoanLimit
import com.example.data.getLoanLimitDisplay
import com.example.data.getTotalRdDeposited
import com.example.ui.theme.*
import com.example.util.PdfPrintHelper
import java.net.URLEncoder
import java.util.Locale

@Composable
fun MembersScreen(
    repository: SocietyRepository,
    onNavigateToPaymentDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var selectedSortIndex by remember { mutableStateOf(0) }

    // Dialog & Screen States
    var fullScreenLedgerMember by remember { mutableStateOf<Member?>(null) }
    var profileDialogMember by remember { mutableStateOf<Member?>(null) }
    var pinDialogMember by remember { mutableStateOf<Member?>(null) }
    var selectedLedgerPayment by remember { mutableStateOf<Payment?>(null) }

    val filterOptions = listOf("Dues Pending", "With Loan", "All Members")
    val sortOptions = listOf("Loan: High ➔ Low", "Loan: Low ➔ High", "RD Balance", "Name (A-Z)")

    val filteredMembers = remember(members, payments, searchQuery, selectedFilterIndex, selectedSortIndex) {
        var list = members.filter { m ->
            when (selectedFilterIndex) {
                0 -> m.pendingDues > 0 || (m.gullakLoan > 0 || m.emergencyLoan > 0)
                1 -> (m.gullakLoan > 0 || m.emergencyLoan > 0)
                else -> true
            }
        }

        if (searchQuery.isNotEmpty()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.mobile.contains(searchQuery) ||
                it.id.contains(searchQuery, ignoreCase = true)
            }
        }

        when (selectedSortIndex) {
            0 -> list.sortedByDescending { it.gullakLoan + it.emergencyLoan }
            1 -> list.sortedBy { it.gullakLoan + it.emergencyLoan }
            2 -> list.sortedByDescending { it.getTotalRdDeposited(payments) }
            3 -> list.sortedBy { it.name }
            else -> list
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Consolidated Compact Header & Filter Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Header Title & Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("👥 SOCIETY MEMBERS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${members.size} Total Members • Tap name to open Ledger", color = TextSecondary, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryGreenDark)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("${filteredMembers.size} Shown", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Row 2: Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    placeholder = { Text("Search name, mobile, or ID...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = AccentGold, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    )
                )

                // Row 3: Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            modifier = Modifier.weight(1f),
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (index == 0) Color(0xFF7F1D1D) else PrimaryGreenDark,
                                selectedLabelColor = if (index == 0) AccentGold else PrimaryGreen,
                                containerColor = CardDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedFilterIndex == index) AccentGold else CardBorder,
                                selectedBorderColor = AccentGold,
                                enabled = true,
                                selected = selectedFilterIndex == index
                            )
                        )
                    }
                }

                // Row 4: Scrollable Sort Chips
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = AccentBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Sort:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(sortOptions.size) { idx ->
                        val sortName = sortOptions[idx]
                        FilterChip(
                            selected = selectedSortIndex == idx,
                            onClick = { selectedSortIndex = idx },
                            label = { Text(sortName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = AccentBlue,
                                containerColor = BgDark,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }
        }

        // Members List - Tapping on Name / Card Opens Ledger Directly
        items(filteredMembers, key = { it.id }) { member ->
            val totalLoan = member.gullakLoan + member.emergencyLoan
            val totalRdDeposited = member.getTotalRdDeposited(payments)
            val loanLimitDisplay = member.getLoanLimitDisplay(payments)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .clickable { fullScreenLedgerMember = member },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                shape = RoundedCornerShape(10.dp)
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (totalLoan > 0) Color(0xFF450A0A) else CardDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1),
                                    color = if (totalLoan > 0) AccentRed else AccentGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = member.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger", tint = AccentBlue, modifier = Modifier.size(13.dp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📱 ${member.mobile}", color = TextSecondary, fontSize = 11.sp)
                                    if (member.isAppInstalled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF064E3B))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("📲 Active", color = PrimaryGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF451A03))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("⚠️ Uninstalled", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("RD ₹${member.monthlyRd}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (totalLoan > 0) {
                                    Text("Loan ₹$totalLoan", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }

                            // 3-Dot Menu for Profile & Admin Controls
                            var showDropdown by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showDropdown = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(CardDark)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = AccentGold, modifier = Modifier.size(18.dp))
                                }

                                DropdownMenu(
                                    expanded = showDropdown,
                                    onDismissRequest = { showDropdown = false },
                                    modifier = Modifier.background(CardDark)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("👤 Member Profile & KYC", color = TextPrimary, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = AccentBlue, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            showDropdown = false
                                            profileDialogMember = member
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🔑 App PIN & Settings", color = TextPrimary, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = "PIN", tint = AccentGold, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            showDropdown = false
                                            pinDialogMember = member
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📄 Open Ledger", color = TextPrimary, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger", tint = PrimaryGreen, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            showDropdown = false
                                            fullScreenLedgerMember = member
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bottom info strip with accurate Loan Limit and RD balance
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CardDark,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Accumulated RD: ₹$totalRdDeposited", color = TextSecondary, fontSize = 10.sp)
                            Text("Loan Limit: $loanLimitDisplay", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("Due: ${member.dueDay.take(6)}", color = AccentGold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 1: FULL SCREEN MEMBER COMPLETE LEDGER (User Request 2, 4, 5, 6)
    // =========================================================================
    fullScreenLedgerMember?.let { member ->
        val memberPayments = remember(payments, member) {
            payments.filter { it.memberId == member.id }.sortedByDescending { it.date }
        }
        val totalRd = member.getTotalRdDeposited(payments)
        val activeLoan = member.gullakLoan + member.emergencyLoan
        val limitDisplay = member.getLoanLimitDisplay(payments)

        Dialog(
            onDismissRequest = { fullScreenLedgerMember = null },
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
                        .padding(12.dp)
                ) {
                    // TOP BAR: Title, Close, Print PDF, Send WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { fullScreenLedgerMember = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Column {
                                Text(
                                    text = member.name,
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Text("ID: ${member.id} • ${member.mobile}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        // COMPACT ACTION BUTTONS: Print PDF & WhatsApp (User Request 5)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    PdfPrintHelper.printMemberPassbookHtml(context, member, payments)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Print PDF", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    PdfPrintHelper.sendPassbookToWhatsApp(context, member, payments)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("WhatsApp", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // STATS SUMMARY ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MONTHLY RD", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("₹${member.monthlyRd}", color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1.2f),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RD BALANCE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("₹%,d".format(Locale.ENGLISH, totalRd), color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1.2f),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ACTIVE LOAN", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("₹%,d".format(Locale.ENGLISH, activeLoan), color = if (activeLoan > 0) AccentRed else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1.3f),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LOAN LIMIT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(limitDisplay, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SECTION HEADER & HINT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📜 TRANSACTIONS REGISTER (${memberPayments.size})",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "👆 Tap entry to Edit / Delete in Payments",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // TRANSACTIONS LIST
                    if (memberPayments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No payment transactions recorded for this member yet.", color = TextSecondary, fontSize = 12.sp)
                                Text("Opening RD balance: ₹${member.openingRd}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(memberPayments, key = { it.txnId }) { p ->
                                val markupOrigin = when {
                                    p.remarks.contains("Web App", ignoreCase = true) -> "🌐 Web App"
                                    p.remarks.contains("Admin", ignoreCase = true) -> "🏢 Admin Counter"
                                    p.remarks.contains("Self", ignoreCase = true) || p.mode.contains("UPI", ignoreCase = true) -> "📱 Member App / UPI"
                                    else -> "🏛️ Society Counter"
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedLedgerPayment = p
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                Text(p.date, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("• ${p.txnId}", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Text(
                                                "₹${p.totalAmount}",
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                        }

                                        // Narration & Origin Markup
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = p.remarks.ifEmpty { "Monthly RD Deposit" },
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF1E293B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "$markupOrigin • ${p.mode}",
                                                    color = AccentGold,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Breakdown pill
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = BgDark,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("RD: ₹${p.rdAmount}", color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("Int: ₹${p.interestAmount}", color = TextSecondary, fontSize = 10.sp)
                                                if (p.penaltyAmount > 0) {
                                                    Text("Pen: ₹${p.penaltyAmount}", color = AccentRed, fontSize = 10.sp)
                                                }
                                                if (p.loanRepayAmount > 0) {
                                                    Text("Loan Repay: ₹${p.loanRepayAmount}", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                if (p.waiverAmount > 0) {
                                                    Text("Waiver: -₹${p.waiverAmount}", color = AccentGold, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 2: MEMBER PROFILE & KYC (Moved to 3-Dot Menu as per Request 2)
    // =========================================================================
    profileDialogMember?.let { m ->
        val totalLoan = m.gullakLoan + m.emergencyLoan
        val totalRd = m.getTotalRdDeposited(payments)
        val limitDisplay = m.getLoanLimitDisplay(payments)

        AlertDialog(
            onDismissRequest = { profileDialogMember = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = AccentGold)
                    Text("Member Profile: ${m.name}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Member ID: ${m.id}", color = TextSecondary, fontSize = 11.sp)
                    Text("Mobile: ${m.mobile}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Address: ${m.address.ifEmpty { "Kakrola, Delhi" }}", color = TextSecondary, fontSize = 11.sp)
                    Text("Nominee: ${m.nominee.ifEmpty { "Self / Nominee" }}", color = TextSecondary, fontSize = 11.sp)
                    Text("Payment Due Date: ${m.dueDay}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monthly RD:", color = TextMuted, fontSize = 11.sp)
                        Text("₹${m.monthlyRd} / month", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accumulated RD Balance:", color = TextMuted, fontSize = 11.sp)
                        Text("₹%,d".format(Locale.ENGLISH, totalRd), color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Outstanding Loan:", color = TextMuted, fontSize = 11.sp)
                        Text("₹%,d".format(Locale.ENGLISH, totalLoan), color = if (totalLoan > 0) AccentRed else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Eligible Loan Limit:", color = TextMuted, fontSize = 11.sp)
                        Text(limitDisplay, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("App Status:", color = TextMuted, fontSize = 11.sp)
                        Text(if (m.isAppInstalled) "Installed 📲" else "Not Installed ⚠️", color = if (m.isAppInstalled) PrimaryGreen else AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = m
                        profileDialogMember = null
                        fullScreenLedgerMember = selected
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Open Ledger 📜", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileDialogMember = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // =========================================================================
    // DIALOG 3: MEMBER APP PIN & ADMIN CONTROLS
    // =========================================================================
    pinDialogMember?.let { m ->
        var editablePin by remember { mutableStateOf(m.loginPin) }
        var editableLoanLimit by remember { mutableStateOf(if (m.customLimit > 0) m.customLimit.toString() else m.loanLimit.toString()) }
        var notifEnabled by remember { mutableStateOf(m.notificationsEnabled) }

        AlertDialog(
            onDismissRequest = { pinDialogMember = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = "PIN", tint = AccentGold)
                    Text("PIN & Settings: ${m.name}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Member Login PIN (सदस्य लॉगिन पिन):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = editablePin,
                            onValueChange = { if (it.length <= 6) editablePin = it },
                            label = { Text("PIN (Default: 1234)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val msg = "Namaste ${m.name} Ji, Gullak Society App me aapka Login PIN: $editablePin hai. Kripya app download karke login karein: https://gullaksociety.in"
                                try {
                                    val cleanPhone = m.mobile.filter { it.isDigit() }
                                    val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                    val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${URLEncoder.encode(msg, "UTF-8")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("📲 Send PIN", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = editableLoanLimit,
                        onValueChange = { editableLoanLimit = it },
                        label = { Text("Custom Loan Limit (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0B1120))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automated Reminders:", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(if (notifEnabled) "Notifications Active" else "Notifications Muted", color = if (notifEnabled) PrimaryGreen else AccentRed, fontSize = 11.sp)
                        }
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = {
                                notifEnabled = it
                                repository.toggleMemberNotifications(m.id)
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editablePin.isNotEmpty()) {
                            repository.updateMemberPin(m.id, editablePin)
                        }
                        val limit = editableLoanLimit.toIntOrNull() ?: m.loanLimit
                        repository.updateMemberLoanLimit(m.id, limit)
                        Toast.makeText(context, "Member Settings Updated Successfully!", Toast.LENGTH_SHORT).show()
                        pinDialogMember = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Changes", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pinDialogMember = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
    // =========================================================================
    // DIALOG 4: READABLE TRANSACTION ENTRY DETAIL (User Request 2)
    // =========================================================================
    selectedLedgerPayment?.let { p ->
        val markupOrigin = when {
            p.remarks.contains("Web App", ignoreCase = true) -> "🌐 Web App"
            p.remarks.contains("Admin", ignoreCase = true) -> "🏢 Admin Counter"
            p.remarks.contains("Self", ignoreCase = true) || p.mode.contains("UPI", ignoreCase = true) -> "📱 Member App / UPI"
            else -> "🏛️ Society Counter"
        }

        AlertDialog(
            onDismissRequest = { selectedLedgerPayment = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Receipt", tint = PrimaryGreen)
                        Text("Transaction Detail", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(markupOrigin, color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Receipt No / Txn ID:", color = TextSecondary, fontSize = 11.sp)
                                Text(p.txnId, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date & Time:", color = TextSecondary, fontSize = 11.sp)
                                Text(p.date, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Member:", color = TextSecondary, fontSize = 11.sp)
                                Text("${p.memberName} (${p.memberId})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Mode:", color = TextSecondary, fontSize = 11.sp)
                                Text(p.mode, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            if (p.utrNumber.isNotBlank()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("UTR / Ref No:", color = TextSecondary, fontSize = 11.sp)
                                    Text(p.utrNumber, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Itemized Breakdown Table
                    Text("💰 Amount Breakdown:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("RD Deposit:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${p.rdAmount}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan Interest:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${p.interestAmount}", color = TextPrimary, fontSize = 11.sp)
                            }
                            if (p.penaltyAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Penalty / Late Fee:", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${p.penaltyAmount}", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            if (p.loanRepayAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Loan Principal Repaid:", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${p.loanRepayAmount}", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            if (p.waiverAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount / Waiver:", color = TextSecondary, fontSize = 11.sp)
                                    Text("-₹${p.waiverAmount}", color = AccentGold, fontSize = 11.sp)
                                }
                            }
                            HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Received:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("₹${p.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }

                    // Narration / Remarks
                    if (p.remarks.isNotBlank()) {
                        Text("Narration / Remarks:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            color = BgDark,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = p.remarks,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val txnId = p.txnId
                            selectedLedgerPayment = null
                            fullScreenLedgerMember = null
                            onNavigateToPaymentDetail(txnId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF064E3B), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✏️ Edit Entry", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLedgerPayment = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
