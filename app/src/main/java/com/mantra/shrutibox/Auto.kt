package com.mantra.shrutibox

import kotlin.math.abs

// AUTO VOICE (v7, Marko: "As soon as I hit the right pitch, you jump to the next note, and then again after 4 seconds
// you activate the voice recording ... until I reach the end of the scale"). The walk alone, no Android: Test 1 runs
// it with a clock it is handed, so its timing is checked without a phone.

/**
 * A walk up [notes] (valves, ascending): each note PLAYs [playS] seconds, then the shruti stops and the voice SINGs it
 * back; when the voice stays within [within] percent of a semitone for [holdS] seconds the next note plays. After the
 * last one the walk is DONE. [tick] is called every frame with the time and the tuner's reading.
 */
// v8 (Marko): "you play the note for 3 seconds And then I sing it. If I hover inside the 20% ... for 2 seconds, you go
// to the next note."
class AutoRun(val notes: List<Int>, val playS: Double = 3.0, val holdS: Double = 2.0, val within: Double = 20.0) {
    enum class Phase { PLAY, SING, DONE }

    var index = 0
        private set
    var phase = Phase.PLAY
        private set
    private var since = Double.NaN
    private var onNoteSince: Double? = null

    /** The note being played or sung now. */
    val note: Int get() = notes[index.coerceAtMost(notes.size - 1)]

    /** Moves the walk on to [now] (seconds); [percent] is how far the voice is, null when none is heard. True when the phase changed. */
    fun tick(now: Double, percent: Double?): Boolean {
        if (since.isNaN()) since = now
        when (phase) {
            Phase.PLAY -> if (now - since >= playS - 1e-9) { phase = Phase.SING; since = now; onNoteSince = null; return true }
            Phase.SING -> {
                if (percent != null && abs(percent) <= within) {
                    val from = onNoteSince ?: now.also { onNoteSince = it }
                    if (now - from >= holdS - 1e-9) {
                        index++; since = now; onNoteSince = null
                        phase = if (index >= notes.size) Phase.DONE else Phase.PLAY
                        return true
                    }
                } else onNoteSince = null
            }
            Phase.DONE -> {}
        }
        return false
    }

    companion object {
        /** Sa Re Ga ma Pa Dha Ni: the scale when no raga is chosen. */
        val MAJOR = listOf(0, 2, 4, 5, 7, 9, 11)

        /** The walk from [start] up through [scale] to Sa'. A start outside the scale is sung first, then the scale goes on. */
        fun from(start: Int, scale: List<Int>): List<Int> =
            (listOf(start) + scale.filter { it > start } + 12).filter { it in 0..12 && it >= start }.distinct().sorted()

        /** The 88 keys of a piano, MIDI 21 to 108 (A-1 to C7 in the Launchkey's naming, A0 to C8 in scientific), as MIDI notes: the trainer's range is chosen inside them (v13). */
        val PIANO = 21..108
        /** How close the voice must stay, ± percent of a semitone (v13, Marko: "The hardest part is 20% ... then 40%, then 60%"). */
        val DIFFICULTIES = listOf(20, 40, 60)

        /** True when MIDI [note] is Sa or a note of [scale] in key [key] (Sa = C in C). */
        fun inScale(note: Int, key: Int, scale: List<Int>): Boolean = Math.floorMod(note - key, 12).let { it == 0 || it in scale }

        /** The first note of the walk: the lowest note of [scale] at or above [low]. */
        fun firstInRange(low: Int, key: Int, scale: List<Int>): Int = (low..PIANO.last).firstOrNull { inScale(it, key, scale) } ?: low

        /**
         * The walk through the range (v13), in MIDI notes: [start] (a key or a valve may start outside the scale, it is
         * sung first), then every note of [scale] above it up to [high].
         */
        fun walk(start: Int, key: Int, scale: List<Int>, high: Int): List<Int> =
            listOf(start) + ((start + 1)..maxOf(start, high)).filter { inScale(it, key, scale) }
    }
}

/**
 * ▶ SCALE (v7, the raga's notes heard in order): each of [notes] sounds alone for [stepS] seconds, then the next;
 * the last one a little longer. Like [AutoRun] it is handed its clock, so Test 1 checks it without a phone.
 */
class ScaleRun(val notes: List<Int>, val stepS: Double = 0.7) {
    var index = 0
        private set
    val done: Boolean get() = index >= notes.size
    val note: Int get() = notes[index.coerceAtMost(notes.size - 1)]
    private var since = Double.NaN

    /** Moves on to [now] (seconds); true when the note changed or the scale ended. */
    fun tick(now: Double): Boolean {
        if (since.isNaN()) since = now
        if (done) return false
        val hold = if (index == notes.size - 1) stepS * 2 else stepS
        if (now - since < hold - 1e-9) return false
        index++; since = now
        return true
    }
}

/**
 * ARP (v8, Marko: "any preset chord will play as arpeggio, one note after the other"): the notes of a card one at a
 * time, low to high, round and round every [stepS] seconds, until it is stopped.
 */
class ArpRun(notes: List<Int>, val stepS: Double = 0.4) {
    val notes = notes.distinct().sorted()
    var index = 0
        private set
    val note: Int get() = notes[index % notes.size]
    private var since = Double.NaN

    /** Moves on to [now] (seconds); true when the next note begins. */
    fun tick(now: Double): Boolean {
        if (since.isNaN()) since = now
        if (notes.size < 2 || now - since < stepS - 1e-9) return false
        index = (index + 1) % notes.size; since = now
        return true
    }
}

/**
 * The sequencer (v13, Marko: "user press on each preset and then it gets the number, and then when user press sequence
 * button this sequences is starting"): the numbered CUSTOM presets one after the other, one beat each, round and round.
 * [bpm] is read at every beat, so a change is heard at the next one.
 */
class SeqRun(val steps: List<List<Int>>, var bpm: Int) {
    var index = 0
        private set
    val valves: List<Int> get() = steps[index % steps.size]
    private var since = Double.NaN

    /** Moves on to [now] (seconds); true when the next step begins. */
    fun tick(now: Double): Boolean {
        if (since.isNaN()) since = now
        if (steps.size < 2 || now - since < 60.0 / bpm.coerceIn(BPM.first, BPM.last) - 1e-9) return false
        index = (index + 1) % steps.size; since = now
        return true
    }

    companion object { val BPM = 10..240 }
}
