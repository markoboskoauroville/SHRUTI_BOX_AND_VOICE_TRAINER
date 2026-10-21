package com.mantra.shrutibox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.pow

/** Test 1: the reeds and the mixer, on a plain JVM, no Android (android-app.md §2). */
class ReedsTest {
    private val rate = 48_000
    private fun peak(m: Mixer, seconds: Double): Float {
        val buf = FloatArray(1024); var p = 0f
        repeat((seconds * rate / buf.size).toInt()) { m.render(buf); for (s in buf) p = maxOf(p, abs(s)) }
        return p
    }

    @Test fun thirteenValves() = assertEquals(13, Shruti.NAMES.size)
    @Test fun twelveKeys() = assertEquals(12, Shruti.KEYS.size)
    @Test fun saInCIsC3() = assertEquals(130.8128, Shruti.freq(0, 0), 1e-3)
    @Test fun upperSaIsTheOctave() = assertEquals(2.0, Shruti.freq(0, 12) / Shruti.freq(0, 0), 1e-9)
    @Test fun paIsTheFifth() = assertEquals(2.0.pow(7 / 12.0), Shruti.freq(0, 7) / Shruti.freq(0, 0), 1e-9)
    @Test fun keyMovesSa() = assertEquals(Shruti.freq(0, 2), Shruti.freq(2, 0), 1e-9)
    @Test fun keyIsClamped() = assertEquals(Shruti.freq(11, 0), Shruti.freq(40, 0), 1e-9)
    @Test fun valveIsClamped() = assertEquals(Shruti.freq(0, 12), Shruti.freq(0, 99), 1e-9)
    @Test fun fiveUpperValves() = assertEquals(5, (0 until 13).count { Shruti.isUpperRow(it) })
    @Test fun saAndPaOnTheLowerRow() { assertFalse(Shruti.isUpperRow(0)); assertFalse(Shruti.isUpperRow(7)); assertFalse(Shruti.isUpperRow(12)) }

    @Test fun westernFollowsTheKey() { assertEquals("D", Shruti.western(2, 0)); assertEquals("A", Shruti.western(2, 7)); assertEquals("C", Shruti.western(0, 12)) }
    @Test fun detectsNothingWhenClosed() = assertEquals("", Shruti.detect(0, emptyList()))
    @Test fun detectsTheFifth() = assertEquals("Perfect fifth", Shruti.detect(0, listOf(7, 0)))
    @Test fun detectsAMajorChordInTheKey() = assertEquals("D major", Shruti.detect(2, listOf(0, 4, 7)))
    @Test fun detectsAMinorChordOnPa() = assertEquals("E minor", Shruti.detect(0, listOf(4, 7, 11)))
    @Test fun detectsTheSeventh() = assertEquals("C7", Shruti.detect(0, listOf(0, 4, 7, 10)))
    @Test fun unnamedIsEmpty() = assertEquals("", Shruti.detect(0, listOf(0, 1, 2)))

    @Test fun fivePresetTabsAsOnThePage() = assertEquals(listOf(5, 6, 6, 12, 12), Presets.TABS.map { it.presets.size })
    @Test fun everyPresetOpensRealValves() = Presets.TABS.flatMap { it.presets }.forEach { p ->
        assertTrue(p.name, p.valves.isNotEmpty() && p.valves.all { it in 0 until Shruti.COUNT } && p.valves.distinct().size == p.valves.size)
    }
    @Test fun pageNotesBecomeValves() = assertEquals(listOf(0, 6, 7, 12), Presets.valves("C,F#,G,C2"))
    @Test fun aChordPresetIsDetected() = assertEquals("G major", Shruti.detect(0, Presets.TABS[4].presets[2].valves))

    // the voice trainer's ear (v6): tones of known pitch, a voice-like one with harmonics, silence, noise
    private fun tone(hz: Double, n: Int = 4096, harmonics: Int = 1, amp: Double = 0.3) = FloatArray(n) { i ->
        var s = 0.0
        for (h in 1..harmonics) s += amp / h * kotlin.math.sin(2 * Math.PI * hz * h * i / rate)
        s.toFloat()
    }
    @Test fun hearsASineWithinTwoPercent() = listOf(98.0, 130.8128, 196.0, 261.63, 440.0, 880.0).forEach { hz ->
        val got = Pitch.detect(tone(hz), 4096, rate)!!
        assertEquals("at $hz", 0.0, Pitch.percentOff(got, hz), 2.0)
    }
    @Test fun hearsAVoiceWithHarmonics() {
        val got = Pitch.detect(tone(220.0, harmonics = 8), 4096, rate)!!
        assertEquals(0.0, Pitch.percentOff(got, 220.0), 2.0)
    }
    @Test fun hearsTwentyPercentSharp() {
        val hz = 196.0 * 2.0.pow(20.0 / 1200)
        assertEquals(20.0, Pitch.percentOff(Pitch.detect(tone(hz), 4096, rate)!!, 196.0), 2.0)
    }
    @Test fun silenceHasNoPitch() = assertEquals(null, Pitch.detect(FloatArray(4096), 4096, rate))
    @Test fun noiseHasNoPitch() {
        val r = java.util.Random(7)
        assertEquals(null, Pitch.detect(FloatArray(4096) { (r.nextGaussian() * 0.2).toFloat() }, 4096, rate))
    }
    @Test fun anOctaveIsOnTheNote() { assertEquals(0.0, Pitch.percentOff(440.0, 220.0), 1e-9); assertEquals(0.0, Pitch.percentOff(110.0, 440.0), 1e-9) }
    @Test fun flatIsMinus() = assertEquals(-30.0, Pitch.percentOff(220.0 * 2.0.pow(-30.0 / 1200), 220.0), 1e-6)
    @Test fun aSemitoneIsAHundred() = assertEquals(100.0, Pitch.percentOff(220.0 * 2.0.pow(1.0 / 12), 220.0), 1e-6)
    @Test fun ledsFromTheMiddle() {
        assertEquals(0, Pitch.led(2.0)); assertEquals(4, Pitch.led(18.0)); assertEquals(-2, Pitch.led(-12.0))
        assertEquals(Pitch.SIDE, Pitch.led(300.0)); assertEquals(-Pitch.SIDE, Pitch.led(-90.0))
    }

    // the tuner's window (Ear.kt): 2048 samples, 43 ms, enough down to a low bass
    @Test fun hearsInTheShortWindow() = listOf(82.41, 110.0, 196.0, 329.63, 523.25, 987.77).forEach { hz ->
        val got = Pitch.detect(tone(hz, n = 2048, harmonics = 5), 2048, rate)!!
        assertEquals("at $hz", 0.0, Pitch.percentOff(got, hz), 2.0)
    }
    @Test fun aQuietRoomIsSilence() = assertEquals(null, Pitch.detect(tone(220.0, n = 2048, amp = 0.005), 2048, rate))
    @Test fun fftRoundTrip() {
        val re = DoubleArray(16) { kotlin.math.sin(it * 0.7) }; val im = DoubleArray(16); val keep = re.copyOf()
        Pitch.fft(re, im, false); Pitch.fft(re, im, true)
        for (i in 0 until 16) assertEquals(keep[i], re[i], 1e-9)
    }
    @Test fun hushSilencesAtOnce() {
        val m = Mixer(rate); m.setOpen(0, true); m.setOpen(7, true); peak(m, 0.5)
        m.hush(); peak(m, Mixer.HUSH_S + 0.005)
        assertEquals(0.0, m.level(0), 0.0); assertEquals(0.0, m.level(7), 0.0); assertEquals(0, m.openCount())
    }
    @Test fun namesTheNoteHeard() { assertEquals("A", Pitch.note(440.0)); assertEquals("C", Pitch.note(65.41)); assertEquals("F#", Pitch.note(185.0)) }

    @Test fun waveIsNormalised() {
        val p = Wave.table.maxOf { abs(it) }
        assertEquals(1f, p, 1e-4f)
    }

    @Test fun silentWhenAllClosed() {
        val m = Mixer(rate)
        assertEquals(0f, peak(m, 0.5), 0f)
    }

    @Test fun soundsWhenOneOpen() {
        val m = Mixer(rate); m.setOpen(0, true)
        assertTrue(peak(m, 0.5) > 0.05f)
    }

    @Test fun neverClipsWithEveryValveOpen() {
        val m = Mixer(rate); for (v in 0 until 13) m.setOpen(v, true)
        assertTrue(peak(m, 2.0) < 1f)
    }

    @Test fun attackRampsAndDoesNotClick() {
        val m = Mixer(rate); m.setOpen(7, true)
        val first = FloatArray(48); m.render(first)
        assertTrue("the first millisecond must be near silence", first.maxOf { abs(it) } < 0.02f)
        assertTrue(m.level(7) > 0.0 && m.level(7) < 0.05)
    }

    @Test fun releaseFadesToSilence() {
        val m = Mixer(rate); m.setOpen(4, true); peak(m, 0.5)
        m.setOpen(4, false); peak(m, Mixer.RELEASE_S + 0.1)
        assertEquals(0.0, m.level(4), 0.0)
        assertEquals(0f, peak(m, 0.1), 1e-6f)
    }

    @Test fun closeAllClosesEvery() {
        val m = Mixer(rate); for (v in 0 until 13) m.setOpen(v, true)
        m.closeAll()
        assertEquals(0, m.openCount())
    }

    @Test fun outOfRangeValveIsIgnored() {
        val m = Mixer(rate); m.setOpen(-1, true); m.setOpen(13, true)
        assertEquals(0, m.openCount())
        assertFalse(m.isOpen(13))
    }

    @Test fun bellowsStayInTheirBand() {
        val m = Mixer(rate); m.setOpen(0, true)
        val buf = FloatArray(4800)
        repeat(200) {
            m.render(buf)
            val b = m.breath()
            assertTrue(b >= 1 - Mixer.BELLOWS_DEPTH - 1e-9 && b <= 1 + Mixer.BELLOWS_DEPTH + 1e-9)
        }
    }

    @Test fun renderRespectsN() {
        val m = Mixer(rate); m.setOpen(0, true); peak(m, 0.3)
        val buf = FloatArray(256) { 9f }
        m.render(buf, 100)
        assertEquals(9f, buf[100], 0f)
        assertTrue(abs(buf[50]) <= 1f)
    }
}
