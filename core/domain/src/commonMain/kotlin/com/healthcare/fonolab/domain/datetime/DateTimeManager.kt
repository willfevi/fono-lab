package com.healthcare.fonolab.domain.datetime


/**
 * Multiplatform-ready abstraction for retrieving current date, time, and timezone.
 * In KMP migration, this interface moves directly to `commonMain`.
 */
interface PlatformClock {
    fun nowInstant(): Instant
    fun today(): LocalDate
    fun currentDateTime(): LocalDateTime
    fun zoneId(): ZoneId
}

class SystemPlatformClock(
    private val clock: Clock
) : PlatformClock {
    override fun nowInstant(): Instant = clock.instant()
    override fun today(): LocalDate = currentLocalDate(clock)
    override fun currentDateTime(): LocalDateTime = localDateTimeAt(clock.instant(), clockZone(clock))
    override fun zoneId(): ZoneId = clockZone(clock)
}

data class DetailedAge(
    val years: Int,
    val months: Int,
    val days: Int
) {
    fun toFormattedBr(): String = when {
        years <= 0 && months <= 0 -> "$days dias"
        years <= 0 -> "$months ${if (months == 1) "mês" else "meses"}"
        months == 0 -> "$years ${if (years == 1) "ano" else "anos"}"
        else -> "$years ${if (years == 1) "ano" else "anos"} e $months ${if (months == 1) "mês" else "meses"}"
    }
}

sealed interface DateValidationResult {
    data class Valid(val date: LocalDate, val formattedBr: String) : DateValidationResult
    data class Invalid(val reason: DateErrorReason, val messagePtBr: String) : DateValidationResult
}

enum class DateErrorReason {
    EMPTY,
    INCOMPLETE,
    INVALID_CALENDAR_DATE,
    FUTURE_DATE_NOT_ALLOWED,
    PAST_DATE_TOO_OLD,
    BEFORE_MIN_DATE
}

/**
 * Centralized manager for all clinical and administrative dates and times across FonoLab.
 * Enforces Brazilian standard formatting (`DD/MM/AAAA` and `DD/MM/AAAA HH:mm`), strict calendar
 * validation (including leap years), safe input masking, age calculation, and persistence conversion.
 */
class DateTimeManager(
    private val clock: Clock
) {
    private val strictBrDateFormatter: DateTimeFormatter =
        dateFormatter("dd/MM/uuuu", strict = true)

    private val strictIsoDateFormatter: DateTimeFormatter =
        dateFormatter("uuuu-MM-dd", strict = true)

    private val brDateTimeFormatter: DateTimeFormatter =
        dateFormatter("dd/MM/uuuu HH:mm")

    private val brTimeFormatter: DateTimeFormatter =
        dateFormatter("HH:mm")

    private val brShortHeaderFormatter: DateTimeFormatter =
        dateFormatter("dd 'de' MMM")

    fun today(): LocalDate = currentLocalDate(clock)

    fun nowInstant(): Instant = clock.instant()

    fun formatDateBr(date: LocalDate?): String {
        if (date == null) return ""
        return date.format(strictBrDateFormatter)
    }

    fun formatDateIso(date: LocalDate?): String {
        if (date == null) return ""
        return date.format(strictIsoDateFormatter)
    }

    fun formatDateTimeBr(instant: Instant?): String {
        if (instant == null) return ""
        return localDateTimeAt(instant, clockZone(clock)).format(brDateTimeFormatter)
    }

    fun formatTimeBr(instant: Instant?): String {
        if (instant == null) return ""
        return localDateTimeAt(instant, clockZone(clock)).format(brTimeFormatter)
    }

    fun formatHeaderDateBr(date: LocalDate = today()): String {
        val formatted = date.format(brShortHeaderFormatter).replace(".", "")
        return if (date == today()) "Hoje, $formatted" else formatted
    }

    /**
     * Normalizes raw user typing into a Brazilian `DD/MM/AAAA` mask while typing.
     * If the user types `15052020` or `15/05/2020`, it formats progressively as `15/05/2020`.
     * Also preserves ISO strings if pasted directly by converting them to `DD/MM/AAAA`.
     */
    fun normalizeBrDateInput(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            val isoParsed = runCatching { parseLocalDate(trimmed, strictIsoDateFormatter) }.getOrNull()
            if (isoParsed != null) return formatDateBr(isoParsed)
        }

        val digits = trimmed.filter { it.isDigit() }.take(8)
        return buildString {
            for (i in digits.indices) {
                append(digits[i])
                if ((i == 1 || i == 3) && i < digits.lastIndex) {
                    append('/')
                }
            }
        }
    }

    /**
     * Parses a date string from either Brazilian format (`DD/MM/AAAA`), 8 raw digits (`DDMMAAAA`),
     * or ISO format (`AAAA-MM-DD`), using strict calendar rules (rejects `31/02/2024` or `29/02/2023`,
     * accepts `29/02/2024`).
     */
    fun parseDate(input: String?): LocalDate? {
        if (input.isNullOrBlank()) return null
        val cleaned = input.trim()

        // 1. Standard DD/MM/AAAA
        if (cleaned.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$"))) {
            return runCatching { parseLocalDate(cleaned, strictBrDateFormatter) }.getOrNull()
        }

        // 2. Raw 8 digits DDMMAAAA
        val digitsOnly = cleaned.filter { it.isDigit() }
        if (digitsOnly.length == 8 && !cleaned.contains("-")) {
            val formatted = "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.substring(4, 8)}"
            return runCatching { parseLocalDate(formatted, strictBrDateFormatter) }.getOrNull()
        }

        // 3. ISO AAAA-MM-DD fallback
        if (cleaned.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return runCatching { parseLocalDate(cleaned, strictIsoDateFormatter) }.getOrNull()
        }

        return null
    }

    /**
     * Validates a birth date string in `DD/MM/AAAA` (or ISO) against clinical rules.
     */
    fun validateBirthDate(
        input: String,
        referenceDate: LocalDate = today(),
        maxAgeYears: Long = 120L
    ): DateValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return DateValidationResult.Invalid(
                DateErrorReason.EMPTY,
                "A data de nascimento é obrigatória (DD/MM/AAAA)."
            )
        }
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length < 8 && !trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return DateValidationResult.Invalid(
                DateErrorReason.INCOMPLETE,
                "Digite a data completa no formato DD/MM/AAAA."
            )
        }
        val parsed = parseDate(trimmed)
            ?: return DateValidationResult.Invalid(
                DateErrorReason.INVALID_CALENDAR_DATE,
                "Data inválida no calendário. Verifique dia, mês e ano (DD/MM/AAAA)."
            )

        if (parsed.isAfter(referenceDate)) {
            return DateValidationResult.Invalid(
                DateErrorReason.FUTURE_DATE_NOT_ALLOWED,
                "A data de nascimento não pode ser uma data futura."
            )
        }
        if (parsed.isBefore(referenceDate.minusYears(maxAgeYears))) {
            return DateValidationResult.Invalid(
                DateErrorReason.PAST_DATE_TOO_OLD,
                "Data de nascimento anterior ao limite clínico permitido ($maxAgeYears anos)."
            )
        }

        return DateValidationResult.Valid(parsed, formatDateBr(parsed))
    }

    /**
     * Validates any clinical event/session/assessment date.
     */
    fun validateClinicalDate(
        input: String,
        allowFuture: Boolean = true,
        minDate: LocalDate? = null,
        referenceDate: LocalDate = today()
    ): DateValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return DateValidationResult.Invalid(
                DateErrorReason.EMPTY,
                "A data é obrigatória (DD/MM/AAAA)."
            )
        }
        val parsed = parseDate(trimmed)
            ?: return DateValidationResult.Invalid(
                DateErrorReason.INVALID_CALENDAR_DATE,
                "Data inválida. Use o formato brasileiro DD/MM/AAAA."
            )
        if (!allowFuture && parsed.isAfter(referenceDate)) {
            return DateValidationResult.Invalid(
                DateErrorReason.FUTURE_DATE_NOT_ALLOWED,
                "A data não pode ser futura."
            )
        }
        if (minDate != null && parsed.isBefore(minDate)) {
            return DateValidationResult.Invalid(
                DateErrorReason.BEFORE_MIN_DATE,
                "A data não pode ser anterior a ${formatDateBr(minDate)}."
            )
        }
        return DateValidationResult.Valid(parsed, formatDateBr(parsed))
    }

    fun calculateAge(birthDate: LocalDate, referenceDate: LocalDate = today()): DetailedAge {
        if (referenceDate.isBefore(birthDate)) return DetailedAge(0, 0, 0)
        return dateDifference(birthDate, referenceDate)
    }

    fun toStartOfDayInstant(date: LocalDate): Instant =
        startOfDay(date, clockZone(clock))

    fun toLocalDate(instant: Instant): LocalDate =
        localDateAt(instant, clockZone(clock))

    fun isWithinPeriod(instant: Instant, startDate: LocalDate?, endDate: LocalDate?): Boolean {
        val date = toLocalDate(instant)
        if (startDate != null && date.isBefore(startDate)) return false
        if (endDate != null && date.isAfter(endDate)) return false
        return true
    }
}
