package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.MemberFinancialEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MemberWithFinancials(
    val user: UserEntity,
    val financials: MemberFinancialEntity?
)

data class ImportSummary(
    val newRecords: Int,
    val updatedRecords: Int,
    val duplicateRecords: Int,
    val errors: Int,
    val details: List<String>
)

class GullakRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val financialDao = database.memberFinancialDao()
    private val paymentDao = database.paymentDao()
    private val notificationDao = database.notificationDao()
    private val auditDao = database.auditLogDao()
    private val settingsDao = database.societySettingsDao()

    val settings: Flow<SocietySettingsEntity?> = settingsDao.getSettings()

    // Users & Members
    val activeMembers: Flow<List<UserEntity>> = userDao.getAllActiveMembers()
    val allMembers: Flow<List<UserEntity>> = userDao.getAllMembersIncludingDeleted()
    val totalMemberCount: Flow<Int> = userDao.getMemberCount()
    val activeMemberCount: Flow<Int> = userDao.getActiveMemberCount()
    val inactiveMemberCount: Flow<Int> = userDao.getInactiveMemberCount()

    // Combined Flow for Member + Financials
    val membersWithFinancials: Flow<List<MemberWithFinancials>> = combine(
        userDao.getAllActiveMembers(),
        financialDao.getAllFinancials()
    ) { users, financialsList ->
        val map = financialsList.associateBy { it.userId }
        users.map { user ->
            MemberWithFinancials(user, map[user.userId])
        }
    }

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val pendingPayments: Flow<List<PaymentEntity>> = paymentDao.getPendingPayments()
    val pendingPaymentCount: Flow<Int> = paymentDao.getPendingPaymentCount()
    val totalLoanOutstanding: Flow<Double?> = financialDao.getTotalLoanOutstanding()
    val totalDueAmount: Flow<Double?> = financialDao.getTotalDueAmount()

    fun getTodayCollection(): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return paymentDao.getTodayCollection(calendar.timeInMillis)
    }

    fun getMonthlyCollection(): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return paymentDao.getMonthlyCollection(calendar.timeInMillis)
    }

    fun getPaymentsForUser(userId: String): Flow<List<PaymentEntity>> = paymentDao.getPaymentsByUserId(userId)
    fun getFinancialForUser(userId: String): Flow<MemberFinancialEntity?> = financialDao.getFinancialByUserId(userId)
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllAuditLogs()

    // Seed initial data if database is empty
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val admin = userDao.getAdminUser()
        if (admin == null) {
            // Create Admin
            val adminUser = UserEntity(
                userId = "ADMIN-00001",
                name = "Society Admin",
                mobile = "9876543210",
                pin = "1234",
                role = UserRole.ADMIN,
                status = AccountStatus.ACTIVE,
                remarks = "Master Administrator Account"
            )
            userDao.insertUser(adminUser)

            // Create Initial Society Settings
            settingsDao.insertOrUpdateSettings(
                SocietySettingsEntity(
                    id = 1,
                    societyName = "GULLAK CO OPRATIVE SOCIETY",
                    defaultMonthlyRd = 400.0,
                    defaultLoanInterestRate = 1.0,
                    upiId = "gullaksociety@okaxis",
                    upiPayeeName = "Gullak Co-operative Society",
                    adminMobile = "9876543210"
                )
            )

            // Seed sample 4 members with representative data
            val member1 = UserEntity(
                userId = "USR-00001",
                name = "Rahul Kumar",
                mobile = "9810011111",
                pin = "1234",
                role = UserRole.MEMBER,
                status = AccountStatus.ACTIVE,
                remarks = "Joined Jan 2025"
            )
            val member2 = UserEntity(
                userId = "USR-00002",
                name = "Suresh Sharma",
                mobile = "9810022222",
                pin = "1234",
                role = UserRole.MEMBER,
                status = AccountStatus.ACTIVE,
                remarks = "Active member"
            )
            val member3 = UserEntity(
                userId = "USR-00003",
                name = "Amit Verma",
                mobile = "9810033333",
                pin = "1234",
                role = UserRole.MEMBER,
                status = AccountStatus.ACTIVE,
                remarks = "Loan active"
            )
            val member4 = UserEntity(
                userId = "USR-00004",
                name = "Pooja Devi",
                mobile = "9810044444",
                pin = "1234",
                role = UserRole.MEMBER,
                status = AccountStatus.INACTIVE,
                remarks = "Temporary inactive"
            )

            userDao.insertUsers(listOf(member1, member2, member3, member4))

            // Financials for Members
            // Rahul: RD ₹400 Due, No loan
            val fin1 = MemberFinancialEntity(
                userId = "USR-00001",
                rdAmount = 400.0,
                currentRdDue = 400.0,
                interestDue = 0.0,
                loanOutstanding = 0.0,
                loanEligibility = 50000.0,
                accumulatedRdBonus = 48.0,
                totalPaidThisYear = 2800.0
            )
            // Suresh: RD ₹400 Due, Loan ₹40,000, 1% Interest = ₹400 Due
            val fin2 = MemberFinancialEntity(
                userId = "USR-00002",
                rdAmount = 400.0,
                currentRdDue = 400.0,
                interestDue = 400.0, // 1% of 40,000
                loanOutstanding = 40000.0,
                loanInterestRate = 1.0,
                loanEligibility = 60000.0,
                lastMonthEndBalance = 40000.0,
                accumulatedRdBonus = 36.0,
                totalPaidThisYear = 2400.0
            )
            // Amit: RD ₹400 Paid, Loan ₹25,000, Interest ₹250
            val fin3 = MemberFinancialEntity(
                userId = "USR-00003",
                rdAmount = 400.0,
                currentRdDue = 0.0,
                interestDue = 250.0,
                loanOutstanding = 25000.0,
                loanInterestRate = 1.0,
                loanEligibility = 45000.0,
                lastMonthEndBalance = 25000.0,
                accumulatedRdBonus = 32.0,
                totalPaidThisYear = 3200.0
            )
            // Pooja: Inactive
            val fin4 = MemberFinancialEntity(
                userId = "USR-00004",
                rdAmount = 400.0,
                currentRdDue = 400.0,
                interestDue = 0.0,
                loanOutstanding = 0.0,
                loanEligibility = 30000.0
            )

            financialDao.insertFinancials(listOf(fin1, fin2, fin3, fin4))

            // Sample Pending Payments for Admin to review
            val payment1 = PaymentEntity(
                transactionId = "TXN-20260827-000001",
                userId = "USR-00001",
                userName = "Rahul Kumar",
                userMobile = "9810011111",
                amount = 400.0,
                paymentType = PaymentType.CASH,
                paymentDate = System.currentTimeMillis() - 3600000,
                month = "August 2026",
                status = PaymentStatus.PENDING,
                remarks = "August RD Cash submit to Secretary"
            )

            val payment2 = PaymentEntity(
                transactionId = "TXN-20260827-000002",
                userId = "USR-00002",
                userName = "Suresh Sharma",
                userMobile = "9810022222",
                amount = 800.0, // 400 RD + 400 Interest
                paymentType = PaymentType.ONLINE,
                paymentDate = System.currentTimeMillis() - 7200000,
                month = "August 2026",
                status = PaymentStatus.PENDING,
                remarks = "UPI Ref: 423981029381 (RD + Interest)"
            )

            val payment3 = PaymentEntity(
                transactionId = "TXN-20260827-000003",
                userId = "USR-00003",
                userName = "Amit Verma",
                userMobile = "9810033333",
                amount = 400.0,
                paymentType = PaymentType.ONLINE,
                paymentDate = System.currentTimeMillis() - 86400000,
                month = "August 2026",
                status = PaymentStatus.APPROVED,
                approvedAmount = 400.0,
                approvedBy = "ADMIN-00001",
                approvedAt = System.currentTimeMillis() - 40000000,
                remarks = "Monthly RD"
            )

            paymentDao.insertPayments(listOf(payment1, payment2, payment3))

            // Initial notification
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = "ALL",
                    title = "Welcome to Gullak Co-operative Society",
                    message = "Aapka naya Gullak Society app ready hai. Kripya apna monthly RD aur interest timely jama karein.",
                    type = NotificationType.ANNOUNCEMENT
                )
            )

            auditDao.insertAuditLog(
                AuditLogEntity(
                    action = "SYSTEM_INITIALIZATION",
                    performedBy = "SYSTEM",
                    details = "Gullak Co-operative Society app database initialized with master rules."
                )
            )
        }
    }

    // Authentication
    suspend fun authenticate(mobile: String, pin: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByMobile(mobile.trim())
        if (user == null) {
            Result.failure(Exception("Mobile number register nahi hai."))
        } else if (user.status == AccountStatus.DELETED) {
            Result.failure(Exception("Yeh account delete ho chuka hai. Admin se sampark karein."))
        } else if (user.pin != pin.trim()) {
            Result.failure(Exception("Galat PIN dala hai. Kripya sahi PIN dalein."))
        } else {
            Result.success(user)
        }
    }

    // Generate Next User ID (e.g. USR-00005)
    suspend fun generateNextUserId(): String = withContext(Dispatchers.IO) {
        val maxId = (userDao.getMaxId() ?: 0) + 1
        String.format(Locale.US, "USR-%05d", maxId)
    }

    // Generate Next Transaction ID (e.g. TXN-20260827-000004)
    suspend fun generateTransactionId(): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val dateStr = dateFormat.format(Date())
        val randomNum = (100000..999999).random()
        "TXN-$dateStr-$randomNum"
    }

    // Add Member
    suspend fun addMember(
        name: String,
        mobile: String,
        pin: String = "1234",
        initialRd: Double = 400.0,
        loanOutstanding: Double = 0.0,
        loanEligibility: Double = 50000.0,
        status: AccountStatus = AccountStatus.ACTIVE,
        remarks: String = "",
        adminId: String = "ADMIN-00001"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByMobile(mobile.trim())
        if (existing != null && existing.status != AccountStatus.DELETED) {
            return@withContext Result.failure(Exception("Yeh mobile number ($mobile) pehle se registered hai."))
        }

        val newUserId = generateNextUserId()
        val user = UserEntity(
            userId = newUserId,
            name = name.trim(),
            mobile = mobile.trim(),
            pin = pin.trim(),
            role = UserRole.MEMBER,
            status = status,
            remarks = remarks.trim()
        )
        userDao.insertUser(user)

        // Calculate initial interest if loan outstanding > 0
        val interestDue = if (loanOutstanding > 0) loanOutstanding * 0.01 else 0.0

        val financial = MemberFinancialEntity(
            userId = newUserId,
            rdAmount = initialRd,
            currentRdDue = initialRd,
            interestDue = interestDue,
            loanOutstanding = loanOutstanding,
            loanInterestRate = 1.0,
            loanEligibility = loanEligibility,
            lastMonthEndBalance = loanOutstanding,
            accumulatedRdBonus = 0.0,
            totalPaidThisYear = 0.0
        )
        financialDao.insertFinancial(financial)

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "MEMBER_CREATED",
                performedBy = adminId,
                details = "Created new member $name ($newUserId, $mobile) with RD ₹$initialRd, Loan ₹$loanOutstanding"
            )
        )

        Result.success(user)
    }

    // Update Member
    suspend fun updateMember(
        userId: String,
        name: String,
        mobile: String,
        rdAmount: Double,
        loanOutstanding: Double,
        loanEligibility: Double,
        status: AccountStatus,
        remarks: String,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUserId(userId) ?: return@withContext Result.failure(Exception("Member nahi mila"))
        val updatedUser = user.copy(
            name = name.trim(),
            mobile = mobile.trim(),
            status = status,
            remarks = remarks.trim()
        )
        userDao.updateUser(updatedUser)

        val fin = financialDao.getFinancialByUserIdDirect(userId) ?: MemberFinancialEntity(userId = userId)
        val interestDue = if (loanOutstanding > 0) loanOutstanding * (fin.loanInterestRate / 100.0) else 0.0
        val updatedFin = fin.copy(
            rdAmount = rdAmount,
            loanOutstanding = loanOutstanding,
            loanEligibility = loanEligibility,
            interestDue = interestDue,
            updatedAt = System.currentTimeMillis()
        )
        financialDao.updateFinancial(updatedFin)

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "MEMBER_UPDATED",
                performedBy = adminId,
                details = "Updated member $name ($userId) - Status: $status, RD: ₹$rdAmount, Loan: ₹$loanOutstanding"
            )
        )

        Result.success(Unit)
    }

    // Toggle Inactive
    suspend fun setMemberStatus(userId: String, status: AccountStatus, adminId: String = "ADMIN-00001") = withContext(Dispatchers.IO) {
        userDao.updateMemberStatus(userId, status)
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "MEMBER_STATUS_CHANGED",
                performedBy = adminId,
                details = "Changed member $userId status to $status"
            )
        )
    }

    // Permanently Delete Member
    suspend fun deleteMemberPermanently(userId: String, adminId: String = "ADMIN-00001") = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUserId(userId)
        userDao.permanentlyDeleteUser(userId)
        financialDao.deleteFinancialByUserId(userId)
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "MEMBER_PERMANENTLY_DELETED",
                performedBy = adminId,
                details = "Permanently deleted member ${user?.name ?: userId} ($userId)"
            )
        )
    }

    // Update PIN
    suspend fun updateMemberPin(userId: String, newPin: String, adminId: String = "ADMIN-00001") = withContext(Dispatchers.IO) {
        userDao.updateMemberPin(userId, newPin.trim())
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PIN_RESET",
                performedBy = adminId,
                details = "Reset PIN for user $userId"
            )
        )
    }

    // Update Loan Eligibility (Only Admin)
    suspend fun updateLoanEligibility(userId: String, eligibility: Double, adminId: String = "ADMIN-00001") = withContext(Dispatchers.IO) {
        financialDao.updateLoanEligibility(userId, eligibility)
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "LOAN_ELIGIBILITY_UPDATED",
                performedBy = adminId,
                details = "Set loan eligibility for $userId to ₹$eligibility"
            )
        )
    }

    // Member Payment Request (Cash or Online)
    suspend fun submitPaymentRequest(
        userId: String,
        amount: Double,
        paymentType: PaymentType,
        remarks: String,
        screenshotUrl: String = ""
    ): Result<PaymentEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUserId(userId) ?: return@withContext Result.failure(Exception("User nahi mila"))
        val txnId = generateTransactionId()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val currentMonth = monthFormat.format(Date())

        val payment = PaymentEntity(
            transactionId = txnId,
            userId = userId,
            userName = user.name,
            userMobile = user.mobile,
            amount = amount,
            paymentType = paymentType,
            paymentDate = System.currentTimeMillis(),
            month = currentMonth,
            status = PaymentStatus.PENDING,
            remarks = remarks.trim(),
            screenshotUrl = screenshotUrl
        )

        val id = paymentDao.insertPayment(payment)

        // Add notification for Admin
        notificationDao.insertNotification(
            NotificationEntity(
                userId = "ADMIN-00001",
                title = "New ${paymentType.name} Payment Approval Required",
                message = "${user.name} ne ₹$amount ka ${paymentType.name} payment submit kiya hai. Approval pending hai.",
                type = NotificationType.GENERAL
            )
        )

        Result.success(payment.copy(id = id))
    }

    // Approve Payment
    suspend fun approvePayment(
        paymentId: Long,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payments = paymentDao.getAllPayments().first()
        val payment = payments.find { it.id == paymentId } ?: return@withContext Result.failure(Exception("Payment nahi mili"))

        val approvedAmount = payment.amount
        val timestamp = System.currentTimeMillis()

        paymentDao.approvePayment(
            id = paymentId,
            status = PaymentStatus.APPROVED,
            approvedAmount = approvedAmount,
            approvedBy = adminId,
            approvedAt = timestamp
        )

        // Adjust Member Financials
        adjustFinancialsOnPaymentApproval(payment.userId, approvedAmount, timestamp)

        // In-App Notification to Member
        notificationDao.insertNotification(
            NotificationEntity(
                userId = payment.userId,
                title = "Payment Approved ✅",
                message = "Aapka ₹$approvedAmount ka payment successfully approve ho gaya hai.",
                type = NotificationType.PAYMENT_APPROVAL
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PAYMENT_APPROVED",
                performedBy = adminId,
                details = "Approved payment ${payment.transactionId} of ₹$approvedAmount for user ${payment.userName} (${payment.userId})"
            )
        )

        Result.success(Unit)
    }

    // Approve Payment With Edit
    suspend fun approvePaymentWithEdit(
        paymentId: Long,
        editedAmount: Double,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payments = paymentDao.getAllPayments().first()
        val payment = payments.find { it.id == paymentId } ?: return@withContext Result.failure(Exception("Payment nahi mili"))

        val timestamp = System.currentTimeMillis()

        paymentDao.approvePayment(
            id = paymentId,
            status = PaymentStatus.APPROVED_WITH_EDIT,
            approvedAmount = editedAmount,
            approvedBy = adminId,
            approvedAt = timestamp
        )

        // Adjust Member Financials with Edited Amount
        adjustFinancialsOnPaymentApproval(payment.userId, editedAmount, timestamp)

        // In-App Notification to Member
        notificationDao.insertNotification(
            NotificationEntity(
                userId = payment.userId,
                title = "Payment Approved with Edit ✏️",
                message = "Aapka payment Admin dwara edit karke ₹$editedAmount par approve kiya gaya (Original: ₹${payment.amount}).",
                type = NotificationType.PAYMENT_APPROVAL
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PAYMENT_APPROVED_WITH_EDIT",
                performedBy = adminId,
                details = "Approved with edit ${payment.transactionId}: Original ₹${payment.amount} -> Edited ₹$editedAmount for ${payment.userName}"
            )
        )

        Result.success(Unit)
    }

    // Reject Payment
    suspend fun rejectPayment(
        paymentId: Long,
        reason: String,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payments = paymentDao.getAllPayments().first()
        val payment = payments.find { it.id == paymentId } ?: return@withContext Result.failure(Exception("Payment nahi mili"))

        val timestamp = System.currentTimeMillis()

        paymentDao.rejectPayment(
            id = paymentId,
            reason = reason,
            rejectedBy = adminId,
            timestamp = timestamp
        )

        // In-App Notification to Member
        notificationDao.insertNotification(
            NotificationEntity(
                userId = payment.userId,
                title = "Payment Rejected ❌",
                message = "Aapka ₹${payment.amount} ka payment reject kiya gaya hai. Reason: $reason",
                type = NotificationType.PAYMENT_REJECTION
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PAYMENT_REJECTED",
                performedBy = adminId,
                details = "Rejected payment ${payment.transactionId} of ₹${payment.amount} for ${payment.userName}. Reason: $reason"
            )
        )

        Result.success(Unit)
    }

    private suspend fun adjustFinancialsOnPaymentApproval(userId: String, amount: Double, timestamp: Long) {
        val fin = financialDao.getFinancialByUserIdDirect(userId) ?: return
        var remainingPayment = amount

        var currentRdDue = fin.currentRdDue
        var interestDue = fin.interestDue
        var loanOutstanding = fin.loanOutstanding
        var accumulatedBonus = fin.accumulatedRdBonus

        // 1. Pay RD Due first
        if (currentRdDue > 0) {
            val paidForRd = minOf(currentRdDue, remainingPayment)
            currentRdDue -= paidForRd
            remainingPayment -= paidForRd
            // 1% Bonus interest tracked on RD paid up to Dec 31
            accumulatedBonus += (paidForRd * 0.01)
        }

        // 2. Pay Interest Due next
        if (remainingPayment > 0 && interestDue > 0) {
            val paidForInterest = minOf(interestDue, remainingPayment)
            interestDue -= paidForInterest
            remainingPayment -= paidForInterest
        }

        // 3. Any extra amount reduces Loan Outstanding Principal
        if (remainingPayment > 0 && loanOutstanding > 0) {
            val paidForLoan = minOf(loanOutstanding, remainingPayment)
            loanOutstanding -= paidForLoan
            remainingPayment -= paidForLoan
        }

        val updatedFin = fin.copy(
            currentRdDue = currentRdDue,
            interestDue = interestDue,
            loanOutstanding = loanOutstanding,
            accumulatedRdBonus = accumulatedBonus,
            totalPaidThisYear = fin.totalPaidThisYear + amount,
            lastPaymentDate = timestamp,
            updatedAt = timestamp
        )

        financialDao.updateFinancial(updatedFin)
    }

    // Send Notification to All Active Members
    suspend fun sendNotificationToAll(
        title: String,
        message: String,
        adminId: String = "ADMIN-00001"
    ): Int = withContext(Dispatchers.IO) {
        val activeUsers = userDao.getAllActiveMembers().first()
        val notifications = activeUsers.map { user ->
            NotificationEntity(
                userId = user.userId,
                title = title,
                message = message,
                type = NotificationType.ANNOUNCEMENT
            )
        }
        notificationDao.insertNotifications(notifications)

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "NOTIFICATION_SENT_TO_ALL",
                performedBy = adminId,
                details = "Sent notification to ${activeUsers.size} active members: $title"
            )
        )
        activeUsers.size
    }

    // Send Notification to Single Member
    suspend fun sendNotificationToMember(
        userId: String,
        title: String,
        message: String,
        adminId: String = "ADMIN-00001"
    ) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = title,
                message = message,
                type = NotificationType.DUES_REMINDER
            )
        )
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "NOTIFICATION_SENT_SINGLE",
                performedBy = adminId,
                details = "Sent notification to $userId: $title"
            )
        )
    }

    // Send Payment Reminders according to Master Spec Logic:
    // Only to members who:
    // 1. Are ACTIVE (ignore INACTIVE & DELETED)
    // 2. Have RD Due > 0 OR Interest Due > 0 OR have a PENDING Payment
    // 3. Do NOT send if their payment is APPROVED and current dues are 0
    suspend fun triggerScheduledReminders(customTemplate: String? = null): Int = withContext(Dispatchers.IO) {
        val activeMembersList = userDao.getAllActiveMembers().first()
        val financialsMap = financialDao.getAllFinancials().first().associateBy { it.userId }
        val pendingPaymentsUserIds = paymentDao.getPendingPayments().first().map { it.userId }.toSet()
        val settings = settingsDao.getSettingsDirect() ?: SocietySettingsEntity()

        val messageText = customTemplate ?: settings.template1

        var remindersSent = 0
        for (user in activeMembersList) {
            val fin = financialsMap[user.userId]
            val totalDue = (fin?.currentRdDue ?: 0.0) + (fin?.interestDue ?: 0.0)
            val hasPending = pendingPaymentsUserIds.contains(user.userId)

            if (totalDue > 0 || hasPending) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        userId = user.userId,
                        title = "Society Payment Reminder 🔔",
                        message = messageText,
                        type = NotificationType.DUES_REMINDER
                    )
                )
                remindersSent++
            }
        }

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "REMINDERS_TRIGGERED",
                performedBy = "SYSTEM",
                details = "Sent $remindersSent payment reminders to eligible members."
            )
        )

        remindersSent
    }

    // Update Society Settings
    suspend fun updateSocietySettings(settings: SocietySettingsEntity, adminId: String = "ADMIN-00001") = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "SETTINGS_UPDATED",
                performedBy = adminId,
                details = "Updated society settings: RD ₹${settings.defaultMonthlyRd}, UPI: ${settings.upiId}"
            )
        )
    }

    // Year-End RD Bonus Adjustment (1% Per Month on RD paid up to Dec 31)
    suspend fun applyYearEndBonusAdjustment(adminId: String = "ADMIN-00001"): String = withContext(Dispatchers.IO) {
        val allFins = financialDao.getAllFinancials().first()
        var totalAdjusted = 0.0
        for (fin in allFins) {
            if (fin.accumulatedRdBonus > 0) {
                totalAdjusted += fin.accumulatedRdBonus
                val updatedFin = fin.copy(
                    accumulatedRdBonus = 0.0,
                    updatedAt = System.currentTimeMillis()
                )
                financialDao.updateFinancial(updatedFin)
            }
        }

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "YEAR_END_BONUS_ADJUSTMENT",
                performedBy = adminId,
                details = "Adjusted Year-End RD Bonus of ₹$totalAdjusted across all members."
            )
        )
        "Year-End bonus adjustment of ₹$totalAdjusted completed."
    }

    // Excel CSV Export Logic (Fixed Column Template)
    // Columns: Name, Mobile Number, Payment Info, Amount, Date, Remarks, Transaction ID, Payment Status
    suspend fun generateExcelCsvData(): String = withContext(Dispatchers.IO) {
        val payments = paymentDao.getAllPayments().first()
        val sb = StringBuilder()
        sb.append("Name,Mobile Number,Payment Info,Amount,Date,Remarks,Transaction ID,Payment Status\n")

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        for (p in payments) {
            val dateStr = dateFormat.format(Date(p.paymentDate))
            val amountStr = p.approvedAmount?.toString() ?: p.amount.toString()
            val cleanName = p.userName.replace(",", " ")
            val cleanRemarks = p.remarks.replace(",", " ")
            sb.append("$cleanName,${p.userMobile},${p.paymentType},$amountStr,$dateStr,$cleanRemarks,${p.transactionId},${p.status}\n")
        }
        sb.toString()
    }

    // Excel CSV Import Logic with Duplicate Detection & Conflict Summary
    // Supports Multiple Imports without duplicate corruption
    suspend fun previewAndImportCsv(csvContent: String, isDryRun: Boolean): ImportSummary = withContext(Dispatchers.IO) {
        val lines = csvContent.trim().lines()
        var newCount = 0
        var updatedCount = 0
        var duplicateCount = 0
        var errorCount = 0
        val details = mutableListOf<String>()

        val paymentsToInsert = mutableListOf<PaymentEntity>()
        val membersToCreate = mutableListOf<UserEntity>()
        val financialsToCreate = mutableListOf<MemberFinancialEntity>()

        val existingMembers = userDao.getAllMembersIncludingDeleted().first().associateBy { it.mobile }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        val altDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

        for ((index, line) in lines.withIndex()) {
            if (index == 0 && (line.contains("Name", ignoreCase = true) || line.contains("Mobile", ignoreCase = true))) {
                continue // Skip header row
            }
            val tokens = line.split(",").map { it.trim() }
            if (tokens.size < 4) {
                if (line.isNotBlank()) {
                    errorCount++
                    details.add("Line ${index + 1}: Invalid column count (${tokens.size})")
                }
                continue
            }

            try {
                val name = tokens[0]
                val mobile = tokens[1]
                val paymentInfo = tokens.getOrNull(2) ?: "CASH"
                val amountStr = tokens.getOrNull(3) ?: "0"
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val dateStr = tokens.getOrNull(4) ?: ""
                val remarks = tokens.getOrNull(5) ?: ""
                val txnIdFromCsv = tokens.getOrNull(6) ?: ""
                val statusStr = tokens.getOrNull(7) ?: "APPROVED"

                var paymentDate = System.currentTimeMillis()
                if (dateStr.isNotBlank()) {
                    paymentDate = try {
                        dateFormat.parse(dateStr)?.time ?: altDateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                }

                // Check Duplicate Transaction
                val existingTxn = if (txnIdFromCsv.isNotBlank()) paymentDao.getPaymentByTxnId(txnIdFromCsv) else null
                val duplicatePayment = paymentDao.findDuplicatePayment(mobile, amount, paymentDate)

                if (existingTxn != null || duplicatePayment != null) {
                    duplicateCount++
                    details.add("Duplicate record for $name ($mobile, ₹$amount) - skipped")
                    continue
                }

                // Check Member Existing or New
                var member = existingMembers[mobile]
                val isNewMember = (member == null)
                val userId = if (isNewMember) {
                    val genId = generateNextUserId()
                    val newUser = UserEntity(
                        userId = genId,
                        name = name,
                        mobile = mobile,
                        role = UserRole.MEMBER,
                        status = AccountStatus.ACTIVE,
                        remarks = "Imported from Excel Ledger"
                    )
                    membersToCreate.add(newUser)
                    financialsToCreate.add(
                        MemberFinancialEntity(
                            userId = genId,
                            rdAmount = 400.0,
                            currentRdDue = 0.0,
                            loanOutstanding = 0.0
                        )
                    )
                    newCount++
                    details.add("New member identified: $name ($genId, $mobile)")
                    genId
                } else {
                    updatedCount++
                    details.add("Existing member matched: $name (${member?.userId})")
                    member!!.userId
                }

                val finalTxnId = if (txnIdFromCsv.isNotBlank()) txnIdFromCsv else generateTransactionId()
                val paymentType = if (paymentInfo.contains("ONLINE", ignoreCase = true) || paymentInfo.contains("UPI", ignoreCase = true)) {
                    PaymentType.ONLINE
                } else {
                    PaymentType.CASH
                }

                val status = try {
                    PaymentStatus.valueOf(statusStr.uppercase())
                } catch (e: Exception) {
                    PaymentStatus.APPROVED
                }

                val newPayment = PaymentEntity(
                    transactionId = finalTxnId,
                    userId = userId,
                    userName = name,
                    userMobile = mobile,
                    amount = amount,
                    paymentType = paymentType,
                    paymentDate = paymentDate,
                    month = "Imported Ledger",
                    status = status,
                    remarks = remarks,
                    approvedAmount = if (status == PaymentStatus.APPROVED) amount else null,
                    approvedBy = "EXCEL_IMPORT",
                    approvedAt = if (status == PaymentStatus.APPROVED) paymentDate else null
                )
                paymentsToInsert.add(newPayment)

            } catch (e: Exception) {
                errorCount++
                details.add("Line ${index + 1} Error: ${e.message}")
            }
        }

        // If not dry run, perform batch inserts!
        if (!isDryRun) {
            if (membersToCreate.isNotEmpty()) {
                userDao.insertUsers(membersToCreate)
            }
            if (financialsToCreate.isNotEmpty()) {
                financialDao.insertFinancials(financialsToCreate)
            }
            if (paymentsToInsert.isNotEmpty()) {
                paymentDao.insertPayments(paymentsToInsert)
            }
            auditDao.insertAuditLog(
                AuditLogEntity(
                    action = "EXCEL_IMPORT_EXECUTED",
                    performedBy = "ADMIN",
                    details = "Imported ${paymentsToInsert.size} records. New: $newCount, Updated: $updatedCount, Duplicates: $duplicateCount, Errors: $errorCount"
                )
            )
        }

        ImportSummary(
            newRecords = newCount,
            updatedRecords = updatedCount,
            duplicateRecords = duplicateCount,
            errors = errorCount,
            details = details
        )
    }
}
