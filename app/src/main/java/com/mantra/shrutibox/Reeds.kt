package com.mantra.shrutibox

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

// THE MECHANISM: the reeds, their tuning and the mixer. This file imports nothing from Android (scripts/verify.py
// fails the build the day it does), so Test 1 runs it on a plain JVM (MANTRA_MANIFEST android-app.md §1, §2).

/** The thirteen valves of a shruti box, Sa to the upper Sa, chromatic, in the names a singer uses. */
/**
 * The three loudnesses of a note (v10, Marko: "full volume is full button, level 2 is half full ... level 3 is just one
 * line at the bottom"), stepped by a short tap on its button beside RESET. v9's OFF and long press are gone.
 */
object Loudness {
    val DB = listOf(0, -12, -24)
    val LABELS = listOf("0 dB", "−12 dB", "−24 dB")
    /** How much of the button its light fills at each level: all of it, half, a line (drawn at least 3 dp). */
    val FILL = listOf(1f, 0.5f, 0f)
    /** Level [i] as an amplitude: 10^(dB/20). */
    fun gain(i: Int): Double = 10.0.pow(DB[i.coerceIn(0, DB.size - 1)] / 20.0)
    /** The level after [i]: full → half → line → full. */
    fun next(i: Int): Int = (i + 1) % DB.size
    /**
     * How hard a MIDI key was struck, as an amplitude (v11, Marko: "make it velocity sensitive"): velocity 127 is 0 dB,
     * the softest touch −24 dB, even in dB between them, so the touch is heard as evenly as the levels are.
     */
    fun velocity(vel: Int): Double = 10.0.pow(-24.0 * (1.0 - vel.coerceIn(1, 127) / 127.0) / 20.0)
}

object Shruti {
    val NAMES = listOf("Sa", "re", "Re", "ga", "Ga", "ma", "Ma", "Pa", "dha", "Dha", "ni", "Ni", "Sa'")
    val KEYS = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    const val COUNT = 13
    /** C3. A shruti box sits in the singer's low register; the key moves Sa up from here, to B3 at most. */
    const val C3 = 130.8128

    /** Equal temperament: valve [valve] (0 = Sa) in key [key] (0 = C). */
    fun freq(key: Int, valve: Int): Double = C3 * 2.0.pow((key.coerceIn(0, 11) + valve.coerceIn(0, COUNT - 1)) / 12.0)

    /** The lower row of the reed plate holds the seven notes of the scale and the upper Sa; the upper row the five between. */
    fun isUpperRow(valve: Int): Boolean = valve in setOf(1, 3, 6, 8, 10)

    /**
     * How far the box moves (v16): every note a MIDI keyboard can send, 0 to 127. The Launchkey Mini MK4's Octave − / +
     * reach −4 (keys on notes 0–24) to +5 (108–127); in its naming, where middle C (60) is C3, Sa goes from C-2 to C8.
     */
    const val LOWEST_OCTAVE = -4
    const val HIGHEST_OCTAVE = 6

    /** The MIDI note of the box's Sa: C3 (48) in C, moved by the key and by whole octaves. */
    fun saNote(key: Int, octave: Int): Int = 48 + key.coerceIn(0, 11) + 12 * octave.coerceIn(LOWEST_OCTAVE, HIGHEST_OCTAVE)

    /**
     * The valve a MIDI key opens (v12, Marko: "the octaves are matching, so it's not repeating"): a key inside the octave
     * on the screen, Sa to Sa', is that valve; any other key has none and sounds as a free reed at its own pitch.
     */
    fun valveInWindow(key: Int, octave: Int, note: Int): Int? = (note - saNote(key, octave)).takeIf { it in 0 until COUNT }

    /** The octave that puts MIDI [note] on the screen, Sa to Ni (v12, the box follows the keyboard); null beyond ◀ ▶'s reach. */
    fun octaveFor(key: Int, note: Int): Int? =
        Math.floorDiv(note - 48 - key.coerceIn(0, 11), 12).takeIf { it in LOWEST_OCTAVE..HIGHEST_OCTAVE }

    /** A MIDI note's own pitch, equal temperament from C3 = 48. */
    fun noteFreq(note: Int): Double = C3 * 2.0.pow((note - 48) / 12.0)

    /** The Western name of valve [valve] in key [key]: Sa in D is D. */
    fun western(key: Int, valve: Int): String = KEYS[(key.coerceIn(0, 11) + valve) % 12]

    private val INTERVALS = mapOf(
        listOf(0, 7) to "Perfect fifth", listOf(0, 5) to "Perfect fourth", listOf(0, 12) to "Octave",
        listOf(0, 4) to "Major third", listOf(0, 3) to "Minor third", listOf(0, 7, 12) to "Sa Pa Sa'", listOf(0, 5, 12) to "Sa ma Sa'"
    )
    private val CHORDS = listOf(
        setOf(0, 4, 7) to " major", setOf(0, 3, 7) to " minor", setOf(0, 5, 7) to "sus4", setOf(0, 2, 7) to "sus2",
        setOf(0, 4, 7, 11) to "maj7", setOf(0, 3, 7, 10) to "m7", setOf(0, 4, 7, 10) to "7"
    )

    /**
     * What the open valves make, as the reference page shows it: an interval from Sa, or a chord named in the key
     * (the lowest open valve tried first as its root). Empty when they make nothing with a name.
     */
    fun detect(key: Int, valves: Collection<Int>): String {
        val v = valves.filter { it in 0 until COUNT }.distinct().sorted()
        if (v.isEmpty()) return ""
        INTERVALS[v]?.let { return it }
        val pcs = v.map { it % 12 }.toSet()
        for (root in v.map { it % 12 }.distinct()) {
            val shape = pcs.map { (it - root + 12) % 12 }.toSet()
            CHORDS.firstOrNull { it.first == shape }?.let { return western(key, root) + it.second }
        }
        return ""
    }
}

/** One cycle of a free reed: rich, bright, the odd harmonics a little stronger, normalised to a peak of 1. */
object Wave {
    const val SIZE = 2048
    /**
     * v18 (Marko: "the sound is very strange" on a high C): one reed tone per number of harmonics, so a high note never
     * carries overtones past what digital audio can hold. At C7 (4.2 kHz) the 28 harmonics reached 117 kHz and folded
     * back as harsh unrelated tones (aliasing). Every table is scaled by the full tone's peak, so they match in level.
     */
    val HARMONICS = intArrayOf(28, 20, 14, 10, 7, 5, 3, 2, 1)
    private val raw: List<DoubleArray> by lazy {
        HARMONICS.map { h ->
            val t = DoubleArray(SIZE)
            for (n in 1..h) {
                val a = (1.0 / n.toDouble().pow(0.85)) * (if (n % 2 == 1) 1.0 else 0.62) * exp(-n / 16.0)
                for (i in 0 until SIZE) t[i] += a * sin(2 * PI * n * i / SIZE)
            }
            t
        }
    }
    private val tables: List<FloatArray> by lazy {
        val peak = raw[0].maxOf { abs(it) }
        raw.map { t -> FloatArray(SIZE) { (t[it] / peak).toFloat() } }
    }
    /** The full reed tone (28 harmonics), for the low and middle notes. */
    val table: FloatArray get() = tables[0]
    /** The richest tone whose highest harmonic stays under 0.45 of the sample rate, for a reed moving [step] cycles a sample. */
    fun tableFor(step: Double): FloatArray {
        val allowed = if (step > 0) 0.45 / step else Double.MAX_VALUE
        for (k in HARMONICS.indices) if (HARMONICS[k] <= allowed) return tables[k]
        return tables[HARMONICS.size - 1]
    }
    /** The number of harmonics [tableFor] picks, for the tests. */
    fun harmonicsFor(step: Double): Int { val allowed = if (step > 0) 0.45 / step else Double.MAX_VALUE; return HARMONICS.firstOrNull { it <= allowed } ?: 1 }
}

/**
 * The air through the open reeds. [open] and [key] are set from the screen; [render] is called from the audio
 * thread, one block at a time. Each reed has two phases a cent and a half apart, the beating that makes a reed
 * sound alive; each valve fades in over [ATTACK_S] and out over [RELEASE_S] so a tap never clicks; the bellows
 * breathe the whole box at [BELLOWS_HZ].
 */
class Mixer(private val sampleRate: Int) {
    companion object {
        const val ATTACK_S = 0.12
        const val RELEASE_S = 0.35
        const val BELLOWS_HZ = 0.11
        const val BELLOWS_DEPTH = 0.16
        const val DETUNE_CENTS = 1.5
        const val LEVEL = 0.32
        const val HUSH_S = 0.02
    }

    @Volatile var key: Int = 0
    /** Whole octaves the box is moved by ◀ ▶ (v12): the valves sound this many octaves from C3. */
    @Volatile var octave: Int = 0
    private val open = BooleanArray(Shruti.COUNT)
    // v12: the free reeds, one for every MIDI note, for the keys outside the octave on the screen (no valve, no light)
    private val noteOpen = BooleanArray(128)
    private val noteVolume = DoubleArray(128) { 1.0 }
    private val noteGain = DoubleArray(128)
    private val notePhaseA = DoubleArray(128)
    private val notePhaseB = DoubleArray(128) { it * 0.37 % 1.0 }
    /** Each reed's loudness (v10, its button beside RESET): 1 is 0 dB; 0 would leave the reed silent even when open. */
    private val volume = DoubleArray(Shruti.COUNT) { 1.0 }
    private val gain = DoubleArray(Shruti.COUNT)
    private val phaseA = DoubleArray(Shruti.COUNT)
    private val phaseB = DoubleArray(Shruti.COUNT) { it * 0.37 % 1.0 }
    private var bellows = 0.0
    private var norm = 1.0
    /** v17, the Launchkey's pitch strip: every reed bent by this many semitones (−2 … +2). */
    @Volatile var bendSemis: Double = 0.0
    /** v17, the modulation strip: vibrato depth 0 … 1 (1 = ±40 cents at 5.5 Hz). */
    @Volatile var vibrato: Double = 0.0
    private var vibPhase = 0.0
    /** v13 XFADE: when above 0, every reed swells and fades over this many seconds (the sequencer's crossfade). */
    @Volatile var crossfadeS: Double = 0.0
    private var up = 1.0 / (ATTACK_S * sampleRate)
    private var down = 1.0 / (RELEASE_S * sampleRate)
    /** After [hush]: every reed falls silent in [HUSH_S], fast but without a click, so the microphone can open. */
    private val fast = 1.0 / (HUSH_S * sampleRate)
    private var quick = false

    @Synchronized fun setOpen(valve: Int, isOpen: Boolean) { if (valve in 0 until Shruti.COUNT) open[valve] = isOpen }
    @Synchronized fun isOpen(valve: Int): Boolean = valve in 0 until Shruti.COUNT && open[valve]
    @Synchronized fun closeAll() { open.fill(false); noteOpen.fill(false) }
    /** Closes every valve and silences the box at once (the voice trainer's VOICE, v6): no 0.35 s tail. */
    @Synchronized fun hush() { open.fill(false); noteOpen.fill(false); quick = true }
    /** A free reed (v12): MIDI [note] sounding at its own pitch, at [volume] (1 = 0 dB), or let go. */
    @Synchronized fun setNote(note: Int, isOpen: Boolean, volume: Double = 1.0) {
        if (note !in 0..127) return
        noteOpen[note] = isOpen
        if (isOpen) noteVolume[note] = volume.coerceIn(0.0, 1.0)
    }
    @Synchronized fun isNoteOpen(note: Int): Boolean = note in 0..127 && noteOpen[note]
    @Synchronized fun openCount(): Int = open.count { it }
    /** How far each reed is sounding, 0 to 1, for the screen. */
    @Synchronized fun level(valve: Int): Double =
        if (valve in 0 until Shruti.COUNT && volume[valve] > 0.0) minOf(1.0, gain[valve] / volume[valve]) else 0.0
    /** Sets [valve]'s loudness, 0 (silent) to 1 (0 dB); the reed glides there as it opens and closes. */
    @Synchronized fun setVolume(valve: Int, v: Double) { if (valve in 0 until Shruti.COUNT) volume[valve] = v.coerceIn(0.0, 1.0) }
    @Synchronized fun volume(valve: Int): Double = if (valve in 0 until Shruti.COUNT) volume[valve] else 0.0
    /** The bellows' breath now, 1 - depth to 1 + depth: the screen opens the bellows with it. */
    fun breath(): Double = 1.0 + BELLOWS_DEPTH * sin(2 * PI * bellows)

    /** Fills [out] with the next [n] samples, every one inside -1..1. */
    @Synchronized fun render(out: FloatArray, n: Int = out.size) {
        // v18: each reed reads the richest tone that stays clean at its pitch, chosen once a block
        val valveTable = Array(Shruti.COUNT) { Wave.table }
        val k = key
        val detune = 2.0.pow(DETUNE_CENTS / 1200.0)
        val xf = crossfadeS
        up = 1.0 / ((if (xf > 0) xf else ATTACK_S) * sampleRate)
        down = 1.0 / ((if (xf > 0) xf else RELEASE_S) * sampleRate)
        // the strips: a bend and a vibrato for the whole block (a block is a few milliseconds, so the wobble is smooth)
        vibPhase = (vibPhase + 5.5 * n / sampleRate) % 1.0
        val cents = bendSemis * 100.0 + vibrato * 40.0 * sin(2 * PI * vibPhase)
        val bend = 2.0.pow(cents / 1200.0)
        val shift = 2.0.pow(octave.coerceIn(Shruti.LOWEST_OCTAVE, Shruti.HIGHEST_OCTAVE).toDouble()) * bend
        val step = DoubleArray(Shruti.COUNT) { Shruti.freq(k, it) * shift / sampleRate }
        for (v in 0 until Shruti.COUNT) valveTable[v] = Wave.tableFor(step[v])
        val bellowsStep = BELLOWS_HZ / sampleRate
        val count = max(1, (0 until Shruti.COUNT).count { open[it] && volume[it] > 0.0 } + noteOpen.count { it })
        // Many reeds together are louder: the level follows the square root of how many are open, smoothly, so
        // opening a seventh valve does not jump the other six
        val target = 1.0 / sqrt(count.toDouble())
        for (s in 0 until minOf(n, out.size)) {
            norm += (target - norm) * 0.0005
            bellows = (bellows + bellowsStep) % 1.0
            val air = 1.0 + BELLOWS_DEPTH * sin(2 * PI * bellows)
            var sum = 0.0
            for (v in 0 until Shruti.COUNT) {
                val to = if (open[v]) volume[v] else 0.0
                // the ramps are scaled to the reed's own loudness, so a −24 dB reed swells and fades as slowly as a full one
                val scale = max(volume[v], gain[v])
                gain[v] = if (gain[v] < to) minOf(to, gain[v] + up * to) else max(to, gain[v] - (if (quick) fast else down) * scale)
                if (gain[v] == 0.0) continue
                phaseA[v] = (phaseA[v] + step[v]) % 1.0
                phaseB[v] = (phaseB[v] + step[v] * detune) % 1.0
                val tv = valveTable[v]
                val a = tv[(phaseA[v] * Wave.SIZE).toInt() % Wave.SIZE]
                val b = tv[(phaseB[v] * Wave.SIZE).toInt() % Wave.SIZE]
                sum += gain[v] * (a + b) * 0.5
            }
            // the free reeds: the same reed, fades and beating, at the note's own pitch
            for (nt in 0 until 128) {
                val to = if (noteOpen[nt]) noteVolume[nt] else 0.0
                if (to == 0.0 && noteGain[nt] == 0.0) continue
                val scale = max(noteVolume[nt], noteGain[nt])
                noteGain[nt] = if (noteGain[nt] < to) minOf(to, noteGain[nt] + up * to) else max(to, noteGain[nt] - (if (quick) fast else down) * scale)
                if (noteGain[nt] == 0.0) continue
                val st = Shruti.noteFreq(nt) * bend / sampleRate
                notePhaseA[nt] = (notePhaseA[nt] + st) % 1.0
                notePhaseB[nt] = (notePhaseB[nt] + st * detune) % 1.0
                val tn = Wave.tableFor(st)
                val a = tn[(notePhaseA[nt] * Wave.SIZE).toInt() % Wave.SIZE]
                val b = tn[(notePhaseB[nt] * Wave.SIZE).toInt() % Wave.SIZE]
                sum += noteGain[nt] * (a + b) * 0.5
            }
            out[s] = tanh(sum * norm * air * LEVEL * 1.6).toFloat()
        }
        if (quick && gain.all { it == 0.0 } && noteGain.all { it == 0.0 }) quick = false
    }
}
