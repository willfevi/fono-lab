package java.time

import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

internal fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

internal fun lengthOfMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

internal fun toDaysSinceEpoch(year: Int, month: Int, day: Int): Long {
    var y = year.toLong()
    var m = month.toLong()
    if (m <= 2) {
        y--
        m += 12
    }
    val era = if (y >= 0) y / 400 else (y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (m - 3) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

internal fun fromDaysSinceEpoch(daysSinceEpoch: Long): LocalDate {
    val z = daysSinceEpoch + 719468
    val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (mp + if (mp < 10) 3 else -9).toInt()
    val year = (y + if (m <= 2) 1 else 0).toInt()
    return LocalDate(year, m, d)
}

data class Duration(val seconds: Long) {
    companion object {
        fun between(startInclusive: Instant, endExclusive: Instant): Duration {
            val secs = (endExclusive.epochMilli - startInclusive.epochMilli) / 1000
            return Duration(secs)
        }
        fun ofSeconds(seconds: Long): Duration = Duration(seconds)
        fun ofMinutes(minutes: Long): Duration = Duration(minutes * 60)
    }
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    val value: Int get() = ordinal + 1

    fun getDisplayName(style: TextStyle, locale: Locale?): String {
        return when (this) {
            MONDAY -> if (style == TextStyle.SHORT) "Seg" else "Segunda-feira"
            TUESDAY -> if (style == TextStyle.SHORT) "Ter" else "Terça-feira"
            WEDNESDAY -> if (style == TextStyle.SHORT) "Qua" else "Quarta-feira"
            THURSDAY -> if (style == TextStyle.SHORT) "Qui" else "Quinta-feira"
            FRIDAY -> if (style == TextStyle.SHORT) "Sex" else "Sexta-feira"
            SATURDAY -> if (style == TextStyle.SHORT) "Sáb" else "Sábado"
            SUNDAY -> if (style == TextStyle.SHORT) "Dom" else "Domingo"
        }
    }
}

open class ZoneId(val id: String) {
    companion object {
        fun of(zoneId: String): ZoneId = ZoneId(zoneId)
        fun systemDefault(): ZoneId = ZoneId("America/Sao_Paulo")
    }

    override fun equals(other: Any?): Boolean = other is ZoneId && other.id == id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id
}

class ZoneOffset(id: String) : ZoneId(id) {
    companion object {
        val UTC: ZoneOffset = ZoneOffset("Z")
    }
}

data class Instant(val epochMilli: Long) : Comparable<Instant> {
    val epochSecond: Long get() = epochMilli / 1000

    fun toEpochMilli(): Long = epochMilli

    fun plusSeconds(seconds: Long): Instant = Instant(epochMilli + seconds * 1000)
    fun minusSeconds(seconds: Long): Instant = plusSeconds(-seconds)
    fun isAfter(other: Instant): Boolean = this > other
    fun isBefore(other: Instant): Boolean = this < other

    fun atZone(zone: ZoneId): LocalDateTime = LocalDateTime.ofInstant(this, zone)

    override fun compareTo(other: Instant): Int = epochMilli.compareTo(other.epochMilli)

    override fun toString(): String {
        val dt = atZone(ZoneOffset.UTC)
        return "${dt}Z"
    }

    companion object {
        fun now(): Instant = Instant(currentTimeMillis())
        fun now(clock: Clock): Instant = clock.instant()
        fun ofEpochMilli(epochMilli: Long): Instant = Instant(epochMilli)
        fun ofEpochSecond(epochSecond: Long): Instant = Instant(epochSecond * 1000)
        fun parse(text: String): Instant {
            val clean = text.removeSuffix("Z")
            val parts = clean.split("T")
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val second = if (timeParts.size > 2) timeParts[2].substringBefore('.').toInt() else 0
            val days = toDaysSinceEpoch(year, month, day)
            val ms = days * 86400000L + hour * 3600000L + minute * 60000L + second * 1000L
            return Instant(ms)
        }
    }
}

data class LocalDate(
    val year: Int,
    val monthValue: Int,
    val dayOfMonth: Int
) : Comparable<LocalDate> {

    init {
        require(monthValue in 1..12) { "Mês inválido: $monthValue" }
        require(dayOfMonth in 1..lengthOfMonth(year, monthValue)) { "Dia inválido: $dayOfMonth para o mês $monthValue/$year" }
    }

    val dayOfWeek: DayOfWeek
        get() {
            val m = if (monthValue < 3) monthValue + 12 else monthValue
            val y = if (monthValue < 3) year - 1 else year
            val k = y % 100
            val j = y / 100
            val h = (dayOfMonth + (13 * (m + 1)) / 5 + k + (k / 4) + (j / 4) - (2 * j)) % 7
            val d = ((h + 5) % 7) + 1 // 1 = Seg, 7 = Dom
            return DayOfWeek.entries[d - 1]
        }

    fun isBefore(other: LocalDate): Boolean = this < other
    fun isAfter(other: LocalDate): Boolean = this > other
    fun isEqual(other: LocalDate): Boolean = this == other

    fun plusDays(days: Long): LocalDate = fromDaysSinceEpoch(toDaysSinceEpoch(year, monthValue, dayOfMonth) + days)
    fun minusDays(days: Long): LocalDate = plusDays(-days)

    fun plusMonths(months: Long): LocalDate {
        var m = monthValue + months.toInt()
        var y = year
        while (m > 12) { m -= 12; y++ }
        while (m < 1) { m += 12; y-- }
        val maxDays = lengthOfMonth(y, m)
        return LocalDate(y, m, dayOfMonth.coerceAtMost(maxDays))
    }
    fun minusMonths(months: Long): LocalDate = plusMonths(-months)

    fun plusYears(years: Long): LocalDate = LocalDate(year + years.toInt(), monthValue, dayOfMonth.coerceAtMost(lengthOfMonth(year + years.toInt(), monthValue)))
    fun minusYears(years: Long): LocalDate = plusYears(-years)

    fun atStartOfDay(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime(year, monthValue, dayOfMonth, 0, 0, 0)

    fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    override fun compareTo(other: LocalDate): Int {
        val y = year.compareTo(other.year)
        if (y != 0) return y
        val m = monthValue.compareTo(other.monthValue)
        if (m != 0) return m
        return dayOfMonth.compareTo(other.dayOfMonth)
    }

    override fun toString(): String {
        val y = year.toString().padStart(4, '0')
        val m = monthValue.toString().padStart(2, '0')
        val d = dayOfMonth.toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    companion object {
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDate = LocalDate(year, month, dayOfMonth)
        fun now(): LocalDate = now(Clock.systemDefaultZone())
        fun now(clock: Clock): LocalDate {
            val inst = clock.instant()
            val days = inst.epochMilli / 86400000L
            return fromDaysSinceEpoch(days)
        }
        fun parse(text: String): LocalDate {
            val parts = text.trim().split("-")
            require(parts.size == 3) { "Formato ISO inválido: $text" }
            return of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
        fun parse(text: String, formatter: DateTimeFormatter): LocalDate = formatter.parseLocalDate(text)
    }
}

data class LocalDateTime(
    val year: Int,
    val monthValue: Int,
    val dayOfMonth: Int,
    val hour: Int,
    val minute: Int,
    val second: Int
) : Comparable<LocalDateTime> {

    fun toLocalDate(): LocalDate = LocalDate.of(year, monthValue, dayOfMonth)

    fun format(formatter: DateTimeFormatter): String = formatter.format(this)

    fun toInstant(zone: ZoneId = ZoneId.systemDefault()): Instant {
        val days = toDaysSinceEpoch(year, monthValue, dayOfMonth)
        val ms = days * 86400000L + hour * 3600000L + minute * 60000L + second * 1000L
        return Instant(ms)
    }

    override fun compareTo(other: LocalDateTime): Int {
        val d = LocalDate.of(year, monthValue, dayOfMonth).compareTo(LocalDate.of(other.year, other.monthValue, other.dayOfMonth))
        if (d != 0) return d
        val h = hour.compareTo(other.hour)
        if (h != 0) return h
        val m = minute.compareTo(other.minute)
        if (m != 0) return m
        return second.compareTo(other.second)
    }

    override fun toString(): String {
        val y = year.toString().padStart(4, '0')
        val mo = monthValue.toString().padStart(2, '0')
        val d = dayOfMonth.toString().padStart(2, '0')
        val h = hour.toString().padStart(2, '0')
        val mi = minute.toString().padStart(2, '0')
        val s = second.toString().padStart(2, '0')
        return "$y-$mo-${d}T$h:$mi:$s"
    }

    companion object {
        fun now(): LocalDateTime = now(Clock.systemDefaultZone())
        fun now(clock: Clock): LocalDateTime = ofInstant(clock.instant(), clock.zone)
        fun ofInstant(instant: Instant, zone: ZoneId): LocalDateTime {
            val totalSeconds = instant.epochMilli / 1000
            val second = (totalSeconds % 60).toInt().let { if (it < 0) it + 60 else it }
            val totalMinutes = totalSeconds / 60
            val minute = (totalMinutes % 60).toInt().let { if (it < 0) it + 60 else it }
            val totalHours = totalMinutes / 60
            val hour = (totalHours % 24).toInt().let { if (it < 0) it + 24 else it }
            val totalDays = totalHours / 24
            val localDate = fromDaysSinceEpoch(totalDays)
            return LocalDateTime(localDate.year, localDate.monthValue, localDate.dayOfMonth, hour, minute, second)
        }
    }
}

data class Period(val years: Int, val months: Int, val days: Int) {
    companion object {
        fun between(startDateInclusive: LocalDate, endDateExclusive: LocalDate): Period {
            var years = endDateExclusive.year - startDateInclusive.year
            var months = endDateExclusive.monthValue - startDateInclusive.monthValue
            var days = endDateExclusive.dayOfMonth - startDateInclusive.dayOfMonth

            if (days < 0) {
                months -= 1
                val prevMonth = if (endDateExclusive.monthValue == 1) 12 else endDateExclusive.monthValue - 1
                val prevYear = if (endDateExclusive.monthValue == 1) endDateExclusive.year - 1 else endDateExclusive.year
                days += lengthOfMonth(prevYear, prevMonth)
            }
            if (months < 0) {
                years -= 1
                months += 12
            }
            return Period(years, months, days)
        }
    }
}

abstract class Clock {
    abstract fun instant(): Instant
    abstract val zone: ZoneId

    companion object {
        fun systemDefaultZone(): Clock = SystemClock(ZoneId.systemDefault())
        fun system(zone: ZoneId): Clock = SystemClock(zone)
        fun fixed(instant: Instant, zone: ZoneId): Clock = FixedClock(instant, zone)
    }
}

private class SystemClock(override val zone: ZoneId) : Clock() {
    override fun instant(): Instant = Instant.now()
}

private class FixedClock(private val fixedInstant: Instant, override val zone: ZoneId) : Clock() {
    override fun instant(): Instant = fixedInstant
}
