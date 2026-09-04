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
                rdAmount = 400.0,
                interestAmount = 400.0,
                penaltyAmount = 0.0,
                loanReturnAmount = 0.0,
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

    // Filtered Payments & Collection by Date Range
    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByDateRange(startDate, endDate)

    fun getCollectionByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        paymentDao.getCollectionByDateRange(startDate, endDate)

    // Member Payment Request (with 4-column breakdown)
    suspend fun submitPaymentRequest(
        userId: String,
        amount: Double,
        rdAmount: Double = 400.0,
        interestAmount: Double = 0.0,
        penaltyAmount: Double = 0.0,
        loanReturnAmount: Double = 0.0,
        paymentType: PaymentType = PaymentType.ONLINE,
        paymentMode: String = "ONLINE",
        remarks: String = "",
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
            rdAmount = rdAmount,
            interestAmount = interestAmount,
            penaltyAmount = penaltyAmount,
            loanReturnAmount = loanReturnAmount,
            paymentType = paymentType,
            paymentMode = paymentMode,
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
                title = "New Payment Approval Required 💳",
                message = "${user.name} ne ₹$amount (RD: ₹$rdAmount, Int: ₹$interestAmount, Pen: ₹$penaltyAmount, Loan: ₹$loanReturnAmount) submit kiya hai. Approval pending.",
                type = NotificationType.GENERAL
            )
        )

        Result.success(payment.copy(id = id))
    }

    // Admin: Record Manual / Office Cash Payment Directly
    suspend fun recordOfficeCashPayment(
        userId: String,
        amount: Double,
        rdAmount: Double = 400.0,
        interestAmount: Double = 0.0,
        penaltyAmount: Double = 0.0,
        loanReturnAmount: Double = 0.0,
        paymentMode: String = "OFFICE_CASH",
        remarks: String = "Office Cash Received",
        adminRemarks: String = "Verified by Secretary",
        adminId: String = "ADMIN-00001"
    ): Result<PaymentEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUserId(userId) ?: return@withContext Result.failure(Exception("Member not found"))
        val txnId = generateTransactionId()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val currentMonth = monthFormat.format(Date())
        val timestamp = System.currentTimeMillis()

        val payment = PaymentEntity(
            transactionId = txnId,
            userId = userId,
            userName = user.name,
            userMobile = user.mobile,
            amount = amount,
            rdAmount = rdAmount,
            interestAmount = interestAmount,
            penaltyAmount = penaltyAmount,
            loanReturnAmount = loanReturnAmount,
            paymentType = PaymentType.CASH,
            paymentMode = paymentMode,
            paymentDate = timestamp,
            month = currentMonth,
            status = PaymentStatus.APPROVED,
            remarks = remarks.trim(),
            adminRemarks = adminRemarks.trim(),
            approvedAmount = amount,
            approvedBy = adminId,
            approvedAt = timestamp
        )

        val id = paymentDao.insertPayment(payment)

        // Adjust Financials based on 4 columns
        adjustFinancialsOnDetailedApproval(userId, rdAmount, interestAmount, penaltyAmount, loanReturnAmount, timestamp)

        // In-App Notification to Member
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Office Cash Payment Recorded ✅",
                message = "Aapka ₹$amount ka offline cash payment Office me deposit ho gaya hai (RD: ₹$rdAmount, Int: ₹$interestAmount, Pen: ₹$penaltyAmount, Loan Return: ₹$loanReturnAmount).",
                type = NotificationType.PAYMENT_APPROVAL
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "OFFICE_CASH_PAYMENT_RECORDED",
                performedBy = adminId,
                details = "Recorded Office Cash payment $txnId of ₹$amount for ${user.name} ($userId). Mode: $paymentMode"
            )
        )

        Result.success(payment.copy(id = id))
    }

    // Admin: Approve Payment With Breakdown & Remarks
    suspend fun approvePaymentWithBreakdown(
        paymentId: Long,
        approvedAmount: Double,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        adminRemarks: String,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext Result.failure(Exception("Payment nahi mili"))
        val timestamp = System.currentTimeMillis()

        val isEdited = (approvedAmount != payment.amount || rdAmount != payment.rdAmount || interestAmount != payment.interestAmount || penaltyAmount != payment.penaltyAmount || loanReturnAmount != payment.loanReturnAmount)
        val finalStatus = if (isEdited) PaymentStatus.APPROVED_WITH_EDIT else PaymentStatus.APPROVED

        paymentDao.updatePaymentBreakdownAndStatus(
            id = paymentId,
            status = finalStatus,
            approvedAmount = approvedAmount,
            rdAmount = rdAmount,
            interestAmount = interestAmount,
            penaltyAmount = penaltyAmount,
            loanReturnAmount = loanReturnAmount,
            adminRemarks = adminRemarks.trim(),
            approvedBy = adminId,
            approvedAt = timestamp,
            isReversed = false
        )

        // Adjust Financials accurately
        adjustFinancialsOnDetailedApproval(payment.userId, rdAmount, interestAmount, penaltyAmount, loanReturnAmount, timestamp)

        // Notification to Member
        val remarkText = if (adminRemarks.isNotBlank()) " | Remarks: $adminRemarks" else ""
        val statusText = if (isEdited) "Approved with Adjustment (₹$approvedAmount)" else "Approved (₹$approvedAmount)"
        notificationDao.insertNotification(
            NotificationEntity(
                userId = payment.userId,
                title = "Payment $statusText ✅",
                message = "Aapka payment approve ho gaya hai. RD: ₹$rdAmount, Interest: ₹$interestAmount, Penalty: ₹$penaltyAmount, Loan Return: ₹$loanReturnAmount$remarkText",
                type = NotificationType.PAYMENT_APPROVAL
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PAYMENT_APPROVED_DETAILED",
                performedBy = adminId,
                details = "Approved $paymentId ($finalStatus) for ${payment.userName}: Total ₹$approvedAmount (RD: ₹$rdAmount, Int: ₹$interestAmount, Pen: ₹$penaltyAmount, Loan: ₹$loanReturnAmount). Note: $adminRemarks"
            )
        )

        Result.success(Unit)
    }

    // Admin: Re-edit / Correct / Reverse Approved Payment
    suspend fun reverseOrEditPayment(
        paymentId: Long,
        newApprovedAmount: Double,
        newRdAmount: Double,
        newInterestAmount: Double,
        newPenaltyAmount: Double,
        newLoanReturnAmount: Double,
        newAdminRemarks: String,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext Result.failure(Exception("Payment record not found"))
        val timestamp = System.currentTimeMillis()

        // 1. Rollback old financial adjustments
        rollbackPreviousFinancialAdjustment(payment)

        // 2. Apply new updated breakdown
        paymentDao.updatePaymentBreakdownAndStatus(
            id = paymentId,
            status = PaymentStatus.APPROVED_WITH_EDIT,
            approvedAmount = newApprovedAmount,
            rdAmount = newRdAmount,
            interestAmount = newInterestAmount,
            penaltyAmount = newPenaltyAmount,
            loanReturnAmount = newLoanReturnAmount,
            adminRemarks = newAdminRemarks.trim(),
            approvedBy = adminId,
            approvedAt = timestamp,
            isReversed = false
        )

        adjustFinancialsOnDetailedApproval(payment.userId, newRdAmount, newInterestAmount, newPenaltyAmount, newLoanReturnAmount, timestamp)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = payment.userId,
                title = "Payment Rectified / Adjusted ✏️",
                message = "Admin ne aapke payment #${payment.transactionId} ko correct kiya hai. Naya Approved Amount: ₹$newApprovedAmount. Remarks: $newAdminRemarks",
                type = NotificationType.PAYMENT_APPROVAL
            )
        )

        auditDao.insertAuditLog(
            AuditLogEntity(
                action = "PAYMENT_RE_EDITED_BY_ADMIN",
                performedBy = adminId,
                details = "Admin re-edited payment ${payment.transactionId} for ${payment.userName} to ₹$newApprovedAmount (RD: ₹$newRdAmount, Int: ₹$newInterestAmount, Pen: ₹$newPenaltyAmount, Loan: ₹$newLoanReturnAmount)"
            )
        )

        Result.success(Unit)
    }

    private suspend fun adjustFinancialsOnDetailedApproval(
        userId: String,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        timestamp: Long
    ) {
        val fin = financialDao.getFinancialByUserIdDirect(userId) ?: return
        var currentRdDue = maxOf(0.0, fin.currentRdDue - rdAmount)
        var interestDue = maxOf(0.0, fin.interestDue - interestAmount)
        var penaltyDue = maxOf(0.0, fin.penaltyDue - penaltyAmount)
        var loanOutstanding = maxOf(0.0, fin.loanOutstanding - loanReturnAmount)
        var accumulatedBonus = fin.accumulatedRdBonus + (rdAmount * 0.01)
        val totalPaid = fin.totalPaidThisYear + (rdAmount + interestAmount + penaltyAmount + loanReturnAmount)

        val updatedFin = fin.copy(
            currentRdDue = currentRdDue,
            interestDue = interestDue,
            penaltyDue = penaltyDue,
            loanOutstanding = loanOutstanding,
            accumulatedRdBonus = accumulatedBonus,
            totalPaidThisYear = totalPaid,
            lastPaymentDate = timestamp,
            updatedAt = timestamp
        )
        financialDao.updateFinancial(updatedFin)
    }

    private suspend fun rollbackPreviousFinancialAdjustment(payment: PaymentEntity) {
        val fin = financialDao.getFinancialByUserIdDirect(payment.userId) ?: return
        val oldRd = payment.rdAmount
        val oldInt = payment.interestAmount
        val oldPen = payment.penaltyAmount
        val oldLoan = payment.loanReturnAmount
        val oldTotal = payment.approvedAmount ?: payment.amount

        val restoredRdDue = fin.currentRdDue + oldRd
        val restoredIntDue = fin.interestDue + oldInt
        val restoredPenDue = fin.penaltyDue + oldPen
        val restoredLoan = fin.loanOutstanding + oldLoan
        val restoredBonus = maxOf(0.0, fin.accumulatedRdBonus - (oldRd * 0.01))
        val restoredTotalPaid = maxOf(0.0, fin.totalPaidThisYear - oldTotal)

        val restoredFin = fin.copy(
            currentRdDue = restoredRdDue,
            interestDue = restoredIntDue,
            penaltyDue = restoredPenDue,
            loanOutstanding = restoredLoan,
            accumulatedRdBonus = restoredBonus,
            totalPaidThisYear = restoredTotalPaid,
            updatedAt = System.currentTimeMillis()
        )
        financialDao.updateFinancial(restoredFin)
    }

    // Approve Payment (Legacy overload keeping compatibility)
    suspend fun approvePayment(
        paymentId: Long,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext Result.failure(Exception("Payment nahi mili"))
        approvePaymentWithBreakdown(
            paymentId = paymentId,
            approvedAmount = payment.amount,
            rdAmount = payment.rdAmount,
            interestAmount = payment.interestAmount,
            penaltyAmount = payment.penaltyAmount,
            loanReturnAmount = payment.loanReturnAmount,
            adminRemarks = payment.remarks,
            adminId = adminId
        )
    }

    // Approve Payment With Edit (Legacy overload keeping compatibility)
    suspend fun approvePaymentWithEdit(
        paymentId: Long,
        editedAmount: Double,
        adminId: String = "ADMIN-00001"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val payment = paymentDao.getPaymentById(paymentId) ?: return@withContext Result.failure(Exception("Payment nahi mili"))
        approvePaymentWithBreakdown(
            paymentId = paymentId,
            approvedAmount = editedAmount,
            rdAmount = minOf(400.0, editedAmount),
            interestAmount = payment.interestAmount,
            penaltyAmount = payment.penaltyAmount,
            loanReturnAmount = maxOf(0.0, editedAmount - 400.0 - payment.interestAmount - payment.penaltyAmount),
            adminRemarks = "Adjusted by Admin",
            adminId = adminId
        )
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

        val rawTemplate = customTemplate ?: settings.template1

        var remindersSent = 0
        for (user in activeMembersList) {
            val fin = financialsMap[user.userId]
            val rdDue = fin?.currentRdDue ?: 400.0
            val intDue = fin?.interestDue ?: 0.0
            val penDue = fin?.calculateLivePenalty() ?: 0.0
            val totalDue = rdDue + intDue + penDue
            val hasPending = pendingPaymentsUserIds.contains(user.userId)

            if (totalDue > 0 || hasPending) {
                val formattedMessage = rawTemplate
                    .replace("{NAME}", user.name)
                    .replace("{AMOUNT}", totalDue.toInt().toString())
                    .replace("{RD}", rdDue.toInt().toString())
                    .replace("{INT}", intDue.toInt().toString())
                    .replace("{PEN}", penDue.toInt().toString())

                notificationDao.insertNotification(
                    NotificationEntity(
                        userId = user.userId,
                        title = "Society Payment Reminder 🔔",
                        message = formattedMessage,
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
                details = "Updated society settings: RD ₹${settings.defaultMonthlyRd}, UPI: ${settings.upiId}, SimSlot: ${settings.selectedSimSlot}, Frequency: ${settings.autoReminderFrequency}"
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

    // Sample Excel CSV Template with only essential columns
    fun generateSampleExcelTemplateCsv(): String {
        val sb = StringBuilder()
        sb.append("Name,Mobile,Monthly_RD,Opening_Loan_Outstanding,Current_Due_Amount,Remarks\n")
        sb.append("Sanjay Gupta,9812345670,400,20000,400,Sample Member 1\n")
        sb.append("Ramesh Kumar,9812345671,400,0,400,Sample Member 2\n")
        sb.append("Sunita Verma,9812345672,400,50000,900,Sample Member 3\n")
        sb.append("Deepak Sharma,9812345673,400,10000,500,Sample Member 4\n")
        return sb.toString()
    }

    // Excel CSV Export Logic (Fixed Column Template)
    suspend fun generateExcelCsvData(): String = withContext(Dispatchers.IO) {
        val payments = paymentDao.getAllPayments().first()
        val sb = StringBuilder()
        sb.append("Name,Mobile Number,Payment Info,Amount,Date,Remarks,Transaction ID,Payment Status\n")

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        for (p in payments) {
            val dateStr = dateFormat.format(Date(p.paymentDate))
            val amountStr = p.approvedAmount?.toString() ?: p.amount.toString()
            val cleanName = p.userName.replace(",", " ")
            val cleanRemarks = (p.adminRemarks.ifBlank { p.remarks }).replace(",", " ")
            sb.append("$cleanName,${p.userMobile},${p.paymentType},$amountStr,$dateStr,$cleanRemarks,${p.transactionId},${p.status}\n")
        }
        sb.toString()
    }

    // Excel CSV Import Logic with Dual-Format Support & Duplicate Prevention
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
        val financialsToUpdate = mutableListOf<MemberFinancialEntity>()

        val existingMembers = userDao.getAllMembersIncludingDeleted().first().associateBy { it.mobile }
        val existingFins = financialDao.getAllFinancials().first().associateBy { it.userId }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        val altDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isBlank()) continue
            if (index == 0 && (line.contains("Name", ignoreCase = true) || line.contains("Mobile", ignoreCase = true))) {
                continue // Skip header row
            }
            val tokens = line.split(",").map { it.trim() }

            try {
                // Check if this is the simplified 6-column Member Setup format
                // Name, Mobile, Monthly_RD, Opening_Loan_Outstanding, Current_Due_Amount, Remarks
                val isSampleFormat = tokens.size >= 5 && (tokens[2].toDoubleOrNull() != null) && (tokens[3].toDoubleOrNull() != null)

                if (isSampleFormat && !line.contains("TXN", ignoreCase = true) && !line.contains("APPROVED", ignoreCase = true)) {
                    val name = tokens[0]
                    val mobile = tokens[1]
                    val monthlyRd = tokens.getOrNull(2)?.toDoubleOrNull() ?: 400.0
                    val openingLoan = tokens.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                    val currentDue = tokens.getOrNull(4)?.toDoubleOrNull() ?: monthlyRd
                    val remarks = tokens.getOrNull(5) ?: "Excel Import"

                    val existingMember = existingMembers[mobile]
                    if (existingMember != null) {
                        val existingFin = existingFins[existingMember.userId]
                        if (existingFin != null) {
                            financialsToUpdate.add(
                                existingFin.copy(
                                    rdAmount = monthlyRd,
                                    loanOutstanding = openingLoan,
                                    currentRdDue = currentDue,
                                    interestDue = if (openingLoan > 0) openingLoan * 0.01 else 0.0,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            updatedCount++
                            details.add("Updated Member financials: $name ($mobile)")
                        }
                    } else {
                        val newId = generateNextUserId()
                        val newUser = UserEntity(
                            userId = newId,
                            name = name,
                            mobile = mobile,
                            pin = "1234",
                            role = UserRole.MEMBER,
                            status = AccountStatus.ACTIVE,
                            remarks = remarks
                        )
                        membersToCreate.add(newUser)
                        financialsToCreate.add(
                            MemberFinancialEntity(
                                userId = newId,
                                rdAmount = monthlyRd,
                                currentRdDue = currentDue,
                                interestDue = if (openingLoan > 0) openingLoan * 0.01 else 0.0,
                                loanOutstanding = openingLoan,
                                loanEligibility = maxOf(50000.0, openingLoan * 1.5),
                                lastMonthEndBalance = openingLoan
                            )
                        )
                        newCount++
                        details.add("New Member added: $name ($newId, $mobile, Loan: ₹$openingLoan)")
                    }
                } else {
                    // Standard 8-column Transaction Ledger Format
                    val name = tokens[0]
                    val mobile = tokens[1]
                    val paymentInfo = tokens.getOrNull(2) ?: "CASH"
                    val amount = tokens.getOrNull(3)?.toDoubleOrNull() ?: 0.0
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

                    val existingMember = existingMembers[mobile]
                    val userId = if (existingMember == null) {
                        val genId = generateNextUserId()
                        val newUser = UserEntity(
                            userId = genId,
                            name = name,
                            mobile = mobile,
                            role = UserRole.MEMBER,
                            status = AccountStatus.ACTIVE,
                            remarks = "Imported from Ledger"
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
                        details.add("Existing member matched: $name (${existingMember.userId})")
                        existingMember.userId
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
                        rdAmount = minOf(400.0, amount),
                        paymentType = paymentType,
                        paymentMode = paymentType.name,
                        paymentDate = paymentDate,
                        month = "Imported Ledger",
                        status = status,
                        remarks = remarks,
                        approvedAmount = if (status == PaymentStatus.APPROVED) amount else null,
                        approvedBy = "EXCEL_IMPORT",
                        approvedAt = if (status == PaymentStatus.APPROVED) paymentDate else null
                    )
                    paymentsToInsert.add(newPayment)
                }
            } catch (e: Exception) {
                errorCount++
                details.add("Line ${index + 1} Error: ${e.message}")
            }
        }

        if (!isDryRun) {
            if (membersToCreate.isNotEmpty()) userDao.insertUsers(membersToCreate)
            if (financialsToCreate.isNotEmpty()) financialDao.insertFinancials(financialsToCreate)
            for (f in financialsToUpdate) financialDao.updateFinancial(f)
            if (paymentsToInsert.isNotEmpty()) paymentDao.insertPayments(paymentsToInsert)

            auditDao.insertAuditLog(
                AuditLogEntity(
                    action = "EXCEL_IMPORT_EXECUTED",
                    performedBy = "ADMIN",
                    details = "Imported records. New: $newCount, Updated: $updatedCount, Duplicates: $duplicateCount, Errors: $errorCount"
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

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsReadForUser(userId)
    }

    // 100% Free Cloud Backup & Restore System
    // Exports full database state to portable JSON matching Google Apps Script Web App SYNC_ALL format
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val users = userDao.getAllActiveMembers().first()
        val financials = financialDao.getAllFinancials().first()
        val payments = paymentDao.getAllPayments().first()
        val settings = settingsDao.getSettingsDirect() ?: SocietySettingsEntity()

        val finMap = financials.associateBy { it.userId }
        val dateFmt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.US)
        val yearFmt = SimpleDateFormat("yyyy", Locale.US)

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"action\": \"SYNC_ALL\",\n")
        sb.append("  \"data\": {\n")

        // members
        sb.append("    \"members\": [\n")
        users.forEachIndexed { i, u ->
            val f = finMap[u.userId]
            val monthlyRd = f?.rdAmount ?: settings.defaultMonthlyRd
            val openingBal = f?.totalPaidThisYear ?: 0.0
            val bonusShare = f?.accumulatedRdBonus ?: 0.0
            val dateJoined = dateFmt.format(Date(u.createdAt))
            val cleanNotes = u.remarks.replace("\"", "\\\"").replace("\n", " ")
            val cleanName = u.name.replace("\"", "\\\"")

            sb.append("      {")
            sb.append("\"id\":\"${u.userId}\",")
            sb.append("\"name\":\"$cleanName\",")
            sb.append("\"mobile\":\"${u.mobile}\",")
            sb.append("\"aadhaar\":\"\",")
            sb.append("\"address\":\"Society Member\",")
            sb.append("\"dateJoined\":\"$dateJoined\",")
            sb.append("\"monthlyRD\":$monthlyRd,")
            sb.append("\"openingBal\":$openingBal,")
            sb.append("\"bonusShare\":$bonusShare,")
            sb.append("\"status\":\"${u.status.name}\",")
            sb.append("\"notes\":\"$cleanNotes\"")
            sb.append("}${if (i < users.size - 1) "," else ""}\n")
        }
        sb.append("    ],\n")

        // payments
        sb.append("    \"payments\": [\n")
        payments.forEachIndexed { i, p ->
            val pDate = Date(p.paymentDate)
            val dateStr = dateFmt.format(pDate)
            val monthStr = if (p.month.isNotBlank()) p.month else monthFmt.format(pDate)
            val yearVal = yearFmt.format(pDate).toIntOrNull() ?: 2026
            val cleanName = p.userName.replace("\"", "\\\"")
            val approvedAmt = p.approvedAmount ?: p.amount
            val recBy = if (p.approvedBy.isNotBlank()) p.approvedBy else "Admin"

            sb.append("      {")
            sb.append("\"receiptNo\":\"${p.transactionId}\",")
            sb.append("\"date\":\"$dateStr\",")
            sb.append("\"id\":\"${p.userId}\",")
            sb.append("\"name\":\"$cleanName\",")
            sb.append("\"month\":\"$monthStr\",")
            sb.append("\"year\":$yearVal,")
            sb.append("\"rd\":${p.rdAmount},")
            sb.append("\"fine\":${p.penaltyAmount},")
            sb.append("\"interest\":${p.interestAmount},")
            sb.append("\"principalRepay\":${p.loanReturnAmount},")
            sb.append("\"total\":$approvedAmt,")
            sb.append("\"mode\":\"${p.paymentMode}\",")
            sb.append("\"by\":\"$recBy\"")
            sb.append("}${if (i < payments.size - 1) "," else ""}\n")
        }
        sb.append("    ],\n")

        // loans
        sb.append("    \"loans\": [\n")
        val activeLoanMembers = users.filter { (finMap[it.userId]?.loanOutstanding ?: 0.0) > 0.0 }
        activeLoanMembers.forEachIndexed { i, u ->
            val f = finMap[u.userId]
            val outstanding = f?.loanOutstanding ?: 0.0
            val cleanName = u.name.replace("\"", "\\\"")
            val loanId = "LN-${u.userId.takeLast(4)}"
            val dateStr = dateFmt.format(Date(u.createdAt))
            val interestAmt = (outstanding * settings.defaultLoanInterestRate / 100.0)

            sb.append("      {")
            sb.append("\"loanId\":\"$loanId\",")
            sb.append("\"date\":\"$dateStr\",")
            sb.append("\"id\":\"${u.userId}\",")
            sb.append("\"name\":\"$cleanName\",")
            sb.append("\"type\":\"Gullak Loan\",")
            sb.append("\"principal\":$outstanding,")
            sb.append("\"rate\":${settings.defaultLoanInterestRate},")
            sb.append("\"interest\":$interestAmt,")
            sb.append("\"repaid\":0.0,")
            sb.append("\"outstanding\":$outstanding,")
            sb.append("\"status\":\"ACTIVE\",")
            sb.append("\"closeDate\":\"\",")
            sb.append("\"notes\":\"\"")
            sb.append("}${if (i < activeLoanMembers.size - 1) "," else ""}\n")
        }
        sb.append("    ]\n")

        sb.append("  }\n")
        sb.append("}\n")
        sb.toString()
    }

    suspend fun syncWithGoogleSheet(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = exportFullBackupJson()
            val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                instanceFollowRedirects = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
            }
            conn.outputStream.use { os ->
                os.write(jsonPayload.toByteArray(Charsets.UTF_8))
            }
            val responseCode = conn.responseCode
            if (responseCode in 200..399) {
                val responseBody = try {
                    val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                    stream?.bufferedReader()?.use { it.readText() } ?: "Success"
                } catch (e: Exception) {
                    "Sync Success"
                }
                settingsDao.insertOrUpdateSettings(
                    (settingsDao.getSettingsDirect() ?: SocietySettingsEntity()).copy(
                        lastCloudSyncTime = System.currentTimeMillis(),
                        cloudSyncUrl = url
                    )
                )
                auditDao.insertAuditLog(
                    AuditLogEntity(
                        action = "GOOGLE_SHEET_SYNC_SUCCESS",
                        performedBy = "ADMIN",
                        details = "Google Sheet Live Sync Executed. Server Response: ${responseBody.take(120)}"
                    )
                )
                Result.success("Google Sheet Sync Successful! ✅ Data Updated.")
            } else {
                Result.failure(Exception("HTTP Server Error code: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
