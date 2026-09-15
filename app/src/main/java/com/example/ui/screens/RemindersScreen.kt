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
import com.example.data.AutoReminderConfig
import com.example.data.ReminderTemplate
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val autoConfig by repository.autoReminderConfig.collectAsState()
    val reminderTemplates by repository.reminderTemplates.collectAsState()

    // Current month pattern (e.g. "-09-2026")
    val currentMonthPattern = remember {
        java.text.SimpleDateFormat("-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    // Members whose payment has been approved in current month are EXCLUDED from dues reminder
    val paidMemberIdsThisMonth = remember(payments, currentMonthPattern) {
        payments.filter { it.date.contains(currentMonthPattern) }.map { it.memberId }.toSet()
    }

    val dueMembers = remember(members, paidMemberIdsThisMonth) {
        members.filter { m ->
            val hasPaidThisMonth = paidMemberIdsThisMonth.contains(m.id)
            val isMuted = !m.notificationsEnabled
            !hasPaidThisMonth && !isMuted && (m.pendingDues > 0 || m.gullakLoan > 0 || m.emergencyLoan > 0)
        }
    }

    val notInstalledMembers = remember(members) {
        members.filter { !it.isAppInstalled }
    }

    // Template Dropdown & Editable State
    var selectedTemplateId by remember { mutableStateOf(reminderTemplates.firstOrNull()?.id ?: "t1") }
    var isTemplateMenuExpanded by remember { mutableStateOf(false) }

    val activeTemplate = remember(reminderTemplates, selectedTemplateId) {
        reminderTemplates.find { it.id == selectedTemplateId } ?: reminderTemplates.first()
    }

    var editableNotificationTitle by remember(activeTemplate.id) { mutableStateOf(activeTemplate.notificationTitle) }
    var editableTemplateBody by remember(activeTemplate.id) { mutableStateOf(activeTemplate.body) }

    // Dispatch Audience Selection ("DUE", "ALL", "NOT_INSTALLED")
    var selectedAudience by remember { mutableStateOf("DUE") }

    // Dialog States
    var showBulkSmsConfirmation by remember { mutableStateOf(false) }
    var showAppInstallBulkSms by remember { mutableStateOf(false) }
    var memberSearchQuery by remember { mutableStateOf("") }

    // Auto Reminder Setup State
    var isAutoEnabled by remember(autoConfig) { mutableStateOf(autoConfig.isEnabled) }
    var selectedFreq by remember(autoConfig) { mutableStateOf(autoConfig.frequency) }
    var selectedTime by remember(autoConfig) { mutableStateOf(autoConfig.preferredTime) }
    val frequencies = listOf("Daily", "Every 2 Days", "Weekly")
    val times = listOf("10:00 AM", "02:00 PM", "06:00 PM")

    val targetRecipients = remember(selectedAudience, dueMembers, members, notInstalledMembers) {
        when (selectedAudience) {
            "ALL" -> members
            "NOT_INSTALLED" -> notInstalledMembers
            else -> dueMembers
        }
    }

    val filteredMemberList = remember(members, memberSearchQuery) {
        val q = memberSearchQuery.trim().lowercase()
        if (q.isBlank()) members
        else members.filter { it.name.lowercase().contains(q) || it.mobile.contains(q) || it.address.lowercase().contains(q) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header
        item {
            Column {
                Text("🔔 RD & LOAN REMINDER HUB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    "Smart Reminders (${dueMembers.size} Due Members • ${members.size} Total • ${notInstalledMembers.size} Need App)",
                    color = AccentGold,
                    fontSize = 11.sp
                )
            }
        }

        // ================== CARD 1: DROP-DOWN TEMPLATE MENU (EDITABLE - 5 TEMPLATES) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, PrimaryGreen, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = "Templates", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                            Text("Push & SMS Template Menu (5 Presets)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("Editable ✏️", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text("Select Template from Dropdown:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    // Template Dropdown Box
                    ExposedDropdownMenuBox(
                        expanded = isTemplateMenuExpanded,
                        onExpandedChange = { isTemplateMenuExpanded = !isTemplateMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = activeTemplate.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTemplateMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AccentGold,
                                unfocusedTextColor = AccentGold,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isTemplateMenuExpanded,
                            onDismissRequest = { isTemplateMenuExpanded = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            reminderTemplates.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(t.name, color = if (t.id == selectedTemplateId) AccentGold else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(t.notificationTitle, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                        }
                                    },
                                    onClick = {
                                        selectedTemplateId = t.id
                                        editableNotificationTitle = t.notificationTitle
                                        editableTemplateBody = t.body
                                        isTemplateMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            when (t.id) {
                                                "t1" -> Icons.Default.NotificationsActive
                                                "t2" -> Icons.Default.Warning
                                                "t3" -> Icons.Default.CardGiftcard
                                                "t4" -> Icons.Default.CreditCard
                                                else -> Icons.Default.Download
                                            },
                                            contentDescription = null,
                                            tint = if (t.id == selectedTemplateId) AccentGold else PrimaryGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Editable Notification Title
                    Text("Notification Title (साफ नोटिफिकेशन शीर्षक):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editableNotificationTitle,
                        onValueChange = { editableNotificationTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. 📢 Gullak Society RD Due Alert", color = TextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Editable Template Body
                    Text("Push & SMS Message Body (संदेश विषय):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editableTemplateBody,
                        onValueChange = { editableTemplateBody = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Helper Variable Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tags:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        listOf("[Member_Name]", "[Amount]", "[15th]", "[App_Link]").forEach { tag ->
                            Surface(
                                onClick = { editableTemplateBody += " $tag" },
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(tag, color = PrimaryGreen, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }

                    // Save Template Edits Button
                    Button(
                        onClick = {
                            val updated = activeTemplate.copy(
                                notificationTitle = editableNotificationTitle.trim(),
                                body = editableTemplateBody.trim()
                            )
                            repository.updateReminderTemplate(updated)
                            Toast.makeText(context, "Template '${activeTemplate.name}' updated & saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save / Update This Template", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ================== CARD 2: INSTANT DISPATCH ACTIONS (PUSH ALERT & BULK SMS) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, AccentGold, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.SendToMobile, contentDescription = "Dispatch", tint = AccentGold, modifier = Modifier.size(18.dp))
                            Text("Instant Dispatch Actions", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("${targetRecipients.size} Selected", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Audience Selector Chips
                    Text("Select Target Recipients:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedAudience == "DUE",
                            onClick = { selectedAudience = "DUE" },
                            label = { Text("Due (${dueMembers.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF451A03),
                                selectedLabelColor = AccentGold,
                                containerColor = BgDark,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = selectedAudience == "ALL",
                            onClick = { selectedAudience = "ALL" },
                            label = { Text("All (${members.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreenDark,
                                selectedLabelColor = PrimaryGreen,
                                containerColor = BgDark,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = selectedAudience == "NOT_INSTALLED",
                            onClick = { selectedAudience = "NOT_INSTALLED" },
                            label = { Text("Need App (${notInstalledMembers.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF713F12),
                                selectedLabelColor = AccentGold,
                                containerColor = BgDark,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    // Active Template Preview Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BgDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Active Template: ${activeTemplate.name}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Title: $editableNotificationTitle", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Message: $editableTemplateBody", color = TextSecondary, fontSize = 10.sp, maxLines = 2)
                        }
                    }

                    // Dispatch Buttons (Requirement 4 & 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1-Click Push Notification Dispatch (Clean title, sound & vibration on device without noise text)
                        Button(
                            onClick = {
                                val count = targetRecipients.size
                                val cleanTitle = editableNotificationTitle.trim().ifEmpty { "📢 Gullak Society Alert" }
                                val formattedMessage = if (count == 1) {
                                    val m = targetRecipients.first()
                                    editableTemplateBody
                                        .replace("[Member_Name]", m.name)
                                        .replace("[Amount]", "${m.monthlyRd}")
                                } else {
                                    "${editableTemplateBody.take(70)}... ($count recipients targeted)"
                                }

                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = cleanTitle,
                                    message = formattedMessage
                                )
                                repository.addAuditLog(
                                    "PUSH REMINDER DISPATCHED",
                                    "Dispatched '$cleanTitle' for $count recipients (${selectedAudience} list)."
                                )
                                Toast.makeText(context, "Push Alert sent to $count recipients!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Push", tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Alert 🔔", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // 1-Click Bulk SMS Dispatch (Works with the same template)
                        Button(
                            onClick = { showBulkSmsConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "SMS", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIM Bulk SMS 📱", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ================== CARD 3: AUTO REMINDER ENGINE (SCHEDULED) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
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
                            Icon(Icons.Default.Schedule, contentDescription = "Auto", tint = AccentGold, modifier = Modifier.size(18.dp))
                            Text("Auto Reminder Engine (ऑटो शेड्यूल)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isAutoEnabled,
                            onCheckedChange = { isAutoEnabled = it }
                        )
                    }

                    Text("Frequency & Schedule:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    // Frequency Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        frequencies.forEach { freq ->
                            FilterChip(
                                selected = selectedFreq == freq,
                                onClick = { selectedFreq = freq },
                                label = { Text(freq, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF451A03),
                                    selectedLabelColor = AccentGold,
                                    containerColor = BgDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Time Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        times.forEach { t ->
                            FilterChip(
                                selected = selectedTime == t,
                                onClick = { selectedTime = t },
                                label = { Text(t, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreenDark,
                                    selectedLabelColor = PrimaryGreen,
                                    containerColor = BgDark,
                                    labelColor = TextMuted
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            repository.updateAutoReminderConfig(
                                AutoReminderConfig(
                                    isEnabled = isAutoEnabled,
                                    frequency = selectedFreq,
                                    preferredTime = selectedTime,
                                    customTemplate = editableTemplateBody.trim()
                                )
                            )
                            Toast.makeText(context, "Auto Reminder Schedule Saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Save Auto Reminder Schedule", color = Color(0xFF451A03), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ================== CARD 4: APP NOT INSTALLED REMINDER ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEAB308), RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📲 App Installation Campaign", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${notInstalledMembers.size} Members Pending", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(
                        "In members ne abhi tak Gullak App install nahi ki hai. Inhe 1-click me download link bhejein:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Button(
                        onClick = { showAppInstallBulkSms = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF854D0E)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Invite", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send App Download Invite (${notInstalledMembers.size} Members)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Individual Member Reminders List Header & Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Individual Member Reminders (${members.size})", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tap SMS / WA to send", color = TextMuted, fontSize = 10.sp)
                }
                OutlinedTextField(
                    value = memberSearchQuery,
                    onValueChange = { memberSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filter members by name, mobile...", color = TextMuted, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (memberSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { memberSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
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

        items(filteredMemberList, key = { it.id }) { m ->
            val totalLoan = m.gullakLoan + m.emergencyLoan
            val isPaid = m.pendingDues == 0 && totalLoan == 0
            val hasPaidThisMonth = paidMemberIdsThisMonth.contains(m.id)

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
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("📱 ${m.mobile} • RD: ₹${m.monthlyRd}", color = TextSecondary, fontSize = 10.sp)
                        if (hasPaidThisMonth) {
                            Text("✅ Month Payment Approved (Auto Reminder Excluded)", color = PrimaryGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        } else if (isPaid) {
                            Text("✅ All Dues Cleared (Auto Reminder Excluded)", color = PrimaryGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        } else if (totalLoan > 0) {
                            Text("Loan: ₹$totalLoan (Due: 15th)", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Individual SMS using selected template
                        Button(
                            onClick = {
                                val smsText = editableTemplateBody
                                    .replace("[Member_Name]", m.name)
                                    .replace("[Amount]", "${m.monthlyRd}")
                                    .replace("[15th]", "15th")
                                    .replace("[App_Link]", "https://gullaksociety.in")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${m.mobile}")).apply {
                                    putExtra("sms_body", smsText)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SMS", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }

                        // Individual WhatsApp
                        Button(
                            onClick = {
                                val msg = editableTemplateBody
                                    .replace("[Member_Name]", m.name)
                                    .replace("[Amount]", "${m.monthlyRd}")
                                    .replace("[15th]", "15th")
                                    .replace("[App_Link]", "https://gullaksociety.in")
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=91${m.mobile}&text=${URLEncoder.encode(msg, "UTF-8")}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("WA", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // Bulk SMS Dispatch Dialog (Using currently selected & active template)
    if (showBulkSmsConfirmation) {
        val allNumbers = targetRecipients.map { it.mobile }.filter { it.length >= 10 }
        AlertDialog(
            onDismissRequest = { showBulkSmsConfirmation = false },
            title = {
                Text("Dispatch Bulk SMS to ${allNumbers.size} Members", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recipients (${allNumbers.size} selected via SIM):", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = allNumbers.joinToString(", "),
                        color = AccentGold,
                        fontSize = 10.sp,
                        maxLines = 3
                    )
                    Text("Selected Template (${activeTemplate.name}):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(editableTemplateBody, color = TextPrimary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val phoneList = allNumbers.joinToString(";")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneList")).apply {
                                putExtra("sms_body", editableTemplateBody)
                            }
                            context.startActivity(intent)
                            repository.addAuditLog("BULK SMS DISPATCHED", "Triggered SIM Bulk SMS for ${allNumbers.size} members using template '${activeTemplate.name}'.")
                        } catch (e: Exception) {
                            Toast.makeText(context, "SMS Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        showBulkSmsConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Open SIM SMS App 🚀", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkSmsConfirmation = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // App Install Campaign SMS Dialog
    if (showAppInstallBulkSms) {
        val notInstalledNumbers = notInstalledMembers.map { it.mobile }.filter { it.length >= 10 }
        val installMsg = "Namaste Ji, Gullak Co-operative Society ki official Android App download karein aur apni RD/Loan kist live dekhein: https://gullaksociety.in/download - Gullak Society"
        AlertDialog(
            onDismissRequest = { showAppInstallBulkSms = false },
            title = {
                Text("Invite ${notInstalledNumbers.size} Members to Install App", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Send app installation link to uninstalled members:", color = TextSecondary, fontSize = 11.sp)
                    Text(installMsg, color = TextPrimary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val phoneList = notInstalledNumbers.joinToString(";")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneList")).apply {
                                putExtra("sms_body", installMsg)
                            }
                            context.startActivity(intent)
                            repository.addAuditLog("APP INVITE DISPATCHED", "Sent app download invite to ${notInstalledNumbers.size} members.")
                        } catch (e: Exception) {
                            Toast.makeText(context, "SMS Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        showAppInstallBulkSms = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text("Send App Invites 📲", color = Color(0xFF451A03), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppInstallBulkSms = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
