package com.healthcare.fonolab.domain.datetime.wasm

class Locale(val language: String, val country: String = "") {
    companion object {
        fun forLanguageTag(tag: String): Locale {
            val parts = tag.split("-")
            return if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
        }

        fun getDefault(): Locale = Locale("pt", "BR")
    }

    override fun toString(): String = if (country.isEmpty()) language else "${language}_$country"
}
