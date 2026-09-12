import os

# 1. TasksScreen.kt
tasks_screen = """package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocietyRepository
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    repository: SocietyRepository,
    onNavigateToPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()

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
        // App Header
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreenDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("LIVE ACTIVE", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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

        // Section Title: Member Overview
        item {
            Text("👥 Society Members Quick Overview", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(members.take(15)) { member ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.take(1),
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Column {
                            Text(member.name, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("📱 ${member.mobile} • Due: ${member.dueDay}", color = TextSecondary, fontSize = 11.sp)
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
"""
with open("app/src/main/java/com/example/ui/screens/TasksScreen.kt", "w", encoding="utf-8") as f:
    f.write(tasks_screen)

# 2. MembersScreen.kt
members_screen = """package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun MembersScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedMember by remember { mutableStateOf<Member?>(null) }

    val filteredMembers = remember(members, searchQuery, selectedFilter) {
        members.filter { m ->
            val matchQuery = searchQuery.isEmpty() ||
                    m.name.contains(searchQuery, ignoreCase = true) ||
                    m.mobile.contains(searchQuery) ||
                    m.id.contains(searchQuery, ignoreCase = true)
            val matchFilter = when (selectedFilter) {
                "LOAN" -> (m.gullakLoan > 0 || m.emergencyLoan > 0)
                "DUES" -> m.pendingDues > 0
                else -> true
            }
            matchQuery && matchFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("👥 SOCIETY MEMBERS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Total ${members.size} Official Members", color = PrimaryGreen, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("${filteredMembers.size} Shown", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name, mobile or ID...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${members.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreenDark,
                    selectedLabelColor = PrimaryGreen
                )
            )
            FilterChip(
                selected = selectedFilter == "LOAN",
                onClick = { selectedFilter = "LOAN" },
                label = { Text("With Loan") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7F1D1D),
                    selectedLabelColor = AccentRed
                )
            )
            FilterChip(
                selected = selectedFilter == "DUES",
                onClick = { selectedFilter = "DUES" },
                label = { Text("Dues") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF78350F),
                    selectedLabelColor = AccentGold
                )
            )
        }

        // Members List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredMembers, key = { it.id }) { m ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(10.dp),
                    onClick = { selectedMember = m }
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreenDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m.name.take(1),
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text(m.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${m.id} • 📱 ${m.mobile}", color = TextSecondary, fontSize = 11.sp)
                                if (m.nominee.isNotEmpty()) {
                                    Text("Nominee: ${m.nominee}", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${m.monthlyRd}/mo", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val totLoan = m.gullakLoan + m.emergencyLoan
                            if (totLoan > 0) {
                                Text("Loan: ₹$totLoan", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(m.status, color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Member Details Dialog
    selectedMember?.let { m ->
        AlertDialog(
            onDismissRequest = { selectedMember = null },
            confirmButton = {
                Button(
                    onClick = { selectedMember = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Close", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(m.name, color = AccentGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Member ID: ${m.id}", color = TextSecondary, fontSize = 12.sp)
                    Text("Mobile: ${m.mobile}", color = TextSecondary, fontSize = 12.sp)
                    Text("Address: ${m.address}", color = TextSecondary, fontSize = 12.sp)
                    Text("Nominee: ${m.nominee}", color = TextSecondary, fontSize = 12.sp)
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Monthly RD: ₹${m.monthlyRd}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    Text("Opening RD Base: ₹${m.openingRd}", color = AccentBlue)
                    Text("Gullak Loan: ₹${m.gullakLoan}", color = if (m.gullakLoan > 0) AccentRed else TextSecondary)
                    Text("Emergency Loan: ₹${m.emergencyLoan}", color = if (m.emergencyLoan > 0) AccentRed else TextSecondary)
                    Text("Due Date: ${m.dueDay}", color = TextSecondary, fontSize = 12.sp)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
"""
with open("app/src/main/java/com/example/ui/screens/MembersScreen.kt", "w", encoding="utf-8") as f:
    f.write(members_screen)

# 3. PaymentsScreen.kt
payments_screen = """package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()

    var showReceiveDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<Member?>(members.firstOrNull()) }
    var rdInput by remember { mutableStateOf("400") }
    var interestInput by remember { mutableStateOf("0") }
    var penaltyInput by remember { mutableStateOf("0") }
    var payMode by remember { mutableStateOf("CASH") }
    var remarksInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("💳 PAYMENTS & RECEIPTS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${payments.size} Transactions Recorded", color = PrimaryGreen, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    if (selectedMember == null && members.isNotEmpty()) {
                        selectedMember = members.first()
                    }
                    showReceiveDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF064E3B))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Receive RD", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
            }
        }

        // Payments History List
        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No receipts recorded yet.", color = TextMuted, fontSize = 14.sp)
                    Text("Click 'Receive RD' to record payment.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(payments, key = { it.txnId }) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(p.memberName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${p.txnId} • ${p.date}", color = TextMuted, fontSize = 11.sp)
                                Text("Mode: ${p.mode}", color = if (p.mode == "ONLINE") AccentBlue else AccentGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${p.totalAmount}", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text("RD: ₹${p.rdAmount} | Int: ₹${p.interestAmount}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Receive Payment Dialog
    if (showReceiveDialog) {
        AlertDialog(
            onDismissRequest = { showReceiveDialog = false },
            title = {
                Text("Receive Member Payment", color = AccentGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Member:", color = TextSecondary, fontSize = 12.sp)
                    // Dropdown for Member selection
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = selectedMember?.let { "${it.name} (${it.mobile})" } ?: "Select Member",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.mobile})", color = TextPrimary, fontSize = 13.sp) },
                                    onClick = {
                                        selectedMember = m
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = rdInput,
                        onValueChange = { rdInput = it },
                        label = { Text("RD Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = interestInput,
                        onValueChange = { interestInput = it },
                        label = { Text("Interest Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { payMode = "CASH" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (payMode == "CASH") PrimaryGreen else Color(0xFF1E293B)
                            )
                        ) {
                            Text("💵 Cash", color = if (payMode == "CASH") Color(0xFF064E3B) else TextPrimary)
                        }
                        Button(
                            onClick = { payMode = "ONLINE" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (payMode == "ONLINE") AccentBlue else Color(0xFF1E293B)
                            )
                        ) {
                            Text("📱 Online/UPI", color = if (payMode == "ONLINE") Color(0xFF060913) else TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = selectedMember
                        if (m != null) {
                            val rd = rdInput.toIntOrNull() ?: 0
                            val intr = interestInput.toIntOrNull() ?: 0
                            val pen = penaltyInput.toIntOrNull() ?: 0
                            repository.recordPayment(
                                memberId = m.id,
                                memberName = m.name,
                                mobile = m.mobile,
                                rdAmount = rd,
                                interestAmount = intr,
                                penaltyAmount = pen,
                                mode = payMode,
                                remarks = remarksInput
                            )
                            Toast.makeText(context, "Payment Recorded Successfully!", Toast.LENGTH_SHORT).show()
                            showReceiveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Receipt ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceiveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
"""
with open("app/src/main/java/com/example/ui/screens/PaymentsScreen.kt", "w", encoding="utf-8") as f:
    f.write(payments_screen)

# 4. RemindersScreen.kt
reminders_screen = """package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import java.net.URLEncoder

@Composable
fun RemindersScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🔔 RD & LOAN REMINDERS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Monthly Collection Due: 15th of month", color = AccentGold, fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryGreenDark, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📢 1-Click WhatsApp Reminder System", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Member ko WhatsApp par RD / Loan installment reminder message direct bhejein:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(members) { m ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("📱 ${m.mobile} • Monthly RD: ₹${m.monthlyRd}", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            val msg = "Namaste ${m.name} Ji, Gullak Co-operative Society ki taraf se aapki monthly RD (₹${m.monthlyRd}) ka reminder hai. Kripya samay par jama karein. Dhanyawad!"
                            try {
                                val url = "https://api.whatsapp.com/send?phone=91${m.mobile}&text=${URLEncoder.encode(msg, "UTF-8")}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not installed or error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
"""
with open("app/src/main/java/com/example/ui/screens/RemindersScreen.kt", "w", encoding="utf-8") as f:
    f.write(reminders_screen)

# 5. MainScreen.kt
main_screen = """package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.SocietyRepository
import com.example.ui.theme.*

@Composable
fun MainScreen(
    repository: SocietyRepository
) {
    var selectedTab by remember { mutableStateOf(0) }

    val navItems = listOf(
        NavigationItem("Tasks", Icons.Default.Home),
        NavigationItem("Members", Icons.Default.People),
        NavigationItem("Payments", Icons.Default.CreditCard),
        NavigationItem("Reminders", Icons.Default.Notifications),
        NavigationItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF060913),
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
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
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
"""
with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w", encoding="utf-8") as f:
    f.write(main_screen)

# 6. MainActivity.kt
main_activity = """package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.SocietyRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.GullakSocietyTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: SocietyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = SocietyRepository(applicationContext)

        setContent {
            GullakSocietyTheme {
                MainScreen(repository = repository)
            }
        }
    }
}
"""
with open("app/src/main/java/com/example/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(main_activity)

print("All Jetpack Compose screens and MainActivity created successfully.")
