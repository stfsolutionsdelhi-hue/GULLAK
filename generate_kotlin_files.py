import os, json

os.makedirs("app/src/main/java/com/example/ui/theme", exist_ok=True)
os.makedirs("app/src/main/java/com/example/data", exist_ok=True)
os.makedirs("app/src/main/java/com/example/ui/screens", exist_ok=True)

# 1. Color.kt
color_kt = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF060913)
val CardDark = Color(0xFF0F172A)
val CardBorder = Color(0xFF1E293B)
val PrimaryGreen = Color(0xFF10B981)
val PrimaryGreenDark = Color(0xFF064E3B)
val AccentGold = Color(0xFFFBBF24)
val AccentBlue = Color(0xFF38BDF8)
val AccentRed = Color(0xFFEF4444)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val HeaderGold = Color(0xFFF59E0B)
"""

with open("app/src/main/java/com/example/ui/theme/Color.kt", "w", encoding="utf-8") as f:
    f.write(color_kt)

# 2. Type.kt
type_kt = """package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = TextMuted
    )
)
"""

with open("app/src/main/java/com/example/ui/theme/Type.kt", "w", encoding="utf-8") as f:
    f.write(type_kt)

# 3. Theme.kt
theme_kt = """package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryGreenDark,
    secondary = AccentGold,
    background = BgDark,
    surface = CardDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun GullakSocietyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
"""

with open("app/src/main/java/com/example/ui/theme/Theme.kt", "w", encoding="utf-8") as f:
    f.write(theme_kt)

# 4. Models.kt
# Read real 67 members from Part1_Server.gs
with open("Part1_Server.gs", "r", encoding="utf-8") as f:
    p1 = f.read()

import re
m_start = p1.find("function get67RealMembersArray()")
m_arr_start = p1.find("[", m_start)
m_arr_end = p1.find("];", m_arr_start) + 1
m_json_str = p1[m_arr_start:m_arr_end]
members_raw = json.loads(m_json_str)

kotlin_members = []
for m in members_raw:
    # [id, name, phone, address, nominee, monthlyRd, status, joinDate, openingRd, dueDay, gullakLoan, emergencyLoan, pendingDues, npaLoss]
    m_id = m[0]
    m_name = m[1].replace('"', '\\"')
    m_phone = str(m[2])
    m_addr = str(m[3]).replace('"', '\\"')
    m_nominee = str(m[4]).replace('"', '\\"')
    m_rd = int(m[5])
    m_status = str(m[6])
    m_date = str(m[7])
    m_open_rd = int(m[8])
    m_due = str(m[9]).replace('"', '\\"')
    m_g_loan = int(m[10])
    m_e_loan = int(m[11])
    m_dues = int(m[12])
    m_npa = int(m[13])
    kotlin_members.append(f'        Member(id = "{m_id}", name = "{m_name}", mobile = "{m_phone}", address = "{m_addr}", nominee = "{m_nominee}", monthlyRd = {m_rd}, status = "{m_status}", joinDate = "{m_date}", openingRd = {m_open_rd}, dueDay = "{m_due}", gullakLoan = {m_g_loan}, emergencyLoan = {m_e_loan}, pendingDues = {m_dues}, npaLoss = {m_npa})')

members_block = ",\n".join(kotlin_members)

models_kt = f"""package com.example.data

data class Member(
    val id: String,
    val name: String,
    val mobile: String,
    val address: String = "",
    val nominee: String = "",
    val monthlyRd: Int = 400,
    val status: String = "ACTIVE",
    val joinDate: String = "2026-01-01",
    val openingRd: Int = 4800,
    val dueDay: String = "15th of every month",
    val gullakLoan: Int = 0,
    val emergencyLoan: Int = 0,
    val pendingDues: Int = 0,
    val npaLoss: Int = 0
)

data class Payment(
    val txnId: String,
    val date: String,
    val memberId: String,
    val memberName: String,
    val mobile: String,
    val rdAmount: Int,
    val interestAmount: Int,
    val penaltyAmount: Int = 0,
    val loanRepayAmount: Int = 0,
    val totalAmount: Int,
    val mode: String = "CASH", // CASH or ONLINE
    val remarks: String = ""
)

data class Loan(
    val loanId: String,
    val memberId: String,
    val memberName: String,
    val mobile: String,
    val type: String = "Gullak Loan",
    val principal: Int,
    val interestRate: Double = 1.0,
    val outstanding: Int,
    val issueDate: String = "2026-01-01",
    val status: String = "ACTIVE"
)

data class AuditLog(
    val id: String,
    val timestamp: String,
    val title: String,
    val details: String
)

object DefaultData {{
    val REAL_67_MEMBERS = listOf(
{members_block}
    )
}}
"""

with open("app/src/main/java/com/example/data/Models.kt", "w", encoding="utf-8") as f:
    f.write(models_kt)

# 5. SocietyRepository.kt
repo_kt = """package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
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

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _webAppUrl = MutableStateFlow("")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _syncStatus = MutableStateFlow("Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        val savedUrl = prefs.getString("web_app_url", "") ?: ""
        _webAppUrl.value = savedUrl

        val memJson = prefs.getString("members_cache", null)
        if (memJson.isNullOrEmpty()) {
            _members.value = DefaultData.REAL_67_MEMBERS
            saveMembersToLocal(DefaultData.REAL_67_MEMBERS)
        } else {
            try {
                val list = parseMembersJson(memJson)
                if (list.size < 10) {
                    _members.value = DefaultData.REAL_67_MEMBERS
                    saveMembersToLocal(DefaultData.REAL_67_MEMBERS)
                } else {
                    _members.value = list
                }
            } catch (e: Exception) {
                _members.value = DefaultData.REAL_67_MEMBERS
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

        val logTime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        _auditLogs.value = listOf(
            AuditLog("LOG-001", logTime, "SYSTEM INITIALIZATION", "Gullak Society Android App Loaded with ${_members.value.size} Members.")
        )
    }

    fun saveWebAppUrl(url: String) {
        val clean = url.trim()
        _webAppUrl.value = clean
        prefs.edit().putString("web_app_url", clean).apply()
        addAuditLog("CLOUD URL UPDATED", "Connected Web App: $clean")
    }

    fun restore67RealMembers() {
        _members.value = DefaultData.REAL_67_MEMBERS
        saveMembersToLocal(DefaultData.REAL_67_MEMBERS)
        addAuditLog("RESTORE MEMBERS", "Restored all 67 official society members.")
    }

    fun recordPayment(
        memberId: String,
        memberName: String,
        mobile: String,
        rdAmount: Int,
        interestAmount: Int,
        penaltyAmount: Int,
        mode: String,
        remarks: String
    ) {
        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val txnId = "TXN-${System.currentTimeMillis() % 100000}"
        val total = rdAmount + interestAmount + penaltyAmount
        val payment = Payment(
            txnId = txnId,
            date = dateStr,
            memberId = memberId,
            memberName = memberName,
            mobile = mobile,
            rdAmount = rdAmount,
            interestAmount = interestAmount,
            penaltyAmount = penaltyAmount,
            totalAmount = total,
            mode = mode,
            remarks = remarks
        )
        val updated = listOf(payment) + _payments.value
        _payments.value = updated
        savePaymentsToLocal(updated)
        addAuditLog("PAYMENT RECEIVED", "₹$total received from $memberName ($mode)")
    }

    private fun addAuditLog(title: String, details: String) {
        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val log = AuditLog("LOG-${System.currentTimeMillis() % 10000}", time, title, details)
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    suspend fun syncWithGoogleSheet(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = _webAppUrl.value.trim()
        if (url.isEmpty()) {
            return@withContext Pair(false, "Web App URL not configured in Settings.")
        }
        _isSyncing.value = true
        _syncStatus.value = "Connecting to Google Sheet..."
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
                            npaLoss = m.optInt("npaLoss", 0)
                        )
                    )
                }
                if (parsedMembers.isNotEmpty()) {
                    _members.value = parsedMembers
                    saveMembersToLocal(parsedMembers)
                }
            }

            _isSyncing.value = false
            _syncStatus.value = "Synced successfully with Google Sheet!"
            addAuditLog("CLOUD SYNC SUCCESS", "Synced ${_members.value.size} members from Google Sheet.")
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
            obj.put("totalAmount", p.totalAmount)
            obj.put("mode", p.mode)
            obj.put("remarks", p.remarks)
            arr.put(obj)
        }
        prefs.edit().putString("payments_cache", arr.toString()).apply()
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
                    npaLoss = m.optInt("npaLoss", 0)
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
                    totalAmount = p.optInt("totalAmount"),
                    mode = p.optString("mode", "CASH"),
                    remarks = p.optString("remarks", "")
                )
            )
        }
        return list
    }
}
"""

with open("app/src/main/java/com/example/data/SocietyRepository.kt", "w", encoding="utf-8") as f:
    f.write(repo_kt)

print("Models.kt and SocietyRepository.kt created.")
