package com.example

import com.example.data.AppRepository
import com.example.model.AttendanceStatus
import com.example.model.ExpenseCategory
import com.example.model.LiveStatus
import com.example.model.PrivacyMode
import com.example.model.VehicleType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BirlikteLogicTest {

    @Test
    fun testRepositoryInitialData() {
        val repo = AppRepository()
        val event = repo.getCurrentEvent()
        assertNotNull(event)
        assertEquals("BRLKT-4892", event?.inviteCode)
        assertTrue(event?.members?.isNotEmpty() == true)

        val me = event?.members?.find { it.isCurrentUser }
        assertNotNull(me)
        assertEquals(AttendanceStatus.COMING, me?.attendance)
    }

    @Test
    fun testExpenseAdditionAndSettlement() {
        val repo = AppRepository()
        val initialCount = repo.expenses.value.size

        repo.addExpense(
            title = "Otoyol Geçiş Ücreti",
            amount = 120.0,
            paidByMemberId = "user-me",
            splitMemberIds = listOf("user-me", "m-burak"),
            category = ExpenseCategory.TRANSPORT
        )

        assertEquals(initialCount + 1, repo.expenses.value.size)
        assertTrue(repo.debtSettlements.value.isNotEmpty())
    }

    @Test
    fun testStatusUpdate() {
        val repo = AppRepository()
        repo.updateMyStatus(
            attendance = AttendanceStatus.COMING,
            liveStatus = LiveStatus.ARRIVED,
            departureTime = "19:30",
            originName = "Kadıköy İskele",
            vehicleType = VehicleType.WALK,
            privacyMode = PrivacyMode.PRECISE
        )

        val updatedMe = repo.getCurrentEvent()?.members?.find { it.isCurrentUser }
        assertEquals(LiveStatus.ARRIVED, updatedMe?.liveStatus)
        assertEquals(VehicleType.WALK, updatedMe?.vehicleType)
    }
}
