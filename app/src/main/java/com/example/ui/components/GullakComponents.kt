package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.ui.theme.GullakClay
import com.example.ui.theme.GullakDanger
import com.example.ui.theme.GullakDangerContainer
import com.example.ui.theme.GullakGold
import com.example.ui.theme.GullakGoldContainer
import com.example.ui.theme.GullakGoldLight
import com.example.ui.theme.GullakInfo
import com.example.ui.theme.GullakInfoContainer
import com.example.ui.theme.GullakNavyDark
import com.example.ui.theme.GullakPrimary
import com.example.ui.theme.GullakPrimaryLight
import com.example.ui.theme.GullakSuccess
import com.example.ui.theme.GullakSuccessContainer
import com.example.ui.theme.GullakWarning
import com.example.ui.theme.GullakWarningContainer
import java.text.NumberFormat
import java.util.Locale

// Helper for Indian Currency format (₹ 40,000)
fun formatRupees(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace("INR", "₹").replace(".00", "")
}

// WhatsApp Intent Trigger Helper
fun openWhatsApp(context: Context, mobileNumber: String, prefilledText: String) {
    try {
        val cleanNumber = mobileNumber.replace("+", "").replace(" ", "").trim()
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(prefilledText)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp nahi khula. Number: $mobileNumber", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GullakTopBar(
    title: String,
    subtitle: String? = null,
    onLogoutClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    unreadNotificationCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GullakPrimary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("topbar_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GullakGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        actions = {
            if (onNotificationClick != null) {
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onNotificationClick, modifier = Modifier.testTag("topbar_notif_btn")) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(GullakDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            if (onLogoutClick != null) {
                IconButton(onClick = onLogoutClick, modifier = Modifier.testTag("topbar_logout_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        }
    )
}

@Composable
fun StatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        PaymentStatus.APPROVED -> Triple(GullakSuccessContainer, GullakSuccess, "APPROVED ✅")
        PaymentStatus.PENDING -> Triple(GullakWarningContainer, GullakClay, "PENDING ⏳")
        PaymentStatus.REJECTED -> Triple(GullakDangerContainer, GullakDanger, "REJECTED ❌")
        PaymentStatus.APPROVED_WITH_EDIT -> Triple(GullakInfoContainer, GullakInfo, "APPROVED (EDITED) ✏️")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AccountStatusBadge(
    status: AccountStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        AccountStatus.ACTIVE -> Triple(GullakSuccessContainer, GullakSuccess, "Active")
        AccountStatus.INACTIVE -> Triple(GullakWarningContainer, GullakClay, "Inactive")
        AccountStatus.DELETED -> Triple(GullakDangerContainer, GullakDanger, "Deleted")
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    hindiTitle: String,
    amount: Double,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hindiTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formatRupees(amount),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (amount > 0 && iconColor == GullakDanger) GullakDanger else iconColor
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Cash Payment Modal Dialog
@Composable
fun CashPaymentModal(
    defaultAmount: Double,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, month: String, remarks: String) -> Unit
) {
    var amountText by remember { mutableStateOf(defaultAmount.toInt().toString()) }
    var monthText by remember { mutableStateOf("August 2026") }
    var remarksText by remember { mutableStateOf("Cash submitted to Society Admin") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = GullakGold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text("Cash Payment / नकद भुगतान", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Pay Now → Cash", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it },
                    label = { Text("Month (महीना)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_month_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Remarks (टिप्पणी / विवरण)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_remarks_input"),
                    maxLines = 2
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = GullakDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    color = GullakGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ Cash submit karne ke baad Admin ise verify karke approve karenge.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F)),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMsg = "Kripya sahi amount dalein (Please enter valid amount)"
                    } else {
                        onSubmit(amt, monthText, remarksText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakPrimary),
                modifier = Modifier.testTag("cash_submit_btn")
            ) {
                Text("Submit Request / जमा करें")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("cash_cancel_btn")) {
                Text("Cancel / रद्द करें")
            }
        }
    )
}

// Online UPI Payment Modal Dialog
@Composable
fun OnlinePaymentModal(
    defaultAmount: Double,
    upiId: String,
    payeeName: String,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, remarks: String) -> Unit
) {
    var amountText by remember { mutableStateOf(defaultAmount.toInt().toString()) }
    var refRemarks by remember { mutableStateOf("UPI Payment Ref No: ") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = GullakPrimaryLight,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text("Pay Online / UPI QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Scan & Pay with Google Pay, PhonePe, Paytm", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual UPI QR Canvas Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, GullakPrimaryLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = payeeName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GullakNavyDark)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Stylized QR pattern box
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF0F172A)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "QR",
                                        tint = Color.White,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        text = "SCAN TO PAY",
                                        color = GullakGoldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Copy UPI ID row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(upiId))
                                    Toast
                                        .makeText(context, "UPI ID Copied: $upiId", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "UPI: $upiId",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = GullakPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = GullakPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("online_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                OutlinedTextField(
                    value = refRemarks,
                    onValueChange = { refRemarks = it },
                    label = { Text("UPI UTR / Reference No / Remarks") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("online_ref_input"),
                    maxLines = 2
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = GullakDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMsg = "Kripya sahi amount dalein"
                    } else {
                        onSubmit(amt, refRemarks)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                modifier = Modifier.testTag("online_submit_btn")
            ) {
                Text("Payment Done / Submit for Approval")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("online_cancel_btn")) {
                Text("Cancel")
            }
        }
    )
}

// Payment Rejection Dialog with Standard Master Spec Reasons
@Composable
fun RejectPaymentDialog(
    memberName: String,
    amount: Double,
    onDismiss: () -> Unit,
    onConfirmReject: (reason: String) -> Unit
) {
    val reasons = listOf(
        "Amount abhi prapt nahi hua (Amount not received)",
        "Payment verify nahi hua (Payment not verified)",
        "Galat Amount (Incorrect amount)",
        "Screenshot / UTR Required (Screenshot needed)",
        "Other (Any other reason)"
    )
    var selectedIndex by remember { mutableStateOf(0) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GullakDanger, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Reject Payment / भुगतान अस्वीकार", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Member: $memberName\nAmount: ${formatRupees(amount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Text(
                    text = "Select Rejection Reason (कारण चुनें):",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                reasons.forEachIndexed { index, reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = (selectedIndex == index),
                            onClick = { selectedIndex = index },
                            colors = RadioButtonDefaults.colors(selectedColor = GullakDanger)
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                if (selectedIndex == 4) {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Custom Reason / कारण लिखें") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reject_custom_reason_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedIndex == 4 && customReason.isNotBlank()) {
                        customReason.trim()
                    } else {
                        reasons[selectedIndex]
                    }
                    onConfirmReject(finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakDanger),
                modifier = Modifier.testTag("reject_confirm_btn")
            ) {
                Text("Reject Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("reject_cancel_btn")) {
                Text("Cancel")
            }
        }
    )
}

// Approve With Edit Dialog
@Composable
fun ApproveWithEditDialog(
    memberName: String,
    originalAmount: Double,
    onDismiss: () -> Unit,
    onConfirmApproveEdit: (editedAmount: Double) -> Unit
) {
    var editedAmountText by remember { mutableStateOf(originalAmount.toInt().toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Edit, contentDescription = null, tint = GullakInfo, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Approve with Edit / राशि बदलकर स्वीकार करें", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Member: $memberName",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    color = GullakInfoContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Original Amount Submitted: ${formatRupees(originalAmount)}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GullakInfo, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Admin dwara badli gayi amount hi Member ke account mein adjust hogi.",
                            style = MaterialTheme.typography.bodySmall.copy(color = GullakNavyDark)
                        )
                    }
                }

                OutlinedTextField(
                    value = editedAmountText,
                    onValueChange = { editedAmountText = it },
                    label = { Text("Edited Amount (बदली हुई राशि)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approve_edit_amount_input"),
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = GullakDanger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = editedAmountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMsg = "Kripya valid amount dalein"
                    } else {
                        onConfirmApproveEdit(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakInfo),
                modifier = Modifier.testTag("approve_edit_confirm_btn")
            ) {
                Text("Approve & Adjust")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("approve_edit_cancel_btn")) {
                Text("Cancel")
            }
        }
    )
}

// Confirmation Dialog Generic
@Composable
fun GullakConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    cancelButtonText: String = "Cancel",
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDanger) GullakDanger else GullakPrimary
                ),
                modifier = Modifier.testTag("dialog_confirm_btn")
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_btn")) {
                Text(cancelButtonText)
            }
        }
    )
}

// -------------------------------------------------------------
// 1. Comprehensive 4-Column Member "Pay Now" Dialog
// -------------------------------------------------------------
@Composable
fun PayNowComprehensiveDialog(
    monthlyRdDefault: Double,
    interestDueDefault: Double,
    penaltyDueDefault: Double,
    loanOutstanding: Double,
    upiId: String,
    payeeName: String,
    onDismiss: () -> Unit,
    onSubmitPayment: (
        totalAmount: Double,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        paymentType: PaymentType,
        paymentMode: String,
        remarks: String
    ) -> Unit
) {
    var rdText by remember { mutableStateOf(monthlyRdDefault.toInt().toString()) }
    var interestText by remember { mutableStateOf(interestDueDefault.toInt().toString()) }
    var penaltyText by remember { mutableStateOf(penaltyDueDefault.toInt().toString()) }
    var loanReturnText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("ONLINE_UPI") } // ONLINE_UPI or CASH_OFFICE
    var remarksText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val rdVal = rdText.toDoubleOrNull() ?: 0.0
    val intVal = interestText.toDoubleOrNull() ?: 0.0
    val penVal = penaltyText.toDoubleOrNull() ?: 0.0
    val loanVal = loanReturnText.toDoubleOrNull() ?: 0.0
    val calculatedTotal = rdVal + intVal + penVal + loanVal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = GullakGold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text("Pay Monthly Dues / भुगतान करें", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("4-Column Granular Breakdown", fontSize = 11.sp, color = GullakGoldLight)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Mode Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedPaymentMode = "ONLINE_UPI" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPaymentMode == "ONLINE_UPI") GullakGold else Color.Transparent,
                            contentColor = if (selectedPaymentMode == "ONLINE_UPI") Color(0xFF0F172A) else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Online UPI QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedPaymentMode = "CASH_OFFICE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPaymentMode == "CASH_OFFICE") GullakGold else Color.Transparent,
                            contentColor = if (selectedPaymentMode == "CASH_OFFICE") Color(0xFF0F172A) else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Office Cash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 4 Breakdown Columns in 2x2 grid or compact column list
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                    border = BorderStroke(1.dp, GullakGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rdText,
                                onValueChange = { rdText = it },
                                label = { Text("1. RD (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = interestText,
                                onValueChange = { interestText = it },
                                label = { Text("2. Interest (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = penaltyText,
                                onValueChange = { penaltyText = it },
                                label = { Text("3. Penalty (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = loanReturnText,
                                onValueChange = { loanReturnText = it },
                                label = { Text("4. Loan Return (₹)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }

                // Total Calculation Display Box
                Surface(
                    color = GullakGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Payable Amount:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F),
                            fontSize = 13.sp
                        )
                        Text(
                            text = formatRupees(calculatedTotal),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F),
                            fontSize = 17.sp
                        )
                    }
                }

                if (selectedPaymentMode == "ONLINE_UPI") {
                    // UPI Launch and Copy Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPI: $upiId",
                            style = MaterialTheme.typography.bodySmall.copy(color = GullakGoldLight),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            clipboardManager.setText(AnnotatedString(upiId))
                            Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Copy UPI", fontSize = 11.sp, color = GullakGold)
                        }
                    }

                    Button(
                        onClick = {
                            val upiUri = Uri.parse("upi://pay?pa=$upiId&pn=${Uri.encode(payeeName)}&am=$calculatedTotal&cu=INR&tn=SocietyDues")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                context.startActivity(Intent.createChooser(intent, "Pay with UPI App"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI App found. Copy UPI ID to pay.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open GPay / PhonePe / Paytm (₹$calculatedTotal)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("UTR No / Ref / Remarks (टिप्पणी)") },
                    placeholder = { Text("e.g. UTR 4238910293") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = GullakDanger, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (calculatedTotal <= 0) {
                        errorMsg = "Total payment amount ₹0 se jyada honi chahiye"
                    } else {
                        val paymentType = if (selectedPaymentMode == "ONLINE_UPI") PaymentType.ONLINE else PaymentType.CASH
                        onSubmitPayment(
                            calculatedTotal,
                            rdVal,
                            intVal,
                            penVal,
                            loanVal,
                            paymentType,
                            selectedPaymentMode,
                            remarksText.ifBlank { "Monthly Payment" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakGold, contentColor = Color(0xFF0F172A))
            ) {
                Text("Submit for Approval ✅", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// 2. Admin Detailed Approval & Adjustment Dialog (with 4 columns)
// -------------------------------------------------------------
@Composable
fun AdminDetailedApprovalDialog(
    paymentId: Long,
    userName: String,
    submittedAmount: Double,
    initialRd: Double,
    initialInt: Double,
    initialPen: Double,
    initialLoan: Double,
    initialRemarks: String,
    onDismiss: () -> Unit,
    onApprove: (
        approvedAmount: Double,
        rd: Double,
        int: Double,
        pen: Double,
        loan: Double,
        adminRemarks: String
    ) -> Unit,
    onReject: () -> Unit
) {
    var rdText by remember { mutableStateOf(initialRd.toInt().toString()) }
    var intText by remember { mutableStateOf(initialInt.toInt().toString()) }
    var penText by remember { mutableStateOf(initialPen.toInt().toString()) }
    var loanText by remember { mutableStateOf(initialLoan.toInt().toString()) }
    var adminRemarksText by remember { mutableStateOf("Verified & Approved") }

    val rd = rdText.toDoubleOrNull() ?: 0.0
    val int = intText.toDoubleOrNull() ?: 0.0
    val pen = penText.toDoubleOrNull() ?: 0.0
    val loan = loanText.toDoubleOrNull() ?: 0.0
    val totalApproved = rd + int + pen + loan

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Verify & Approve Payment 💳", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Member: $userName | Submitted: ${formatRupees(submittedAmount)}", fontSize = 12.sp, color = GullakGoldLight)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialRemarks.isNotBlank()) {
                    Text(
                        text = "Member Note: $initialRemarks",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                }

                Text("Adjust 4-Column Breakdown if required:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rdText,
                        onValueChange = { rdText = it },
                        label = { Text("RD (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = intText,
                        onValueChange = { intText = it },
                        label = { Text("Interest (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = penText,
                        onValueChange = { penText = it },
                        label = { Text("Penalty (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = loanText,
                        onValueChange = { loanText = it },
                        label = { Text("Loan Return (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    color = GullakGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Final Approved Total:", fontWeight = FontWeight.Bold, color = Color(0xFF78350F), fontSize = 12.sp)
                        Text(formatRupees(totalApproved), fontWeight = FontWeight.ExtraBold, color = Color(0xFF78350F), fontSize = 14.sp)
                    }
                }

                OutlinedTextField(
                    value = adminRemarksText,
                    onValueChange = { adminRemarksText = it },
                    label = { Text("Admin Remarks / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApprove(totalApproved, rd, int, pen, loan, adminRemarksText) },
                colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess)
            ) {
                Text("Approve Payment ✅")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = GullakDanger)
                ) {
                    Text("Reject ❌")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// -------------------------------------------------------------
// 3. Admin Direct Office Cash Entry Dialog
// -------------------------------------------------------------
@Composable
fun AdminRecordOfficeCashDialog(
    membersList: List<com.example.data.model.UserEntity>,
    financialsMap: Map<String, com.example.data.model.MemberFinancialEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        userId: String,
        amount: Double,
        rd: Double,
        int: Double,
        pen: Double,
        loan: Double,
        mode: String,
        remarks: String,
        adminRemarks: String
    ) -> Unit
) {
    var selectedUserId by remember { mutableStateOf(membersList.firstOrNull()?.userId ?: "") }
    val selectedMember = membersList.find { it.userId == selectedUserId }
    val fin = financialsMap[selectedUserId]

    var rdText by remember { mutableStateOf("400") }
    var intText by remember { mutableStateOf(fin?.interestDue?.toInt()?.toString() ?: "0") }
    var penText by remember { mutableStateOf(fin?.calculateLivePenalty()?.toInt()?.toString() ?: "0") }
    var loanText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("OFFICE_CASH") }
    var adminRemarks by remember { mutableStateOf("Office Cash Verified by Admin") }

    val rd = rdText.toDoubleOrNull() ?: 0.0
    val int = intText.toDoubleOrNull() ?: 0.0
    val pen = penText.toDoubleOrNull() ?: 0.0
    val loan = loanText.toDoubleOrNull() ?: 0.0
    val total = rd + int + pen + loan

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Record Office Payment 💵", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Direct cash deposit by member", fontSize = 11.sp, color = GullakGoldLight)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Member (सदस्य चुनें):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                // Simple Member Quick Selector
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        membersList.take(6).forEach { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUserId = user.userId
                                        val uFin = financialsMap[user.userId]
                                        intText = (uFin?.interestDue?.toInt() ?: 0).toString()
                                        penText = (uFin?.calculateLivePenalty()?.toInt() ?: 0).toString()
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedUserId == user.userId),
                                    onClick = {
                                        selectedUserId = user.userId
                                        val uFin = financialsMap[user.userId]
                                        intText = (uFin?.interestDue?.toInt() ?: 0).toString()
                                        penText = (uFin?.calculateLivePenalty()?.toInt() ?: 0).toString()
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = GullakGold)
                                )
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("ID: ${user.userId} | Mob: ${user.mobile}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rdText,
                        onValueChange = { rdText = it },
                        label = { Text("RD (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = intText,
                        onValueChange = { intText = it },
                        label = { Text("Interest (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = penText,
                        onValueChange = { penText = it },
                        label = { Text("Penalty (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = loanText,
                        onValueChange = { loanText = it },
                        label = { Text("Loan Return (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    color = GullakGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Cash Deposited:", fontWeight = FontWeight.Bold, color = Color(0xFF78350F), fontSize = 12.sp)
                        Text(formatRupees(total), fontWeight = FontWeight.ExtraBold, color = Color(0xFF78350F), fontSize = 14.sp)
                    }
                }

                OutlinedTextField(
                    value = adminRemarks,
                    onValueChange = { adminRemarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUserId.isNotBlank() && total > 0) {
                        onSubmit(selectedUserId, total, rd, int, pen, loan, mode, "Office Cash", adminRemarks)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GullakGold, contentColor = Color(0xFF0F172A))
            ) {
                Text("Record & Deposit 💵", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// 4. Outstanding Defaulters & Instant WhatsApp / SMS Dispatch Dialog
// -------------------------------------------------------------
@Composable
fun OutstandingDefaultersDialog(
    metricTitle: String,
    membersWithDues: List<Pair<com.example.data.model.UserEntity, com.example.data.model.MemberFinancialEntity>>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Outstanding Dues List: $metricTitle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${membersWithDues.size} Members have pending amounts", fontSize = 12.sp, color = GullakGoldLight)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (membersWithDues.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No pending dues found in this category! 🎉", color = GullakSuccess, fontWeight = FontWeight.Bold)
                    }
                } else {
                    membersWithDues.forEach { (user, fin) ->
                        val pen = fin.calculateLivePenalty()
                        val totalDue = fin.currentRdDue + fin.interestDue + pen
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text("RD: ₹${fin.currentRdDue.toInt()} | Int: ₹${fin.interestDue.toInt()} | Pen: ₹${pen.toInt()}", fontSize = 11.sp, color = GullakGoldLight)
                                    Text("Loan Balance: ₹${fin.loanOutstanding.toInt()}", fontSize = 10.sp, color = Color.LightGray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatRupees(totalDue), fontWeight = FontWeight.ExtraBold, color = GullakDanger, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // WhatsApp Action
                                        Button(
                                            onClick = {
                                                val msg = "Namaste ${user.name} ji, Gullak Society mein aapki kul deynari ₹${totalDue.toInt()} hai (RD: ₹${fin.currentRdDue.toInt()}, Interest: ₹${fin.interestDue.toInt()}, Penalty: ₹${pen.toInt()}). Kripya samay par jama karein."
                                                com.example.util.SmsHelper.openWhatsApp(context, user.mobile, msg)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GullakSuccess),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("WA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        // SMS Action
                                        Button(
                                            onClick = {
                                                val msg = "Gullak Society Alert: ${user.name} ji, aapke dues ₹${totalDue.toInt()} pending hain. Pay via App/UPI."
                                                com.example.util.SmsHelper.sendSmsViaSim(context, user.mobile, msg, 0)
                                                Toast.makeText(context, "SMS sent to ${user.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GullakGold, contentColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("SMS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GullakGold, contentColor = Color(0xFF0F172A))) {
                Text("Done")
            }
        }
    )
}

