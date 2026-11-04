package com.healthcare.fonolab.domain.patient

import java.time.Clock
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import kotlin.random.Random

data class Age(val years: Int, val months: Int)

/** Age is always derived from the birth date; it is never persisted. */
class AgeCalculator @Inject constructor(private val clock: Clock) {

    fun ageOf(birthDate: LocalDate): Age = ageOn(birthDate, LocalDate.now(clock))

    companion object {
        fun ageOn(birthDate: LocalDate, referenceDate: LocalDate): Age {
            if (referenceDate.isBefore(birthDate)) return Age(0, 0)
            val period = Period.between(birthDate, referenceDate)
            return Age(years = period.years, months = period.months)
        }
    }
}

/**
 * Generates short, human-friendly internal identifiers such as `FL-26-K7Q3`.
 * Ambiguous characters (0/O, 1/I) are excluded.
 */
class InternalCodeGenerator @Inject constructor(
    private val clock: Clock,
    private val random: Random,
) {
    fun generate(): String {
        val year = LocalDate.now(clock).year % 100
        val suffix = (1..SUFFIX_LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")
        return "FL-%02d-%s".format(year, suffix)
    }

    private companion object {
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        const val SUFFIX_LENGTH = 4
    }
}
