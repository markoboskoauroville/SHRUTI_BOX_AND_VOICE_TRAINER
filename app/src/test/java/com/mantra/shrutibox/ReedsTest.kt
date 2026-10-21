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

    @Test fun threeTabs() = assertEquals(listOf("DRONES", "RAGAS", "CHORDS"), Presets.TABS.map { it.title })
    // v7, the ground truth: a raga's drone never sounds a note the raga does not have (Puriya has no Pa)
    @Test fun noRagaDroneOutsideItsScale() = Presets.TABS[1].presets.forEach { r ->
        assertTrue(r.name, r.scale.isNotEmpty())
        assertTrue(r.name, r.valves.all { it % 12 in r.scale })
    }
    @Test fun ragaDronesAreOneNoteBesideSa() = Presets.TABS[1].presets.forEach { r ->
        assertTrue(r.name, r.valves == Presets.PA || r.valves == Presets.MA || r.valves == Presets.NI)
    }
    @Test fun theKnownTunings() {
        val r = Presets.TABS[1].presets.associateBy { it.name }
        assertEquals(Presets.NI, r.getValue("Puriya").valves); assertEquals(Presets.NI, r.getValue("Marwa").valves)
        assertEquals(Presets.MA, r.getValue("Malkauns").valves); assertEquals(Presets.MA, r.getValue("Bageshri").valves)
        assertEquals(Presets.PA, r.getValue("Yaman").valves); assertEquals(Presets.PA, r.getValue("Bhairavi").valves)
    }
    @Test fun ragasWithoutPaHaveNoPa() = Presets.TABS[1].presets.filter { 7 !in it.scale }.forEach { assertFalse(it.name, 7 in it.valves) }
    @Test fun namesAreUnique() = Presets.TABS.forEach { t -> assertEquals(t.title, t.presets.size, t.presets.map { it.name }.toSet().size) }
    @Test fun everyChordIsTheChordItNames() {
        val want = mapOf("Major" to "C major", "Minor" to "C minor", "Major on Pa" to "G major", "Major on ma" to "F major",
            "Minor on Dha" to "A minor", "Minor on Re" to "D minor", "Sus4" to "Csus4", "Sus2" to "Csus2",
            "Major 7th" to "Cmaj7", "Minor 7th" to "Cm7", "Dominant 7th" to "G7")
        Presets.TABS[2].presets.forEach { c -> assertEquals(c.name, want.getValue(c.name), Shruti.detect(0, c.valves)) }
    }
    @Test fun everyPresetOpensRealValves() = Presets.TABS.flatMap { it.presets }.forEach { p ->
        assertTrue(p.name, p.valves.isNotEmpty() && p.valves.all { it in 0 until Shruti.COUNT } && p.valves.distinct().size == p.valves.size)
    }
    @Test fun pageNotesBecomeValves() = assertEquals(listOf(0, 6, 7, 12), Presets.valves("C,F#,G,C2"))

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

    // auto voice (v7): the walk, with a clock of its own
    @Test fun autoWalksTheScaleFromTheTappedNote() = assertEquals(listOf(4, 5, 7, 9, 11, 12), AutoRun.from(4, AutoRun.MAJOR))
    @Test fun autoFromANoteOutsideTheScale() = assertEquals(listOf(1, 2, 4, 5, 7, 9, 11, 12), AutoRun.from(1, AutoRun.MAJOR))
    @Test fun autoFollowsARaga() = assertEquals(listOf(0, 3, 5, 8, 10, 12), AutoRun.from(0, listOf(0, 3, 5, 8, 10)))
    // v8: the note plays 3 s; the voice must stay within ±20 % for 2 s
    @Test fun autoPlaysThreeSecondsThenListens() {
        val r = AutoRun(listOf(0, 2))
        assertFalse(r.tick(0.0, null)); assertFalse(r.tick(2.9, null)); assertEquals(AutoRun.Phase.PLAY, r.phase)
        assertTrue(r.tick(3.0, null)); assertEquals(AutoRun.Phase.SING, r.phase)
    }
    @Test fun autoMovesOnAfterTwoSecondsWithinTwenty() {
        val r = AutoRun(listOf(0, 2)); r.tick(0.0, null); r.tick(3.0, null)
        assertFalse(r.tick(3.1, 19.0)); assertFalse(r.tick(4.0, 35.0))                  // on, then out: the count restarts
        assertFalse(r.tick(4.1, -18.0)); assertFalse(r.tick(6.0, 12.0))                 // 1.9 s on the note
        assertTrue(r.tick(6.1, 5.0))                                                     // 2.0 s
        assertEquals(AutoRun.Phase.PLAY, r.phase); assertEquals(2, r.note)
    }
    @Test fun arpGoesRoundTheChord() {
        val a = ArpRun(listOf(7, 0, 4)); assertEquals(0, a.note)
        assertFalse(a.tick(0.0)); assertFalse(a.tick(0.3)); assertTrue(a.tick(0.4)); assertEquals(4, a.note)
        assertTrue(a.tick(0.8)); assertEquals(7, a.note); assertTrue(a.tick(1.2)); assertEquals(0, a.note)
    }
    @Test fun arpOfOneNoteStays() { val a = ArpRun(listOf(5)); a.tick(0.0); assertFalse(a.tick(5.0)); assertEquals(5, a.note) }
    @Test fun autoEndsAtTheTop() {
        val r = AutoRun(listOf(12)); r.tick(0.0, null); r.tick(3.0, null); r.tick(3.1, 0.0); r.tick(5.1, 0.0)
        assertEquals(AutoRun.Phase.DONE, r.phase)
    }

    // ▶ SCALE (v7): each raga's aroha and avaroha as the card writes them, read into valves
    private fun raga(name: String) = Presets.TABS[1].presets.first { it.name == name }
    @Test fun bhairavWalksUpAndDown() = assertEquals(
        listOf(0, 1, 4, 5, 7, 8, 11, 12, 11, 8, 7, 5, 4, 1, 0), raga("Bhairav").walk)
    @Test fun yamanRisesFromSaNotTheMandraNi() = assertEquals(listOf(0, 2, 4, 6, 9, 11, 12), raga("Yaman").walk.take(7))
    @Test fun marwaLeavesOutTheReOverSa() = assertEquals(listOf(12, 11, 9, 6, 4, 1, 0), raga("Marwa").walk.takeLast(7))
    @Test fun kedarKeepsItsZigzag() = assertEquals(listOf(0, 5, 5, 7, 9, 7, 11, 9, 12), Sargam.walk("Sa ma, ma Pa, Dha Pa, Ni Dha Sa'", upward = true))
    @Test fun bageshriComesDownThroughItsTurns() = assertEquals(
        listOf(12, 10, 9, 5, 7, 9, 3, 5, 3, 2, 0), Sargam.walk("Sa' ni Dha ma Pa Dha ga ma ga Re Sa", upward = false))
    @Test fun everyRagaWalksOnlyItsOwnNotes() = Presets.TABS[1].presets.forEach { r ->
        val w = r.walk
        assertTrue(r.name, w.size >= 10)
        assertEquals(r.name, 0, w.first()); assertEquals(r.name, 0, w.last()); assertTrue(r.name, 12 in w)
        w.forEach { assertTrue("${r.name} sounds ${Shruti.NAMES[it]}", it in r.scale || it == 12) }
    }
    @Test fun scaleStepsAndHoldsTheLast() {
        val s = ScaleRun(listOf(0, 7, 12), stepS = 0.5)
        assertFalse(s.tick(0.0)); assertFalse(s.tick(0.4)); assertTrue(s.tick(0.5)); assertEquals(7, s.note)
        assertTrue(s.tick(1.0)); assertEquals(12, s.note); assertFalse(s.tick(1.9)); assertTrue(s.tick(2.0)); assertTrue(s.done)
    }

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
