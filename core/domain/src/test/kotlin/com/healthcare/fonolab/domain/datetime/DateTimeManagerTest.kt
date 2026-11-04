package com.healthcare.fonolab.domain.datetime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateTimeManagerTest {

    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-10-10T12:00:00Z"),
        ZoneId.of("UTC")
    )

    private val dateTimeManager = DateTimeManager(fixedClock)

    @Test
    fun `parses valid Brazilian DD_MM_AAAA date`() {
        val date = dateTimeManager.parseDate("15/05/2020")
        assertNotNull(date)
        assertEquals(LocalDate.of(2020, 5, 15), date)
        assertEquals("15/05/2020", dateTimeManager.formatDateBr(date))
    }

    @Test
    fun `parses raw 8 digits as DDMMAAAA`() {
        val date = dateTimeManager.parseDate("15052020")
        assertNotNull(date)
        assertEquals(LocalDate.of(2020, 5, 15), date)
    }

    @Test
    fun `validates leap year Feb 29 correctly`() {
        val leapValid = dateTimeManager.validateBirthDate("29/02/2024")
        assertTrue(leapValid is DateValidationResult.Valid)
        assertEquals(LocalDate.of(2024, 2, 29), (leapValid as DateValidationResult.Valid).date)

        val nonLeapInvalid = dateTimeManager.validateBirthDate("29/02/2023")
        assertTrue(nonLeapInvalid is DateValidationResult.Invalid)
        assertEquals(DateErrorReason.INVALID_CALENDAR_DATE, (nonLeapInvalid as DateValidationResult.Invalid).reason)

        val feb31Invalid = dateTimeManager.parseDate("31/02/2024")
        assertNull(feb31Invalid)
    }

    @Test
    fun `rejects future birth date`() {
        val futureResult = dateTimeManager.validateBirthDate("11/10/2026")
        assertTrue(futureResult is DateValidationResult.Invalid)
        assertEquals(DateErrorReason.FUTURE_DATE_NOT_ALLOWED, (futureResult as DateValidationResult.Invalid).reason)
    }

    @Test
    fun `normalizes progressive Brazilian date input`() {
        assertEquals("15", dateTimeManager.normalizeBrDateInput("15"))
        assertEquals("15/05", dateTimeManager.normalizeBrDateInput("1505"))
        assertEquals("15/05/2020", dateTimeManager.normalizeBrDateInput("1505202099"))
    }

    @Test
    fun `calculates detailed age in years months and days`() {
        val age = dateTimeManager.calculateAge(
            birthDate = LocalDate.of(2020, 5, 5),
            referenceDate = LocalDate.of(2026, 10, 10)
        )
        assertEquals(6, age.years)
        assertEquals(5, age.months)
        assertEquals(5, age.days)
        assertEquals("6 anos e 5 meses", age.toFormattedBr())
    }
}
