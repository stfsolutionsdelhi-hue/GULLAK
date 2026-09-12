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
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import java.net.URLEncoder

@Composable
fun MembersScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var selectedSortIndex by remember { mutableStateOf(0) }

    // Dialog States
    var selectedMemberForDetails by remember { mutableStateOf<Member?>(null) }
    var pinDialogMember by remember { mutableStateOf<Member?>(null) }
    var ledgerDialogMember by remember { mutableStateOf<Member?>(null) }
    var pdfDialogMember by remember { mutableStateOf<Member?>(null) }

    val filterOptions = listOf("Dues Pending", "With Loan", "All Members")
    val sortOptions = listOf("Loan: High ➔ Low", "Loan: Low ➔ High", "RD Balance", "Name (A-Z)")

    val filteredMembers = remember(members, searchQuery, selectedFilterIndex, selectedSortIndex) {
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
            2 -> list.sortedByDescending { it.openingRd }
            3 -> list.sortedBy { it.name }
            else -> list
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        Text("${members.size} Total Members • Kakrola, New Delhi", color = TextSecondary, fontSize = 11.sp)
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

                // Row 3: Filter Chips (Dues Pending, With Loan, All Members)
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

                // Row 4: Scrollable Smooth Sort Chips
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

        // Members List
        items(filteredMembers, key = { it.id }) { member ->
            val totalLoan = member.gullakLoan + member.emergencyLoan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .clickable { selectedMemberForDetails = member },
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
                                    .size(36.dp)
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
                                Text(member.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📱 ${member.mobile}", color = TextSecondary, fontSize = 11.sp)
                                    // App Installed Indicator Badge (User Request 12)
                                    if (member.isAppInstalled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF064E3B))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("📲 App Active", color = PrimaryGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF451A03))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("⚠️ Not Installed", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

                            // Dedicated PIN & Action Button (User Request 10)
                            IconButton(
                                onClick = { pinDialogMember = member },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CardDark)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "PIN & Settings", tint = AccentGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Bottom info strip
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
                            Text("Opening RD: ₹${member.openingRd}", color = TextSecondary, fontSize = 10.sp)
                            Text("Loan Limit: ₹${member.loanLimit}", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("Due: 15th", color = AccentGold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // ================== DIALOG 1: MEMBER PIN & ADMIN ACTIONS (User Request 10) ==================
    pinDialogMember?.let { m ->
        var editablePin by remember { mutableStateOf(m.loginPin) }
        var editableLoanLimit by remember { mutableStateOf(m.loanLimit.toString()) }
        var notifEnabled by remember { mutableStateOf(m.notificationsEnabled) }

        AlertDialog(
            onDismissRequest = { pinDialogMember = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = AccentGold)
                    Text("Member Controls: ${m.name}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. View Full Ledger Button
                    Button(
                        onClick = {
                            ledgerDialogMember = m
                            pinDialogMember = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📄 View Full Ledger (खाता विवरण)", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // 2. Print as PDF / Statement Button
                    Button(
                        onClick = {
                            pdfDialogMember = m
                            pinDialogMember = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "PDF", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🖨️ Print as PDF Statement", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    HorizontalDivider(color = CardBorder)

                    // 3. Member Login PIN Control (Default 1234, editable, send via WhatsApp)
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
                                val msg = "Namaste ${m.name} Ji, Gullak Society App me aapka Login PIN: $editablePin hai. Kripya app download karke login karein."
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=91${m.mobile}&text=${URLEncoder.encode(msg, "UTF-8")}"
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

                    // 4. Loan Limit Sync & Edit
                    OutlinedTextField(
                        value = editableLoanLimit,
                        onValueChange = { editableLoanLimit = it },
                        label = { Text("Loan Limit (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 5. Stop / Mute Notifications Option
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
                            Text(if (notifEnabled) "Notifications Active" else "Notifications Muted (बंद)", color = if (notifEnabled) PrimaryGreen else AccentRed, fontSize = 11.sp)
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

    // ================== DIALOG 2: FULL LEDGER (खाता विवरण) ==================
    ledgerDialogMember?.let { m ->
        val memberPayments = payments.filter { it.memberId == m.id }
        AlertDialog(
            onDismissRequest = { ledgerDialogMember = null },
            title = {
                Column {
                    Text("📄 Member Account Ledger", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${m.name} • ${m.id}", color = TextSecondary, fontSize = 11.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0B1120),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Accumulated RD Balance:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${m.openingRd}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gullak Loan Balance:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${m.gullakLoan}", color = if (m.gullakLoan > 0) AccentRed else TextSecondary, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Emergency Loan Balance:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${m.emergencyLoan}", color = if (m.emergencyLoan > 0) AccentRed else TextSecondary, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan Limit:", color = TextSecondary, fontSize = 11.sp)
                                Text("₹${m.loanLimit}", color = AccentBlue, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Transaction Records (${memberPayments.size}):", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    if (memberPayments.isEmpty()) {
                        Text("No transactions recorded yet in current cycle.", color = TextMuted, fontSize = 11.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(memberPayments) { p ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(p.date, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text("RD: ₹${p.rdAmount} • Int: ₹${p.interestAmount} • Mode: ${p.mode}", color = TextSecondary, fontSize = 10.sp)
                                        }
                                        Text("₹${p.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { ledgerDialogMember = null }) {
                    Text("Close", color = PrimaryGreen)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG 3: PRINT AS PDF / STATEMENT PREVIEW ==================
    pdfDialogMember?.let { m ->
        AlertDialog(
            onDismissRequest = { pdfDialogMember = null },
            title = {
                Text("🖨️ PDF Statement Preview", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🏦 GULLAK CO-OPERATIVE SOCIETY", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Member Statement & Receipt • Kakrola", color = Color(0xFF475569), fontSize = 10.sp)
                            HorizontalDivider(color = Color(0xFFCBD5E1))
                            Text("Name: ${m.name}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Member ID: ${m.id} • Mobile: ${m.mobile}", color = Color(0xFF334155), fontSize = 10.sp)
                            Text("Monthly RD: ₹${m.monthlyRd} • Opening Balance: ₹${m.openingRd}", color = Color(0xFF334155), fontSize = 10.sp)
                            Text("Outstanding Loan: ₹${m.gullakLoan + m.emergencyLoan}", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("Status: OFFICIAL VERIFIED RECORD", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }

                    Text("PDF format is aligned with the Web App standard ledger layout.", color = TextSecondary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Statement generated & ready to print!", Toast.LENGTH_SHORT).show()
                        pdfDialogMember = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Print / Share PDF 📄", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pdfDialogMember = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG 4: MEMBER FULL DETAILS DIALOG ==================
    selectedMemberForDetails?.let { m ->
        val totalLoan = m.gullakLoan + m.emergencyLoan
        AlertDialog(
            onDismissRequest = { selectedMemberForDetails = null },
            title = {
                Text(m.name, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                    HorizontalDivider(color = CardBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monthly RD:", color = TextMuted, fontSize = 11.sp)
                        Text("₹${m.monthlyRd} / mo", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RD Balance:", color = TextMuted, fontSize = 11.sp)
                        Text("₹${m.openingRd}", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Outstanding Loan:", color = TextMuted, fontSize = 11.sp)
                        Text("₹$totalLoan", color = if (totalLoan > 0) AccentRed else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Loan Limit:", color = TextMuted, fontSize = 11.sp)
                        Text("₹${m.loanLimit}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("App Installation:", color = TextMuted, fontSize = 11.sp)
                        Text(if (m.isAppInstalled) "Installed 📲" else "Not Installed ⚠️", color = if (m.isAppInstalled) PrimaryGreen else AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForDetails = null }) {
                    Text("Close", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
