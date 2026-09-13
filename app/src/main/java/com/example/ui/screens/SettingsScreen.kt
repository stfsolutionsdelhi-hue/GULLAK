package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocietyRepository
import com.example.data.SocietySettings
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: SocietyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webAppUrl by repository.webAppUrl.collectAsState()
    val isSyncing by repository.isSyncing.collectAsState()
    val syncStatus by repository.syncStatus.collectAsState()
    val isLiveSyncActive by repository.isLiveSyncActive.collectAsState()
    val isSessionLocked by repository.isSessionLocked.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val members by repository.members.collectAsState()
    val societyUpiId by repository.societyUpiId.collectAsState()
    val societyQrUri by repository.societyQrUri.collectAsState()
    val societySettings by repository.societySettings.collectAsState()

    val appDownloadUrl by repository.appDownloadUrl.collectAsState()
    var inputAppDownloadUrl by remember(appDownloadUrl) { mutableStateOf(appDownloadUrl) }

    var inputUrl by remember(webAppUrl) { mutableStateOf(webAppUrl) }
    var showUrlConfirmDialog1 by remember { mutableStateOf(false) }
    var showUrlConfirmDialog2 by remember { mutableStateOf(false) }
    var pendingUrlToSave by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var unlockPasscode by remember { mutableStateOf("") }
    var showEditUpiDialog by remember { mutableStateOf(false) }
    var upiInputText by remember(societyUpiId) { mutableStateOf(societyUpiId) }

    // Society Master Settings Form State (Only society name, upi id, payee name, and whatsapp support)
    var formSocietyName by remember(societySettings) { mutableStateOf(societySettings.societyName) }
    var formUpiId by remember(societySettings) { mutableStateOf(societySettings.societyUpiId) }
    var formUpiPayee by remember(societySettings) { mutableStateOf(societySettings.upiPayeeName) }
    var formAdminWhatsApp by remember(societySettings) { mutableStateOf(societySettings.adminWhatsApp) }

    // Admin Passkey Change State
    var oldAdminPass by remember { mutableStateOf("") }
    var newAdminPass by remember { mutableStateOf("") }
    var confirmAdminPass by remember { mutableStateOf("") }

    var isAuditLogsExpanded by remember { mutableStateOf(false) }

    val rulesAndRegulations by repository.rulesAndRegulations.collectAsState()
    var editableRulesList by remember(rulesAndRegulations) { mutableStateOf(rulesAndRegulations) }
    var newRuleInputText by remember { mutableStateOf("") }
    var isRulesExpanded by remember { mutableStateOf(false) }

    // Photo picker launcher for custom QR image upload
    val qrImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.updateSocietyQrImage(uri.toString())
            Toast.makeText(context, "Society QR Code Image Uploaded & Set Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Admin Lock Screen (Shown when locked via Logout - Requirement 5)
    if (isSessionLocked) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF450A0A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AccentRed, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Admin Panel Locked", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Enter admin passkey to unlock admin controls", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = unlockPasscode,
                onValueChange = { unlockPasscode = it },
                singleLine = true,
                placeholder = { Text("Enter admin passkey", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (repository.unlockSession(unlockPasscode)) {
                        Toast.makeText(context, "Welcome Admin! Session Unlocked.", Toast.LENGTH_SHORT).show()
                        unlockPasscode = ""
                    } else {
                        Toast.makeText(context, "Invalid key! Enter correct admin passkey to unlock.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Unlock Admin Session 🔓", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header with Compact Live Switch & Small Logout Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        color = Color(0xFF064E3B),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GULLAK ADMIN PANEL",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Admin • Society Admin (${members.size} Members)",
                        color = PrimaryGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Live Sync Status Switch (Requirement 11)
                Surface(
                    onClick = {
                        val state = repository.toggleLiveSync()
                        val msg = if (state) "Live Sync is ACTIVE 🟢" else "Live Sync is PAUSED ⏸"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLiveSyncActive) PrimaryGreenDark else Color(0xFF451A03),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveSyncActive) PrimaryGreen else AccentGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isLiveSyncActive) "🟢 LIVE" else "⏸ PAUSE",
                            color = if (isLiveSyncActive) PrimaryGreen else AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Small Clean Logout Button
                Surface(
                    onClick = { showLogoutDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AccentRed, modifier = Modifier.size(13.dp))
                        Text("Logout", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ================== CARD 1: GOOGLE SHEETS LIVE SYNC (Requirement 1, 2, 3, 4 - Editable URL with Double Confirmation) ==================
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Google Sheets & Web App Live Sync",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Frontend Controlled: Poori app aur sabhi member portals isi URL se live sync honge.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Web App Script URL Field with Label Above
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Web App Script URL (Editable & Auto-filled)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://script.google.com/macros/s/.../exec", color = TextMuted, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                            val item = clipboard.primaryClip?.getItemAt(0)
                                            val text = item?.text?.toString() ?: ""
                                            if (text.isNotEmpty()) {
                                                inputUrl = text
                                                Toast.makeText(context, "Pasted from Clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = AccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = AccentGold,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    // App Download URL Field with Label Above
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "App Download URL (For Invite SMS)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = inputAppDownloadUrl,
                            onValueChange = { 
                                inputAppDownloadUrl = it
                                repository.updateAppDownloadUrl(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://gullaksociety.in/download", color = TextMuted, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = AccentGold,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    if (syncStatus.isNotEmpty()) {
                        Text(
                            text = syncStatus,
                            color = if (syncStatus.contains("Error") || syncStatus.contains("Failed") || syncStatus.contains("PAUSED")) AccentRed else PrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val cleanUrl = inputUrl.trim()
                                if (cleanUrl.isNotEmpty() && cleanUrl != webAppUrl) {
                                    pendingUrlToSave = cleanUrl
                                    showUrlConfirmDialog1 = true
                                } else {
                                    // Trigger sync directly with existing saved URL
                                    coroutineScope.launch {
                                        val result = repository.syncWithGoogleSheet()
                                        Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark),
                            shape = RoundedCornerShape(6.dp),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (inputUrl.trim() != webAppUrl) "Update URL & Sync" else "Live Sync", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                repository.refreshAllMembersFromDatabase()
                                Toast.makeText(context, "All ${members.size} Society Members Refreshed!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.People, contentDescription = "Members", tint = AccentGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh Data", color = AccentGold, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val urlToOpen = if (inputUrl.trim().isNotEmpty()) inputUrl.trim() else "https://docs.google.com/spreadsheets"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen))
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Open", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Google Sheet / Web Portal Live Preview", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ================== CARD 2: SOCIETY MASTER SETTINGS (Requirement 2: Only Society Name, UPI ID, Payee Name, Admin WhatsApp) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Master Settings",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "⚙️ Society Master Settings",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Global default RD & Loan Interest Rate are centrally managed by Google Sheets Web App.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    // Society Name
                    OutlinedTextField(
                        value = formSocietyName,
                        onValueChange = { formSocietyName = it },
                        label = { Text("Society Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Society UPI ID
                    OutlinedTextField(
                        value = formUpiId,
                        onValueChange = { formUpiId = it },
                        label = { Text("Society UPI ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // UPI Payee Name
                    OutlinedTextField(
                        value = formUpiPayee,
                        onValueChange = { formUpiPayee = it },
                        label = { Text("UPI Payee Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Admin WhatsApp / Support Mobile
                    OutlinedTextField(
                        value = formAdminWhatsApp,
                        onValueChange = { formAdminWhatsApp = it },
                        label = { Text("Admin WhatsApp / Support Mobile") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Save Society Settings Button
                    Button(
                        onClick = {
                            val updated = societySettings.copy(
                                societyName = formSocietyName.trim(),
                                societyUpiId = formUpiId.trim(),
                                upiPayeeName = formUpiPayee.trim(),
                                adminWhatsApp = formAdminWhatsApp.trim()
                            )
                            repository.updateSocietySettings(updated)
                            Toast.makeText(context, "Society Master Settings Saved Successfully! ✅", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Society Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // ================== CARD 2B: SECURE ADMIN MASTER PASSWORD / PASSKEY (Requirement 4) ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF451A03), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181512)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "🔐 Admin Master Password / Passkey",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Set a secure custom password so no member can access admin settings or unlock admin sessions.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = oldAdminPass,
                        onValueChange = { oldAdminPass = it },
                        label = { Text("Current Passkey (Default: society)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAdminPass,
                            onValueChange = { newAdminPass = it },
                            label = { Text("New Passkey") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = confirmAdminPass,
                            onValueChange = { confirmAdminPass = it },
                            label = { Text("Confirm New") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (newAdminPass.trim().isEmpty()) {
                                Toast.makeText(context, "New passkey cannot be empty!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newAdminPass.trim() != confirmAdminPass.trim()) {
                                Toast.makeText(context, "New passkeys do not match!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (repository.updateAdminPassword(oldAdminPass, newAdminPass)) {
                                Toast.makeText(context, "Admin Master Password Changed Successfully! 🔐", Toast.LENGTH_SHORT).show()
                                oldAdminPass = ""
                                newAdminPass = ""
                                confirmAdminPass = ""
                            } else {
                                Toast.makeText(context, "Incorrect current passkey! Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = "Key", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Admin Passkey", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ================== CARD 3: OFFICIAL SOCIETY PAYMENT QR CODE ==================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEAB308), RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181512)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "📲 Official Society Payment QR Code",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        text = "Admin dwara upload kiya gaya QR code sabhi members ko online pay me dikhega",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // QR Code Preview Box
                    Surface(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (societyQrUri != null) {
                                coil.compose.AsyncImage(
                                    model = societyQrUri,
                                    contentDescription = "Society QR Code",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .padding(4.dp)
                                )
                            } else {
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    drawRect(color = Color.Black, size = Size(size.width, size.height), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                                    val finderSize = 22.dp.toPx()
                                    drawRect(color = Color.Black, topLeft = Offset(4f, 4f), size = Size(finderSize, finderSize))
                                    drawRect(color = Color.Black, topLeft = Offset(size.width - finderSize - 4f, 4f), size = Size(finderSize, finderSize))
                                    drawRect(color = Color.Black, topLeft = Offset(4f, size.height - finderSize - 4f), size = Size(finderSize, finderSize))
                                    drawCircle(color = Color(0xFF047857), radius = 8.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (societyQrUri != null) "CUSTOM QR ACTIVE" else "DEFAULT QR SET",
                                color = Color(0xFF0F172A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = societyUpiId,
                                color = Color(0xFF047857),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action Buttons for QR & UPI ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { qrImageLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF854D0E)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Upload", tint = AccentGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload QR 📷", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showEditUpiDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set UPI ID ✍️", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }

                    // Test "Pay with Any UPI App" Button
                    OutlinedButton(
                        onClick = {
                            val upiUri = Uri.parse("upi://pay?pa=$societyUpiId&pn=${Uri.encode(societySettings.upiPayeeName)}&am=400&cu=INR&tn=Gullak%20Society%20Test")
                            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                val chooser = Intent.createChooser(upiIntent, "Pay via Any UPI App (GPay, PhonePe, Paytm, BHIM)")
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI App found on this device.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen))
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Test", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test \"Pay with Any UPI App\" Forward 🚀", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ================== CARD 4: SECURITY & LOGOUT ==================
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
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("Admin Session Security", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Passkey protected logout ('society')", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Logout", tint = AccentGold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout 🔒", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Card 4.5: Manage Rules & Regulations
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isRulesExpanded = !isRulesExpanded }
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Rules",
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Manage Rules & Regulations 📜",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add, edit, or delete official society guidelines",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isRulesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isRulesExpanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isRulesExpanded) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Official Rules & Regulations",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        if (editableRulesList.isEmpty()) {
                            Text("No rules added yet. Add guidelines below.", color = TextMuted, fontSize = 11.sp)
                        } else {
                            editableRulesList.forEachIndexed { idx, rule ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            color = AccentGold,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = rule,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editableRulesList = editableRulesList.toMutableList().apply { removeAt(idx) }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Rule",
                                            tint = AccentRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (idx < editableRulesList.size - 1) {
                                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 1.dp)

                        // Add new rule input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newRuleInputText,
                                onValueChange = { newRuleInputText = it },
                                placeholder = { Text("Enter rule in Hindi or English...", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = false,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGold,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = BgDark,
                                    unfocusedContainerColor = BgDark
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            Button(
                                onClick = {
                                    if (newRuleInputText.trim().isNotEmpty()) {
                                        editableRulesList = editableRulesList + newRuleInputText.trim()
                                        newRuleInputText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Add", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Save all rules button
                        Button(
                            onClick = {
                                repository.updateRules(editableRulesList)
                                Toast.makeText(context, "Rules and Regulations Saved Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = Color(0xFF064E3B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE SYSTEM RULES 💾", color = Color(0xFF064E3B), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Card 5: System Audit Logs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAuditLogsExpanded = !isAuditLogsExpanded }
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Logs",
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "System Audit Logs (${auditLogs.size})",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (isAuditLogsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isAuditLogsExpanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isAuditLogsExpanded) {
            items(auditLogs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = log.details,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = log.timestamp,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }

    // ================== DIALOG 1: WEB APP URL FIRST CONFIRMATION ==================
    if (showUrlConfirmDialog1) {
        AlertDialog(
            onDismissRequest = { showUrlConfirmDialog1 = false },
            title = {
                Text("⚠️ Change Society Web App URL?", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Aap Society Google Apps Script Web App URL change kar rahe hain. Isse poori Android app aur Member Portal ka live data endpoint badal jayega.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Text("New URL:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(pendingUrlToSave, color = AccentGold, fontSize = 10.sp)
                    Text("Kya aap aage badhna chahte hain?", color = TextSecondary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUrlConfirmDialog1 = false
                        showUrlConfirmDialog2 = true // Trigger Double Confirmation
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text("Yes, Proceed ➡️", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlConfirmDialog1 = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ================== DIALOG 2: WEB APP URL DOUBLE CONFIRMATION (Requirement 1: Type 12345 in fade placeholder to confirm) ==================
    if (showUrlConfirmDialog2) {
        var confirmCodeInput by remember { mutableStateOf("") }
        val isCodeValid = confirmCodeInput.trim() == "12345"

        AlertDialog(
            onDismissRequest = { showUrlConfirmDialog2 = false },
            title = {
                Text("🔴 Step 2: Confirm URL Update", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Kripya dhyan se check karein ki URL me koi typing mistake to nahi hai:",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = Color(0xFF181512),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = pendingUrlToSave,
                            color = PrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(
                        "Safety Verification: Type 12345 to confirm URL update:",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = confirmCodeInput,
                        onValueChange = { confirmCodeInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("12345", color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCodeValid) PrimaryGreen else CardBorder,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = PrimaryGreen,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Text(
                        "Confirm karne par ye URL SharedPreferences me save ho jayegi aur turant live sync start hoga.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isCodeValid) {
                            repository.saveWebAppUrl(pendingUrlToSave)
                            showUrlConfirmDialog2 = false
                            Toast.makeText(context, "Web App URL Successfully Saved & Applied! ✅", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                val result = repository.syncWithGoogleSheet()
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = isCodeValid,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("CONFIRM & APPLY NOW ✅", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlConfirmDialog2 = false }) {
                    Text("Go Back", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Edit Society UPI ID Dialog
    if (showEditUpiDialog) {
        AlertDialog(
            onDismissRequest = { showEditUpiDialog = false },
            title = {
                Text("Set Official Society UPI ID", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter official UPI VPA for payments:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = upiInputText,
                        onValueChange = { upiInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. gullaksociety@okaxis", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (upiInputText.trim().isNotEmpty()) {
                            repository.updateSocietyUpiId(upiInputText.trim())
                            showEditUpiDialog = false
                            Toast.makeText(context, "Society UPI ID Updated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save UPI ID", color = Color(0xFF064E3B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUpiDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Logout Confirmation Dialog (Admin passkey required for logout)
    if (showLogoutDialog) {
        var logoutConfirmInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Confirm Admin Logout", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Admin Panel ko lock karne ke liye admin passkey darj karein:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = logoutConfirmInput,
                        onValueChange = { logoutConfirmInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter admin passkey", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repository.verifyAdminPassword(logoutConfirmInput)) {
                            repository.logoutAdmin()
                            showLogoutDialog = false
                            Toast.makeText(context, "Admin Logged Out & Locked.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter correct admin passkey to confirm logout!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Confirm Logout 🔒", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
