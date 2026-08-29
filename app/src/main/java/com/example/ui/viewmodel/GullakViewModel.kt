package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.MemberFinancialEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentType
import com.example.data.model.SocietySettingsEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.GullakRepository
import com.example.data.repository.ImportSummary
import com.example.data.repository.MemberWithFinancials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: UserEntity) : AuthState()
}

class GullakViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = GullakRepository(database)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Status Message for Snackbars / Dialogs
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Import Summary State
    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    val importSummary: StateFlow<ImportSummary?> = _importSummary.asStateFlow()

    // Repository Flows
    val settings: StateFlow<SocietySettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMembers: StateFlow<List<UserEntity>> = repository.activeMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMembers: StateFlow<List<UserEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val membersWithFinancials: StateFlow<List<MemberWithFinancials>> = repository.membersWithFinancials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMembers: StateFlow<Int> = repository.totalMemberCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeMemberCount: StateFlow<Int> = repository.activeMemberCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inactiveMemberCount: StateFlow<Int> = repository.inactiveMemberCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingPayments: StateFlow<List<PaymentEntity>> = repository.pendingPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPaymentCount: StateFlow<Int> = repository.pendingPaymentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayCollection: StateFlow<Double?> = repository.getTodayCollection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyCollection: StateFlow<Double?> = repository.getMonthlyCollection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLoanOutstanding: StateFlow<Double?> = repository.totalLoanOutstanding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDueAmount: StateFlow<Double?> = repository.totalDueAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Member Reactive Data
    val currentMemberFinancial: StateFlow<MemberFinancialEntity?> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == UserRole.MEMBER) {
                repository.getFinancialForUser(user.userId)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMemberPayments: StateFlow<List<PaymentEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == UserRole.MEMBER) {
                repository.getPaymentsForUser(user.userId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMemberNotifications: StateFlow<List<NotificationEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getNotificationsForUser(user.userId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMemberUnreadCount: StateFlow<Int> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getUnreadNotificationCount(user.userId)
            } else {
                flowOf(0)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun clearMessages() {
        _userMessage.value = null
        _errorMessage.value = null
    }

    fun clearImportSummary() {
        _importSummary.value = null
    }

    // Login
    fun login(mobile: String, pin: String) {
        viewModelScope.launch {
            clearMessages()
            val result = repository.authenticate(mobile, pin)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                _userMessage.value = "Namaste, ${user.name}! Login successful."
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Login failed. Kripya details check karein."
            }
        }
    }

    // Quick Login Switch for Demo / Testing Convenience
    fun quickLoginAs(user: UserEntity) {
        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        _userMessage.value = "Logged in as ${user.name} (${user.role})"
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        clearMessages()
    }

    // Member: Submit Payment Request (with 4-column breakdown)
    fun submitPaymentWithBreakdown(
        totalAmount: Double,
        rdAmount: Double = 400.0,
        interestAmount: Double = 0.0,
        penaltyAmount: Double = 0.0,
        loanReturnAmount: Double = 0.0,
        paymentType: PaymentType = PaymentType.ONLINE,
        paymentMode: String = "ONLINE",
        remarks: String = "",
        screenshotUrl: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.submitPaymentRequest(
                userId = user.userId,
                amount = totalAmount,
                rdAmount = rdAmount,
                interestAmount = interestAmount,
                penaltyAmount = penaltyAmount,
                loanReturnAmount = loanReturnAmount,
                paymentType = paymentType,
                paymentMode = paymentMode,
                remarks = remarks,
                screenshotUrl = screenshotUrl
            )
            result.onSuccess {
                _userMessage.value = "Payment Request Submitted ✅ Status: Pending Approval"
                com.example.util.NotificationHelper.showPushNotification(
                    getApplication(),
                    "Payment Submitted 💳",
                    "Aapka ₹$totalAmount ka payment approval ke liye bhej diya gaya hai."
                )
            }.onFailure {
                _errorMessage.value = "Payment submit nahi ho paya: ${it.message}"
            }
        }
    }

    fun submitPayment(
        amount: Double,
        paymentType: PaymentType = PaymentType.ONLINE,
        remarks: String = ""
    ) {
        submitPaymentWithBreakdown(
            totalAmount = amount,
            rdAmount = 400.0,
            interestAmount = 0.0,
            penaltyAmount = 0.0,
            loanReturnAmount = 0.0,
            paymentType = paymentType,
            paymentMode = if (paymentType == PaymentType.ONLINE) "ONLINE_UPI" else "OFFICE_CASH",
            remarks = remarks
        )
    }

    // Admin: Record Manual / Office Cash Payment Directly
    fun recordOfficeCashPayment(
        userId: String,
        amount: Double,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        paymentMode: String = "OFFICE_CASH",
        remarks: String = "Office Cash Received",
        adminRemarks: String = "Approved by Office"
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.recordOfficeCashPayment(
                userId = userId,
                amount = amount,
                rdAmount = rdAmount,
                interestAmount = interestAmount,
                penaltyAmount = penaltyAmount,
                loanReturnAmount = loanReturnAmount,
                paymentMode = paymentMode,
                remarks = remarks,
                adminRemarks = adminRemarks,
                adminId = admin.userId
            )
            result.onSuccess {
                _userMessage.value = "Office Payment Recorded & Approved Successfully ✅"
                com.example.util.NotificationHelper.showPushNotification(
                    getApplication(),
                    "Office Payment Recorded",
                    "₹$amount recorded for member $userId"
                )
            }.onFailure {
                _errorMessage.value = "Record nahi ho saka: ${it.message}"
            }
        }
    }

    // Admin: Approve Payment with Breakdown & Admin Remarks
    fun approvePaymentWithBreakdown(
        paymentId: Long,
        approvedAmount: Double,
        rdAmount: Double,
        interestAmount: Double,
        penaltyAmount: Double,
        loanReturnAmount: Double,
        adminRemarks: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.approvePaymentWithBreakdown(
                paymentId = paymentId,
                approvedAmount = approvedAmount,
                rdAmount = rdAmount,
                interestAmount = interestAmount,
                penaltyAmount = penaltyAmount,
                loanReturnAmount = loanReturnAmount,
                adminRemarks = adminRemarks,
                adminId = admin.userId
            )
            result.onSuccess {
                _userMessage.value = "Payment Approved & Receipt Generated ✅"
                com.example.util.NotificationHelper.showPushNotification(
                    getApplication(),
                    "Payment Approved ✅",
                    "Payment #$paymentId of ₹$approvedAmount approved successfully."
                )
            }.onFailure {
                _errorMessage.value = "Approval failed: ${it.message}"
            }
        }
    }

    // Admin: Re-edit / Correct / Reverse Approved Payment
    fun reverseOrEditPayment(
        paymentId: Long,
        newApprovedAmount: Double,
        newRdAmount: Double,
        newInterestAmount: Double,
        newPenaltyAmount: Double,
        newLoanReturnAmount: Double,
        newAdminRemarks: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.reverseOrEditPayment(
                paymentId = paymentId,
                newApprovedAmount = newApprovedAmount,
                newRdAmount = newRdAmount,
                newInterestAmount = newInterestAmount,
                newPenaltyAmount = newPenaltyAmount,
                newLoanReturnAmount = newLoanReturnAmount,
                newAdminRemarks = newAdminRemarks,
                adminId = admin.userId
            )
            result.onSuccess {
                _userMessage.value = "Payment Rectified / Adjusted Successfully ✏️"
            }.onFailure {
                _errorMessage.value = "Correction failed: ${it.message}"
            }
        }
    }

    // Admin: Approve Payment (Default Legacy)
    fun approvePayment(paymentId: Long) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.approvePayment(paymentId, admin.userId)
            result.onSuccess {
                _userMessage.value = "Payment Approved Successfully ✅"
            }.onFailure {
                _errorMessage.value = "Payment approve nahi hui: ${it.message}"
            }
        }
    }

    // Admin: Approve Payment With Edit (Legacy)
    fun approvePaymentWithEdit(paymentId: Long, editedAmount: Double) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.approvePaymentWithEdit(paymentId, editedAmount, admin.userId)
            result.onSuccess {
                _userMessage.value = "Payment Approved with Edited Amount (₹$editedAmount) ✏️"
            }.onFailure {
                _errorMessage.value = "Approval failed: ${it.message}"
            }
        }
    }


    // Admin: Reject Payment
    fun rejectPayment(paymentId: Long, reason: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.rejectPayment(paymentId, reason, admin.userId)
            result.onSuccess {
                _userMessage.value = "Payment Rejected. Reason: $reason ❌"
            }.onFailure {
                _errorMessage.value = "Reject nahi ho payi: ${it.message}"
            }
        }
    }

    // Admin: Add Member
    fun addMember(
        name: String,
        mobile: String,
        pin: String,
        initialRd: Double,
        loanOutstanding: Double,
        loanEligibility: Double,
        status: AccountStatus,
        remarks: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.addMember(
                name = name,
                mobile = mobile,
                pin = pin,
                initialRd = initialRd,
                loanOutstanding = loanOutstanding,
                loanEligibility = loanEligibility,
                status = status,
                remarks = remarks,
                adminId = admin.userId
            )
            result.onSuccess { newUser ->
                _userMessage.value = "Member Created Successfully: ${newUser.name} (${newUser.userId}) ✅"
            }.onFailure {
                _errorMessage.value = it.message ?: "Member add nahi ho saka"
            }
        }
    }

    // Admin: Update Member
    fun updateMember(
        userId: String,
        name: String,
        mobile: String,
        rdAmount: Double,
        loanOutstanding: Double,
        loanEligibility: Double,
        status: AccountStatus,
        remarks: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.updateMember(
                userId = userId,
                name = name,
                mobile = mobile,
                rdAmount = rdAmount,
                loanOutstanding = loanOutstanding,
                loanEligibility = loanEligibility,
                status = status,
                remarks = remarks,
                adminId = admin.userId
            )
            result.onSuccess {
                _userMessage.value = "Member Details Updated Successfully ✅"
            }.onFailure {
                _errorMessage.value = it.message ?: "Update nahi ho saka"
            }
        }
    }

    // Admin: Toggle Member Inactive / Active
    fun toggleMemberStatus(userId: String, currentStatus: AccountStatus) {
        val admin = _currentUser.value ?: return
        val newStatus = if (currentStatus == AccountStatus.ACTIVE) AccountStatus.INACTIVE else AccountStatus.ACTIVE
        viewModelScope.launch {
            repository.setMemberStatus(userId, newStatus, admin.userId)
            _userMessage.value = "Member status changed to ${newStatus.name}."
        }
    }

    // Admin: Delete Member Permanently
    fun deleteMemberPermanently(userId: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteMemberPermanently(userId, admin.userId)
            _userMessage.value = "Member Permanently Deleted from System."
        }
    }

    // Admin: Reset / Provide Member PIN
    fun resetMemberPin(userId: String, newPin: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateMemberPin(userId, newPin, admin.userId)
            _userMessage.value = "PIN updated successfully for user $userId to $newPin."
        }
    }

    // Admin: Set Loan Eligibility
    fun updateLoanEligibility(userId: String, eligibility: Double) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateLoanEligibility(userId, eligibility, admin.userId)
            _userMessage.value = "Loan Eligibility updated to ₹$eligibility for $userId."
        }
    }

    // Admin: Send Notification to All Active Members
    fun sendNotificationToAll(title: String, message: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val count = repository.sendNotificationToAll(title, message, admin.userId)
            _userMessage.value = "Notification successfully sent to $count Active Members 📢"
        }
    }

    // Admin: Send Notification to Single Member
    fun sendNotificationToMember(userId: String, title: String, message: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.sendNotificationToMember(userId, title, message, admin.userId)
            _userMessage.value = "Notification sent to member $userId 🔔"
        }
    }

    // Admin: Trigger Dues Reminders
    fun triggerDuesReminders(customTemplate: String? = null) {
        viewModelScope.launch {
            val count = repository.triggerScheduledReminders(customTemplate)
            _userMessage.value = "Sent payment reminders to $count members with pending dues/payments ⏰"
        }
    }

    // Admin: Update Society Settings
    fun updateSocietySettings(newSettings: SocietySettingsEntity) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateSocietySettings(newSettings, admin.userId)
            _userMessage.value = "Society Settings Updated Successfully ⚙️"
        }
    }

    // Admin: Apply Year-End RD Bonus Adjustment
    fun applyYearEndBonusAdjustment() {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.applyYearEndBonusAdjustment(admin.userId)
            _userMessage.value = result
        }
    }

    // Excel CSV Import
    fun previewCsvImport(csvContent: String) {
        viewModelScope.launch {
            val summary = repository.previewAndImportCsv(csvContent, isDryRun = true)
            _importSummary.value = summary
        }
    }

    fun executeCsvImport(csvContent: String) {
        viewModelScope.launch {
            val summary = repository.previewAndImportCsv(csvContent, isDryRun = false)
            _importSummary.value = summary
            _userMessage.value = "Import Completed: ${summary.newRecords} new, ${summary.updatedRecords} updated records added! ✅"
        }
    }
}
