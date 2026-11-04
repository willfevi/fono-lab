package com.healthcare.fonolab.domain.common

/** Difficulty used by goals and game configurations. */
enum class DifficultyLevel { EASY, MEDIUM, HARD }

/** Position of a phoneme inside a word. */
enum class PhonemePosition { INITIAL, MEDIAL, FINAL }

/** How a game presents its instruction to the patient. */
enum class PresentationMode { TEXT, AUDIO, TEXT_AND_AUDIO;

    val usesAudio: Boolean get() = this != TEXT
    val usesText: Boolean get() = this != AUDIO
}
