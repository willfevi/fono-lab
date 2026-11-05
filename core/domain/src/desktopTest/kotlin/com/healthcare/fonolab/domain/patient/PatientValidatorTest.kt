package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.common.DomainResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PatientValidatorTest {

    private val fixedClock = Clock.fixed(
        Instant.parse("2026-10-10T12:00:00Z"),
        ZoneId.of("UTC")
    )
    private val validator = PatientValidator(fixedClock)

    @Test
    fun `valid draft produces Success`() {
        val draft = PatientDraft(
            fullName = "Lucas Pereira da Silva",
            birthDate = LocalDate.of(2020, 5, 15),
            guardianName = "Mariana Pereira",
            guardianContact = "(11) 98765-4321",
            followUpStartDate = LocalDate.of(2026, 1, 10),
            clinicalNotes = "Paciente cooperativo."
        )

        val result = validator.validate(draft)
        assertTrue(result is DomainResult.Success)
        val valid = (result as DomainResult.Success).value
        assertEquals("Lucas Pereira da Silva", valid.fullName)
        assertEquals(LocalDate.of(2020, 5, 15), valid.birthDate)
    }

    @Test
    fun `empty name produces FULL_NAME_REQUIRED`() {
        val draft = PatientDraft(
            fullName = "   ",
            birthDate = LocalDate.of(2020, 5, 15),
            followUpStartDate = LocalDate.of(2026, 1, 10)
        )
        val result = validator.validate(draft)
        assertTrue(result is DomainResult.Failure)
    }

    @Test
    fun `age calculator correctly calculates years and months`() {
        val birthDate = LocalDate.of(2020, 5, 10)
        val reference = LocalDate.of(2026, 10, 10)
        val age = AgeCalculator.ageOn(birthDate, reference)
        assertEquals(6, age.years)
        assertEquals(5, age.months)
    }
}
