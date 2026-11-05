package com.healthcare.fonolab.feature.games

import com.healthcare.fonolab.domain.game.GameDefinition
import com.healthcare.fonolab.domain.game.GameMechanic
import com.healthcare.fonolab.domain.goal.ClinicalArea

object GameCatalogRegistry {

    val games: List<GameDefinition> = listOf(
        GameDefinition(
            id = "sound_hunt",
            title = "Caça ao Som",
            description = "Discriminação e identificação de fonemas e estímulos auditivos em posição inicial, medial e final.",
            clinicalArea = ClinicalArea.SPEECH_AND_PHONOLOGY,
            targetSkills = listOf("Discriminação Auditiva", "Identificação de Fonema", "Atenção Auditiva"),
            indicativeAgeRange = "3 a 8 anos",
            estimatedDurationMinutes = 5,
            mechanic = GameMechanic.AUDITORY_DISCRIMINATION,
            isConfigurable = true,
            iconEmoji = "🎯"
        ),
        GameDefinition(
            id = "hungry_monster",
            title = "Monstrinho Faminto",
            description = "Nomeação rápida e produção articulatória de palavras-alvo com avaliação clínica e hierarquia de pistas.",
            clinicalArea = ClinicalArea.EXPRESSIVE_LANGUAGE,
            targetSkills = listOf("Nomeação Rápida", "Articulação de Fonemas", "Repetição Dirigida"),
            indicativeAgeRange = "3 a 9 anos",
            estimatedDurationMinutes = 6,
            mechanic = GameMechanic.SPEECH_PRODUCTION_NAMING,
            isConfigurable = true,
            iconEmoji = "👾"
        ),
        GameDefinition(
            id = "word_factory",
            title = "Fábrica de Palavras",
            description = "Consciência fonológica: síntese, segmentação silábica, som inicial e manipulação de sílabas.",
            clinicalArea = ClinicalArea.PHONOLOGICAL_AWARENESS,
            targetSkills = listOf("Segmentação Silábica", "Aliteração / Som Inicial", "Síntese Fonológica"),
            indicativeAgeRange = "5 a 10 anos",
            estimatedDurationMinutes = 8,
            mechanic = GameMechanic.PHONOLOGICAL_MANIPULATION,
            isConfigurable = true,
            iconEmoji = "🏭"
        ),
        GameDefinition(
            id = "story_sequence",
            title = "História Embaralhada",
            description = "Ordenação lógica e temporal de cenas, coerência narrativa e estimulação do reconto oral.",
            clinicalArea = ClinicalArea.NARRATIVE_AND_SEQUENCING,
            targetSkills = listOf("Sequenciamento Temporal", "Estrutura Narrativa", "Expressão Oral"),
            indicativeAgeRange = "4 a 12 anos",
            estimatedDurationMinutes = 8,
            mechanic = GameMechanic.TEMPORAL_SEQUENCING,
            isConfigurable = true,
            iconEmoji = "📖"
        ),
        GameDefinition(
            id = "space_mission",
            title = "Missão Espacial",
            description = "Compreensão verbal, seguimento de instruções progressivas e relações espaciais.",
            clinicalArea = ClinicalArea.RECEPTIVE_LANGUAGE,
            targetSkills = listOf("Compreensão Verbal", "Seguimento de Instruções", "Relações Espaciais"),
            indicativeAgeRange = "4 a 10 anos",
            estimatedDurationMinutes = 7,
            mechanic = GameMechanic.RECEPTIVE_IDENTIFICATION,
            isConfigurable = true,
            iconEmoji = "🚀"
        ),
        GameDefinition(
            id = "rhyme_detective",
            title = "Detetive das Rimas",
            description = "Consciência fonológica e correspondência sonora final por meio da investigação de pares que rimam.",
            clinicalArea = ClinicalArea.PHONOLOGICAL_AWARENESS,
            targetSkills = listOf("Identificação de Rimas", "Consciência Fonológica", "Memória Fonológica"),
            indicativeAgeRange = "4 a 9 anos",
            estimatedDurationMinutes = 6,
            mechanic = GameMechanic.PHONOLOGICAL_MANIPULATION,
            isConfigurable = true,
            iconEmoji = "🕵️"
        ),
        GameDefinition(
            id = "magic_mouth",
            title = "Boca Mágica",
            description = "Treino articulatório e fonético com dicas visuais de ponto e modo articulatório e registro de pistas.",
            clinicalArea = ClinicalArea.OROFACIAL_MYOFUNCTIONAL,
            targetSkills = listOf("Ponto e Modo Articulatório", "Estimulabilidade Fonética", "Praxias e Fonemas"),
            indicativeAgeRange = "3 a 11 anos",
            estimatedDurationMinutes = 7,
            mechanic = GameMechanic.SPEECH_PRODUCTION_NAMING,
            isConfigurable = true,
            iconEmoji = "👄"
        ),
        GameDefinition(
            id = "word_market",
            title = "Mercado das Palavras",
            description = "Expansão de vocabulário receptivo e expressivo por meio de categorização e associação semântica.",
            clinicalArea = ClinicalArea.VOCABULARY_AND_SEMANTICS,
            targetSkills = listOf("Categorização Semântica", "Vocabulário Ativo", "Associação Lexical"),
            indicativeAgeRange = "3 a 10 anos",
            estimatedDurationMinutes = 7,
            mechanic = GameMechanic.RECEPTIVE_IDENTIFICATION,
            isConfigurable = true,
            iconEmoji = "🛒"
        ),
        GameDefinition(
            id = "sentence_detective",
            title = "Detetive das Frases",
            description = "Estruturação morfossintática de frases (sujeito + verbo + complemento) com apoio visual.",
            clinicalArea = ClinicalArea.MORPHOSYNTAX,
            targetSkills = listOf("Morfossintaxe", "Construção Frasal", "Concordância Verbal/Nominal"),
            indicativeAgeRange = "4 a 11 anos",
            estimatedDurationMinutes = 8,
            mechanic = GameMechanic.PHONOLOGICAL_MANIPULATION,
            isConfigurable = true,
            iconEmoji = "🧩"
        ),
        GameDefinition(
            id = "sound_memory",
            title = "Memória Sonora",
            description = "Percepção auditiva, associação som-imagem (onomatopeias) e memória sequencial auditiva.",
            clinicalArea = ClinicalArea.MEMORY_AND_ATTENTION,
            targetSkills = listOf("Memória de Trabalho Auditiva", "Associação Som-Imagem", "Sequência Auditiva"),
            indicativeAgeRange = "3 a 10 anos",
            estimatedDurationMinutes = 6,
            mechanic = GameMechanic.AUDITORY_DISCRIMINATION,
            isConfigurable = true,
            iconEmoji = "🔔"
        )
    )

    fun getById(id: String): GameDefinition? = games.firstOrNull { it.id == id }
}
