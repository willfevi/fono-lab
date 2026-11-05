package java.time.format

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

enum class ResolverStyle {
    STRICT, SMART, LENIENT
}

enum class TextStyle {
    FULL, SHORT, NARROW
}

class DateTimeFormatter private constructor(val pattern: String) {

    fun withResolverStyle(style: ResolverStyle): DateTimeFormatter = this

    fun format(date: LocalDate): String {
        return when (pattern) {
            "dd/MM/uuuu", "dd/MM/yyyy" -> {
                val d = date.dayOfMonth.toString().padStart(2, '0')
                val m = date.monthValue.toString().padStart(2, '0')
                val y = date.year.toString().padStart(4, '0')
                "$d/$m/$y"
            }
            "uuuu-MM-dd", "yyyy-MM-dd" -> {
                val d = date.dayOfMonth.toString().padStart(2, '0')
                val m = date.monthValue.toString().padStart(2, '0')
                val y = date.year.toString().padStart(4, '0')
                "$y-$m-$d"
            }
            "dd 'de' MMM" -> {
                val d = date.dayOfMonth.toString().padStart(2, '0')
                val mName = monthShortNamePtBr(date.monthValue)
                "$d de $mName"
            }
            else -> {
                val d = date.dayOfMonth.toString().padStart(2, '0')
                val m = date.monthValue.toString().padStart(2, '0')
                val y = date.year.toString().padStart(4, '0')
                "$d/$m/$y"
            }
        }
    }

    fun format(dateTime: LocalDateTime): String {
        val d = dateTime.dayOfMonth.toString().padStart(2, '0')
        val m = dateTime.monthValue.toString().padStart(2, '0')
        val y = dateTime.year.toString().padStart(4, '0')
        val hh = dateTime.hour.toString().padStart(2, '0')
        val mm = dateTime.minute.toString().padStart(2, '0')
        return when (pattern) {
            "HH:mm" -> "$hh:$mm"
            "dd/MM/uuuu HH:mm", "dd/MM/yyyy HH:mm" -> "$d/$m/$y $hh:$mm"
            else -> "$d/$m/$y $hh:$mm"
        }
    }

    fun parseLocalDate(text: String): LocalDate {
        val trimmed = text.trim()
        if (pattern.contains("/")) {
            val parts = trimmed.split("/")
            require(parts.size == 3) { "Data inválida: $text" }
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            return LocalDate.of(year, month, day)
        } else if (pattern.contains("-")) {
            val parts = trimmed.split("-")
            require(parts.size == 3) { "Data inválida: $text" }
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            return LocalDate.of(year, month, day)
        }
        return LocalDate.parse(trimmed)
    }

    companion object {
        fun ofPattern(pattern: String, locale: Locale? = null): DateTimeFormatter = DateTimeFormatter(pattern)

        private fun monthShortNamePtBr(month: Int): String = when (month) {
            1 -> "jan"
            2 -> "fev"
            3 -> "mar"
            4 -> "abr"
            5 -> "mai"
            6 -> "jun"
            7 -> "jul"
            8 -> "ago"
            9 -> "set"
            10 -> "out"
            11 -> "nov"
            12 -> "dez"
            else -> ""
        }
    }
}
