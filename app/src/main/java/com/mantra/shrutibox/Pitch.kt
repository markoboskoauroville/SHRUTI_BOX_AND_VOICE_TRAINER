package com.mantra.shrutibox

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

// THE EAR of the voice trainer (v6): what pitch a voice sings, and how far it is from the note of the last valve.
// Like Reeds.kt it imports nothing from Android, so Test 1 checks it on a plain JVM with tones of known pitch.

object Pitch {
    /** The lowest and highest voice it listens for: a low bass to a high soprano. */
    const val LOW_HZ = 65.0
    const val HIGH_HZ = 1100.0
    /** Quieter than this (RMS of the frame) is silence: no pitch is reported. */
    const val SILENCE = 0.008
    /** The tuner's row: LEDs either side of the middle one, each this many percent of a semitone. */
    const val SIDE = 5
    const val STEP = 10

    /**
     * YIN (de Cheveigné and Kawahara, 2002): the period whose difference function, cumulatively normalised, first
     * falls under the threshold, refined between samples by a parabola. Null for silence or no clear pitch.
     */
    fun detect(x: FloatArray, n: Int, rate: Int, threshold: Double = 0.12): Double? {
        val len = minOf(n, x.size)
        var energy = 0.0
        for (i in 0 until len) energy += x[i].toDouble() * x[i]
        if (len == 0 || sqrt(energy / len) < SILENCE) return null
        val maxTau = minOf(len / 2, (rate / LOW_HZ).toInt())
        val minTau = maxOf(2, (rate / HIGH_HZ).toInt())
        if (maxTau <= minTau + 2) return null
        val w = len - maxTau
        val d = DoubleArray(maxTau + 1)
        for (tau in 1..maxTau) {
            var s = 0.0
            for (i in 0 until w) { val e = x[i].toDouble() - x[i + tau]; s += e * e }
            d[tau] = s
        }
        // cumulative mean normalised difference
        val c = DoubleArray(maxTau + 1); c[0] = 1.0
        var run = 0.0
        for (tau in 1..maxTau) { run += d[tau]; c[tau] = if (run == 0.0) 1.0 else d[tau] * tau / run }
        var tau = minTau
        var found = -1
        while (tau < maxTau) {
            if (c[tau] < threshold) {
                while (tau + 1 < maxTau && c[tau + 1] < c[tau]) tau++
                found = tau; break
            }
            tau++
        }
        if (found < 0) return null
        val t = found
        val a = c[t - 1]; val b = c[t]; val cc = c[t + 1]
        val den = a - 2 * b + cc
        val exact = if (abs(den) > 1e-12) t + 0.5 * (a - cc) / den else t.toDouble()
        val hz = rate / exact
        return if (hz in LOW_HZ..HIGH_HZ) hz else null
    }

    /**
     * How far [hz] is from [target], in percent of a semitone (a tuner's cents), in the nearest octave: a voice an
     * octave above or below the reed is on the note. -600 up to +600: 100 is a whole semitone off.
     */
    fun percentOff(hz: Double, target: Double): Double {
        val cents = 1200.0 * ln(hz / target) / ln(2.0)
        val r = ((cents % 1200.0) + 1200.0) % 1200.0          // 0 until 1200
        return if (r >= 600.0) r - 1200.0 else r
    }

    /** Which LED lights for [percent]: 0 is the middle, -SIDE the far left (flat), +SIDE the far right (sharp). */
    fun led(percent: Double): Int = (percent / STEP).roundToInt().coerceIn(-SIDE, SIDE)
}
