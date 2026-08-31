package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // e.g. USR-00001 or ADMIN-00001
    val name: String,
    val mobile: String,
    val pin: String = "1234",
    val role: UserRole = UserRole.MEMBER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val remarks: String = ""
)

@Entity(tableName = "member_financials")
data class MemberFinancialEntity(
    @PrimaryKey val userId: String, // foreign key reference to UserEntity.userId
    val rdAmount: Double = 400.0, // Standard default ₹400
    val currentRdDue: Double = 400.0,
    val interestDue: Double = 0.0,
    val penaltyDue: Double = 0.0, // Manual/Recorded penalty
    val loanOutstanding: Double = 0.0,
    val loanInterestRate: Double = 1.0, // 1% per month
    val loanEligibility: Double = 50000.0, // Set only by Admin
    val lastMonthEndBalance: Double = 0.0,
    val accumulatedRdBonus: Double = 0.0, // 1% bonus tracked up to Dec 31
    val totalPaidThisYear: Double = 0.0,
    val lastPaymentDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Dynamic calculation: after 15th of the month, ₹10/day penalty on unpaid RD
    fun calculateLivePenalty(customDay: Int? = null): Double {
        if (currentRdDue <= 0) return penaltyDue
        val cal = Calendar.getInstance()
        val day = customDay ?: cal.get(Calendar.DAY_OF_MONTH)
        return if (day > 15) {
            val lateDays = day - 15
            penaltyDue + (lateDays * 10.0)
        } else {
            penaltyDue
        }
    }

    val totalDue: Double
        get() = currentRdDue + interestDue + calculateLivePenalty()
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String, // e.g. TXN-20260827-000001
    val userId: String,
    val userName: String,
    val userMobile: String,
    val amount: Double, // Total Amount Paid
    val rdAmount: Double = 400.0, // Column 1: RD Fixed ₹400
    val interestAmount: Double = 0.0, // Column 2: Interest Due
    val penaltyAmount: Double = 0.0, // Column 3: Penalty Due (₹10/day after 15th)
    val loanReturnAmount: Double = 0.0, // Column 4: Loan Return Principal
    val paymentType: PaymentType = PaymentType.ONLINE, // CASH or ONLINE
    val paymentMode: String = "ONLINE", // ONLINE, CASH, OFFICE_CASH, BANK_TRANSFER
    val paymentDate: Long = System.currentTimeMillis(),
    val month: String = "", // e.g. "August 2026"
    val status: PaymentStatus = PaymentStatus.PENDING,
    val screenshotUrl: String = "",
    val remarks: String = "", // Member remarks
    val adminRemarks: String = "", // Admin approval comment/adjustment remarks visible to member
    val approvedAmount: Double? = null,
    val approvedBy: String = "",
    val approvedAt: Long? = null,
    val rejectionReason: String = "",
    val isReversed: Boolean = false // If Admin reversed / re-edited this payment
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // specific userId or "ALL"
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.GENERAL,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "society_settings")
data class SocietySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val societyName: String = "GULLAK CO OPRATIVE SOCIETY",
    val defaultMonthlyRd: Double = 400.0,
    val defaultLoanInterestRate: Double = 1.0, // 1% monthly
    val dueDayOfMonth: Int = 15, // 15th of every month
    val penaltyPerDay: Double = 10.0, // ₹10 per day after 15th
    val upiId: String = "gullaksociety@okaxis",
    val upiPayeeName: String = "GULLAK CO-OPERATIVE SOCIETY",
    val adminMobile: String = "9876543210",
    val uploadedQrCodeImage: String = "", // Admin Uploaded QR Code image/data URI
    val autoReminderFrequency: String = "EVERY_2_DAYS", // DAILY, EVERY_2_DAYS, EVERY_3_DAYS, ON_10_AND_15
    val selectedSimSlot: Int = 0, // 0 for SIM 1, 1 for SIM 2
    val reminderFrequencyPerDay: Int = 2,
    val reminderTimes: String = "09:00 AM, 06:00 PM",
    val cloudSyncUrl: String = "", // Free Google Sheets Webhook or Cloud Sync Endpoint
    val lastCloudSyncTime: Long = 0L,
    val softLogoutOnly: Boolean = true, // Keep push notifications active in background
    val template1: String = "नमस्ते {NAME}, Gullak Society में आपकी कुल देय राशि ₹{AMOUNT} (RD: ₹{RD}, Interest: ₹{INT}, Penalty: ₹{PEN}) बकाया है। कृपया 15 तारीख से पहले भुगतान करें।",
    val template2: String = "Reminder: प्रिय {NAME}, आपकी Gullak Society की मासिक RD ₹{RD} और ब्याज ₹{INT} पेंडिंग है। आज ही जमा करें।",
    val template3: String = "सूचना: {NAME} जी, Gullak Society में आपका लोन ब्याज और RD बकाया है। कुल राशि: ₹{AMOUNT}। धन्यवाद।"
)


