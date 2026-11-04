package com.healthcare.fonolab.feature.games

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.game.GameDefinition
import com.healthcare.fonolab.domain.game.GameMechanic
import com.healthcare.fonolab.domain.goal.ClinicalArea

object GameCatalogRegistry {

    val games: List<GameDefinition> = listOf(
        GameDefinition(
            id = "sound_hunt",
            title = "Caça ao Som",
            description = "Discriminação e identificação de fonemas e estímulos auditivos com alternativas visuais.",
            clinicalArea = ClinicalArea.SPEECH_AND_PHONOLOGY,
            targetSkills = listOf("Discriminação Auditiva", "Identificação de Fonema", "Atenção Auditiva"),
            indicativeAgeRange = "3 a 8 anos",
            estimatedDurationMinutes = 5,
            mechanic = GameMechanic.AUDITORY_DISCRIMINATION,
            isConfigurable = true,
            iconEmoji = "🎯"
        ),
        GameDefinition(
            id = "space_mission",
            title = "Missão Espacial",
            description = "Compreensão verbal, seguimento de instruções espaciais e vocabulário semântico no espaço.",
            clinicalArea = ClinicalArea.RECEPTIVE_LANGUAGE,
            targetSkills = listOf("Compreensão Verbal", "Seguimento de Instruções", "Relações Espaciais"),
            indicativeAgeRange = "4 a 10 anos",
            estimatedDurationMinutes = 7,
            mechanic = GameMechanic.RECEPTIVE_IDENTIFICATION,
            isConfigurable = true,
            iconEmoji = "🚀"
        ),
        GameDefinition(
            id = "story_sequence",
            title = "História Embaralhada",
            description = "Ordenação de cenas temporais, narrativa estruturada e estimulação da linguagem expressiva.",
            clinicalArea = ClinicalArea.NARRATIVE_AND_SEQUENCING,
            targetSkills = listOf("Sequenciamento Temporal", "Estrutura Narrativa", "Expressão Oral"),
            indicativeAgeRange = "4 a 12 anos",
            estimatedDurationMinutes = 8,
            mechanic = GameMechanic.TEMPORAL_SEQUENCING,
            isConfigurable = true,
            iconEmoji = "📖"
        ),
        GameDefinition(
            id = "hungry_monster",
            title = "Monstrinho Faminto",
            description = "Nomeação e produção de fala com feedback lúdico do monstrinho e avaliação rápida do fonoaudiólogo.",
            clinicalArea = ClinicalArea.EXPRESSIVE_LANGUAGE,
            targetSkills = listOf("Nomeação Rápida", "Articulação de Fonemas", "Repetição"),
            indicativeAgeRange = "3 a 9 anos",
            estimatedDurationMinutes = 6,
            mechanic = GameMechanic.SPEECH_PRODUCTION_NAMING,
            isConfigurable = true,
            iconEmoji = "👾"
        ),
        GameDefinition(
            id = "word_factory",
            title = "Fábrica de Palavras",
            description = "Consciência fonológica: rimas, som inicial, segmentação silábica e montagem de palavras.",
            clinicalArea = ClinicalArea.PHONOLOGICAL_AWARENESS,
            targetSkills = listOf("Rimas", "Aliteração / Som Inicial", "Segmentação Silábica"),
            indicativeAgeRange = "5 a 10 anos",
            estimatedDurationMinutes = 8,
            mechanic = GameMechanic.PHONOLOGICAL_MANIPULATION,
            isConfigurable = true,
            iconEmoji = "🏭"
        )
    )

    fun getById(id: String): GameDefinition? = games.firstOrNull { it.id == id }
}
