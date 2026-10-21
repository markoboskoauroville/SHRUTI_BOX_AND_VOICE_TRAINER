package com.mantra.shrutibox

// THE PRESETS (v7, Marko: "do the ground truth and check my app for truth and precision"). Rebuilt from sources, not
// from the reference page, whose drone combinations stacked colour notes no tradition tunes (checked 21.10.2026):
//   - the tanpura tunes ONE string away from Sa: Pa as a rule; shuddha ma when Pa is omitted or ma rules; shuddha Ni
//     for the Pa-less Marwa family; rarely Dha (Wikipedia "Tanpura"; sangtar.com/2007/05/tanpura; ragasphere.com
//     "The tanpura"; H. Bakshi's tuning guide at tosslevy.nl). A box keeps that: Sa, Sa' and the one tuning note.
//   - each raga's thaat, aroha / avaroha, time and mood: its Wikipedia article and ragajunglism.org/ragas/<name>,
//     whose "Tanpura" line names the tuning; where they disagree the card says so.
// Like Reeds.kt this file imports nothing from Android: Test 1 checks every preset, and that no raga's drone sounds a
// note the raga does not have.

/**
 * One card. [valves] is what it opens (0 = Sa … 12 = Sa'). [scale] is the raga's notes, for the test; empty for a
 * drone or a chord. [level] is the small line under the name, [about] what it is, [usedIn] where it is used.
 */
class Preset(
    val name: String, val level: String, val about: String, val usedIn: String, val valves: List<Int>,
    val scale: List<Int> = emptyList()
)

class PresetTab(val title: String, val intro: String, val presets: List<Preset>)

object Presets {
    /** The page writes its notes in C: C to B and C2 for the upper C, which are valves 0 to 12 here. */
    private val NOTE = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C2")
    fun valves(notes: String): List<Int> = notes.split(",").map { n -> NOTE.indexOf(n.trim()).also { require(it >= 0) { "no note $n" } } }

    /** The drone of each tuning, as a box plays it. */
    val PA = listOf(0, 7, 12)
    val MA = listOf(0, 5, 12)
    val NI = listOf(0, 11, 12)

    private fun p(name: String, level: String, about: String, usedIn: String, notes: String) = Preset(name, level, about, usedIn, valves(notes))
    private fun raga(name: String, thaat: String, time: String, mood: String, up: String, down: String, drone: List<Int>, scale: List<Int>) =
        Preset(name, "$thaat · $time".uppercase(), mood, "↑ $up   ↓ $down", drone, scale)

    val TABS = listOf(
        PresetTab(
            "DRONES", "A tanpura tunes one string away from Sa: Pa as a rule, ma or Ni when the raga has no Pa. These are those tunings.",
            listOf(
                Preset("Pa tuning", "THE STANDARD", "Sa Pa Sa': the tanpura's Pa-sa-sa-Sa.", "Every raga that has Pa: Yaman, Bhairav, Bhairavi, Darbari, Bihag…", PA),
                Preset("ma tuning", "WHEN PA IS MISSING OR WEAK", "Sa ma Sa': the tanpura's Ma-sa-sa-Sa.", "Malkauns (no Pa), Bageshri (ma rules).", MA),
                Preset("Ni tuning", "THE MARWA FAMILY", "Sa Ni Sa': the tanpura's Ni-sa-sa-Sa. Ni beats against Sa'; that is its colour.", "Marwa, Puriya, Sohini: no Pa, no shuddha ma.", NI),
                Preset("Dha tuning", "RARE", "Sa Dha Sa': the less common tuning to shuddha Dha.", "An alternative for Marwa; Hindol (Wikipedia).", listOf(0, 9, 12)),
                Preset("Pa and Ni", "FIVE-STRING TANPURA", "Sa Pa Ni Sa': Pa-Ni-sa-sa-Sa, the fifth string on Ni.", "The Kalyan family: Yaman, Shuddh Kalyan, Bihag.", listOf(0, 7, 11, 12)),
                Preset("ma and Ni", "FIVE-STRING TANPURA", "Sa ma Ni Sa': Ma-Ni-sa-sa-Sa.", "A ma tuning with Ni's colour.", listOf(0, 5, 11, 12)),
                Preset("Sa Pa", "THE FIFTH", "Sa and Pa alone: the plainest drone.", "Any raga with Pa; voice practice.", listOf(0, 7)),
                Preset("Sa Sa'", "THE OCTAVE", "Sa and Sa' alone: no tuning note at all.", "Not a raga's tuning: a neutral ground for any practice.", listOf(0, 12)),
            )
        ),
        PresetTab(
            "RAGAS", "Each raga's drone, as its tanpura is tuned. Under it the raga's thaat and time, its mood, and its notes going up ↑ and down ↓.",
            listOf(
                raga("Yaman", "Kalyan", "first night", "Serene, devotional. Kalyan of old.", "Ni Re Ga Ma Dha Ni Sa'", "Sa' Ni Dha Pa Ma Ga Re Sa", PA, listOf(0, 2, 4, 6, 7, 9, 11)),
                raga("Shuddh Kalyan", "Kalyan", "evening", "Ma and Ni only coming down.", "Sa Re Ga Pa Dha Sa'", "Sa' Ni Dha Pa Ma Ga Re Sa", PA, listOf(0, 2, 4, 6, 7, 9, 11)),
                raga("Bhairav", "Bhairav", "dawn", "Grave, serene, devotional.", "Sa re Ga ma Pa dha Ni Sa'", "Sa' Ni dha Pa ma Ga re Sa", PA, listOf(0, 1, 4, 5, 7, 8, 11)),
                raga("Ahir Bhairav", "Bhairav", "early morning", "Early morning.", "Sa re Ga ma Pa Dha ni Sa'", "Sa' ni Dha Pa ma Ga re Sa", PA, listOf(0, 1, 4, 5, 7, 9, 10)),
                raga("Bhairavi", "Bhairavi", "morning; a concert's close", "Devotional, deep.", "Sa re ga ma Pa dha ni Sa'", "Sa' ni dha Pa ma ga re Sa", PA, listOf(0, 1, 3, 5, 7, 8, 10)),
                raga("Todi", "Todi", "late morning", "Pensive, plaintive. Miyan ki Todi.", "Sa re ga Ma dha Ni Sa'", "Sa' Ni dha Pa Ma ga re Sa", PA, listOf(0, 1, 3, 6, 7, 8, 11)),
                raga("Bhimpalasi", "Kafi", "afternoon", "Tuned to Pa; some tune to ma, its vadi.", "ni Sa ga ma Pa ni Sa'", "Sa' ni Dha Pa ma ga Re Sa", PA, listOf(0, 2, 3, 5, 7, 9, 10)),
                raga("Shree", "Purvi", "sunset", "Grave, majestic.", "Sa re Ma Pa Ni Sa'", "Sa' Ni dha Pa Ma Ga re Sa", PA, listOf(0, 1, 4, 6, 7, 8, 11)),
                raga("Puriya", "Marwa", "sunset", "Serious, peaceful. No Pa: tuned to Ni.", "Ni re Ga Ma Dha Ni re Sa'", "Sa' Ni Dha Ma Ga re Sa", NI, listOf(0, 1, 4, 6, 9, 11)),
                raga("Marwa", "Marwa", "sunset", "Longing, austere. No Pa: tuned to Ni (some to Dha).", "Dha Ni re Ga Ma Dha Ni re Sa'", "re' Ni Dha Ma Ga re Sa", NI, listOf(0, 1, 4, 6, 9, 11)),
                raga("Sohini", "Marwa", "before dawn", "No Pa: tuned to Ni. Its mood: sources differ.", "Sa Ga Ma Dha Ni Sa'", "Sa' Ni Dha Ma Ga re Sa", NI, listOf(0, 1, 4, 6, 9, 11)),
                raga("Bhoopali", "Kalyan", "first night", "Peaceful, devotional. Five notes.", "Sa Re Ga Pa Dha Sa'", "Sa' Dha Pa Ga Re Sa", PA, listOf(0, 2, 4, 7, 9)),
                raga("Hamsadhwani", "Bilawal", "evening", "From the Carnatic south. Five notes.", "Sa Re Ga Pa Ni Sa'", "Sa' Ni Pa Ga Re Sa", PA, listOf(0, 2, 4, 7, 11)),
                raga("Durga", "Bilawal", "night", "Five notes. Sources differ: love, or heroic.", "Sa Re ma Pa Dha Sa'", "Sa' Dha Pa ma Re Sa", PA, listOf(0, 2, 5, 7, 9)),
                raga("Kedar", "Kalyan", "first night", "Reverent; both ma and Ma.", "Sa ma, ma Pa, Dha Pa, Ni Dha Sa'", "Sa' Ni Dha Pa, Ma Pa Dha Pa, ma Re Sa", PA, listOf(0, 2, 4, 5, 6, 7, 9, 11)),
                raga("Bihag", "Bilawal", "night", "Ma only in Pa Ma Ga ma Ga.", "Ni Sa Ga ma Pa Ni Sa'", "Sa' Ni Dha Pa, Ma Pa Ga ma Ga Re Sa", PA, listOf(0, 2, 4, 5, 6, 7, 9, 11)),
                raga("Khamaj", "Khamaj", "night", "Romantic; Ni going up, ni coming down.", "Sa Ga ma Pa Dha Ni Sa'", "Sa' ni Dha Pa ma Ga Re Sa", PA, listOf(0, 2, 4, 5, 7, 9, 10, 11)),
                raga("Desh", "Khamaj", "night", "Sweet, romantic.", "Sa Re ma Pa Ni Sa'", "Sa' ni Dha Pa ma Ga Re Ga Sa", PA, listOf(0, 2, 4, 5, 7, 9, 10, 11)),
                raga("Kafi", "Kafi", "evening", "Love (shringar).", "Sa Re ga ma Pa Dha ni Sa'", "Sa' ni Dha Pa ma ga Re Sa", PA, listOf(0, 2, 3, 5, 7, 9, 10)),
                raga("Bageshri", "Kafi", "midnight", "Waiting for the beloved. ma rules: tuned to ma.", "Sa ga ma Dha ni Sa'", "Sa' ni Dha ma Pa Dha ga ma ga Re Sa", MA, listOf(0, 2, 3, 5, 7, 9, 10)),
                raga("Darbari", "Asavari", "midnight", "Grave, serious. Darbari Kanada.", "Sa Re ga ma Pa dha ni Sa'", "Sa' dha ni Pa, ma Pa ga ma Re Sa", PA, listOf(0, 2, 3, 5, 7, 8, 10)),
                raga("Malkauns", "Bhairavi", "after midnight", "Serious, meditative. No Re, no Pa: tuned to ma.", "Sa ga ma dha ni Sa'", "Sa' ni dha ma ga Sa", MA, listOf(0, 3, 5, 8, 10)),
                raga("Jog", "Kafi or Khamaj", "late evening", "Both Ga and ga. Its thaat is disputed.", "Sa Ga ma Pa ni Sa'", "Sa' ni Pa ma Ga ma ga Sa", PA, listOf(0, 3, 4, 5, 7, 10)),
                raga("Charukeshi", "from the Carnatic", "time not fixed", "Poignant.", "Sa Re Ga ma Pa dha ni Sa'", "Sa' ni dha Pa ma Ga Re Sa", PA, listOf(0, 2, 4, 5, 7, 8, 10)),
            )
        ),
        PresetTab(
            "CHORDS", "Western chords as drones. Named here in C; they move with the key.",
            listOf(
                p("Major", "", "The basic major chord, bright.", "Sa Ga Pa", "C,E,G"),
                p("Minor", "", "The minor third: darker.", "Sa ga Pa", "C,D#,G"),
                p("Major on Pa", "", "The dominant (V): it wants to return to Sa.", "Pa Ni Re", "G,B,D"),
                p("Major on ma", "", "The subdominant (IV): warm.", "ma Dha Sa", "F,A,C"),
                p("Minor on Dha", "", "The relative minor (vi).", "Dha Sa Ga", "A,C,E"),
                p("Minor on Re", "", "The supertonic minor (ii).", "Re ma Dha", "D,F,A"),
                p("Sus4", "", "ma in place of the third: open, unresolved.", "Sa ma Pa", "C,F,G"),
                p("Sus2", "", "Re in place of the third: open, bright. The same notes as Sus4 on Pa.", "Sa Re Pa", "C,D,G"),
                p("Major 7th", "", "The major chord with Ni.", "Sa Ga Pa Ni", "C,E,G,B"),
                p("Minor 7th", "", "The minor chord with ni: mellow.", "Sa ga Pa ni", "C,D#,G,A#"),
                p("Dominant 7th", "", "The dominant with ma: it pulls to Sa.", "Pa Ni Re ma", "G,B,D,F"),
            )
        ),
    )
}
