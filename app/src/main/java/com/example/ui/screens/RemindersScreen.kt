package com.example.ui.screens

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
import com.example.data.AutoReminderConfig
import com.example.data.SocietyRepository
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.net.URLEncoder

@Composable
fun RemindersScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by repository.members.collectAsState()
    val payments by repository.payments.collectAsState()
    val autoConfig by repository.autoReminderConfig.collectAsState()

    // Current month pattern (e.g. "-09-2026")
    val currentMonthPattern = remember {
        java.text.SimpleDateFormat("-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    // User Requirement 2: Members whose payment has been approved in current month are EXCLUDED from auto reminder,
    // even if they have active loan or lingering penalty dues!
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

    // Requirement 12: Members who have NOT installed the app
    val notInstalledMembers = remember(members) {
        members.filter { !it.isAppInstalled }
    }

    var showBulkSmsConfirmation by remember { mutableStateOf(false) }
    var showAppInstallBulkSms by remember { mutableStateOf(false) }

    // Auto Reminder Setup State (User Request 2)
    var isAutoEnabled by remember(autoConfig) { mutableStateOf(autoConfig.isEnabled) }
    var selectedFreq by remember(autoConfig) { mutableStateOf(autoConfig.frequency) }
    var selectedTime by remember(autoConfig) { mutableStateOf(autoConfig.preferredTime) }
    var editableTemplate by remember(autoConfig) { mutableStateOf(autoConfig.customTemplate) }
    val frequencies = listOf("Daily", "Every 2 Days", "Weekly")
    val times = listOf("10:00 AM", "02:00 PM", "06:00 PM")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        item {
            Column {
                Text("🔔 RD & LOAN REMINDER HUB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Smart Reminders (${dueMembers.size} Unpaid Due Members • ${notInstalledMembers.size} Need App)", color = AccentGold, fontSize = 11.sp)
            }
        }

        // ================== CARD 1: AUTO REMINDER SETUP & TEMPLATES (User Request 2 & 3) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, AccentGold, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
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
                            Icon(Icons.Default.Schedule, contentDescription = "Auto", tint = AccentGold, modifier = Modifier.size(18.dp))
                            Text("Auto Reminder Engine (ऑटो रिमाइंडर)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isAutoEnabled,
                            onCheckedChange = { isAutoEnabled = it }
                        )
                    }

                    Text(
                        "Frequency & Schedule:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

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

                    // Editable Template
                    Text("Push & SMS Reminder Template:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editableTemplate,
                        onValueChange = { editableTemplate = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        )
                    )

                    Button(
                        onClick = {
                            repository.updateAutoReminderConfig(
                                AutoReminderConfig(
                                    isEnabled = isAutoEnabled,
                                    frequency = selectedFreq,
                                    preferredTime = selectedTime,
                                    customTemplate = editableTemplate
                                )
                            )
                            Toast.makeText(context, "Auto Reminder Setup Saved Successfully!", Toast.LENGTH_SHORT).show()
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

        // ================== CARD 2: 1-CLICK PUSH NOTIFICATION & BULK SMS ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryGreen, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Instant Dispatch Actions (${dueMembers.size} Due Members)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1-Click Push Notification Button (With Sound & Vibration)
                        Button(
                            onClick = {
                                val count = dueMembers.size
                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = "📢 Gullak Society RD Due Alert (Sound & Vibrate)",
                                    message = "15th Due Date Reminder: $count members have pending RD/Loan payments."
                                )
                                repository.addAuditLog("PUSH REMINDER FIRED", "Triggered push alert for $count due members.")
                                Toast.makeText(context, "Push Alert sent with notification sound & vibrate!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Push", tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Alert 🔔", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // 1-Click Bulk SMS via Phone SIM
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

        // ================== CARD 3: APP NOT INSTALLED REMINDER (User Request 12) ==================
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

        // Individual Member Reminders List
        item {
            Text(
                "Individual Member Reminders (${members.size} Total)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(members, key = { it.id }) { m ->
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
                        // Individual SMS
                        Button(
                            onClick = {
                                val smsText = "Namaste ${m.name} Ji, Gullak Co-operative Society ki monthly RD (₹${m.monthlyRd}) ka payment reminder hai. Kripya 15 tarikh tak jama karein. Dhanyawad!"
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
                                val msg = "Namaste ${m.name} Ji, Gullak Society ki તરફ se aapki monthly RD (₹${m.monthlyRd}) ka reminder hai. Kripya samay par jama karein."
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

    // Bulk SMS Dispatch Dialog (Dues Members)
    if (showBulkSmsConfirmation) {
        val allNumbers = dueMembers.map { it.mobile }.filter { it.length >= 10 }
        AlertDialog(
            onDismissRequest = { showBulkSmsConfirmation = false },
            title = {
                Text("Dispatch Bulk SMS to ${allNumbers.size} Due Members", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Auto-selected Recipients (${allNumbers.size} Numbers from SIM):", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = allNumbers.joinToString(", "),
                        color = AccentGold,
                        fontSize = 10.sp,
                        maxLines = 3
                    )
                    Text("Message Template:", color = TextSecondary, fontSize = 11.sp)
                    Text(editableTemplate, color = TextPrimary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val phoneList = allNumbers.joinToString(";")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneList")).apply {
                                putExtra("sms_body", editableTemplate)
                            }
                            context.startActivity(intent)
                            repository.addAuditLog("BULK SMS DISPATCHED", "Triggered SIM Bulk SMS for ${allNumbers.size} members.")
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

    // App Install Campaign SMS Dialog (Requirement 12)
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
