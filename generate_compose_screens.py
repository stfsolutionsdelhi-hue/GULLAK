import os

# 1. SettingsScreen.kt
settings_screen = """package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.data.SocietyRepository
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
    val auditLogs by repository.auditLogs.collectAsState()
    val members by repository.members.collectAsState()

    var inputUrl by remember(webAppUrl) { mutableStateOf(webAppUrl) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        color = Color(0xFF064E3B),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                Column {
                    Text(
                        text = "GULLAK ADMIN",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Admin • Society Admin (${members.size} Members)",
                        color = PrimaryGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Card 1: Google Sheets Software & 2-Way Live Sync
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryGreen, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Cloud",
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Google Sheets Software & 2-Way Live Sync",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "लैपटॉप/डेस्कटॉप वेब डैशबोर्ड व लाइव डेटा सिंक (100% Free)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Google Apps Script Web App URL paste karein jisse App aur Google Sheet aapas me live synchronize ho sakein:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // URL Input Field
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Google Apps Script Web App URL",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                    val item = clipboard.primaryClip?.getItemAt(0)
                                    val text = item?.text?.toString() ?: ""
                                    if (text.isNotEmpty()) {
                                        inputUrl = text
                                        repository.saveWebAppUrl(text)
                                        Toast.makeText(context, "URL Pasted & Saved!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = AccentGold
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
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Sync Status message if active
                    if (syncStatus.isNotEmpty()) {
                        Text(
                            text = syncStatus,
                            color = if (syncStatus.contains("Error") || syncStatus.contains("Failed")) AccentRed else PrimaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Button 1: Live Sync Now
                    Button(
                        onClick = {
                            if (inputUrl.trim().isNotEmpty()) {
                                repository.saveWebAppUrl(inputUrl.trim())
                            }
                            coroutineScope.launch {
                                val result = repository.syncWithGoogleSheet()
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synchronizing...", color = TextPrimary, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔄 Live Sync 🔄", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Button 2: Load / Restore 67 Real Members
                    Button(
                        onClick = {
                            repository.restore67RealMembers()
                            Toast.makeText(context, "All 67 Real Society Members Loaded!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = "Members", tint = AccentGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📥 Load / Restore 67 Real Members", color = AccentGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    // Button 3: Open Google Sheet / Web Portal Live Preview
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
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen))
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Open", tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("👁 Open Google Sheet / Web Portal Live Preview 👁", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Card 2: Advance Settings (एडवांस सेटिंग्स)
        item {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Advance Settings (एडवांस सेटिंग्स)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Admin Session & System Safety Controls. Panel se logout karne ke liye neeche button ka upyog karein.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = {
                            Toast.makeText(context, "Admin Session Locked", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Logout", tint = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔒 Logout Admin Panel (सुरक्षित लॉगआउट) 🔒", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 3: System Audit Logs
        item {
            Text(
                text = "System Audit Logs (${auditLogs.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(auditLogs) { log ->
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
                        Text(
                            text = log.title,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = log.details,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = log.timestamp,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w", encoding="utf-8") as f:
    f.write(settings_screen)

print("SettingsScreen.kt created.")
