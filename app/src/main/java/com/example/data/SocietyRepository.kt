package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.NotificationHelper
import com.example.util.NotificationTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SocietyRepository(private val context: Context) {

    companion object {
        const val DEFAULT_WEB_APP_URL =
            "https://script.google.com/macros/s/AKfycbycqoHn2MDGhW36kd3fLNdTpzs_kO7rxm-Qvn6RLnLYvVuGoCjc5Xxgk23D05ZD2LtOmQ/exec"

        fun sanitizeDueDay(raw: String): String {
            val clean = raw.trim()
            if (clean.isEmpty() || clean.equals("null", ignoreCase = true)) {
                return "15th of every month"
            }
            if (clean.equals("15th of every month", ignoreCase = true) ||
                clean.equals("15th of every Month", ignoreCase = true)
            ) {
                return "15th of every month"
            }

            // If it's a date string like "Tue Sep 15 2026 00:00:00 GMT+0530 (India Standard Time)" or ISO timestamp
            if (clean.contains("GMT", ignoreCase = true) ||
                clean.contains("Time", ignoreCase = true) ||
                clean.contains("T00:00") ||
                clean.contains("00:00:00")
            ) {
                val stripped = clean.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
                val patterns = listOf(
                    "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
                    "EEE MMM dd yyyy HH:mm:ss",
                    "EEE MMM dd yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd",
                    "dd-MM-yyyy",
                    "dd/MM/yyyy"
                )
                for (p in patterns) {
                    try {
                        val sdf = SimpleDateFormat(p, Locale.ENGLISH)
                        val d = sdf.parse(stripped)
                        if (d != null) {
                            val cal = Calendar.getInstance().apply { time = d }
                            val day = cal.get(Calendar.DAY_OF_MONTH)
                            return formatDayOrdinal(day)
                        }
                    } catch (_: Exception) {}
                }
                if (clean.contains(" 15 ") || clean.contains("-15") || clean.contains("/15")) {
                    return "15th of every month"
                }
            }

            // Check if string matches simple ISO date "2026-09-15"
            if (clean.matches(Regex("^\\d{4}-\\d{2}-\\d{2}.*"))) {
                try {
                    val day = clean.substring(8, 10).toInt()
                    return formatDayOrdinal(day)
                } catch (_: Exception) {}
            }

            // Check if string matches "15/09/2026" or "15-09-2026"
            if (clean.matches(Regex("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}$"))) {
                try {
                    val day = clean.split(Regex("[/-]"))[0].toInt()
                    return formatDayOrdinal(day)
                } catch (_: Exception) {}
            }

            // Check if plain number like "15"
            if (clean.all { it.isDigit() }) {
                val d = clean.toIntOrNull() ?: 15
                return formatDayOrdinal(d)
            }

            // If it already ends with "of every month"
            if (clean.contains("of every month", ignoreCase = true)) {
                return clean
            }

            return clean
        }

        private fun formatDayOrdinal(day: Int): String {
            val suffix = when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
            return "${day}${suffix} of every month"
        }

        fun sanitizeJoinDate(raw: String): String {
            val clean = raw.trim()
            if (clean.isEmpty() || clean.equals("null", ignoreCase = true)) {
                return "2026-01-01"
            }
            if (clean.contains("GMT", ignoreCase = true) ||
                clean.contains("Time", ignoreCase = true) ||
                clean.contains("00:00:00") ||
                clean.contains("T")
            ) {
                val stripped = clean.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
                val patterns = listOf(
                    "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
                    "EEE MMM dd yyyy HH:mm:ss",
                    "EEE MMM dd yyyy",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
                )
                for (p in patterns) {
                    try {
                        val sdf = SimpleDateFormat(p, Locale.ENGLISH)
                        val d = sdf.parse(stripped)
                        if (d != null) {
                            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
                        }
                    } catch (_: Exception) {}
                }
            }
            return clean
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("gullak_app_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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

    private val _webAppUrl = MutableStateFlow(DEFAULT_WEB_APP_URL)
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

    private val _reminderTemplates = MutableStateFlow<List<ReminderTemplate>>(DEFAULT_REMINDER_TEMPLATES)
    val reminderTemplates: StateFlow<List<ReminderTemplate>> = _reminderTemplates.asStateFlow()

    fun updateReminderTemplate(updatedTemplate: ReminderTemplate) {
        val updatedList = _reminderTemplates.value.map {
            if (it.id == updatedTemplate.id) updatedTemplate else it
        }
        _reminderTemplates.value = updatedList
        saveReminderTemplatesToLocal(updatedList)
        addAuditLog("TEMPLATE UPDATED", "Reminder Template '${updatedTemplate.name}' updated.")
    }

    private fun saveReminderTemplatesToLocal(list: List<ReminderTemplate>) {
        val arr = JSONArray()
        list.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("name", t.name)
            obj.put("notificationTitle", t.notificationTitle)
            obj.put("body", t.body)
            arr.put(obj)
        }
        prefs.edit().putString("reminder_templates_cache", arr.toString()).apply()
    }

    private fun loadReminderTemplatesFromLocal(): List<ReminderTemplate> {
        val json = prefs.getString("reminder_templates_cache", null) ?: return DEFAULT_REMINDER_TEMPLATES
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<ReminderTemplate>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ReminderTemplate(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        notificationTitle = obj.getString("notificationTitle"),
                        body = obj.getString("body")
                    )
                )
            }
            if (list.isNotEmpty()) list else DEFAULT_REMINDER_TEMPLATES
        } catch (_: Exception) {
            DEFAULT_REMINDER_TEMPLATES
        }
    }

    private val _rulesAndRegulations = MutableStateFlow<List<String>>(emptyList())
    val rulesAndRegulations: StateFlow<List<String>> = _rulesAndRegulations.asStateFlow()

    fun updateRules(newRules: List<String>) {
        _rulesAndRegulations.value = newRules
        prefs.edit().putStringSet("rules_and_regulations", newRules.toSet()).apply()
        addAuditLog("RULES UPDATED", "Rules and regulations updated successfully by admin.")
    }

    init {
        NotificationHelper.currentRoleProvider = {
            Pair(!_isSessionLocked.value, _loggedInMemberId.value)
        }
        loadLocalData()
    }

    private fun loadLocalData() {
        val savedUrl = prefs.getString("web_app_url", null)
        if (savedUrl.isNullOrBlank() || savedUrl.contains("AKfycbz_gullak_society_master_sync_v64")) {
            _webAppUrl.value = DEFAULT_WEB_APP_URL
            prefs.edit().putString("web_app_url", DEFAULT_WEB_APP_URL).apply()
        } else {
            _webAppUrl.value = savedUrl
        }

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
        val autoRemFreq = prefs.getString("auto_rem_freq", "1st to 15th Daily") ?: "1st to 15th Daily"
        val autoRemTime = prefs.getString("auto_rem_time", "09:00 AM") ?: "09:00 AM"
        val autoRemTmpl = prefs.getString("auto_rem_tmpl", "Dear member, aapki Gullak RD deposit ki tareekh 15 hai. Kripya apna anshdan samay par jama karein.") ?: ""
        _autoReminderConfig.value = AutoReminderConfig(
            isEnabled = isAutoRemEnabled,
            frequency = autoRemFreq,
            preferredTime = autoRemTime,
            customTemplate = autoRemTmpl
        )

        _reminderTemplates.value = loadReminderTemplatesFromLocal()

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
        if (payJson.isNullOrEmpty()) {
            _payments.value = DefaultData.INITIAL_PAYMENTS
            savePaymentsToLocal(DefaultData.INITIAL_PAYMENTS)
        } else {
            try {
                val list = parsePaymentsJson(payJson)
                _payments.value = if (list.isNotEmpty()) list else DefaultData.INITIAL_PAYMENTS
            } catch (e: Exception) {
                _payments.value = DefaultData.INITIAL_PAYMENTS
            }
        }

        val appJson = prefs.getString("approvals_cache", null)
        if (appJson.isNullOrEmpty()) {
            _pendingApprovals.value = DefaultData.SAMPLE_APPROVALS
            saveApprovalsToLocal(DefaultData.SAMPLE_APPROVALS)
        } else {
            try {
                val list = parseApprovalsJson(appJson)
                _pendingApprovals.value = list
            } catch (e: Exception) {
                _pendingApprovals.value = emptyList()
            }
        }

        val auditList = mutableListOf<AuditLog>()
        auditList.add(AuditLog("LOG-001", "01-09-2026 10:00", "SYSTEM INITIALIZED", "Gullak Society App loaded securely."))
        _auditLogs.value = auditList
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
        val cleanPin = newPin.trim().ifEmpty { "1234" }
        val updated = _members.value.map { m ->
            if (m.id == memberId) m.copy(loginPin = cleanPin) else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER PIN UPDATED", "PIN updated to $cleanPin for Member ID: $memberId")

        // Async sync to Web App / Google Sheet if configured
        val url = _webAppUrl.value.trim()
        if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cleanUrl = if (url.startsWith("https://script.google.com/") && !url.endsWith("/exec")) {
                        if (url.endsWith("/")) "${url}exec" else "$url/exec"
                    } else url
                    val encodedId = URLEncoder.encode(memberId, "UTF-8")
                    val encodedPin = URLEncoder.encode(cleanPin, "UTF-8")
                    val queryUrl = if (cleanUrl.contains("?")) "$cleanUrl&action=updatePin&id=$encodedId&pin=$encodedPin"
                                   else "$cleanUrl?action=updatePin&id=$encodedId&pin=$encodedPin"
                    val req = Request.Builder().url(queryUrl).get().build()
                    client.newCall(req).execute()
                } catch (e: Exception) {
                    // Local save remains secure
                }
            }
        }
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
            context = context,
            title = "GULLAK SYNC STATUS",
            message = "Society Live Sync is now: $stateText",
            target = NotificationTarget.ADMIN_ONLY
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
        if (clean.isBlank()) {
            clean = DEFAULT_WEB_APP_URL
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

        addAuditLog(
            "PAYMENT EDITED (PASSBOOK MARKUP)",
            "Txn $txnId edited. RD: ₹$newRd, Int: ₹$newInterest, Pen: ₹$newPenalty, Repay: ₹$newLoanRepay, Total: ₹$finalTotal"
        )
    }

    fun deletePayment(txnId: String) {
        val payment = _payments.value.find { it.txnId == txnId } ?: return
        val updatedPayments = _payments.value.filter { it.txnId != txnId }
        _payments.value = updatedPayments
        savePaymentsToLocal(updatedPayments)

        val memberId = payment.memberId
        val updatedMembers = _members.value.map { m ->
            if (m.id == memberId) {
                m.copy(
                    gullakLoan = m.gullakLoan + payment.loanRepayAmount,
                    pendingDues = m.pendingDues + payment.rdAmount,
                    openingRd = (m.openingRd - payment.rdAmount).coerceAtLeast(0)
                )
            } else m
        }
        _members.value = updatedMembers
        saveMembersToLocal(updatedMembers)
        addAuditLog("PAYMENT DELETED", "Deleted receipt $txnId of ₹${payment.totalAmount} for ${payment.memberName}")
    }

    fun refreshAllMembersFromDatabase() {
        _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS
        saveMembersToLocal(DefaultData.INITIAL_SOCIETY_MEMBERS)
        addAuditLog("DATABASE REFRESH", "Restored society members directory.")
    }

    fun addMember(member: Member) {
        val sanitized = member.copy(
            dueDay = sanitizeDueDay(member.dueDay),
            joinDate = sanitizeJoinDate(member.joinDate)
        )
        val current = _members.value
        val updated = current + sanitized
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER ADDED", "Added member: ${sanitized.name} (${sanitized.id})")
    }

    fun updateMember(member: Member) {
        val sanitized = member.copy(
            dueDay = sanitizeDueDay(member.dueDay),
            joinDate = sanitizeJoinDate(member.joinDate)
        )
        val current = _members.value
        val updated = current.map { if (it.id == sanitized.id) sanitized else it }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER UPDATED", "Updated profile: ${sanitized.name} (${sanitized.id})")
    }

    fun deleteMember(memberId: String) {
        val current = _members.value
        val m = current.find { it.id == memberId }
        val updated = current.filter { it.id != memberId }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER DELETED", "Deleted member: ${m?.name ?: memberId}")
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
        remarks: String = "",
        utrNumber: String = ""
    ): Payment {
        val total = (rdAmount + interestAmount + penaltyAmount + loanRepayAmount) - waiverAmount
        val finalTotal = if (total < 0) 0 else total
        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val txnId = "TXN-${System.currentTimeMillis() % 100000}"

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
            utrNumber = utrNumber,
            isEdited = false
        )

        val updatedPayments = listOf(payment) + _payments.value
        _payments.value = updatedPayments
        savePaymentsToLocal(updatedPayments)

        val updatedMembers = _members.value.map { m ->
            if (m.id == memberId) {
                val newGullakLoan = (m.gullakLoan - loanRepayAmount).coerceAtLeast(0)
                val newPendingDues = (m.pendingDues - rdAmount).coerceAtLeast(0)
                val newOpeningRd = m.openingRd + rdAmount
                m.copy(
                    gullakLoan = newGullakLoan,
                    pendingDues = newPendingDues,
                    openingRd = newOpeningRd,
                    penaltyApplicable = 0
                )
            } else {
                m
            }
        }
        _members.value = updatedMembers
        saveMembersToLocal(updatedMembers)

        addAuditLog(
            "PAYMENT RECORDED",
            "Payment of ₹$finalTotal ($mode) for $memberName. RD: ₹$rdAmount, Int: ₹$interestAmount, Loan: ₹$loanRepayAmount"
        )

        NotificationHelper.sendPushNotification(
            context = context,
            title = "PAYMENT CONFIRMED",
            message = "Receipt generated for $memberName: ₹$finalTotal received via $mode.",
            target = NotificationTarget.MEMBER_ONLY,
            targetMemberId = memberId
        )

        return payment
    }

    fun submitPaymentForApproval(approval: PaymentApproval) {
        val updatedList = listOf(approval) + _pendingApprovals.value
        _pendingApprovals.value = updatedList
        saveApprovalsToLocal(updatedList)

        addAuditLog(
            "APPROVAL SUBMITTED",
            "Member ${approval.memberName} submitted ₹${approval.totalAmount} (${approval.mode}) for Admin Approval. Ref: ${approval.utrNumber}"
        )

        NotificationHelper.sendPushNotification(
            context = context,
            title = "NEW PAYMENT APPROVAL NEEDED",
            message = "${approval.memberName} submitted ₹${approval.totalAmount} payment (${approval.mode}). Tap to review and approve.",
            target = NotificationTarget.ADMIN_ONLY
        )
    }

    fun submitPaymentForApproval(
        memberId: String,
        memberName: String,
        mobile: String,
        requestedRd: Int,
        requestedInterest: Int,
        requestedPenalty: Int,
        requestedLoanRepay: Int,
        mode: String,
        utrNumber: String = "",
        remarks: String = ""
    ) {
        val total = (requestedRd + requestedInterest + requestedPenalty + requestedLoanRepay)
        val finalTotal = if (total < 0) 0 else total
        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val approvalId = "APP-${System.currentTimeMillis() % 100000}"

        val approval = PaymentApproval(
            id = approvalId,
            memberId = memberId,
            memberName = memberName,
            mobile = mobile,
            requestedRd = requestedRd,
            requestedInterest = requestedInterest,
            requestedPenalty = requestedPenalty,
            requestedLoanRepay = requestedLoanRepay,
            waiver = 0,
            totalAmount = finalTotal,
            mode = mode,
            utrNumber = utrNumber,
            remarks = remarks,
            date = dateStr,
            status = "PENDING"
        )
        submitPaymentForApproval(approval)
    }

    fun submitMemberPayment(
        memberId: String,
        memberName: String,
        mobile: String,
        rd: Int,
        interest: Int,
        penalty: Int,
        loanRepay: Int,
        waiver: Int = 0,
        mode: String = "ONLINE / UPI",
        utr: String = "",
        remarks: String = ""
    ) {
        val total = (rd + interest + penalty + loanRepay) - waiver
        val approval = PaymentApproval(
            id = "APP-${System.currentTimeMillis() % 100000}",
            memberId = memberId,
            memberName = memberName,
            mobile = mobile,
            requestedRd = rd,
            requestedInterest = interest,
            requestedPenalty = penalty,
            requestedLoanRepay = loanRepay,
            waiver = waiver,
            totalAmount = if (total < 0) 0 else total,
            mode = mode,
            utrNumber = utr,
            remarks = remarks,
            date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
            status = "PENDING"
        )
        submitPaymentForApproval(approval)
    }

    fun approvePayment(
        approvalId: String,
        customizedRd: Int? = null,
        customizedIntr: Int? = null,
        customizedPen: Int? = null,
        customizedLoanRepay: Int? = null,
        customizedWaiver: Int? = null,
        adminRemarks: String = ""
    ): Boolean {
        val approval = _pendingApprovals.value.find { it.id == approvalId } ?: return false
        val finalRd = customizedRd ?: approval.requestedRd
        val finalIntr = customizedIntr ?: approval.requestedInterest
        val finalPen = customizedPen ?: approval.requestedPenalty
        val finalLoan = customizedLoanRepay ?: approval.requestedLoanRepay
        val finalWaiver = customizedWaiver ?: approval.waiver
        return approvePaymentRequest(approvalId, finalRd, finalIntr, finalPen, finalLoan, finalWaiver, adminRemarks)
    }

    fun approvePaymentRequest(approvalId: String): Boolean {
        val approval = _pendingApprovals.value.find { it.id == approvalId } ?: return false
        return approvePaymentRequest(
            approvalId = approval.id,
            editedRd = approval.requestedRd,
            editedInterest = approval.requestedInterest,
            editedPenalty = approval.requestedPenalty,
            editedLoanRepay = approval.requestedLoanRepay,
            waiver = approval.waiver,
            adminRemarks = ""
        )
    }

    fun approvePaymentRequest(
        approvalId: String,
        editedRd: Int,
        editedInterest: Int,
        editedPenalty: Int,
        editedLoanRepay: Int,
        waiver: Int,
        adminRemarks: String = ""
    ): Boolean {
        val currentApprovals = _pendingApprovals.value
        val approval = currentApprovals.find { it.id == approvalId } ?: return false

        val isEdited = (editedRd != approval.requestedRd ||
                editedInterest != approval.requestedInterest ||
                editedPenalty != approval.requestedPenalty ||
                editedLoanRepay != approval.requestedLoanRepay ||
                waiver > 0)

        val total = (editedRd + editedInterest + editedPenalty + editedLoanRepay) - waiver
        val finalTotal = if (total < 0) 0 else total
        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val txnId = "TXN-${System.currentTimeMillis() % 100000}"

        val remarksText = if (isEdited) {
            "${approval.remarks} | Admin Approved (Modified: $adminRemarks)".trim()
        } else {
            if (adminRemarks.isNotEmpty()) "${approval.remarks} | $adminRemarks" else approval.remarks
        }

        val payment = Payment(
            txnId = txnId,
            date = dateStr,
            memberId = approval.memberId,
            memberName = approval.memberName,
            mobile = approval.mobile,
            rdAmount = editedRd,
            interestAmount = editedInterest,
            penaltyAmount = editedPenalty,
            loanRepayAmount = editedLoanRepay,
            waiverAmount = waiver,
            totalAmount = finalTotal,
            mode = approval.mode,
            remarks = remarksText,
            utrNumber = approval.utrNumber,
            isEdited = isEdited
        )

        val updatedPayments = listOf(payment) + _payments.value
        _payments.value = updatedPayments
        savePaymentsToLocal(updatedPayments)

        val updatedMembers = _members.value.map { m ->
            if (m.id == approval.memberId) {
                val newGullakLoan = (m.gullakLoan - editedLoanRepay).coerceAtLeast(0)
                val newPendingDues = (m.pendingDues - editedRd).coerceAtLeast(0)
                val newOpeningRd = m.openingRd + editedRd
                m.copy(
                    gullakLoan = newGullakLoan,
                    pendingDues = newPendingDues,
                    openingRd = newOpeningRd,
                    penaltyApplicable = 0
                )
            } else {
                m
            }
        }
        _members.value = updatedMembers
        saveMembersToLocal(updatedMembers)

        val updatedApprovals = currentApprovals.map {
            if (it.id == approvalId) it.copy(status = if (isEdited) "APPROVED WITH EDITED" else "APPROVED") else it
        }
        _pendingApprovals.value = updatedApprovals
        saveApprovalsToLocal(updatedApprovals)

        addAuditLog(
            if (isEdited) "APPROVAL ACCEPTED WITH EDITS" else "APPROVAL ACCEPTED",
            "Payment request for ${approval.memberName} approved. Final Total: ₹$finalTotal"
        )

        NotificationHelper.sendPushNotification(
            context = context,
            title = "PAYMENT APPROVED",
            message = "Your ₹$finalTotal payment has been approved and added to your Passbook.",
            target = NotificationTarget.MEMBER_ONLY,
            targetMemberId = approval.memberId
        )

        return true
    }

    fun rejectPayment(approvalId: String, reason: String = "Rejected by Admin"): Boolean {
        return rejectPaymentRequest(approvalId, reason)
    }

    fun rejectPaymentRequest(approvalId: String, reason: String = "Rejected by Admin"): Boolean {
        val current = _pendingApprovals.value
        val item = current.find { it.id == approvalId } ?: return false

        val updated = current.map {
            if (it.id == approvalId) it.copy(status = "REJECTED", rejectionReason = reason) else it
        }
        _pendingApprovals.value = updated
        saveApprovalsToLocal(updated)

        addAuditLog(
            "APPROVAL REJECTED",
            "Payment request of ₹${item.totalAmount} for ${item.memberName} was rejected. Reason: $reason"
        )

        NotificationHelper.sendPushNotification(
            context = context,
            title = "PAYMENT VERIFICATION FAILED",
            message = "Payment of ₹${item.totalAmount} was rejected: $reason",
            target = NotificationTarget.MEMBER_ONLY,
            targetMemberId = item.memberId
        )

        return true
    }

    fun addAuditLog(title: String, details: String) {
        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val log = AuditLog("LOG-${System.currentTimeMillis() % 10000}", time, title, details)
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    suspend fun syncWithGoogleSheet(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!_isLiveSyncActive.value) {
            return@withContext Pair(false, "Live Sync is currently PAUSED by Admin. Tap Live toggle to resume.")
        }
        var url = _webAppUrl.value.trim()
        if (url.isBlank() || url.contains("AKfycbz_gullak_society_master_sync_v64")) {
            url = DEFAULT_WEB_APP_URL
            _webAppUrl.value = DEFAULT_WEB_APP_URL
            prefs.edit().putString("web_app_url", DEFAULT_WEB_APP_URL).apply()
        }
        if (url.startsWith("https://script.google.com/") && !url.endsWith("/exec")) {
            if (url.endsWith("/")) {
                url += "exec"
            } else {
                url += "/exec"
            }
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
                return@withContext Pair(false, "Failed to connect: HTTP ${response.code}. Please verify Web App URL.")
            }

            val json = JSONObject(body)
            val dataObj = if (json.has("data")) json.getJSONObject("data") else json
            if (dataObj.has("members")) {
                val memArray = dataObj.getJSONArray("members")
                val parsedMembers = mutableListOf<Member>()
                for (i in 0 until memArray.length()) {
                    val m = memArray.getJSONObject(i)
                    val rawPhone = m.optString("mobile", "")
                    val rawDue = m.optString("dueDay", "15th of every month")
                    val rawJoin = m.optString("joinDate", m.optString("dateJoined", "2026-01-01"))

                    parsedMembers.add(
                        Member(
                            id = m.optString("id", "MEM$i"),
                            name = m.optString("name", "Unknown"),
                            mobile = sanitizeMobileNumber(rawPhone).ifEmpty { rawPhone.trim() },
                            address = m.optString("address", ""),
                            nominee = m.optString("nominee", ""),
                            monthlyRd = m.optInt("monthlyRd", m.optInt("rd", 400)),
                            status = m.optString("status", "ACTIVE"),
                            joinDate = sanitizeJoinDate(rawJoin),
                            openingRd = m.optInt("openingRd", m.optInt("rdPaid", 4800)),
                            dueDay = sanitizeDueDay(rawDue),
                            gullakLoan = m.optInt("gullakLoan", m.optInt("opLoan", 0)),
                            emergencyLoan = m.optInt("emergencyLoan", 0),
                            pendingDues = m.optInt("pendingDues", 0),
                            npaLoss = m.optInt("npaLoss", 0),
                            loanLimit = m.optInt("loanLimit", m.optInt("customLimit", 0)),
                            loginPin = m.optString("loginPin", "1234"),
                            notificationsEnabled = m.optBoolean("notificationsEnabled", true),
                            isAppInstalled = m.optBoolean("isAppInstalled", false),
                            penaltyApplicable = m.optInt("penaltyApplicable", m.optInt("penalty", m.optInt("opPen", 0))),
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
                context = context,
                title = "CLOUD SYNC COMPLETED",
                message = "Successfully synchronized ${_members.value.size} society members with Google Sheets.",
                target = NotificationTarget.ADMIN_ONLY
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
            obj.put("joinDate", sanitizeJoinDate(m.joinDate))
            obj.put("openingRd", m.openingRd)
            obj.put("dueDay", sanitizeDueDay(m.dueDay))
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
            val rawPhone = m.optString("mobile", "")
            val rawDue = m.optString("dueDay", "15th of every month")
            val rawJoin = m.optString("joinDate", "2026-01-01")

            list.add(
                Member(
                    id = m.optString("id"),
                    name = m.optString("name"),
                    mobile = sanitizeMobileNumber(rawPhone).ifEmpty { rawPhone.trim() },
                    address = m.optString("address"),
                    nominee = m.optString("nominee"),
                    monthlyRd = m.optInt("monthlyRd", 400),
                    status = m.optString("status", "ACTIVE"),
                    joinDate = sanitizeJoinDate(rawJoin),
                    openingRd = m.optInt("openingRd", 4800),
                    dueDay = sanitizeDueDay(rawDue),
                    gullakLoan = m.optInt("gullakLoan", 0),
                    emergencyLoan = m.optInt("emergencyLoan", 0),
                    pendingDues = m.optInt("pendingDues", 0),
                    npaLoss = m.optInt("npaLoss", 0),
                    loanLimit = m.optInt("loanLimit", 0),
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
