package com.artswipe.domain.util

object StyleEngine {
    fun getPersonality(style: String): Pair<String, String> {
        return when (style.lowercase()) {
            "impressionism" -> "The Dreamer" to "You find beauty in the fleeting moments and the play of light."
            "baroque" -> "The Dramatist" to "You are drawn to grand scales, intense emotions, and theatrical contrast."
            "modernism" -> "The Visionary" to "You appreciate progress, bold forms, and the breaking of tradition."
            "surrealism" -> "The Wanderer" to "You love the illogical, the dreamlike, and the depth of the subconscious."
            "realism" -> "The Grounded" to "You value truth, authenticity, and the beauty of everyday life."
            "abstract" -> "The Free Spirit" to "You see beyond the surface, finding meaning in color, shape, and emotion."
            "renaissance" -> "The Classicist" to "You admire balance, harmony, and the timeless pursuit of human perfection."
            else -> "The Explorer" to "You are just beginning your journey into the world of art discovery."
        }
    }

    fun getStyleDescription(style: String): String {
        return when (style.lowercase()) {
            "impressionism" -> "Impressionism is a 19th-century art movement characterized by relatively small, thin, yet visible brush strokes, open composition, and emphasis on accurate depiction of light in its changing qualities."
            "baroque" -> "The Baroque is a highly ornate and often extravagant style of architecture, music, dance, painting, sculpture and other arts that flourished in Europe from the early 17th century until the 1740s."
            "modernism" -> "Modernism is both a philosophical movement and an art movement that, along with cultural trends and changes, arose from wide-scale and far-reaching transformations in Western society during the late 19th and early 20th centuries."
            "surrealism" -> "Surrealism is a cultural movement that started in 1917, and is best known for its visual artworks and writings. Artists painted unnerving, illogical scenes with photographic precision, creating strange creatures from everyday objects."
            "realism" -> "Realism was an artistic movement that began in France in the 1850s, after the 1848 Revolution. Realists rejected Romanticism, which had dominated French literature and art since the late 18th century."
            "abstract" -> "Abstract art uses visual language of shape, form, color and line to create a composition which may exist with a degree of independence from visual references in the world."
            "renaissance" -> "The Renaissance was a fervent period of European cultural, artistic, political and economic 'rebirth' following the Middle Ages. Generally described as taking place from the 14th century to the 17th century."
            else -> "A unique art style that contributes to the rich history of human expression."
        }
    }
}
