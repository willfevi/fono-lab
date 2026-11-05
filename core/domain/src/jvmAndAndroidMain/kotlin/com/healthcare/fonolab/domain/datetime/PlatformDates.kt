package com.healthcare.fonolab.domain.datetime

import java.time.Duration
import java.time.Period
import java.time.ZoneOffset
import java.time.format.ResolverStyle
import java.time.format.TextStyle
import java.util.Locale

actual typealias Clock = java.time.Clock
actual typealias Instant = java.time.Instant
actual typealias LocalDate = java.time.LocalDate
actual typealias LocalDateTime = java.time.LocalDateTime
actual typealias DateTimeFormatter = java.time.format.DateTimeFormatter

private val ptBr = Locale.forLanguageTag("pt-BR")

actual fun systemClock(zoneId: String): Clock = Clock.system(java.time.ZoneId.of(zoneId))
actual fun clockZone(clock: Clock): ZoneId = ZoneId(clock.zone.id)
actual fun currentLocalDate(clock: Clock?): LocalDate =
    if (clock == null) LocalDate.now() else LocalDate.now(clock)
actual fun localDateTimeAt(instant: Instant, zone: ZoneId): LocalDateTime =
    LocalDateTime.ofInstant(instant, java.time.ZoneId.of(zone.id))
actual fun localDateAt(instant: Instant, zone: ZoneId): LocalDate =
    instant.atZone(java.time.ZoneId.of(zone.id)).toLocalDate()
actual fun startOfDay(date: LocalDate, zone: ZoneId): Instant =
    date.atStartOfDay(java.time.ZoneId.of(zone.id)).toInstant()
actual fun utcDateAtEpochMillis(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

actual fun dateFormatter(pattern: String, strict: Boolean): DateTimeFormatter {
    val formatter = DateTimeFormatter.ofPattern(pattern, ptBr)
    return if (strict) formatter.withResolverStyle(ResolverStyle.STRICT) else formatter
}

actual fun parseLocalDate(text: String, formatter: DateTimeFormatter): LocalDate = LocalDate.parse(text, formatter)
actual fun LocalDate.isBefore(other: LocalDate): Boolean = compareTo(other) < 0
actual fun LocalDate.isAfter(other: LocalDate): Boolean = compareTo(other) > 0
actual fun isoWeekday(date: LocalDate): Int = date.dayOfWeek.value
actual fun shortWeekdayPtBr(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, ptBr)

actual fun dateDifference(start: LocalDate, end: LocalDate): DetailedAge {
    val period = Period.between(start, end)
    return DetailedAge(period.years, period.months, period.days)
}

actual fun secondsBetween(start: Instant, end: Instant): Long = Duration.between(start, end).seconds
