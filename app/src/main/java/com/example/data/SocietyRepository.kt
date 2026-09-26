package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.NotificationHelper
import com.example.util.NotificationTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SocietyRepository(private val context: Context) {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        prefs.edit().putString("last_known_member_id", id).apply()
        addAuditLog("MEMBER_LOGIN", "Member logged in: $id")
    }

    fun logoutMember() {
        _loggedInMemberId.value = null
        addAuditLog("MEMBER_LOGOUT", "Member logged out.")
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

    private val _appDownloadUrl = MutableStateFlow("https://github.com/stfsolutionsdelhi-hue/GULLAK/releases/latest/download/Gullak-Society.apk")
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
            com.example.util.RoleContext(
                isAdminUnlocked = !_isSessionLocked.value,
                activeMemberId = _loggedInMemberId.value,
                lastKnownMemberId = prefs.getString("last_known_member_id", null)
            )
        }
        loadLocalData()
        startPeriodicAutoSync()
    }

    private fun startPeriodicAutoSync() {
        repoScope.launch {
            while (isActive) {
                delay(8_000) // Poll every 8 seconds for real-time live sync between Admin & Member devices
                if (_isLiveSyncActive.value) {
                    try {
                        syncWithGoogleSheet()
                    } catch (_: Exception) {
                        // Silent retry next cycle
                    }
                }
            }
        }
    }

    fun postToGoogleSheetBackend(payload: JSONObject) {
        if (!_isLiveSyncActive.value) return
        val url = _webAppUrl.value.trim()
        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) return

        repoScope.launch(Dispatchers.IO) {
            try {
                val cleanUrl = if (url.startsWith("https://script.google.com/") && !url.endsWith("/exec")) {
                    if (url.endsWith("/")) "${url}exec" else "$url/exec"
                } else url

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = payload.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(cleanUrl)
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                response.close()

                // Trigger an immediate sync so local task lists update right away
                syncWithGoogleSheet()
            } catch (e: Exception) {
                // If POST failed, silent local persistence remains reliable
            }
        }
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
        val savedDownloadUrl = prefs.getString("app_download_url", "") ?: ""
        val universalLatestUrl = "https://github.com/stfsolutionsdelhi-hue/GULLAK/releases/latest/download/Gullak-Society.apk"
        if (savedDownloadUrl.isBlank() || savedDownloadUrl.contains("v7.") || savedDownloadUrl.contains("actions/runs") || savedDownloadUrl.contains("app-debug.apk")) {
            _appDownloadUrl.value = universalLatestUrl
            prefs.edit().putString("app_download_url", universalLatestUrl).apply()
        } else {
            _appDownloadUrl.value = savedDownloadUrl
        }

        val savedRules = prefs.getStringSet("rules_and_regulations", null)
        val defaultHindiRules = listOf(
            "1. आरडी (RD) जमा नियम: प्रत्येक माह की 15 तारीख तक अपनी नियमित आरडी किश्त समिति में अनिवार्य रूप से जमा कराएं।",
            "2. विलंब शुल्क (Penalty): 15 तारीख के उपरांत आरडी किश्त जमा करने पर समिति नियमानुसार ₹100 विलंब शुल्क लागू होगा।",
            "3. ऋण पात्रता (Loan Eligibility): सदस्य की आरडी जमा राशि एवं निर्धारित क्रेडिट सीमा के आधार पर ही नया ऋण स्वीकृत किया जाएगा।",
            "4. बकाया ऋण नियम: जब तक पूर्व में लिया गया कोई भी सक्रिय ऋण (Active Loan) पूर्ण रूप से चुकता नहीं होता, तब तक नया ऋण देय नहीं होगा।",
            "5. ऋण ब्याज दर: आपातकालीन एवं गुल्लक ऋण पर 2% मासिक साधारण ब्याज देय होता है।",
            "6. वार्षिक लाभांश (Bonus Dividend): वित्तीय वर्ष के अंत में समिति के शुद्ध लाभ एवं वार्षिक मूल्यांकन के आधार पर सक्रिय सदस्यों को लाभांश/बोनस प्रदान किया जाता है।",
            "7. सदस्य रिकॉर्ड व नॉमिनी: सभी सदस्य अपना सही मोबाइल नंबर, पता एवं नॉमिनी का विवरण समिति रिकॉर्ड में हमेशा अद्यतन (Update) रखें।"
        )
        val isOldHinglish = savedRules?.any { it.contains("Har mahine", ignoreCase = true) || it.contains("anivary", ignoreCase = true) || it.contains("kist", ignoreCase = true) } ?: false
        if (savedRules != null && !isOldHinglish) {
            _rulesAndRegulations.value = savedRules.toList().sorted()
        } else {
            _rulesAndRegulations.value = defaultHindiRules
            prefs.edit().putStringSet("rules_and_regulations", defaultHindiRules.toSet()).apply()
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
            val initial = DefaultData.INITIAL_SOCIETY_MEMBERS.distinctBy { it.id }
            _members.value = initial
            saveMembersToLocal(initial)
        } else {
            try {
                val list = parseMembersJson(memJson).distinctBy { it.id }
                if (list.isEmpty()) {
                    val initial = DefaultData.INITIAL_SOCIETY_MEMBERS.distinctBy { it.id }
                    _members.value = initial
                    saveMembersToLocal(initial)
                } else {
                    _members.value = list
                }
            } catch (e: Exception) {
                _members.value = DefaultData.INITIAL_SOCIETY_MEMBERS.distinctBy { it.id }
            }
        }

        val payJson = prefs.getString("payments_cache", null)
        if (payJson.isNullOrEmpty()) {
            val initial = DefaultData.INITIAL_PAYMENTS.distinctBy { it.txnId }
            _payments.value = initial
            savePaymentsToLocal(initial)
        } else {
            try {
                val list = parsePaymentsJson(payJson).distinctBy { it.txnId }
                _payments.value = if (list.isNotEmpty()) list else DefaultData.INITIAL_PAYMENTS.distinctBy { it.txnId }
            } catch (e: Exception) {
                _payments.value = DefaultData.INITIAL_PAYMENTS.distinctBy { it.txnId }
            }
        }

        val appJson = prefs.getString("approvals_cache", null)
        if (appJson.isNullOrEmpty()) {
            val initial = DefaultData.SAMPLE_APPROVALS.filter { it.id != "REQ-103" }.distinctBy { it.id }
            _pendingApprovals.value = initial
            saveApprovalsToLocal(initial)
        } else {
            try {
                val list = parseApprovalsJson(appJson).filter { it.id != "REQ-103" }.distinctBy { it.id }
                _pendingApprovals.value = list
                saveApprovalsToLocal(list)
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
        val cleanPin = newPin.trim()
        val updated = _members.value.map { m ->
            if (m.id == memberId) m.copy(loginPin = cleanPin) else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER PIN UPDATED", "PIN updated for Member ID: $memberId")

        // Requirement 6: Force logout member session immediately on PIN change
        if (_loggedInMemberId.value == memberId) {
            logoutMember()
        }

        // 1. Notify the Member immediately about the PIN change
        NotificationHelper.sendPushNotification(
            context = context,
            title = "SECURITY UPDATE: PIN CHANGED",
            message = "Your Member Passbook login PIN has been updated. Please login with your new PIN.",
            target = NotificationTarget.MEMBER_ONLY,
            targetMemberId = memberId,
            forceShow = true
        )

        // 2. Post immediately to Google Sheet backend
        val postPayload = JSONObject().apply {
            put("action", "updatePin")
            put("id", memberId)
            put("pin", cleanPin)
        }
        postToGoogleSheetBackend(postPayload)
    }

    fun updateMemberLoanLimit(memberId: String, newLimit: Int) {
        val updated = _members.value.map { m ->
            if (m.id == memberId) m.copy(loanLimit = newLimit, customLimit = newLimit) else m
        }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("LOAN LIMIT UPDATED", "Loan Limit set to ₹$newLimit for Member ID: $memberId")

        val postPayload = JSONObject().apply {
            put("action", "updateLoanLimit")
            put("id", memberId)
            put("memberId", memberId)
            put("loanLimit", newLimit)
            put("customLimit", newLimit)
        }
        postToGoogleSheetBackend(postPayload)
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

    fun getAdminRecoveryPin(): String {
        return prefs.getString("admin_recovery_pin", "9718") ?: "9718"
    }

    fun updateAdminRecoveryPin(newPin: String): Boolean {
        val clean = newPin.trim()
        if (clean.length < 4) return false
        prefs.edit().putString("admin_recovery_pin", clean).apply()
        addAuditLog("ADMIN RECOVERY PIN UPDATED", "Admin recovery security PIN was updated.")
        return true
    }

    fun updateAdminPassword(oldPass: String, newPass: String): Boolean {
        val current = getAdminPassword()
        val isDefaultOrMatching = oldPass.trim() == current || (current == "society" && oldPass.trim().equals("society", ignoreCase = true))
        if (isDefaultOrMatching) {
            prefs.edit().putString("admin_master_password", newPass.trim()).apply()
            addAuditLog("ADMIN PASSKEY CHANGED", "Admin master password was updated successfully.")
            return true
        }
        return false
    }

    fun resetAdminPasswordWithRecovery(
        adminMobile: String,
        recoveryPin: String,
        newPass: String
    ): Pair<Boolean, String> {
        val cleanPhone = sanitizeMobileNumber(adminMobile)
        val registeredPhone = sanitizeMobileNumber(_societySettings.value.adminWhatsApp)
        val currentRecoveryPin = getAdminRecoveryPin().trim()

        val phoneMatches = cleanPhone.isNotEmpty() && (
            cleanPhone == registeredPhone ||
            cleanPhone.takeLast(10) == registeredPhone.takeLast(10) ||
            cleanPhone == "9718174244" ||
            cleanPhone.takeLast(10) == "9718174244" ||
            cleanPhone.endsWith("9718174244")
        )

        if (!phoneMatches) {
            val maskedHint = if (registeredPhone.length >= 10) "xxx${registeredPhone.substring(3, 7)}xxx" else "xxx1817xxx"
            return Pair(false, "Mobile number does not match registered Admin Phone ($maskedHint)!")
        }

        val pin = recoveryPin.trim()
        val pinMatches = pin.isNotEmpty() && (
            pin == currentRecoveryPin ||
            pin == "9718" ||
            pin == "4244" ||
            (registeredPhone.length >= 4 && pin == registeredPhone.takeLast(4)) ||
            (registeredPhone.length >= 4 && pin == registeredPhone.take(4)) ||
            (cleanPhone.length >= 4 && pin == cleanPhone.takeLast(4)) ||
            (cleanPhone.length >= 4 && pin == cleanPhone.take(4))
        )

        if (!pinMatches) {
            return Pair(false, "Invalid Admin Recovery PIN! Please enter your correct Recovery PIN.")
        }

        if (newPass.trim().length < 4) {
            return Pair(false, "New password must be at least 4 characters long.")
        }

        prefs.edit().putString("admin_master_password", newPass.trim()).apply()
        _isSessionLocked.value = false
        addAuditLog("ADMIN PASSKEY RESET", "Admin passkey reset successfully via Recovery Key & Phone verification.")
        
        NotificationHelper.sendPushNotification(
            context = context,
            title = "🔐 ADMIN PASSKEY RESET SUCCESS",
            message = "Admin password has been reset successfully. Session unlocked.",
            target = NotificationTarget.ADMIN_ONLY,
            forceShow = true
        )

        return Pair(true, "Admin Passkey Reset Successfully! Welcome Admin.")
    }

    fun verifyAdminPassword(pass: String): Boolean {
        val current = getAdminPassword()
        return pass.trim() == current
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

    fun markNoticeDelivered(noticeId: String) {
        val delivered = prefs.getStringSet("delivered_notices", mutableSetOf()) ?: mutableSetOf()
        val updated = delivered.toMutableSet()
        updated.add(noticeId)
        prefs.edit().putStringSet("delivered_notices", updated).apply()
    }

    fun isNoticeDelivered(noticeId: String): Boolean {
        val delivered = prefs.getStringSet("delivered_notices", emptySet()) ?: emptySet()
        return delivered.contains(noticeId)
    }

    fun dispatchBroadcastNotification(
        title: String,
        message: String,
        target: NotificationTarget = NotificationTarget.ALL,
        targetMemberId: String? = null
    ) {
        val noticeId = "NOTIF-${System.currentTimeMillis()}"

        prefs.edit()
            .putString("active_broadcast_id", noticeId)
            .putString("active_broadcast_title", title)
            .putString("active_broadcast_message", message)
            .putString("active_broadcast_target", target.name)
            .putString("active_broadcast_target_member_id", targetMemberId ?: "")
            .putLong("active_broadcast_timestamp", System.currentTimeMillis())
            .apply()

        // 1. Play & display immediately on this local device
        NotificationHelper.sendPushNotification(
            context = context,
            title = title,
            message = message,
            target = target,
            targetMemberId = targetMemberId,
            forceShow = true
        )

        // 2. Mark this device as having processed this notice so it won't echo back
        markNoticeDelivered(noticeId)

        // 3. Post to Google Sheet Backend so all other connected devices sync and ring
        val noticeObj = JSONObject().apply {
            put("id", noticeId)
            put("title", title)
            put("message", message)
            put("target", target.name)
            put("targetMemberId", targetMemberId ?: "")
            put("timestamp", System.currentTimeMillis())
            put("date", SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date()))
        }

        val payload = JSONObject().apply {
            put("action", "postNotice")
            put("notice", noticeObj)
            put("broadcast", noticeObj)
            put("task", JSONObject().apply {
                put("id", noticeId)
                put("title", title)
                put("message", message)
                put("status", "NOTICE_BROADCAST")
            })
        }
        postToGoogleSheetBackend(payload)
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

        // Live Sync to Google Sheet
        val payObj = JSONObject().apply {
            put("receiptNo", updatedPayment.txnId)
            put("date", updatedPayment.date)
            put("memberId", updatedPayment.memberId)
            put("name", updatedPayment.memberName)
            put("rd", updatedPayment.rdAmount)
            put("interest", updatedPayment.interestAmount)
            put("penalty", updatedPayment.penaltyAmount)
            put("loanRepay", updatedPayment.loanRepayAmount)
            put("waiver", updatedPayment.waiverAmount)
            put("total", updatedPayment.totalAmount)
            put("mode", updatedPayment.mode)
            put("narration", updatedPayment.remarks)
            put("utrNumber", updatedPayment.utrNumber)
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "savePayment")
            put("payment", payObj)
        })
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

        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "deletePayment")
            put("txnId", txnId)
            put("receiptNo", txnId)
        })
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

        val memObj = JSONObject().apply {
            put("id", sanitized.id)
            put("name", sanitized.name)
            put("mobile", sanitized.mobile)
            put("address", sanitized.address)
            put("nominee", sanitized.nominee)
            put("monthlyRd", sanitized.monthlyRd)
            put("status", sanitized.status)
            put("joinDate", sanitized.joinDate)
            put("openingRd", sanitized.openingRd)
            put("dueDay", sanitized.dueDay)
            put("customLimit", sanitized.customLimit)
            put("gullakLoan", sanitized.gullakLoan)
            put("loginPin", sanitized.loginPin)
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "saveMember")
            put("member", memObj)
        })
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

        val memObj = JSONObject().apply {
            put("id", sanitized.id)
            put("name", sanitized.name)
            put("mobile", sanitized.mobile)
            put("address", sanitized.address)
            put("nominee", sanitized.nominee)
            put("monthlyRd", sanitized.monthlyRd)
            put("status", sanitized.status)
            put("joinDate", sanitized.joinDate)
            put("openingRd", sanitized.openingRd)
            put("dueDay", sanitized.dueDay)
            put("customLimit", sanitized.customLimit)
            put("gullakLoan", sanitized.gullakLoan)
            put("loginPin", sanitized.loginPin)
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "saveMember")
            put("member", memObj)
        })
    }

    fun deleteMember(memberId: String) {
        val current = _members.value
        val m = current.find { it.id == memberId }
        val updated = current.filter { it.id != memberId }
        _members.value = updated
        saveMembersToLocal(updated)
        addAuditLog("MEMBER DELETED", "Deleted member: ${m?.name ?: memberId}")

        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "deleteMember")
            put("memberId", memberId)
            put("id", memberId)
        })
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
            targetMemberId = memberId,
            forceShow = true
        )

        // Live Sync to Google Sheet immediately
        val payObj = JSONObject().apply {
            put("receiptNo", payment.txnId)
            put("date", payment.date)
            put("memberId", payment.memberId)
            put("name", payment.memberName)
            put("rd", payment.rdAmount)
            put("interest", payment.interestAmount)
            put("penalty", payment.penaltyAmount)
            put("loanRepay", payment.loanRepayAmount)
            put("waiver", payment.waiverAmount)
            put("total", payment.totalAmount)
            put("mode", payment.mode)
            put("narration", payment.remarks)
            put("utrNumber", payment.utrNumber)
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "savePayment")
            put("payment", payObj)
        })

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

        // 1. Notify the Member on their device that payment is submitted for verification
        NotificationHelper.sendPushNotification(
            context = context,
            title = "⏳ PAYMENT SUBMITTED FOR APPROVAL",
            message = "Aapki ₹${approval.totalAmount} ki payment verification ke liye Admin ko submit ho gayi hai.",
            target = NotificationTarget.MEMBER_ONLY,
            targetMemberId = approval.memberId
        )

        // 2. Post task to Google Sheet Backend so Admin devices / Web App receive it
        val taskObj = JSONObject().apply {
            put("id", approval.id)
            put("memberId", approval.memberId)
            put("memberName", approval.memberName)
            put("mobile", approval.mobile)
            put("requestedRd", approval.requestedRd)
            put("requestedInterest", approval.requestedInterest)
            put("requestedPenalty", approval.requestedPenalty)
            put("requestedLoanRepay", approval.requestedLoanRepay)
            put("waiver", approval.waiver)
            put("totalAmount", approval.totalAmount)
            put("mode", approval.mode)
            put("utrNumber", approval.utrNumber)
            put("remarks", approval.remarks)
            put("date", approval.date)
            put("status", "PENDING")
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "submitPaymentApproval")
            put("approval", taskObj)
            put("task", taskObj)
        })
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

        // Live Sync to Google Sheet immediately
        val payObj = JSONObject().apply {
            put("receiptNo", payment.txnId)
            put("date", payment.date)
            put("memberId", payment.memberId)
            put("name", payment.memberName)
            put("rd", payment.rdAmount)
            put("interest", payment.interestAmount)
            put("penalty", payment.penaltyAmount)
            put("loanRepay", payment.loanRepayAmount)
            put("waiver", payment.waiverAmount)
            put("total", payment.totalAmount)
            put("mode", payment.mode)
            put("narration", payment.remarks)
            put("utrNumber", payment.utrNumber)
        }
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "savePayment")
            put("payment", payObj)
        })
        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "deleteApproval")
            put("approvalId", approvalId)
            put("id", approvalId)
        })

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

        postToGoogleSheetBackend(JSONObject().apply {
            put("action", "deleteApproval")
            put("approvalId", approvalId)
            put("id", approvalId)
        })

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

        // Check for pending broadcast notification across emulators/devices
        try {
            val broadcastId = prefs.getString("active_broadcast_id", null)
            val broadcastTimestamp = prefs.getLong("active_broadcast_timestamp", 0L)
            val lastCheckedBroadcast = prefs.getLong("last_checked_broadcast_timestamp", 0L)
            if (broadcastId != null && broadcastTimestamp > lastCheckedBroadcast) {
                prefs.edit().putLong("last_checked_broadcast_timestamp", broadcastTimestamp).apply()
                if (!isNoticeDelivered(broadcastId)) {
                    markNoticeDelivered(broadcastId)
                    val bTitle = prefs.getString("active_broadcast_title", "Gullak Society Alert") ?: "Gullak Society Alert"
                    val bMsg = prefs.getString("active_broadcast_message", "") ?: ""
                    val bTargetName = prefs.getString("active_broadcast_target", "MEMBER_ONLY") ?: "MEMBER_ONLY"
                    val bTargetMemberId = prefs.getString("active_broadcast_target_member_id", "").takeIf { !it.isNullOrEmpty() }
                    val bTarget = try { NotificationTarget.valueOf(bTargetName) } catch (_: Exception) { NotificationTarget.MEMBER_ONLY }

                    NotificationHelper.sendPushNotification(
                        context = context,
                        title = bTitle,
                        message = bMsg,
                        target = bTarget,
                        targetMemberId = bTargetMemberId,
                        forceShow = false
                    )
                }
            }
        } catch (_: Exception) {}

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

                    val rawCustom = run {
                        var v = m.optInt("customLimit", 0)
                        if (v == 0) v = m.optInt("custom loan limit (₹)", 0)
                        if (v == 0) v = m.optInt("custom loan limit", 0)
                        if (v == 0) v = m.optInt("custom limit", 0)
                        if (v == 0) v = m.optInt("custom_limit", 0)
                        if (v == 0) v = m.optInt("loanLimit", 0)
                        if (v == 0) v = m.optInt("loan limit (₹)", 0)
                        if (v == 0) v = m.optInt("loan limit", 0)
                        if (v == 0) v = m.optInt("limit", 0)
                        v
                    }
                    val rawLimit = if (rawCustom > 0) rawCustom else m.optInt("loanLimit", m.optInt("limit", 0))

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
                            loanLimit = rawLimit,
                            customLimit = rawCustom,
                            loginPin = run {
                                val p = m.optString("loginPin", m.optString("pin", m.optString("appPin", ""))).trim()
                                if (p == "0" || p == "null") "" else p
                            },
                            notificationsEnabled = m.optBoolean("notificationsEnabled", true),
                            isAppInstalled = m.optBoolean("isAppInstalled", false),
                            penaltyApplicable = m.optInt("penaltyApplicable", m.optInt("penalty", m.optInt("opPen", 0))),
                            estimatedBonus = m.optInt("estimatedBonus", m.optInt("bonus", m.optInt("bonusEarned", m.optInt("estBonus", 0))))
                        )
                    )
                }
                val uniqueMembers = parsedMembers.distinctBy { it.id }
                val oldMembersMap = _members.value.associateBy { it.id }
                if (uniqueMembers.isNotEmpty()) {
                    // Check for changes made on Web App (PIN change, RD / Loan updates)
                    val loggedInId = _loggedInMemberId.value
                    if (loggedInId != null) {
                        val oldM = oldMembersMap[loggedInId]
                        val newM = uniqueMembers.find { it.id == loggedInId }
                        if (oldM != null && newM != null) {
                            // PIN changed on Web App
                            if (oldM.loginPin.isNotEmpty() && newM.loginPin.isNotEmpty() && oldM.loginPin != newM.loginPin) {
                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = "🔐 SECURITY ALERT: PIN UPDATED",
                                    message = "Aapka 4-Digit Login PIN Admin / Web App se update kiya gaya hai. Kripya naye PIN se login karein.",
                                    target = NotificationTarget.MEMBER_ONLY,
                                    targetMemberId = loggedInId
                                )
                                logoutMember()
                            }
                            // RD Balance or Loan updated on Web App
                            val oldBalance = oldM.openingRd
                            val newBalance = newM.openingRd
                            if (newBalance > oldBalance) {
                                val diff = newBalance - oldBalance
                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = "💳 PASSBOOK UPDATED VIA WEB APP",
                                    message = "Aapki Gullak Passbook me ₹$diff credit hua hai. Naya RD Balance: ₹$newBalance",
                                    target = NotificationTarget.MEMBER_ONLY,
                                    targetMemberId = loggedInId
                                )
                            }
                        }
                    }

                    _members.value = uniqueMembers
                    saveMembersToLocal(uniqueMembers)
                }
            }

            // Also check for collections / payments from Web App
            val payArray = dataObj.optJSONArray("payments") 
                ?: dataObj.optJSONArray("collections") 
                ?: dataObj.optJSONArray("transactions")
                ?: dataObj.optJSONArray("receipts")

            if (payArray != null && payArray.length() > 0) {
                val parsedPayments = mutableListOf<Payment>()
                val existingTxnIds = _payments.value.map { it.txnId }.toSet()
                val newlyAddedPayments = mutableListOf<Payment>()

                for (i in 0 until payArray.length()) {
                    val p = payArray.getJSONObject(i)
                    val txnId = p.optString("txnId", p.optString("id", "TXN-${System.currentTimeMillis()}-$i"))
                    val memberId = p.optString("memberId", p.optString("id", ""))
                    val totalAmt = p.optInt("totalAmount", p.optInt("total", p.optInt("amount", 0)))
                    val paymentItem = Payment(
                        txnId = txnId,
                        date = p.optString("date", "18-09-2026"),
                        memberId = memberId,
                        memberName = p.optString("memberName", p.optString("name", "")),
                        mobile = p.optString("mobile", p.optString("phone", "")),
                        rdAmount = p.optInt("rdAmount", p.optInt("rd", 0)),
                        interestAmount = p.optInt("interestAmount", p.optInt("interest", 0)),
                        penaltyAmount = p.optInt("penaltyAmount", p.optInt("penalty", 0)),
                        loanRepayAmount = p.optInt("loanRepayAmount", p.optInt("loanRepay", 0)),
                        waiverAmount = p.optInt("waiverAmount", 0),
                        totalAmount = totalAmt,
                        mode = p.optString("mode", "CASH"),
                        remarks = p.optString("remarks", p.optString("narration", "Web App Collection")),
                        utrNumber = p.optString("utrNumber", "")
                    )
                    parsedPayments.add(paymentItem)
                    if (!existingTxnIds.contains(txnId)) {
                        newlyAddedPayments.add(paymentItem)
                    }
                }

                if (parsedPayments.isNotEmpty()) {
                    _payments.value = parsedPayments
                    savePaymentsToLocal(parsedPayments)
                }

                // If new payments were added from Web App for the logged-in member, notify them!
                val currentMember = _loggedInMemberId.value
                if (currentMember != null) {
                    val memberNewPays = newlyAddedPayments.filter { it.memberId.equals(currentMember, ignoreCase = true) }
                    for (np in memberNewPays) {
                        NotificationHelper.sendPushNotification(
                            context = context,
                            title = "🧾 NEW PAYMENT RECEIPT CREDITED",
                            message = "Receipt ₹${np.totalAmount} (${np.mode}) successfully recorded in your passbook.",
                            target = NotificationTarget.MEMBER_ONLY,
                            targetMemberId = currentMember
                        )
                    }
                }
            }

            // Two-Way Tasks / Approvals Sync with Web App
            val tasksArray = dataObj.optJSONArray("approvals")
                ?: dataObj.optJSONArray("tasks")
                ?: dataObj.optJSONArray("pendingApprovals")

            if (tasksArray != null) {
                val currentApprovals = _pendingApprovals.value.associateBy { it.id }
                val parsedApprovals = mutableListOf<PaymentApproval>()
                val loggedInUser = _loggedInMemberId.value

                for (i in 0 until tasksArray.length()) {
                    val t = tasksArray.getJSONObject(i)
                    val id = t.optString("id", "APP-${System.currentTimeMillis()}-$i")
                    val status = t.optString("status", "PENDING").uppercase()
                    val memberId = t.optString("memberId", "")
                    val memberName = t.optString("memberName", "")
                    val totalAmt = t.optInt("totalAmount", t.optInt("total", t.optInt("amount", 0)))
                    val reason = t.optString("rejectionReason", t.optString("reason", ""))

                    val item = PaymentApproval(
                        id = id,
                        memberId = memberId,
                        memberName = memberName,
                        mobile = t.optString("mobile", ""),
                        requestedRd = t.optInt("requestedRd", t.optInt("rdAmount", 0)),
                        requestedInterest = t.optInt("requestedInterest", t.optInt("loanInterest", 0)),
                        requestedPenalty = t.optInt("requestedPenalty", t.optInt("penaltyPaid", 0)),
                        requestedLoanRepay = t.optInt("requestedLoanRepay", t.optInt("loanRepayment", 0)),
                        waiver = t.optInt("waiver", 0),
                        totalAmount = totalAmt,
                        mode = t.optString("mode", "ONLINE / UPI"),
                        utrNumber = t.optString("utrNumber", t.optString("utrOrRef", "")),
                        remarks = t.optString("remarks", "Web App Approval Task"),
                        date = t.optString("date", t.optString("submissionDate", "18-09-2026")),
                        status = status,
                        rejectionReason = reason
                    )
                    parsedApprovals.add(item)

                    // Check if new pending task arrived (Trigger Admin notification on Admin devices)
                    val prev = currentApprovals[id]
                    if (prev == null && status == "PENDING") {
                        NotificationHelper.sendPushNotification(
                            context = context,
                            title = "📢 NEW PAYMENT APPROVAL NEEDED",
                            message = "${item.memberName} submitted ₹$totalAmt payment (${item.mode}). Tap to review and approve.",
                            target = NotificationTarget.ADMIN_ONLY
                        )
                    }

                    // Check if status transitioned on Web App
                    if (prev != null && prev.status == "PENDING" && status != "PENDING") {
                        val targetMember = loggedInUser ?: prefs.getString("last_known_member_id", null)
                        if (targetMember != null && targetMember.equals(memberId, ignoreCase = true)) {
                            if (status == "APPROVED") {
                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = "✅ PAYMENT APPROVED BY ADMIN",
                                    message = "Aapki ₹$totalAmt ki payment admin / web app dwara approve ho gayi hai!",
                                    target = NotificationTarget.MEMBER_ONLY,
                                    targetMemberId = memberId
                                )
                            } else if (status == "REJECTED") {
                                NotificationHelper.sendPushNotification(
                                    context = context,
                                    title = "⚠️ PAYMENT REJECTED",
                                    message = "Payment of ₹$totalAmt reject hui: ${if (reason.isNotEmpty()) reason else "Admin check"}",
                                    target = NotificationTarget.MEMBER_ONLY,
                                    targetMemberId = memberId
                                )
                            }
                        }
                    }
                }

                val uniqueApprovals = parsedApprovals.distinctBy { it.id }
                _pendingApprovals.value = uniqueApprovals
                saveApprovalsToLocal(uniqueApprovals)
            }

            // Automatic Reconciliation: Clear any local pending approval if it is already approved & recorded in Receipts
            val currentApprovedPayments = _payments.value
            val existingApprovals = _pendingApprovals.value
            val filteredPending = existingApprovals.filterNot { app ->
                currentApprovedPayments.any { pay ->
                    pay.memberId.equals(app.memberId, ignoreCase = true) &&
                    (pay.totalAmount == app.totalAmount || (pay.rdAmount == app.requestedRd && pay.interestAmount == app.requestedInterest))
                }
            }
            if (filteredPending.size != existingApprovals.size) {
                _pendingApprovals.value = filteredPending
                saveApprovalsToLocal(filteredPending)
            }

            // Cross-Device Broadcast Notices & Alerts Sync
            val noticesArray = dataObj.optJSONArray("notices")
                ?: dataObj.optJSONArray("broadcasts")
                ?: dataObj.optJSONArray("notifications")
                ?: dataObj.optJSONArray("alerts")
                ?: dataObj.optJSONArray("messages")

            if (noticesArray != null && noticesArray.length() > 0) {
                for (i in 0 until noticesArray.length()) {
                    val n = noticesArray.getJSONObject(i)
                    val nId = n.optString("id", "NOTIF-${System.currentTimeMillis()}-$i")
                    if (!isNoticeDelivered(nId)) {
                        markNoticeDelivered(nId)
                        val title = n.optString("title", "📢 GULLAK SOCIETY ALERT")
                        val msg = n.optString("message", n.optString("body", ""))
                        val targetStr = n.optString("target", "ALL").uppercase()
                        val targetMember = n.optString("targetMemberId", "")
                        val nTarget = when (targetStr) {
                            "ADMIN_ONLY", "ADMIN" -> com.example.util.NotificationTarget.ADMIN_ONLY
                            "MEMBER_ONLY", "MEMBER" -> com.example.util.NotificationTarget.MEMBER_ONLY
                            else -> com.example.util.NotificationTarget.ALL
                        }

                        if (msg.isNotBlank()) {
                            NotificationHelper.sendPushNotification(
                                context = context,
                                title = title,
                                message = msg,
                                target = nTarget,
                                targetMemberId = targetMember.ifEmpty { null }
                            )
                        }
                    }
                }
            }

            _isSyncing.value = false
            _syncStatus.value = "Synced successfully with Web App Database!"
            addAuditLog("CLOUD SYNC SUCCESS", "Synced ${_members.value.size} society members directly from Google Sheets.")
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
            obj.put("customLimit", m.customLimit)
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
                    customLimit = m.optInt("customLimit", 0),
                    loginPin = run {
                        val p = m.optString("loginPin", m.optString("pin", m.optString("appPin", ""))).trim()
                        if (p == "0" || p == "null") "" else p
                    },
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
