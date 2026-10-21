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
class AutoRun(val notes: List<Int>, val playS: Double = 4.0, val holdS: Double = 0.3, val within: Double = 10.0) {
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
            Phase.PLAY -> if (now - since >= playS) { phase = Phase.SING; since = now; onNoteSince = null; return true }
            Phase.SING -> {
                if (percent != null && abs(percent) <= within) {
                    val from = onNoteSince ?: now.also { onNoteSince = it }
                    if (now - from >= holdS) {
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
    }
}
