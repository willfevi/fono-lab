package com.healthcare.fonolab.domain.datetime

expect abstract class Clock {
    abstract fun instant(): Instant
}

data class ZoneId(val id: String)

expect class Instant : Comparable<Instant> {
    override fun compareTo(other: Instant): Int
    fun plusSeconds(seconds: Long): Instant
    fun minusSeconds(seconds: Long): Instant
    fun isAfter(other: Instant): Boolean
}

expect class LocalDate {
    fun getYear(): Int
    fun getDayOfMonth(): Int
    fun plusDays(days: Long): LocalDate
    fun minusDays(days: Long): LocalDate
    fun minusMonths(months: Long): LocalDate
    fun minusYears(years: Long): LocalDate
    fun format(formatter: DateTimeFormatter): String
}

expect class LocalDateTime {
    fun format(formatter: DateTimeFormatter): String
}

expect class DateTimeFormatter

expect fun systemClock(zoneId: String): Clock
expect fun clockZone(clock: Clock): ZoneId
expect fun currentLocalDate(clock: Clock? = null): LocalDate
expect fun localDateTimeAt(instant: Instant, zone: ZoneId): LocalDateTime
expect fun localDateAt(instant: Instant, zone: ZoneId): LocalDate
expect fun startOfDay(date: LocalDate, zone: ZoneId): Instant
expect fun utcDateAtEpochMillis(millis: Long): LocalDate
expect fun dateFormatter(pattern: String, strict: Boolean = false): DateTimeFormatter
expect fun parseLocalDate(text: String, formatter: DateTimeFormatter): LocalDate
expect fun LocalDate.isBefore(other: LocalDate): Boolean
expect fun LocalDate.isAfter(other: LocalDate): Boolean
expect fun isoWeekday(date: LocalDate): Int
expect fun shortWeekdayPtBr(date: LocalDate): String
expect fun dateDifference(start: LocalDate, end: LocalDate): DetailedAge
expect fun secondsBetween(start: Instant, end: Instant): Long
