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
