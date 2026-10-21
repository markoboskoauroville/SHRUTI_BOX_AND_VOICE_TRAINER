package com.mantra.shrutibox

// THE PRESETS of Marko's web shruti box (reference-v2-layout.html), every card of its five tabs, in valves from Sa
// (0) to the upper Sa (12) so they follow the key. Like Reeds.kt this file imports nothing from Android: Test 1 checks
// every preset on a plain JVM.

/** One card: a name, a level when the page gives one, what it is, where it is used, and the valves it opens. */
class Preset(val name: String, val level: String, val about: String, val usedIn: String, val valves: List<Int>)

class PresetTab(val title: String, val intro: String, val presets: List<Preset>)

object Presets {
    /** The page writes its notes in C: C to B and C2 for the upper C, which are valves 0 to 12 here. */
    private val NOTE = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C2")
    fun valves(notes: String): List<Int> = notes.split(",").map { n -> NOTE.indexOf(n.trim()).also { require(it >= 0) { "no note $n" } } }

    private fun p(name: String, level: String, about: String, usedIn: String, notes: String) = Preset(name, level, about, usedIn, valves(notes))

    val TABS = listOf(
        PresetTab(
            "BASIC", "The foundations of the drone in Indian classical music. Sa-Ga is not traditional, kept for learning.",
            listOf(
                p("Sa Pa", "beginner", "The most used drone: a perfect fifth.", "Most ragas, classical, folk and devotional music.", "C,G"),
                p("Sa ma", "beginner", "A perfect fourth. Stable and grounding.", "Ragas where Ma leads, like Bageshri and Malkauns.", "C,F"),
                p("Sa Sa'", "beginner", "The octave. Simple, and enough for the voice.", "First vocal exercises, simple melodies.", "C,C2"),
                p("Sa Pa Sa'", "beginner", "The traditional drone: stable and full.", "Most classical concerts, the standard tanpura tuning.", "C,G,C2"),
                p("Sa Ga", "beginner", "A major third, bright and uplifting. Not traditional for a drone.", "Light classical music in a major mood.", "C,E"),
            )
        ),
        PresetTab(
            "MIDDLE", "More colour in the drone. Some are not traditional, kept for learning.",
            listOf(
                p("Sa ma Pa", "intermediate", "Richer: the fourth and the fifth together.", "Ragas where Ma leads, like Bageshri and Malkauns.", "C,F,G"),
                p("Sa Ma Pa", "intermediate", "With the sharp (tivra) Ma.", "Yaman, Puriya, Marwa.", "C,F#,G"),
                p("Sa Ni Sa'", "intermediate", "Tension and release on the leading note Ni. Not traditional.", "Ragas with a strong Ni, like Yaman.", "C,B,C2"),
                p("Sa ni Sa'", "intermediate", "With the flat (komal) Ni. Not traditional.", "Ragas like Bhairavi.", "C,A#,C2"),
                p("Sa ga Pa", "intermediate", "A minor third: a more melancholic sound. Not traditional.", "Ragas with komal Ga, like Bhairavi.", "C,D#,G"),
                p("Sa Pa Ga", "intermediate", "A major third for a brighter tone. Not traditional.", "Ragas of a major character.", "C,G,E"),
            )
        ),
        PresetTab(
            "ADVANCED", "Complex drones for particular moods and ragas. Many are not traditional, kept for learning.",
            listOf(
                p("Sa Pa ni Sa'", "advanced", "The fifth and the minor seventh: rich. Not traditional.", "Complex ragas of the Bhairavi family.", "C,G,A#,C2"),
                p("Sa re Pa", "advanced", "Tension of the minor second, komal Re. Not traditional.", "Ragas like Shree.", "C,C#,G"),
                p("Sa Pa Dha Sa'", "advanced", "The major sixth for a richer texture. Not traditional.", "Light classical forms.", "C,G,A,C2"),
                p("Sa ga ma ni", "advanced", "A drone for a particular mood. Not traditional.", "The Bhairavi family.", "C,D#,F,A#"),
                p("Sa Re Pa Ni", "advanced", "Bright and complex, natural Re and Ni. Not traditional.", "The Kalyan family.", "C,D,G,B"),
                p("Sa Ma Dha Sa'", "advanced", "The Lydian sound of the sharp Ma. Not traditional.", "Puriya, Marwa.", "C,F#,A,C2"),
            )
        ),
        PresetTab(
            "RAGAS", "The drone each raga is usually sung over.",
            listOf(
                p("Yaman", "", "Evening. Devotion and peace.", "Sa Pa Sa'", "C,G,C2"),
                p("Kalyan", "", "Evening. Majestic, bright.", "Sa Pa Sa'", "C,G,C2"),
                p("Bhairav", "", "Early morning. Serenity and devotion.", "Sa Pa Sa'", "C,G,C2"),
                p("Bhairavi", "", "Morning. Deep feeling.", "Sa Pa Sa'", "C,G,C2"),
                p("Darbari", "", "Late night. Deep and serene.", "Sa Pa Sa'", "C,G,C2"),
                p("Bhimpalasi", "", "Afternoon. Romantic.", "Sa Pa Sa'", "C,G,C2"),
                p("Puriya", "", "Evening. Tense, dramatic.", "Sa Pa Sa'", "C,G,C2"),
                p("Bihag", "", "Night. Romantic, joyful.", "Sa Pa Sa'", "C,G,C2"),
                p("Bageshri", "", "Late night. Longing.", "Sa ma Sa'", "C,F,C2"),
                p("Shree", "", "Sunset. Deep and serene.", "Sa Pa Sa'", "C,G,C2"),
                p("Durga", "", "Evening. Brave and strong.", "Sa Pa Sa'", "C,G,C2"),
                p("Malkauns", "", "Late night. Deep and intense.", "Sa ma Sa'", "C,F,C2"),
            )
        ),
        PresetTab(
            "CHORDS", "Western chords as drones. Named here in C; they move with the key.",
            listOf(
                p("Major", "", "The basic major chord, bright and happy.", "Sa Ga Pa", "C,E,G"),
                p("Minor", "", "Sad, melancholic: the minor third.", "Sa ga Pa", "C,D#,G"),
                p("Major on Pa", "", "The dominant: it creates tension.", "Pa Ni Re", "G,B,D"),
                p("Major on ma", "", "The subdominant: a warm sound.", "ma Dha Sa", "F,A,C"),
                p("Minor on Dha", "", "The relative minor. Wistful.", "Dha Sa Ga", "A,C,E"),
                p("Minor on Re", "", "Used in many melancholic progressions.", "Re ma Dha", "D,F,A"),
                p("Sus4", "", "Neither major nor minor: tension.", "Sa ma Pa", "C,F,G"),
                p("Sus4 on Pa", "", "Open, common in folk music.", "Pa Sa Re", "G,C,D"),
                p("Sus2", "", "Bright and open, unresolved.", "Sa Re Pa", "C,D,G"),
                p("Major 7th", "", "Jazzy, refined: the seventh degree.", "Sa Ga Pa Ni", "C,E,G,B"),
                p("Minor 7th", "", "Mellow, jazz and R&B.", "Sa ga Pa ni", "C,D#,G,A#"),
                p("Dominant 7th", "", "Tension that wants to resolve to Sa.", "Pa Ni Re ma", "G,B,D,F"),
            )
        ),
    )
}
