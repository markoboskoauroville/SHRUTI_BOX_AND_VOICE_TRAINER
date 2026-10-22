package com.mantra.shrutibox

import kotlin.math.pow
import kotlin.math.roundToInt

// THE PIANO (v22, Marko: "Piano and Shruti Box 2 sounds", "find free piano samples which are the best quality ... no big
// files"). Salamander Grand Piano V3 by Alexander Holm (Yamaha C5, CC BY 3.0), cut to its own grid of 30 notes, one
// every minor third from A0 to C8, in two layers (soft v5, strong v13), 5 s each, mono AAC (assets/piano, 2.5 MB). Like Reeds.kt this file imports
// nothing from Android: Test 1 plays it with samples it makes itself. PianoLoader.kt decodes the real ones.

/** One recorded note: its MIDI number, its layer (0 soft, 1 strong), the sound and the rate it was recorded at. */
class PianoSample(val midi: Int, val layer: Int, val data: ShortArray, val rate: Int)

/**
 * Up to [MAX_VOICES] notes at once. A note plays the nearest recorded note, sped up or slowed down to its own pitch, the
 * soft or the strong layer by its velocity; it rings until the recording ends, and a release damps it in [RELEASE_S].
 * [next] is called from the audio thread for every sample; [noteOn] and [noteOff] from anywhere (synchronised).
 */
class Piano(private val outRate: Int) {
    companion object {
        const val LOWEST = 21                     // A0
        const val HIGHEST = 108                   // C8
        const val MAX_VOICES = 24
        const val RELEASE_S = 0.25
        const val SOFT_BELOW = 0.55               // a velocity under this plays the soft layer

        /** The recorded note nearest to [midi] on the minor-third grid A0, C1, D#1, F#1, A1 … C8. */
        fun nearest(midi: Int): Int = (LOWEST + 3 * ((midi - LOWEST) / 3.0).roundToInt()).coerceIn(LOWEST, HIGHEST)
    }

    /** Set once the samples are decoded; until then the piano is silent. */
    @Volatile var samples: Map<Int, Array<PianoSample?>> = emptyMap()

    private class Voice(val id: Int, val s: PianoSample, val ratio: Double, val gain: Double) {
        var pos = 0.0
        var damp = 1.0
        var releasing = false
    }
    private val voices = ArrayList<Voice>()
    private val dampStep = 1.0 / (RELEASE_S * outRate)

    fun setSamples(list: List<PianoSample>) {
        val m = HashMap<Int, Array<PianoSample?>>()
        for (s in list) m.getOrPut(s.midi) { arrayOfNulls(2) }[s.layer.coerceIn(0, 1)] = s
        samples = m
    }

    /** A key [id] struck: MIDI [midi] at [velocity] 0 … 1. The same id struck again restarts its note. */
    @Synchronized fun noteOn(id: Int, midi: Int, velocity: Double) {
        if (midi !in LOWEST..HIGHEST) return
        val pair = samples[nearest(midi)] ?: return
        val layer = if (velocity < SOFT_BELOW) 0 else 1
        val s = pair[layer] ?: pair[1 - layer] ?: return
        voices.removeAll { it.id == id }
        if (voices.size >= MAX_VOICES) voices.removeAt(0)
        val ratio = 2.0.pow((midi - s.midi) / 12.0) * s.rate / outRate
        // the layer gives the colour, the velocity the level within it
        voices += Voice(id, s, ratio, 0.35 + 0.65 * velocity.coerceIn(0.0, 1.0))
    }

    /** Key [id] let go: its note is damped. */
    @Synchronized fun noteOff(id: Int) { for (v in voices) if (v.id == id) v.releasing = true }

    /** Every note damped; [now] stops them at once (the voice trainer's VOICE). */
    @Synchronized fun allOff(now: Boolean = false) { if (now) voices.clear() else voices.forEach { it.releasing = true } }

    @Synchronized fun count(): Int = voices.size

    /** The next sample of all notes together, each bent by [bend] (the pitch strip and the vibrato). */
    @Synchronized fun next(bend: Double): Double {
        if (voices.isEmpty()) return 0.0
        var sum = 0.0
        val it = voices.iterator()
        while (it.hasNext()) {
            val v = it.next()
            val d = v.s.data
            val i = v.pos.toInt()
            if (i + 1 >= d.size || v.damp <= 0.0) { it.remove(); continue }
            val frac = v.pos - i
            val x = (d[i] * (1 - frac) + d[i + 1] * frac) / 32768.0
            sum += x * v.gain * v.damp
            v.pos += v.ratio * bend
            if (v.releasing) v.damp -= dampStep
        }
        return sum
    }
}
