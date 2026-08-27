package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val loanOutstanding: Double = 0.0,
    val loanInterestRate: Double = 1.0, // 1% per month
    val loanEligibility: Double = 50000.0, // Set only by Admin
    val lastMonthEndBalance: Double = 0.0,
    val accumulatedRdBonus: Double = 0.0, // 1% bonus tracked up to Dec 31
    val totalPaidThisYear: Double = 0.0,
    val lastPaymentDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDue: Double
        get() = currentRdDue + interestDue
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String, // e.g. TXN-20260827-000001
    val userId: String,
    val userName: String,
    val userMobile: String,
    val amount: Double,
    val paymentType: PaymentType, // CASH or ONLINE
    val paymentDate: Long = System.currentTimeMillis(),
    val month: String = "", // e.g. "August 2026"
    val status: PaymentStatus = PaymentStatus.PENDING,
    val screenshotUrl: String = "",
    val remarks: String = "",
    val approvedAmount: Double? = null,
    val approvedBy: String = "",
    val approvedAt: Long? = null,
    val rejectionReason: String = ""
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
    val upiId: String = "gullaksociety@okaxis",
    val upiPayeeName: String = "Gullak Co-operative Society",
    val adminMobile: String = "9876543210",
    val reminderFrequencyPerDay: Int = 2,
    val reminderTimes: String = "09:00 AM, 06:00 PM",
    val template1: String = "Aapka society ka amount dues hai. Kripya payment kar dein.",
    val template2: String = "Aapka monthly RD/payment pending hai. Kripya samay par payment karein.",
    val template3: String = "Reminder: Aapka society payment abhi pending hai."
)
