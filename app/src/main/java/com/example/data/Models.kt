package com.example.data

const val APP_VERSION = "v8.0"
const val APP_VERSION_TAG = "v8.0 (Cross-Device Push Sync, Approval Reconciliation & Status Bar Fix)"
const val APP_BUILD_DATE = "26 Sep 2026"
const val APP_SYNC_ENGINE = "Two-Way Cloud & Live Web App Sync Engine"

fun sanitizeMobileNumber(phone: String): String {
    var temp = phone.trim()
    if (temp.contains("E", ignoreCase = true)) {
        try {
            val bigDecimal = java.math.BigDecimal(temp)
            temp = bigDecimal.toPlainString()
        } catch (e: Exception) {
            // fallback
        }
    }
    if (temp.contains(".")) {
        temp = temp.substringBefore(".")
    }
    var clean = temp.filter { it.isDigit() }
    if (clean.length == 12 && clean.startsWith("91")) {
        clean = clean.substring(2)
    }
    if (clean.length == 11 && clean.startsWith("0")) {
        clean = clean.substring(1)
    }
    return clean
}

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
    val npaLoss: Int = 0,
    val loanLimit: Int = 50000,
    val customLimit: Int = 0,
    val loginPin: String = "",
    val notificationsEnabled: Boolean = true,
    val isAppInstalled: Boolean = false,
    val penaltyApplicable: Int = 0,
    val estimatedBonus: Int = 0
)

fun Member.getTotalRdDeposited(payments: List<Payment> = emptyList()): Int {
    return openingRd + payments.filter { it.memberId == id }.sumOf { it.rdAmount }
}

fun Member.getEffectiveLoanLimit(payments: List<Payment> = emptyList()): Int {
    if (status.equals("INACTIVE", ignoreCase = true)) return 0
    // Rule: Any active loan dues = Loan limit is strictly 0
    val totalActiveLoan = gullakLoan + emergencyLoan
    if (totalActiveLoan > 0) return 0
    if (customLimit > 0) return customLimit
    val totalRd = getTotalRdDeposited(payments)
    val calc = totalRd * 2
    return if (calc > 0) calc else 0
}

fun Member.getLoanLimitDisplay(payments: List<Payment> = emptyList()): String {
    if (status.equals("INACTIVE", ignoreCase = true)) return "₹0"
    val totalActiveLoan = gullakLoan + emergencyLoan
    if (totalActiveLoan > 0) {
        return "₹0"
    }
    val limit = getEffectiveLoanLimit(payments)
    return "₹%,d".format(java.util.Locale.ENGLISH, limit)
}

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
    val waiverAmount: Int = 0,
    val totalAmount: Int,
    val mode: String = "CASH", // CASH or ONLINE / UPI
    val remarks: String = "",
    val utrNumber: String = "",
    val isEdited: Boolean = false,
    val type: String = "CREDIT" // CREDIT or DEBIT
)

data class PaymentApproval(
    val id: String,
    val memberId: String,
    val memberName: String,
    val mobile: String,
    val requestedRd: Int,
    val requestedInterest: Int,
    val requestedPenalty: Int,
    val requestedLoanRepay: Int,
    val waiver: Int = 0,
    val totalAmount: Int,
    val mode: String = "ONLINE / UPI",
    val utrNumber: String = "",
    val remarks: String = "",
    val date: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String = ""
)

data class ReminderTemplate(
    val id: String,
    val name: String,
    val notificationTitle: String,
    val body: String
)

val DEFAULT_REMINDER_TEMPLATES = listOf(
    ReminderTemplate(
        id = "t1",
        name = "1. RD & Loan Due Date Reminder (15th)",
        notificationTitle = "📢 Gullak Society RD Due Alert",
        body = "Namaste [Member_Name] Ji, Gullak Co-operative Society ki monthly RD (₹[Amount]) aur loan kist ka reminder hai. Kripya 15 tarikh tak payment samay par jama karein. Dhanyawad!"
    ),
    ReminderTemplate(
        id = "t2",
        name = "2. Urgent Overdue & Penalty Alert",
        notificationTitle = "⚠️ Gullak Society: Payment Overdue Notice",
        body = "Urgent Alert [Member_Name] Ji: Gullak Society RD/Loan payment ki due date nikal chuki hai. Kripya bina vilamb (penalty) ke turant bhuqtan karein: https://gullaksociety.in"
    ),
    ReminderTemplate(
        id = "t3",
        name = "3. Annual Bonus & Dividend Announcement",
        notificationTitle = "🎉 Gullak Society: Annual Bonus Credited",
        body = "Shubh Suchna: Gullak Society ke sabhi active members ka varshik bonus & dividend calculate ho gaya hai. Apni live passbook check karein!"
    ),
    ReminderTemplate(
        id = "t4",
        name = "4. Emergency & Gullak Loan Facility Alert",
        notificationTitle = "💳 Gullak Society: Loan Facility Available",
        body = "Society Alert: Gullak Society emergency loan aur gullak loan suvidha uplabdh hai. Apni eligible loan limit aur low interest details app me dekhein."
    ),
    ReminderTemplate(
        id = "t5",
        name = "5. App Download & Passbook Verification",
        notificationTitle = "📲 Gullak Society: Official App Download",
        body = "Namaste! Gullak Co-operative Society official Android App download karein aur apni live passbook, payment receipts aur loan status dekhein: https://gullaksociety.in"
    )
)

data class AutoReminderConfig(
    val isEnabled: Boolean = true,
    val frequency: String = "Every 2 Days", // "Daily", "Every 2 Days", "Weekly"
    val preferredTime: String = "10:00 AM",
    val customTemplate: String = "Namaste [Member_Name] Ji, Gullak Society ki monthly RD (₹[Amount]) aur loan kist ka reminder hai. Kripya samay par jama karein. - Gullak Society"
)

data class SocietySettings(
    val societyName: String = "GULLAK CO OPRATIVE SOCIETY",
    val defaultRd: Int = 400,
    val loanRate: Double = 1.0,
    val societyUpiId: String = "gullaksociety@okaxis",
    val upiPayeeName: String = "Gullak Co-operative Society",
    val adminWhatsApp: String = "9718174244"
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

object DefaultData {
    val SAMPLE_APPROVALS = listOf(
        PaymentApproval(
            id = "REQ-101",
            memberId = "MEM010120263",
            memberName = "Amit S/O Sunil (Omwati Aunti Ji ) 102022",
            mobile = "8287127921",
            requestedRd = 400,
            requestedInterest = 180,
            requestedPenalty = 50,
            requestedLoanRepay = 1000,
            waiver = 0,
            totalAmount = 1630,
            mode = "ONLINE / UPI",
            utrNumber = "UPI/412398457612",
            date = "12-09-2026 10:15",
            status = "PENDING"
        ),
        PaymentApproval(
            id = "REQ-102",
            memberId = "MEM010120267",
            memberName = "Chanchal D/O Anil Padosi 022022",
            mobile = "9910216942",
            requestedRd = 400,
            requestedInterest = 160,
            requestedPenalty = 0,
            requestedLoanRepay = 500,
            waiver = 0,
            totalAmount = 1060,
            mode = "ONLINE / UPI",
            utrNumber = "PAYTM/9938471102",
            date = "12-09-2026 09:40",
            status = "PENDING"
        )
    )

    val INITIAL_SOCIETY_MEMBERS = listOf(
        Member(id = "MEM010120261", name = "Afsana Sister Pappu Ji 012025", mobile = "9773841314", address = "Mohan Garden", nominee = "Pappu Ji", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM010120262", name = "Ajay Kumar Garg Ref Suresh Lala Ji 012025", mobile = "9873898898", address = "Kakrola", nominee = "Suresh Lala Ji", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM010120263", name = "Amit S/O Sunil (Omwati Aunti Ji ) 102022", mobile = "8287127921", address = "Vikas Vihar Kakrola", nominee = "Omwati Aunti", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15200, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 18000, pendingDues = 400, loanLimit = 60000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 50),
        Member(id = "MEM010120264", name = "Arvind Kumar 022022X2", mobile = "9350743408", address = "Ghaziabad", nominee = "Rekha Kumari", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 7500, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM010120265", name = "ASHA DEVI REF SUSHIL SO SHILA JI 012025", mobile = "9311043442", address = "Vikas Vihar", nominee = "Sushil", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM010120266", name = "Ashish Aswal Ashu Vikas Vihar 022022", mobile = "9899801307", address = "C-141 Vikas Vihar Kakrola", nominee = "Sarita Aswal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14000, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 9000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM010120267", name = "Chanchal D/O Anil Padosi 022022", mobile = "9910216942", address = "Vikas Vihar Kakrola", nominee = "Anil Padosi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 16000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM010120268", name = "Chanda Devi Ref Shila Devi 022024", mobile = "8447218816", address = "Kakrola", nominee = "Shila Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 9200, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM010120269", name = "Deep Lal - Reena Devi 022023", mobile = "9871869719", address = "Vikas Vihar Kakrola", nominee = "Reena Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14000, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 4000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202610", name = "Deep Lal Electrician 022022", mobile = "9871869719", address = "Vikas Vihar Kakrola", nominee = "Deep Lal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 3000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202611", name = "DEVENDER SINGH REF RAVI 202501", mobile = "9456304719", address = "Kakrola", nominee = "Ravi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202612", name = "Geeta Devi Wo Narender 012025", mobile = "7042511156", address = "Vikas Vihar Kakrola", nominee = "Narender", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202613", name = "Hari Ram Ji Vikas Vihar 032022", mobile = "9650013268", address = "Kakrola", nominee = "Hari Ram", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202614", name = "Hirender Kumar - 2 - Neetu 012023", mobile = "9599356910", address = "Vikas Vihar Kakrola", nominee = "Neetu", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15360, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 5050, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202615", name = "Hirender Kumar -1- 022022", mobile = "9599356910", address = "Vikas Vihar Kakrola", nominee = "Hirender", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 17802, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 3030, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202616", name = "Jagdish Mehto X2  022022", mobile = "7042511481", address = "Jj Colony Bharat Vihar", nominee = "Jagdish", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202617", name = "Jagriti Sharma W/O Jugal Kishor 012023", mobile = "9953111505", address = "Vikas Vihar Kakrola", nominee = "Jugal Kishor", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 15000, pendingDues = 400, loanLimit = 60000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202618", name = "JAHANVI SHARMA DO JAGRITI JI 012025", mobile = "9953111505", address = "Vikas Vihar Kakrola", nominee = "Jagriti Sharma", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202619", name = "Jot Singh Ref Ravi 012025", mobile = "8178738999", address = "Kakrola", nominee = "Ravi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202620", name = "Jugal Kishor Ji X2 072022", mobile = "9310732656", address = "Vikas Vihar Kakrola", nominee = "Jagriti Sharma", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202621", name = "JYOTI JOSHI JI REF JAGRITI JI 012025", mobile = "9716124006", address = "Vikas Vihar Kakrola", nominee = "Jagriti Ji", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202622", name = "Kazim So Mumina Khatoon Ref Pappu 012025", mobile = "8287493771", address = "Kakrola", nominee = "Mumina Khatoon", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202623", name = "KEERTHI R S DO SOMYA MADAM 202501", mobile = "7827596703", address = "Kakrola", nominee = "Somya Madam", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202624", name = "KIRAN DEVI WO SUSHIL KUMAR 202501", mobile = "7042480937", address = "Vikas Vihar Kakrola", nominee = "Sushil Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202625", name = "Kuwar Pal -1 X2 082022", mobile = "9871130935", address = "Vikas Vihar Kakrola", nominee = "Kuwar Pal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202626", name = "Kuwar Pal-2 X2 082022", mobile = "9871130935", address = "Vikas Vihar Kakrola", nominee = "Kuwar Pal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202627", name = "Mukesh Sharma Ji X2 022022", mobile = "8285405743", address = "Vikas Vihar", nominee = "Mukesh", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 5623, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202628", name = "NANDINI JI 202501", mobile = "8383071508", address = "SULAHKUL VIHAR", nominee = "Nandini", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202629", name = "Narayan Yadav X2 032022", mobile = "9599959948", address = "Vikas Vihar", nominee = "Narayan", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202630", name = "Narender Babblu Bo Ravi 012025", mobile = "9354214597", address = "Kakrola", nominee = "Ravi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202631", name = "Narender Kumar S/O Shila Devi 012023", mobile = "7042511156", address = "S/O Shila Devi Vikas Vihar Kakrola", nominee = "Shila Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 2000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202632", name = "Neeraj Renew So Raghuveer Ji 012025", mobile = "9891811697", address = "Kakrola", nominee = "Raghuveer Ji", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202633", name = "Omwati Aunti M/O Anil Kumar 022022", mobile = "9971157481", address = "C-143 Vikas Vihar Kakrola", nominee = "Anil Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 17800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202634", name = "Pappu Carpainter - 1 - 022022", mobile = "9911563986", address = "Vikas Vihar Kakrola", nominee = "Pappu", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 13000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202635", name = "Pappu Carpainter - 2 - Nargis 102022", mobile = "9911563986", address = "Vikas Vihar Kakrola", nominee = "Nargis", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 21000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202636", name = "Pawan Kumar X2 072022", mobile = "8368934198", address = "S/O Rakesh Kumar Vikas Vihar", nominee = "Rakesh Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 19230, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202637", name = "Peter Masih 042022", mobile = "99990023275", address = "Mohan Garden", nominee = "Peter", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202638", name = "Raj Kumar (Colony) Kakrola 062022", mobile = "8750830986", address = "Vikas Vihar Kakrola", nominee = "Raj Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 22136, pendingDues = 400, loanLimit = 60000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202639", name = "Raja Ram Ji Ref Deepak 062022", mobile = "9810812331", address = "Narela", nominee = "Deepak", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202640", name = "Ram Bharose Ji Goyla Dairy 022022", mobile = "9717961768", address = "Goyla Dairy 9717961768 , 0838392003", nominee = "Ram Bharose", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16200, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202641", name = "Ravi Garwali 022022", mobile = "7042085508", address = "Vikas Vihar Kakrola", nominee = "Ravi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 17000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 50),
        Member(id = "MEM0101202642", name = "Sanjay Kumar -1- Ref DeeplaI 022022", mobile = "9650862110", address = "Bharat Vihar Kakrola", nominee = "Deep Lal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202643", name = "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023", mobile = "9650862110", address = "Bharat Vihar Kakrola", nominee = "Sandeep Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202645", name = "Sanjay Yadav -1 X2 022022", mobile = "7827004101", address = "Vikas Vihar Kakrola", nominee = "Sanjay Yadav", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 23000, pendingDues = 400, loanLimit = 60000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202646", name = "Sanjay Yadav -2- Shubhankar 072023", mobile = "7827004101", address = "Vikas Vihar Kakrola", nominee = "Shubhankar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 5000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202647", name = "Santosh Mehto X2 022022", mobile = "9968062512", address = "Bharat Vihar Kakrola", nominee = "Santosh", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 18800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 17000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202648", name = "Santosh Mistri Ref DeeplaI 012025", mobile = "9891703298", address = "Kakrola", nominee = "Deep Lal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202649", name = "Sarika 022022", mobile = "9718174244", address = "Kakrola", nominee = "Sarika", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15584, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 12000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202650", name = "Sarita Aswal Wo Ashish 012025", mobile = "9899801307", address = "Kakrola", nominee = "Ashish Aswal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 25000, pendingDues = 400, loanLimit = 60000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202651", name = "Shila Devi Ref Omwati Aunti X2 092022", mobile = "9643588165", address = "Vikas Vihar Kakrola", nominee = "Omwati Aunti", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 16000, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 11000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202652", name = "Somya Madam Ref Jagriti Sharma 012023", mobile = "7827596703", address = "Kakrola", nominee = "Jagriti Sharma", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 14400, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 16000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202653", name = "Sushil Ji So Sheela Devi 012025", mobile = "7042480937", address = "Vikas Vihar", nominee = "Sheela Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202654", name = "URUZ KHATMA DO MUMINA REF PAPPU 012025", mobile = "8287493771", address = "Kakrola", nominee = "Mumina Khatoon", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 4800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 0, loanLimit = 40000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202655", name = "Viney Electrician Ref Deep Lal 052023", mobile = "7065708037", address = "Vikas Vihar Kakrola", nominee = "Deep Lal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 12800, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 18000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202656", name = "Vishnu Aggarwal -1 102022", mobile = "9773557036", address = "Kakrola", nominee = "Vishnu", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 10000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202657", name = "Vishnu Aggarwal -2 102022", mobile = "9773557036", address = "Kakrola", nominee = "Vishnu", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 15600, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 10000, pendingDues = 400, loanLimit = 50000, loginPin = "1234", isAppInstalled = true, penaltyApplicable = 0),
        Member(id = "MEM0101202658", name = "Parvesh Ansari Ref DeeplaI 010126", mobile = "9315426875", address = "Kakrola", nominee = "Deep Lal", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-12", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202659", name = "Hazrat Ref Parvesh Ansari 010126", mobile = "9718172262", address = "Dda Flat Janak Puri", nominee = "Parvesh Ansari", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-12", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202660", name = "Mintu Devi Ref Chanda Devi 012026", mobile = "7033953938", address = "Vikas Vihar", nominee = "Chanda Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-15", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202661", name = "Mariam R/O Rupam & Shila Devi", mobile = "8826567542", address = "Bharat Vihar Kakrola", nominee = "Shila Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-19", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202662", name = "Rupam Ref Shila Devi 012026", mobile = "8130546714", address = "Vikas Vihar Kakrola", nominee = "Shila Devi", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-19", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202663", name = "Surender Rawat 012026", mobile = "9266782629", address = "Vikas Vihar Kakrola", nominee = "Sumitra Rawat", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-19", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202664", name = "Sumitra Rawat Wo Surender 012026", mobile = "9266782629", address = "Vikas Vihar Kakrola", nominee = "Surender Rawat", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-19", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202665", name = "Priya Sood Ref Raj Kumar 012026", mobile = "8750830986", address = "House Number B-115 Surya Vihar Binda", nominee = "Raj Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202666", name = "Raj Kumari Ref Raj Kumar 012026", mobile = "8750830986", address = "B-75 Bharat Vihar Kakrola 9810424981", nominee = "Raj Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202667", name = "Arvind Kumar Rekha Kumari 012026", mobile = "9350743408", address = "Gazhiabad", nominee = "Arvind Kumar", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-31", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0),
        Member(id = "MEM0101202668", name = "Rakhi Madam Ref Shila Ji 012026", mobile = "9311633238", address = "Delhi", nominee = "Shila Ji", monthlyRd = 400, status = "ACTIVE", joinDate = "2026-01-01", openingRd = 0, dueDay = "15th of every month", gullakLoan = 0, emergencyLoan = 0, pendingDues = 400, loanLimit = 40000, loginPin = "1234", isAppInstalled = false, penaltyApplicable = 0)
    )

    val INITIAL_PAYMENTS = listOf(
        Payment(
            txnId = "TXN-88401",
            date = "12-09-2026",
            memberId = "MEM010120263",
            memberName = "Amit S/O Sunil (Omwati Aunti Ji ) 102022",
            mobile = "8287127921",
            rdAmount = 400,
            interestAmount = 180,
            penaltyAmount = 0,
            loanRepayAmount = 1000,
            waiverAmount = 0,
            totalAmount = 1580,
            mode = "ONLINE / UPI",
            remarks = "Monthly RD + Interest + Loan repayment",
            utrNumber = "UPI/412398457612",
            isEdited = false
        ),
        Payment(
            txnId = "TXN-88402",
            date = "11-09-2026",
            memberId = "MEM010120266",
            memberName = "Ashish Aswal Ashu Vikas Vihar 022022",
            mobile = "9899801307",
            rdAmount = 400,
            interestAmount = 90,
            penaltyAmount = 0,
            loanRepayAmount = 500,
            waiverAmount = 0,
            totalAmount = 990,
            mode = "CASH",
            remarks = "Cash Deposit",
            utrNumber = "",
            isEdited = false
        )
    )
}
