package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.repository.GullakRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GullakSocietyUnitTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GullakRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = GullakRepository(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSeedingAndAuthentication() = runBlocking {
        repository.seedInitialDataIfEmpty()

        // Admin Auth
        val adminAuth = repository.authenticate("9876543210", "1234")
        assertTrue(adminAuth.isSuccess)
        assertEquals("Society Admin", adminAuth.getOrNull()?.name)

        // Member Auth
        val memberAuth = repository.authenticate("9810011111", "1234")
        assertTrue(memberAuth.isSuccess)
        assertEquals("Rahul Kumar", memberAuth.getOrNull()?.name)
    }

    @Test
    fun testPaymentSubmissionAndApprovalWorkflow() = runBlocking {
        repository.seedInitialDataIfEmpty()

        // Rahul Kumar submits cash payment of 400
        val submitResult = repository.submitPaymentRequest(
            userId = "USR-00001",
            amount = 400.0,
            paymentType = PaymentType.CASH,
            remarks = "August RD Cash"
        )
        assertTrue(submitResult.isSuccess)

        val pendingList = repository.pendingPayments.first()
        val createdPayment = pendingList.find { it.userId == "USR-00001" }
        assertNotNull(createdPayment)
        assertEquals(PaymentStatus.PENDING, createdPayment?.status)

        // Admin approves payment
        val approveResult = repository.approvePayment(createdPayment!!.id, "ADMIN-001")
        assertTrue(approveResult.isSuccess)

        val updatedFinancial = repository.getFinancialForUser("USR-00001").first()
        assertNotNull(updatedFinancial)
        assertEquals(0.0, updatedFinancial?.currentRdDue ?: 1.0, 0.01)
    }

    @Test
    fun testPaymentRejectWithReason() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val submitResult = repository.submitPaymentRequest(
            userId = "USR-00001",
            amount = 500.0,
            paymentType = PaymentType.ONLINE,
            remarks = "Wrong amount test"
        )
        val paymentId = submitResult.getOrThrow().id

        val rejectReason = "Galat Amount (Incorrect amount)"
        val rejectResult = repository.rejectPayment(paymentId, rejectReason, "ADMIN-001")
        assertTrue(rejectResult.isSuccess)

        val payment = database.paymentDao().getPaymentById(paymentId)
        assertEquals(PaymentStatus.REJECTED, payment?.status)
        assertEquals(rejectReason, payment?.rejectionReason)
    }

    @Test
    fun testApproveWithEditWorkflow() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val submitResult = repository.submitPaymentRequest(
            userId = "USR-00002",
            amount = 1000.0,
            paymentType = PaymentType.CASH,
            remarks = "Submitted 1000"
        )
        val paymentId = submitResult.getOrThrow().id

        // Admin edits to 800 and approves
        val editResult = repository.approvePaymentWithEdit(paymentId, 800.0, "ADMIN-001")
        assertTrue(editResult.isSuccess)

        val payment = database.paymentDao().getPaymentById(paymentId)
        assertEquals(PaymentStatus.APPROVED_WITH_EDIT, payment?.status)
        assertEquals(800.0, payment?.approvedAmount ?: 0.0, 0.01)
    }

    @Test
    fun testReminderExcludesInactiveMembers() = runBlocking {
        repository.seedInitialDataIfEmpty()

        // Pooja Devi (USR-00004) is INACTIVE
        val inactiveUser = database.userDao().getUserById("USR-00004")
        assertEquals(AccountStatus.INACTIVE, inactiveUser?.status)

        val reminderCount = repository.triggerScheduledReminders()
        // Reminders should only go to active members with dues
        val notifications = database.notificationDao().getNotificationsForUser("USR-00004").first()
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun testCsvImportAndSummary() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val csvData = """
Name,Mobile Number,Payment Info,Amount,Date,Remarks,Transaction ID,Payment Status
Vikram Batra,9810099999,ONLINE,400.0,27-08-2026 12:00,New Member RD,TXN-20260827-777888,APPROVED
""".trimIndent()

        val summary = repository.previewAndImportCsv(csvData, isDryRun = false)
        assertEquals(1, summary.newRecords)
        assertEquals(0, summary.duplicateRecords)

        val newMember = database.userDao().getUserByMobile("9810099999")
        assertNotNull(newMember)
        assertEquals("Vikram Batra", newMember?.name)
    }
}
