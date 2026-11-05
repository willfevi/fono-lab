package com.healthcare.fonolab.domain.datetime

import com.healthcare.fonolab.domain.datetime.wasm.Duration
import com.healthcare.fonolab.domain.datetime.wasm.Locale
import com.healthcare.fonolab.domain.datetime.wasm.Period
import com.healthcare.fonolab.domain.datetime.wasm.ResolverStyle
import com.healthcare.fonolab.domain.datetime.wasm.TextStyle
import com.healthcare.fonolab.domain.datetime.wasm.ZoneOffset

actual typealias Clock = com.healthcare.fonolab.domain.datetime.wasm.Clock
actual typealias Instant = com.healthcare.fonolab.domain.datetime.wasm.Instant
actual typealias LocalDate = com.healthcare.fonolab.domain.datetime.wasm.LocalDate
actual typealias LocalDateTime = com.healthcare.fonolab.domain.datetime.wasm.LocalDateTime
actual typealias DateTimeFormatter = com.healthcare.fonolab.domain.datetime.wasm.DateTimeFormatter

private val ptBr = Locale.forLanguageTag("pt-BR")

actual fun systemClock(zoneId: String): Clock =
    Clock.system(com.healthcare.fonolab.domain.datetime.wasm.ZoneId.of(zoneId))
actual fun clockZone(clock: Clock): ZoneId = ZoneId(clock.zone.id)
actual fun currentLocalDate(clock: Clock?): LocalDate =
    if (clock == null) LocalDate.now() else LocalDate.now(clock)
actual fun localDateTimeAt(instant: Instant, zone: ZoneId): LocalDateTime =
    LocalDateTime.ofInstant(instant, com.healthcare.fonolab.domain.datetime.wasm.ZoneId.of(zone.id))
actual fun localDateAt(instant: Instant, zone: ZoneId): LocalDate =
    instant.atZone(com.healthcare.fonolab.domain.datetime.wasm.ZoneId.of(zone.id)).toLocalDate()
actual fun startOfDay(date: LocalDate, zone: ZoneId): Instant =
    date.atStartOfDay(com.healthcare.fonolab.domain.datetime.wasm.ZoneId.of(zone.id)).toInstant()
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
