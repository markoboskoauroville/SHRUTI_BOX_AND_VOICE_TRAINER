package com.mantra.shrutibox

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// THE EAR of the voice trainer (v6): what pitch a voice sings, and how far it is from the note of the last valve.
// Like Reeds.kt it imports nothing from Android, so Test 1 checks it on a plain JVM with tones of known pitch.
//
// The method is a tuner's: McLeod's pitch method (MPM, McLeod and Wyvill, "A smarter way to find pitch", 2005), the
// normalised square difference function computed through an FFT. Its peak picking and parabolic interpolation are
// converted to Kotlin from sevagh/pitch-detection (src/mpm.cpp, src/parabolic_interpolation.cpp, MIT licence,
// Copyright (c) Sevag Hanssian). The speed comes from how it is fed (Ear.kt): a window of 2048 samples analysed
// again every 512 new ones, as Google's guitar tuner (googlearchive/guitar-tuner) analyses on every frame.

object Pitch {
    /** The lowest and highest voice it listens for: a low bass to a high soprano. */
    const val LOW_HZ = 65.0
    const val HIGH_HZ = 1100.0
    /** Quieter than this (RMS of the window) is silence: no pitch is reported. */
    const val SILENCE = 0.01
    /** How clear a pitch must be (the highest NSDF peak, 0..1) to count: a held voice is 0.8 and more, noise far less. */
    const val CLARITY = 0.6
    /** MPM's own constants (sevagh): the first peak this close to the highest is the period; smaller ones are ignored. */
    private const val CUTOFF = 0.93
    private const val SMALL_CUTOFF = 0.5
    /** The tuner's row: LEDs either side of the middle one, each this many percent of a semitone. */
    const val SIDE = 10
    const val STEP = 5

    /** The pitch of [x] (its first [n] samples) in Hz, or null for silence, noise or no clear pitch. */
    fun detect(x: FloatArray, n: Int, rate: Int): Double? {
        val len = minOf(n, x.size)
        if (len < 64) return null
        var energy = 0.0
        for (i in 0 until len) energy += x[i].toDouble() * x[i]
        if (sqrt(energy / len) < SILENCE) return null

        // autocorrelation r(τ) through the FFT, zero-padded so it is linear, not circular
        var size = 1
        while (size < 2 * len) size = size shl 1
        val re = DoubleArray(size)
        val im = DoubleArray(size)
        for (i in 0 until len) re[i] = x[i].toDouble()
        fft(re, im, inverse = false)
        for (i in 0 until size) { re[i] = re[i] * re[i] + im[i] * im[i]; im[i] = 0.0 }
        fft(re, im, inverse = true)

        // the normalised square difference function: n(τ) = 2 r(τ) / m(τ), m(τ) = Σ x[j]² + x[j+τ]²
        val maxTau = minOf(len - 2, (rate / LOW_HZ).toInt())
        val nsdf = DoubleArray(maxTau + 1)
        var m = 2.0 * re[0]
        for (tau in 0..maxTau) {
            if (tau > 0) { val a = x[tau - 1].toDouble(); val b = x[len - tau].toDouble(); m -= a * a + b * b }
            nsdf[tau] = if (m > 1e-12) 2.0 * re[tau] / m else 0.0
        }

        // MPM: the key maxima, one per positive lobe; the first one near the highest is the period
        var highest = 0.0
        val estimates = ArrayList<Pair<Double, Double>>()
        for (i in peaks(nsdf)) {
            if (nsdf[i] > SMALL_CUTOFF) {
                val e = parabola(nsdf, i)
                estimates += e
                highest = maxOf(highest, e.second)
            }
        }
        if (estimates.isEmpty() || highest < CLARITY) return null
        val period = estimates.first { it.second >= CUTOFF * highest }.first
        val hz = rate / period
        return if (hz in LOW_HZ..HIGH_HZ) hz else null
    }

    /** sevagh's peak_picking: past the zero lag's lobe, the highest point of every positive lobe. */
    private fun peaks(nsdf: DoubleArray): List<Int> {
        val out = ArrayList<Int>()
        val size = nsdf.size
        var pos = 0
        var cur = 0
        while (pos < (size - 1) / 3 && nsdf[pos] > 0) pos++
        while (pos < size - 1 && nsdf[pos] <= 0.0) pos++
        if (pos == 0) pos = 1
        while (pos < size - 1) {
            if (nsdf[pos] > nsdf[pos - 1] && nsdf[pos] >= nsdf[pos + 1] && (cur == 0 || nsdf[pos] > nsdf[cur])) cur = pos
            pos++
            if (pos < size - 1 && nsdf[pos] <= 0) {
                if (cur > 0) { out += cur; cur = 0 }
                while (pos < size - 1 && nsdf[pos] <= 0.0) pos++
            }
        }
        if (cur > 0) out += cur
        return out
    }

    /** sevagh's parabolic_interpolation: the true top of the peak at [x], between samples, and its height. */
    private fun parabola(a: DoubleArray, x: Int): Pair<Double, Double> {
        if (x < 1 || x >= a.size - 1) return x.toDouble() to a[x]
        val den = a[x + 1] + a[x - 1] - 2 * a[x]
        val delta = a[x - 1] - a[x + 1]
        return if (den == 0.0) x.toDouble() to a[x] else (x + delta / (2 * den)) to (a[x] - delta * delta / (8 * den))
    }

    /** An in-place radix-2 FFT; the inverse is scaled by 1/size. [re] and [im] are the same power-of-two length. */
    fun fft(re: DoubleArray, im: DoubleArray, inverse: Boolean) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { var t = re[i]; re[i] = re[j]; re[j] = t; t = im[i]; im[i] = im[j]; im[j] = t }
        }
        var len = 2
        while (len <= n) {
            val ang = 2 * PI / len * (if (inverse) 1 else -1)
            val wr = cos(ang); val wi = sin(ang)
            for (i in 0 until n step len) {
                var cr = 1.0; var ci = 0.0
                for (k in 0 until len / 2) {
                    val ur = re[i + k]; val ui = im[i + k]
                    val vr = re[i + k + len / 2] * cr - im[i + k + len / 2] * ci
                    val vi = re[i + k + len / 2] * ci + im[i + k + len / 2] * cr
                    re[i + k] = ur + vr; im[i + k] = ui + vi
                    re[i + k + len / 2] = ur - vr; im[i + k + len / 2] = ui - vi
                    val nr = cr * wr - ci * wi; ci = cr * wi + ci * wr; cr = nr
                }
            }
            len = len shl 1
        }
        if (inverse) for (i in 0 until n) { re[i] /= n; im[i] /= n }
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

    /** The note a pitch is nearest to, as the keys name it: what the singer actually sang. */
    fun note(hz: Double): String = Shruti.KEYS[(((12.0 * ln(hz / Shruti.C3) / ln(2.0)).roundToInt() % 12) + 12) % 12]

    /** Which LED lights for [percent]: 0 is the middle, -SIDE the far left (flat), +SIDE the far right (sharp). */
    fun led(percent: Double): Int = (percent / STEP).roundToInt().coerceIn(-SIDE, SIDE)
}
