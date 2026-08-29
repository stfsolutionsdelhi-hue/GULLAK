package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.MemberFinancialEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE role = 'MEMBER' AND status != 'DELETED' ORDER BY id ASC")
    fun getAllActiveMembers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'MEMBER' ORDER BY id ASC")
    fun getAllMembersIncludingDeleted(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE mobile = :mobile AND status != 'DELETED' LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByUserId(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    suspend fun getAdminUser(): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND status != 'DELETED'")
    fun getMemberCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND status = 'ACTIVE'")
    fun getActiveMemberCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'MEMBER' AND status = 'INACTIVE'")
    fun getInactiveMemberCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateMemberStatus(userId: String, status: AccountStatus)

    @Query("UPDATE users SET pin = :newPin WHERE userId = :userId")
    suspend fun updateMemberPin(userId: String, newPin: String)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun permanentlyDeleteUser(userId: String)

    @Query("SELECT MAX(id) FROM users")
    suspend fun getMaxId(): Long?
}

@Dao
interface MemberFinancialDao {
    @Query("SELECT * FROM member_financials WHERE userId = :userId LIMIT 1")
    fun getFinancialByUserId(userId: String): Flow<MemberFinancialEntity?>

    @Query("SELECT * FROM member_financials WHERE userId = :userId LIMIT 1")
    suspend fun getFinancialByUserIdDirect(userId: String): MemberFinancialEntity?

    @Query("SELECT * FROM member_financials")
    fun getAllFinancials(): Flow<List<MemberFinancialEntity>>

    @Query("SELECT SUM(loanOutstanding) FROM member_financials")
    fun getTotalLoanOutstanding(): Flow<Double?>

    @Query("SELECT SUM(currentRdDue + interestDue) FROM member_financials")
    fun getTotalDueAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancial(financial: MemberFinancialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancials(financials: List<MemberFinancialEntity>)

    @Update
    suspend fun updateFinancial(financial: MemberFinancialEntity)

    @Query("UPDATE member_financials SET loanEligibility = :eligibility, updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateLoanEligibility(userId: String, eligibility: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM member_financials WHERE userId = :userId")
    suspend fun deleteFinancialByUserId(userId: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = 'PENDING' ORDER BY paymentDate DESC")
    fun getPendingPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT COUNT(*) FROM payments WHERE status = 'PENDING'")
    fun getPendingPaymentCount(): Flow<Int>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY paymentDate DESC")
    fun getPaymentsByUserId(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE transactionId = :txnId LIMIT 1")
    suspend fun getPaymentByTxnId(txnId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE userMobile = :mobile AND amount = :amount AND strftime('%Y-%m-%d', paymentDate / 1000, 'unixepoch') = strftime('%Y-%m-%d', :date / 1000, 'unixepoch') LIMIT 1")
    suspend fun findDuplicatePayment(mobile: String, amount: Double, date: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE paymentDate >= :startDate AND paymentDate <= :endDate ORDER BY paymentDate DESC")
    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(COALESCE(approvedAmount, amount)) FROM payments WHERE status IN ('APPROVED', 'APPROVED_WITH_EDIT') AND paymentDate >= :startDate AND paymentDate <= :endDate")
    fun getCollectionByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Query("UPDATE payments SET status = :status, approvedAmount = :approvedAmount, rdAmount = :rdAmount, interestAmount = :interestAmount, penaltyAmount = :penaltyAmount, loanReturnAmount = :loanReturnAmount, adminRemarks = :adminRemarks, approvedBy = :approvedBy, approvedAt = :approvedAt, isReversed = :isReversed WHERE id = :id")
    suspend fun updatePaymentBreakdownAndStatus(
        id: Long,
        status: PaymentStatus,
        approvedAmount: Double,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        adminRemarks: String,
        approvedBy: String,
        approvedAt: Long,
        isReversed: Boolean
    )

    @Query("SELECT SUM(COALESCE(approvedAmount, amount)) FROM payments WHERE status IN ('APPROVED', 'APPROVED_WITH_EDIT') AND paymentDate >= :startOfDayTimestamp")
    fun getTodayCollection(startOfDayTimestamp: Long): Flow<Double?>

    @Query("SELECT SUM(COALESCE(approvedAmount, amount)) FROM payments WHERE status IN ('APPROVED', 'APPROVED_WITH_EDIT') AND paymentDate >= :startOfMonthTimestamp")
    fun getMonthlyCollection(startOfMonthTimestamp: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET status = :status, approvedAmount = :approvedAmount, approvedBy = :approvedBy, approvedAt = :approvedAt WHERE id = :id")
    suspend fun approvePayment(id: Long, status: PaymentStatus, approvedAmount: Double, approvedBy: String, approvedAt: Long)

    @Query("UPDATE payments SET status = 'REJECTED', rejectionReason = :reason, approvedBy = :rejectedBy, approvedAt = :timestamp WHERE id = :id")
    suspend fun rejectPayment(id: Long, reason: String, rejectedBy: String, timestamp: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR userId = 'ALL') AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
    suspend fun markAllAsReadForUser(userId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}

@Dao
interface SocietySettingsDao {
    @Query("SELECT * FROM society_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SocietySettingsEntity?>

    @Query("SELECT * FROM society_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SocietySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SocietySettingsEntity)
}
