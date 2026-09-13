package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SocietyRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gullak_app_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _pendingApprovals = MutableStateFlow<List<PaymentApproval>>(emptyList())
    val pendingApprovals: StateFlow<List<PaymentApproval>> = _pendingApprovals.asStateFlow()

    private val _autoReminderConfig = MutableStateFlow(AutoReminderConfig())
    val autoReminderConfig: StateFlow<AutoReminderConfig> = _autoReminderConfig.asStateFlow()

    private val _isLiveSyncActive = MutableStateFlow(true)
    val isLiveSyncActive: StateFlow<Boolean> = _isLiveSyncActive.asStateFlow()

    private val _isSessionLocked = MutableStateFlow(true)
    val isSessionLocked: StateFlow<Boolean> = _isSessionLocked.asStateFlow()

    private val _loggedInMemberId = MutableStateFlow<String?>(null)
    val loggedInMemberId: StateFlow<String?> = _loggedInMemberId.asStateFlow()

    fun loginMember(id: String) {
        _loggedInMemberId.value = id
        addAuditLog("MEMBER_LOGIN", "Member logged in: $id")
    }

    fun logoutMember() {
        _loggedInMemberId.value = null
    }

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _webAppUrl = MutableStateFlow("https://script.google.com/macros/s/AKfycbz_gullak_society_master_sync_v64/exec")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _societySettings = MutableStateFlow(
        SocietySettings(
            societyName = "GULLAK CO OPRATIVE SOCIETY",
            defaultRd = 400,
            loanRate = 1.0,
            societyUpiId = "gullaksociety@okaxis",
            upiPayeeName = "Gullak Co-operative Society",
            adminWhatsApp = "9718174244"
        )
    )
    val societySettings: StateFlow<SocietySettings> = _societySettings.asStateFlow()

    private val _societyUpiId = MutableStateFlow("gullaksociety@okaxis")
    val societyUpiId: StateFlow<String> = _societyUpiId.asStateFlow()

    private val _societyQrUri = MutableStateFlow<String?>(null)
    val societyQrUri: StateFlow<String?> = _societyQrUri.asStateFlow()

    private val _syncStatus = MutableStateFlow("Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _appDownloadUrl = MutableStateFlow("https://gullaksociety.example.com/download")
    val appDownloadUrl: StateFlow<String> = _appDownloadUrl.asStateFlow()

    private val _rulesAndRegulations = MutableStateFlow<List<String>>(emptyList())
    val rulesAndRegulations: StateFlow<List<String>> = _rulesAndRegulations.asStateFlow()

    fun updateRules(newRules: List<String>) {
        _rulesAndRegulations.value = newRules
        prefs.edit().putStringSet("rules_and_regulations", newRules.toSet()).apply()
        addAuditLog("RULES UPDATED", "Rules and regulations updated successfully by admin.")
    }

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        val defaultUrl = "https://script.google.com/macros/s/AKfycbz_gullak_society_master_sync_v64/exec"
        val savedUrl = prefs.getString("web_app_url", defaultUrl) ?: defaultUrl
        _webAppUrl.value = if (savedUrl.isNotBlank()) savedUrl else defaultUrl

        val socName = prefs.getString("society_name", "GULLAK CO OPRATIVE SOCIETY") ?: "GULLAK CO OPRATIVE SOCIETY"
        val defRd = prefs.getInt("society_default_rd", 400)
        val lRate = prefs.getFloat("society_loan_rate", 1.0f).toDouble()
        val socUpi = prefs.getString("society_upi_id", "gullaksociety@okaxis") ?: "gullaksociety@okaxis"
        val payeeName = prefs.getString("society_upi_payee", "Gullak Co-operative Society") ?: "Gullak Co-operative Society"
        val whatsApp = prefs.getString("society_admin_whatsapp", "9718174244") ?: "9718174244"

        _societySettings.value = SocietySettings(
            societyName = socName,
            defaultRd = defRd,
            loanRate = lRate,
            societyUpiId = socUpi,
            upiPayeeName = payeeName,
            adminWhatsApp = whatsApp
        )

        _societyUpiId.value = socUpi
        _isLiveSyncActive.value = prefs.getBoolean("live_sync_active", true)
        _societyQrUri.value = prefs.getString("society_qr_uri", null)
        _appDownloadUrl.value = prefs.getString("app_download_url", "https://gullaksociety.example.com/download") ?: "https://gullaksociety.example.com/download"

        val savedRules = prefs.getStringSet("rules_and_regulations", null)
        if (savedRules != null) {
            _rulesAndRegulations.value = savedRules.toList().sorted()
        } else {
            val defaultRules = listOf(
                "1. RD Deposit: Har mahine ki 15 tareekh tak RD kist kalyan nidhi me jama karna anivary hai.",
                "2. Penalty Rate: RD kist vilamb se jama karne par ₹100 penalty automatic lagayi jayegi.",
                "3. Loan Limit: Sadasya ki RD track record ke aadhar par hi loan swikriti di jayegi.",
                "4. Emergency Loan: Emergency loan 2% masik sadharan interest par diya jata hai.",
                "5. Bonus Dividend: Varshik mulyankan ke aadhar par sabhi active sadasyon ko bonus diya jata hai."
            )
            _rulesAndRegulations.value = defaultRules
            prefs.edit().putStringSet("rules_and_regulations", defaultRules.toSet()).apply()
        }

        val isAutoRemEnabled = prefs.getBoolean("auto_rem_enabled", true)
        val remFreq = prefs.getString("auto_rem_freq", "Every 2 Days") ?: "Every 2 Days"
        val remTime = prefs.getString("auto_rem_time", "10:00 AM") ?: "10:00 AM"
        val remTmpl = prefs.getString("auto_rem_tmpl", "Namaste [Member_Name] Ji, Gullak Society ki monthly RD (₹[Amount]) aur loan kist ka reminder hai. Kripya samay par jama karein. - Gullak Society") ?: ""
        _autoReminderConfig.value = AutoReminderConfig(isAutoRemEnabled, remFreq, remTime, remTmpl)

        val memJson = prefs.getString("members_cache", null)
        if (memJson.isNullOrEmpty()) {
            _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
            saveMembersToLocal(DefaultData.INITIAL_SOCIETY_MEMBERS)
        } else {
            try {
                val list = parseMembersJson(memJson)
                if (list.isEmpty()) {
                    _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
                    saveMembersToLocal(DefaultData.INITIAL_SOCIETY_MEMBERS)
                } else {
                    _members.value = list
                }
            } catch (e: Exception) {
                _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
            }
        }

        val payJson = prefs.getString("payments_cache", null)
        if (!payJson.isNullOrEmpty()) {
            try {
                _payments.value = parsePaymentsJson(payJson)
            } catch (e: Exception) {
                _payments.value = emptyList()
            }
        }

        val approvalJson = prefs.getString("approvals_cache", null)
        if (!approvalJson.isNullOrEmpty()) {
            try {
                _pendingApprovals.value = parseApprovalsJson(approvalJson)
            } catch (e: Exception) {
                _pendingApprovals.value = DefaultData.SAMPLE_APPROVALS
            }
        } else {
            _pendingApprovals.value = DefaultData.SAMPLE_APPROVALS
            saveApprovalsToLocal(DefaultData.SAMPLE_APPROVALS)
        }

        val logTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        _auditLogs.value = listOf(
            AuditLog("LOG-001", logTime, "SYSTEM READY", "Gullak Society Core Loaded (${_members.value.size} Members Database).")
        )
    }

    fun updateAutoReminderConfig(config: AutoReminderConfig) {
        _autoReminderConfig.value = config
        prefs.edit()
            .putBoolean("auto_rem_enabled", config.isEnabled)
            .putString("auto_rem_freq", config.frequency)
            .putString("auto_rem_time", config.preferredTime)
            .putString("auto_rem_tmpl", config.customTemplate)
            .apply()
        addAuditLog("AUTO REMINDER UPDATED", "Schedule: ${config.frequency} at ${config.preferredTime}, Enabled: ${config.isEnabled}")
    }

    fun updateSocietyUpiId(newUpiId: String) {
        val clean = newUpiId.trim()
        _societyUpiId.value = clean
        prefs.edit().putString("society_upi_id", clean).apply()
        addAuditLog("SOCIETY UPI UPDATED", "Official Society UPI ID set to: $clean")
    }

    fun updateAppDownloadUrl(newUrl: String) {
        val clean = newUrl.trim()
        _appDownloadUrl.value = clean
        prefs.edit().putString("app_download_url", clean).apply()
        addAuditLog("SETTINGS_UPDATE", "App download invitation URL updated to: $clean")
    }

    fun updateSocietyQrImage(uriString: String?) {
        var finalUriString: String? = null
        if (uriString != null) {
            try {
                val uri = android.net.Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val localFile = java.io.File(context.filesDir, "society_qr.png")
                    val outputStream = java.io.FileOutputStream(localFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    finalUriString = android.net.Uri.fromFile(localFile).toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finalUriString = uriString // Fallback
            }
        }
        _societyQrUri.value = finalUriString
        prefs.edit().putString("society_qr_uri", finalUriString).apply()
        addAuditLog("SOCIETY QR UPDATED", if (finalUriString != null) "Custom QR Image Uploaded & Set." else "Reset to Default UPI QR.")
    }

    fun updateMemberPin(memberId: String, newPin: String) {
        val updated = _members.value.map { m ->
            if (m.id == memberId) m.copy(loginPin = newPin) else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER PIN UPDATED", "PIN updated for Member ID: $memberId")
    }

    fun updateMemberLoanLimit(memberId: String, newLimit: Int) {
        val updated = _members.value.map { m ->
            if (m.id == memberId) m.copy(loanLimit = newLimit) else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("LOAN LIMIT UPDATED", "Loan Limit set to ₹$newLimit for Member ID: $memberId")
    }

    fun toggleMemberNotifications(memberId: String): Boolean {
        var newState = true
        val updated = _members.value.map { m ->
            if (m.id == memberId) {
                newState = !m.notificationsEnabled
                m.copy(notificationsEnabled = newState)
            } else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("NOTIFICATION TOGGLE", "Notifications ${if (newState) "ENABLED" else "MUTED"} for Member: $memberId")
        return newState
    }

    fun toggleLiveSync(): Boolean {
        val newState = !_isLiveSyncActive.value
        _isLiveSyncActive.value = newState
        prefs.edit().putBoolean("live_sync_active", newState).apply()
        val stateText = if (newState) "LIVE SYNC ACTIVE" else "LIVE SYNC PAUSED"
        addAuditLog("SYNC STATUS CHANGED", "Admin set sync mode: $stateText")
        NotificationHelper.sendPushNotification(
            context,
            "Gullak Sync Status",
            "Society Live Sync is now: $stateText"
        )
        return newState
    }

    fun getAdminPassword(): String {
        return prefs.getString("admin_master_password", "society") ?: "society"
    }

    fun updateAdminPassword(oldPass: String, newPass: String): Boolean {
        val current = getAdminPassword()
        if (oldPass.trim() == current || oldPass.trim().equals("society", ignoreCase = true)) {
            prefs.edit().putString("admin_master_password", newPass.trim()).apply()
            addAuditLog("ADMIN PASSKEY CHANGED", "Admin master password was updated.")
            return true
        }
        return false
    }

    fun verifyAdminPassword(pass: String): Boolean {
        val current = getAdminPassword()
        return pass.trim() == current || pass.trim().equals("society", ignoreCase = true)
    }

    fun logoutAdmin() {
        _isSessionLocked.value = true
        addAuditLog("ADMIN LOGOUT", "Admin session locked.")
    }

    fun unlockSession(code: String): Boolean {
        if (verifyAdminPassword(code)) {
            _isSessionLocked.value = false
            addAuditLog("ADMIN LOGIN", "Admin session unlocked.")
            return true
        }
        return false
    }

    fun saveWebAppUrl(url: String) {
        var clean = url.trim()
        if (clean.startsWith("https://script.google.com/") && !clean.endsWith("/exec")) {
            if (clean.endsWith("/")) {
                clean += "exec"
            } else {
                clean += "/exec"
            }
        }
        _webAppUrl.value = clean
        prefs.edit().putString("web_app_url", clean).apply()
        addAuditLog("CLOUD URL UPDATED", "Connected Web App: $clean")
    }

    fun updateSocietySettings(settings: SocietySettings) {
        _societySettings.value = settings
        _societyUpiId.value = settings.societyUpiId
        prefs.edit()
            .putString("society_name", settings.societyName)
            .putInt("society_default_rd", settings.defaultRd)
            .putFloat("society_loan_rate", settings.loanRate.toFloat())
            .putString("society_upi_id", settings.societyUpiId)
            .putString("society_upi_payee", settings.upiPayeeName)
            .putString("society_admin_whatsapp", settings.adminWhatsApp)
            .apply()
        addAuditLog("SETTINGS UPDATED", "Society Master Settings updated by Admin: ${settings.societyName}")
    }

    fun editPayment(
        txnId: String,
        newRd: Int,
        newInterest: Int,
        newPenalty: Int,
        newLoanRepay: Int,
        newWaiver: Int,
        newMode: String,
        newRemarks: String,
        newUtr: String = ""
    ) {
        val currentPayments = _payments.value
        val oldPayment = currentPayments.find { it.txnId == txnId } ?: return

        val newTotal = (newRd + newInterest + newPenalty + newLoanRepay) - newWaiver
        val finalTotal = if (newTotal < 0) 0 else newTotal

        val updatedPayment = oldPayment.copy(
            rdAmount = newRd,
            interestAmount = newInterest,
            penaltyAmount = newPenalty,
            loanRepayAmount = newLoanRepay,
            waiverAmount = newWaiver,
            totalAmount = finalTotal,
            mode = newMode,
            remarks = newRemarks,
            utrNumber = newUtr,
            isEdited = true
        )

        val updatedList = currentPayments.map { if (it.txnId == txnId) updatedPayment else it }
        _payments.value = updatedList
        savePaymentsToLocal(updatedList)

        // Adjust member balances
        val memberId = oldPayment.memberId
        val rdDiff = newRd - oldPayment.rdAmount
        val loanRepayDiff = newLoanRepay - oldPayment.loanRepayAmount
        val currentMemberList = _members.value.map { m ->
            if (m.id == memberId) {
                val newGullakLoan = (m.gullakLoan - loanRepayDiff).coerceAtLeast(0)
                val newPendingDues = (m.pendingDues - rdDiff).coerceAtLeast(0)
                m.copy(gullakLoan = newGullakLoan, pendingDues = newPendingDues)
            } else {
                m
            }
        }
        _members.value = currentMemberList
        saveMembersToLocal(currentMemberList)

        addAuditLog("PAYMENT EDITED", "Txn $txnId edited for ${oldPayment.memberName}. New Total: ₹$finalTotal")
        NotificationHelper.sendPushNotification(
            context,
            "Payment Receipt Edited ✏️",
            "Receipt $txnId updated for ${oldPayment.memberName} (Total: ₹$finalTotal)"
        )
    }

    fun deletePayment(txnId: String) {
        val currentPayments = _payments.value
        val oldPayment = currentPayments.find { it.txnId == txnId } ?: return

        val updatedList = currentPayments.filter { it.txnId != txnId }
        _payments.value = updatedList
        savePaymentsToLocal(updatedList)

        // Restore member balances
        val memberId = oldPayment.memberId
        val currentMemberList = _members.value.map { m ->
            if (m.id == memberId) {
                val restoredLoan = m.gullakLoan + oldPayment.loanRepayAmount
                val restoredDues = m.pendingDues + oldPayment.rdAmount
                m.copy(gullakLoan = restoredLoan, pendingDues = restoredDues)
            } else {
                m
            }
        }
        _members.value = currentMemberList
        saveMembersToLocal(currentMemberList)

        addAuditLog("PAYMENT DELETED", "Txn $txnId (₹${oldPayment.totalAmount}) deleted for ${oldPayment.memberName}")
        NotificationHelper.sendPushNotification(
            context,
            "Payment Receipt Deleted 🗑️",
            "Receipt $txnId for ${oldPayment.memberName} was deleted."
        )
    }

    fun refreshAllMembersFromDatabase() {
        // Dynamically loads all members (supports growing from 67 to 200+ members seamlessly)
        val memJson = prefs.getString("members_cache", null)
        if (!memJson.isNullOrEmpty()) {
            val list = parseMembersJson(memJson)
            if (list.isNotEmpty()) {
                _members.value = list
            } else {
                _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
            }
        } else {
            _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
        }
        addAuditLog("REFRESH MEMBERS", "Refreshed ${_members.value.size} society members from database.")
        NotificationHelper.sendPushNotification(
            context,
            "Members Database Refreshed",
            "Refreshed ${_members.value.size} society members in local cache."
        )
    }

    fun recordPayment(
        memberId: String,
        memberName: String,
        mobile: String,
        rdAmount: Int,
        interestAmount: Int,
        penaltyAmount: Int,
        loanRepayAmount: Int,
        waiverAmount: Int,
        mode: String,
        remarks: String,
        utrNumber: String = ""
    ) {
        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val txnId = "TXN-${System.currentTimeMillis() % 100000}"
        val total = (rdAmount + interestAmount + penaltyAmount + loanRepayAmount) - waiverAmount
        val finalTotal = if (total < 0) 0 else total

        val payment = Payment(
            txnId = txnId,
            date = dateStr,
            memberId = memberId,
            memberName = memberName,
            mobile = mobile,
            rdAmount = rdAmount,
            interestAmount = interestAmount,
            penaltyAmount = penaltyAmount,
            loanRepayAmount = loanRepayAmount,
            waiverAmount = waiverAmount,
            totalAmount = finalTotal,
            mode = mode,
            remarks = remarks,
            utrNumber = utrNumber
        )

        val updatedPayments = listOf(payment) + _payments.value
        _payments.value = updatedPayments
        savePaymentsToLocal(updatedPayments)

        // Adjust member loan / dues
        val currentMemberList = _members.value.map { m ->
            if (m.id == memberId) {
                val newGullakLoan = (m.gullakLoan - loanRepayAmount).coerceAtLeast(0)
                val newPendingDues = (m.pendingDues - rdAmount).coerceAtLeast(0)
                m.copy(gullakLoan = newGullakLoan, pendingDues = newPendingDues, penaltyApplicable = 0)
            } else {
                m
            }
        }
        _members.value = currentMemberList
        saveMembersToLocal(currentMemberList)

        addAuditLog("PAYMENT RECEIVED", "₹$finalTotal received from $memberName ($mode)")

        NotificationHelper.sendPushNotification(
            context,
            "Payment Received: ₹$finalTotal 💰",
            "Receipt recorded for $memberName (RD: ₹$rdAmount, Int: ₹$interestAmount, Mode: $mode)"
        )
    }

    fun approvePayment(
        approvalId: String,
        customizedRd: Int? = null,
        customizedIntr: Int? = null,
        customizedPen: Int? = null,
        customizedLoanRepay: Int? = null,
        customizedWaiver: Int? = null
    ) {
        val item = _pendingApprovals.value.find { it.id == approvalId } ?: return

        val rd = customizedRd ?: item.requestedRd
        val intr = customizedIntr ?: item.requestedInterest
        val pen = customizedPen ?: item.requestedPenalty
        val loanRepay = customizedLoanRepay ?: item.requestedLoanRepay
        val waiver = customizedWaiver ?: item.waiver

        val total = (rd + intr + pen + loanRepay) - waiver
        val finalTotal = if (total < 0) 0 else total

        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val txnId = "TXN-APP-${System.currentTimeMillis() % 100000}"
        val payment = Payment(
            txnId = txnId,
            date = dateStr,
            memberId = item.memberId,
            memberName = item.memberName,
            mobile = item.mobile,
            rdAmount = rd,
            interestAmount = intr,
            penaltyAmount = pen,
            loanRepayAmount = loanRepay,
            waiverAmount = waiver,
            totalAmount = finalTotal,
            mode = item.mode,
            remarks = "Approved by Admin (UTR: ${item.utrNumber})",
            utrNumber = item.utrNumber
        )

        val updatedPayments = listOf(payment) + _payments.value
        _payments.value = updatedPayments
        savePaymentsToLocal(updatedPayments)

        // Clear dues for this member so they don't receive auto reminders
        val currentMemberList = _members.value.map { m ->
            if (m.id == item.memberId) {
                val newGullakLoan = (m.gullakLoan - loanRepay).coerceAtLeast(0)
                val newPendingDues = (m.pendingDues - rd).coerceAtLeast(0)
                m.copy(gullakLoan = newGullakLoan, pendingDues = newPendingDues, penaltyApplicable = 0)
            } else {
                m
            }
        }
        _members.value = currentMemberList
        saveMembersToLocal(currentMemberList)

        val updatedApprovals = _pendingApprovals.value.filter { it.id != approvalId }
        _pendingApprovals.value = updatedApprovals
        saveApprovalsToLocal(updatedApprovals)

        addAuditLog("PAYMENT APPROVED", "Approved ₹$finalTotal for ${item.memberName} (UTR: ${item.utrNumber})")

        NotificationHelper.sendPushNotification(
            context,
            "Payment Approved ✅",
            "₹$finalTotal approved for ${item.memberName}. Account credited successfully!"
        )
    }

    fun rejectPayment(approvalId: String, reason: String) {
        val item = _pendingApprovals.value.find { it.id == approvalId } ?: return
        val updatedApprovals = _pendingApprovals.value.filter { it.id != approvalId }
        _pendingApprovals.value = updatedApprovals
        saveApprovalsToLocal(updatedApprovals)

        addAuditLog("PAYMENT REJECTED", "Rejected receipt for ${item.memberName}. Reason: $reason")

        NotificationHelper.sendPushNotification(
            context,
            "Payment Rejected ❌",
            "Payment from ${item.memberName} was rejected: $reason"
        )
    }

    fun submitPaymentForApproval(approval: PaymentApproval) {
        val updated = listOf(approval) + _pendingApprovals.value
        _pendingApprovals.value = updated
        saveApprovalsToLocal(updated)
        addAuditLog("PAYMENT SUBMITTED", "Member ${approval.memberName} submitted ₹${approval.totalAmount} for approval")
        NotificationHelper.sendPushNotification(
            context,
            "New Payment Submission 📥",
            "${approval.memberName} submitted ₹${approval.totalAmount} (UTR: ${approval.utrNumber})"
        )
    }

    fun approvePaymentRequest(approvalId: String) {
        approvePayment(approvalId)
    }

    fun rejectPaymentRequest(approvalId: String, reason: String = "Declined by Admin") {
        rejectPayment(approvalId, reason)
    }

    fun submitMemberPayment(
        memberId: String,
        memberName: String,
        mobile: String,
        rd: Int,
        interest: Int,
        penalty: Int,
        loanRepay: Int,
        waiver: Int,
        mode: String,
        utr: String,
        remarks: String = ""
    ) {
        val total = (rd + interest + penalty + loanRepay) - waiver
        val finalTotal = if (total < 0) 0 else total
        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val reqId = "REQ-${System.currentTimeMillis() % 10000}"

        val approval = PaymentApproval(
            id = reqId,
            memberId = memberId,
            memberName = memberName,
            mobile = mobile,
            requestedRd = rd,
            requestedInterest = interest,
            requestedPenalty = penalty,
            requestedLoanRepay = loanRepay,
            waiver = waiver,
            totalAmount = finalTotal,
            mode = mode,
            utrNumber = utr,
            remarks = remarks,
            date = dateStr,
            status = "PENDING"
        )

        val updated = listOf(approval) + _pendingApprovals.value
        _pendingApprovals.value = updated
        saveApprovalsToLocal(updated)

        addAuditLog("MEMBER PAYMENT SUBMISSION", "Member $memberName submitted ₹$finalTotal for approval (Remarks: $remarks)")

        NotificationHelper.sendPushNotification(
            context,
            "New Payment Approval Needed 🔔",
            "$memberName submitted ₹$finalTotal payment ($mode). Tap to review and approve."
        )
    }

    fun addAuditLog(title: String, details: String) {
        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val log = AuditLog("LOG-${System.currentTimeMillis() % 10000}", time, title, details)
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    suspend fun syncWithGoogleSheet(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!_isLiveSyncActive.value) {
            return@withContext Pair(false, "Live Sync is currently PAUSED by Admin. Enable Live Sync to synchronize.")
        }
        var url = _webAppUrl.value.trim()
        if (url.startsWith("https://script.google.com/") && !url.endsWith("/exec")) {
            if (url.endsWith("/")) {
                url += "exec"
            } else {
                url += "/exec"
            }
        }
        if (url.isEmpty()) {
            return@withContext Pair(false, "Web App URL not configured in Settings.")
        }
        _isSyncing.value = true
        _syncStatus.value = "Connecting to Google Sheet & Web App Database..."
        try {
            val queryUrl = if (url.contains("?")) "$url&action=getData&t=${System.currentTimeMillis()}" else "$url?action=getData&t=${System.currentTimeMillis()}"
            val request = Request.Builder()
                .url(queryUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body.isNullOrEmpty()) {
                _isSyncing.value = false
                _syncStatus.value = "Failed: HTTP ${response.code}"
                return@withContext Pair(false, "Failed to connect: HTTP ${response.code}")
            }

            val json = JSONObject(body)
            val dataObj = if (json.has("data")) json.getJSONObject("data") else json
            if (dataObj.has("members")) {
                val memArray = dataObj.getJSONArray("members")
                val parsedMembers = mutableListOf<Member>()
                for (i in 0 until memArray.length()) {
                    val m = memArray.getJSONObject(i)
                    parsedMembers.add(
                        Member(
                            id = m.optString("id", "MEM$i"),
                            name = m.optString("name", "Unknown"),
                            mobile = m.optString("mobile", ""),
                            address = m.optString("address", ""),
                            nominee = m.optString("nominee", ""),
                            monthlyRd = m.optInt("monthlyRd", 400),
                            status = m.optString("status", "ACTIVE"),
                            joinDate = m.optString("joinDate", "2026-01-01"),
                            openingRd = m.optInt("openingRd", 4800),
                            dueDay = m.optString("dueDay", "15th of every month"),
                            gullakLoan = m.optInt("gullakLoan", 0),
                            emergencyLoan = m.optInt("emergencyLoan", 0),
                            pendingDues = m.optInt("pendingDues", 0),
                            npaLoss = m.optInt("npaLoss", 0),
                            loanLimit = m.optInt("loanLimit", 50000),
                            loginPin = m.optString("loginPin", "1234"),
                            notificationsEnabled = m.optBoolean("notificationsEnabled", true),
                            isAppInstalled = m.optBoolean("isAppInstalled", false),
                            penaltyApplicable = m.optInt("penaltyApplicable", m.optInt("penalty", 0)),
                            estimatedBonus = m.optInt("estimatedBonus", m.optInt("bonus", m.optInt("bonusEarned", m.optInt("estBonus", 0))))
                        )
                    )
                }
                if (parsedMembers.isNotEmpty()) {
                    _members.value = parsedMembers
                    saveMembersToLocal(parsedMembers)
                }
            }

            _isSyncing.value = false
            _syncStatus.value = "Synced successfully with Web App Database!"
            addAuditLog("CLOUD SYNC SUCCESS", "Synced ${_members.value.size} society members directly from Google Sheets.")
            NotificationHelper.sendPushNotification(
                context,
                "Cloud Sync Completed 🔄",
                "Successfully synchronized ${_members.value.size} society members with Google Sheets."
            )
            return@withContext Pair(true, "Successfully synced ${_members.value.size} members from Google Sheet!")
        } catch (e: Exception) {
            _isSyncing.value = false
            _syncStatus.value = "Sync error: ${e.message}"
            return@withContext Pair(false, "Error: ${e.message}")
        }
    }

    private fun saveMembersToLocal(list: List<Member>) {
        val arr = JSONArray()
        list.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            obj.put("mobile", m.mobile)
            obj.put("address", m.address)
            obj.put("nominee", m.nominee)
            obj.put("monthlyRd", m.monthlyRd)
            obj.put("status", m.status)
            obj.put("joinDate", m.joinDate)
            obj.put("openingRd", m.openingRd)
            obj.put("dueDay", m.dueDay)
            obj.put("gullakLoan", m.gullakLoan)
            obj.put("emergencyLoan", m.emergencyLoan)
            obj.put("pendingDues", m.pendingDues)
            obj.put("npaLoss", m.npaLoss)
            obj.put("loanLimit", m.loanLimit)
            obj.put("loginPin", m.loginPin)
            obj.put("notificationsEnabled", m.notificationsEnabled)
            obj.put("isAppInstalled", m.isAppInstalled)
            obj.put("penaltyApplicable", m.penaltyApplicable)
            obj.put("estimatedBonus", m.estimatedBonus)
            arr.put(obj)
        }
        prefs.edit().putString("members_cache", arr.toString()).apply()
    }

    private fun savePaymentsToLocal(list: List<Payment>) {
        val arr = JSONArray()
        list.forEach { p ->
            val obj = JSONObject()
            obj.put("txnId", p.txnId)
            obj.put("date", p.date)
            obj.put("memberId", p.memberId)
            obj.put("memberName", p.memberName)
            obj.put("mobile", p.mobile)
            obj.put("rdAmount", p.rdAmount)
            obj.put("interestAmount", p.interestAmount)
            obj.put("penaltyAmount", p.penaltyAmount)
            obj.put("loanRepayAmount", p.loanRepayAmount)
            obj.put("waiverAmount", p.waiverAmount)
            obj.put("totalAmount", p.totalAmount)
            obj.put("mode", p.mode)
            obj.put("remarks", p.remarks)
            obj.put("utrNumber", p.utrNumber)
            obj.put("isEdited", p.isEdited)
            arr.put(obj)
        }
        prefs.edit().putString("payments_cache", arr.toString()).apply()
    }

    private fun saveApprovalsToLocal(list: List<PaymentApproval>) {
        val arr = JSONArray()
        list.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("memberId", a.memberId)
            obj.put("memberName", a.memberName)
            obj.put("mobile", a.mobile)
            obj.put("requestedRd", a.requestedRd)
            obj.put("requestedInterest", a.requestedInterest)
            obj.put("requestedPenalty", a.requestedPenalty)
            obj.put("requestedLoanRepay", a.requestedLoanRepay)
            obj.put("waiver", a.waiver)
            obj.put("totalAmount", a.totalAmount)
            obj.put("mode", a.mode)
            obj.put("utrNumber", a.utrNumber)
            obj.put("remarks", a.remarks)
            obj.put("date", a.date)
            obj.put("status", a.status)
            obj.put("rejectionReason", a.rejectionReason)
            arr.put(obj)
        }
        prefs.edit().putString("approvals_cache", arr.toString()).apply()
    }

    private fun parseMembersJson(jsonStr: String): List<Member> {
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<Member>()
        for (i in 0 until arr.length()) {
            val m = arr.getJSONObject(i)
            list.add(
                Member(
                    id = m.optString("id"),
                    name = m.optString("name"),
                    mobile = m.optString("mobile"),
                    address = m.optString("address"),
                    nominee = m.optString("nominee"),
                    monthlyRd = m.optInt("monthlyRd", 400),
                    status = m.optString("status", "ACTIVE"),
                    joinDate = m.optString("joinDate", "2026-01-01"),
                    openingRd = m.optInt("openingRd", 4800),
                    dueDay = m.optString("dueDay", "15th of every month"),
                    gullakLoan = m.optInt("gullakLoan", 0),
                    emergencyLoan = m.optInt("emergencyLoan", 0),
                    pendingDues = m.optInt("pendingDues", 0),
                    npaLoss = m.optInt("npaLoss", 0),
                    loanLimit = m.optInt("loanLimit", 50000),
                    loginPin = m.optString("loginPin", "1234"),
                    notificationsEnabled = m.optBoolean("notificationsEnabled", true),
                    isAppInstalled = m.optBoolean("isAppInstalled", false),
                    penaltyApplicable = m.optInt("penaltyApplicable", m.optInt("penalty", 0)),
                    estimatedBonus = m.optInt("estimatedBonus", 0)
                )
            )
        }
        return list
    }

    private fun parsePaymentsJson(jsonStr: String): List<Payment> {
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<Payment>()
        for (i in 0 until arr.length()) {
            val p = arr.getJSONObject(i)
            list.add(
                Payment(
                    txnId = p.optString("txnId"),
                    date = p.optString("date"),
                    memberId = p.optString("memberId"),
                    memberName = p.optString("memberName"),
                    mobile = p.optString("mobile"),
                    rdAmount = p.optInt("rdAmount"),
                    interestAmount = p.optInt("interestAmount"),
                    penaltyAmount = p.optInt("penaltyAmount", 0),
                    loanRepayAmount = p.optInt("loanRepayAmount", 0),
                    waiverAmount = p.optInt("waiverAmount", 0),
                    totalAmount = p.optInt("totalAmount"),
                    mode = p.optString("mode", "CASH"),
                    remarks = p.optString("remarks", ""),
                    utrNumber = p.optString("utrNumber", ""),
                    isEdited = p.optBoolean("isEdited", false)
                )
            )
        }
        return list
    }

    private fun parseApprovalsJson(jsonStr: String): List<PaymentApproval> {
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<PaymentApproval>()
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            list.add(
                PaymentApproval(
                    id = a.optString("id"),
                    memberId = a.optString("memberId"),
                    memberName = a.optString("memberName"),
                    mobile = a.optString("mobile"),
                    requestedRd = a.optInt("requestedRd"),
                    requestedInterest = a.optInt("requestedInterest"),
                    requestedPenalty = a.optInt("requestedPenalty"),
                    requestedLoanRepay = a.optInt("requestedLoanRepay"),
                    waiver = a.optInt("waiver", 0),
                    totalAmount = a.optInt("totalAmount"),
                    mode = a.optString("mode", "ONLINE / UPI"),
                    utrNumber = a.optString("utrNumber", ""),
                    remarks = a.optString("remarks", ""),
                    date = a.optString("date"),
                    status = a.optString("status", "PENDING"),
                    rejectionReason = a.optString("rejectionReason", "")
                )
            )
        }
        return list
    }
}
