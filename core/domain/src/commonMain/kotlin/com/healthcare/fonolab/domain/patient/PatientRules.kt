package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.datetime.Clock
import com.healthcare.fonolab.domain.datetime.currentLocalDate
import com.healthcare.fonolab.domain.datetime.dateDifference
import com.healthcare.fonolab.domain.datetime.isBefore
import com.healthcare.fonolab.domain.datetime.LocalDate
import kotlin.random.Random

data class Age(val years: Int, val months: Int)

/** Age is always derived from the birth date; it is never persisted. */
class AgeCalculator(private val clock: Clock) {

    fun ageOf(birthDate: LocalDate): Age = ageOn(birthDate, currentLocalDate(clock))

    companion object {
        fun ageOn(birthDate: LocalDate, referenceDate: LocalDate): Age {
            if (referenceDate.isBefore(birthDate)) return Age(0, 0)
            val period = dateDifference(birthDate, referenceDate)
            return Age(years = period.years, months = period.months)
        }
    }
}

/**
 * Generates short, human-friendly internal identifiers such as `FL-26-K7Q3`.
 * Ambiguous characters (0/O, 1/I) are excluded.
 */
class InternalCodeGenerator(
    private val clock: Clock,
    private val random: Random,
) {
    fun generate(): String {
        val year = currentLocalDate(clock).getYear() % 100
        val suffix = (1..SUFFIX_LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")
        val yStr = year.toString().padStart(2, '0')
        return "FL-$yStr-$suffix"
    }

    private companion object {
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        const val SUFFIX_LENGTH = 4
    }
}
