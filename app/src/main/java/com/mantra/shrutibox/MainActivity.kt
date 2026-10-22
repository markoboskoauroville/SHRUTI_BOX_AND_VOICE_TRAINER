package com.mantra.shrutibox

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// v2 (Marko, 21.10.2026): not a wooden box any more. Black ground, the whole screen is the instrument, the valves
// glowing "as if coming from another dimension, from the reality of angels". v5: the cold blue of v2 and a warm orange,
// chosen in settings.
private val BLACK = Color(0xFF000000)

/** One colour scheme: the light of a valve, its white heart, the halo it throws, the lines and faint fills at rest. */
private class Palette(
    val name: String, val light: Color, val core: Color, val halo: Color, val line: Color, val faint: Color,
    val cardOn: Color, val ink: Color
)

private val COLD = Palette(
    "COLD", light = Color(0xFF9FE3FF), core = Color(0xFFF2FCFF), halo = Color(0xFF5CC8FF), line = Color(0xFF2E5F78),
    faint = Color(0xFF16303D), cardOn = Color(0xFF0A2633), ink = Color(0xFF05222E)
)
private val WARM = Palette(
    "WARM", light = Color(0xFFFFC487), core = Color(0xFFFFF5E8), halo = Color(0xFFFF9A3C), line = Color(0xFF7A4A22),
    faint = Color(0xFF3A2210), cardOn = Color(0xFF331D0B), ink = Color(0xFF2E1503)
)
private val PALETTES = listOf(COLD, WARM)

/** version-visible.md §3: the version, now in settings, opens the latest build. */
private const val LATEST = "https://github.com/markoboskoauroville/SHRUTI_BOX_AND_VOICE_TRAINER/releases/latest"
private const val PREFS = "shruti"

/** What settings hold, every one kept in the preferences. */
private class Settings(prefs: SharedPreferences) {
    var palette by mutableIntStateOf(prefs.getInt("palette", 0).coerceIn(0, PALETTES.size - 1))
    var landscape by mutableStateOf(prefs.getBoolean("landscape", false))
    var showNotes by mutableStateOf(prefs.getBoolean("showNotes", true))
    var showPresets by mutableStateOf(prefs.getBoolean("showPresets", true))
    var showKeys by mutableStateOf(prefs.getBoolean("showKeys", true))
    var showNames by mutableStateOf(prefs.getBoolean("showNames", true))
    var showTrainer by mutableStateOf(prefs.getBoolean("showTrainer", true))
    // v13: the voice trainer's difficulty (± percent of a semitone) and its range on the 88 keys; the sequencer's speed
    var within by mutableIntStateOf(prefs.getInt("within", 20).let { if (it in AutoRun.DIFFICULTIES) it else 20 })
    var rangeLow by mutableIntStateOf(prefs.getInt("rangeLow", 48).coerceIn(AutoRun.PIANO.first, AutoRun.PIANO.last))
    var rangeHigh by mutableIntStateOf(prefs.getInt("rangeHigh", 60).coerceIn(AutoRun.PIANO.first, AutoRun.PIANO.last))
    var bpm by mutableIntStateOf(prefs.getInt("bpm", 30).coerceIn(SeqRun.BPM.first, SeqRun.BPM.last))
    var xfade by mutableStateOf(prefs.getBoolean("xfade", true))
    var xfadeS by mutableStateOf(prefs.getFloat("xfadeS", 1.5f).coerceIn(0.2f, 10f))

    fun save(prefs: SharedPreferences) = prefs.edit {
        putInt("within", within); putInt("rangeLow", rangeLow); putInt("rangeHigh", rangeHigh); putInt("bpm", bpm)
        putBoolean("xfade", xfade); putFloat("xfadeS", xfadeS)
        putInt("palette", palette); putBoolean("landscape", landscape); putBoolean("showNotes", showNotes)
        putBoolean("showPresets", showPresets); putBoolean("showKeys", showKeys); putBoolean("showNames", showNames)
        putBoolean("showTrainer", showTrainer)
    }
}

class MainActivity : ComponentActivity() {
    private val engine = Engine()
    private val ear = Ear()
    private lateinit var midi: Midi
    private val micGranted = mutableStateOf(false)
    private val askMic = registerForActivityResult(ActivityResultContracts.RequestPermission()) { micGranted.value = it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // the whole screen: the status and navigation bars hide, a swipe from the edge brings them back for a moment
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        engine.mixer.key = prefs.getInt("key", 0).coerceIn(0, 11)
        engine.mixer.octave = prefs.getInt("octave", 0).coerceIn(Shruti.LOWEST_OCTAVE, Shruti.HIGHEST_OCTAVE)
        // each valve's loudness, kept like the key: 0 = 0 dB, 1 = −12 dB, 2 = −24 dB (v9's OFF comes back full)
        val levels = IntArray(Shruti.COUNT) { prefs.getInt("level$it", 0).let { l -> if (l in Loudness.LABELS.indices) l else 0 } }
        levels.forEachIndexed { v, i -> engine.mixer.setVolume(v, Loudness.gain(i)) }
        val settings = Settings(prefs)
        midi = Midi(this)
        micGranted.value = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        fixLayout(settings.landscape)
        setContent {
            ShrutiScreen(
                mixer = engine.mixer,
                ear = ear,
                midi = midi,
                micGranted = micGranted.value,
                askMic = { askMic.launch(Manifest.permission.RECORD_AUDIO) },
                settings = settings,
                startTab = prefs.getInt("tab", 0),
                startLevels = levels,
                startCustom = loadCustom(prefs),
                onCustom = { saveCustom(prefs, it) },
                onLevel = { v, i -> prefs.edit { putInt("level$v", i) } },
                onKey = { prefs.edit { putInt("key", it) } },
                onOctave = { prefs.edit { putInt("octave", it) } },
                onTab = { prefs.edit { putInt("tab", it) } },
                onSettings = { settings.save(prefs); fixLayout(settings.landscape) },
                onAnyOpen = { open ->
                    // a drone is played with the hands busy: the screen stays on while a reed sounds
                    if (open) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                },
                onVersion = { openLatest(this) }
            )
        }
    }

    // The layout is fixed by Marko's own choice in settings, portrait or landscape (v5); lint is told so on purpose.
    @SuppressLint("SourceLockedOrientationActivity")
    private fun fixLayout(landscape: Boolean) {
        requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // v13: his own presets, a name and the valves (relative to Sa, so they move with the key), kept as JSON
    private fun loadCustom(prefs: SharedPreferences): List<Preset> = runCatching {
        val a = org.json.JSONArray(prefs.getString("custom", "[]"))
        (0 until a.length()).map { i ->
            val o = a.getJSONObject(i); val v = o.getJSONArray("valves")
            customPreset(o.getString("name"), (0 until v.length()).map { v.getInt(it) })
        }
    }.getOrDefault(emptyList())
    private fun saveCustom(prefs: SharedPreferences, list: List<Preset>) {
        val a = org.json.JSONArray()
        list.forEach { p -> a.put(org.json.JSONObject().put("name", p.name).put("valves", org.json.JSONArray(p.valves))) }
        prefs.edit { putString("custom", a.toString()) }
    }

    override fun onStart() { super.onStart(); engine.start(); midi.start() }
    override fun onStop() { ear.listen(false); midi.stop(); engine.stop(); super.onStop() }
}

private fun openLatest(c: Context) {
    try {
        c.startActivity(Intent(Intent.ACTION_VIEW, LATEST.toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(c, "No browser", Toast.LENGTH_SHORT).show()
    }
}

/** The valves of the card [chosen], to sound again when ▶ SCALE ends. */
private fun chosenPresetValves(tabs: List<PresetTab>, chosen: Pair<Int, Int>?): List<Int> =
    chosen?.let { (t, i) -> tabs.getOrNull(t)?.presets?.getOrNull(i)?.valves }.orEmpty()

/** A CUSTOM card (v13): his name, the notes it opens. */
private fun customPreset(name: String, valves: List<Int>) =
    Preset(name, "", valves.sorted().joinToString(" ") { Shruti.NAMES[it.coerceIn(0, Shruti.COUNT - 1)] }, "", valves.sorted())
private const val CUSTOM_INTRO = "Your own. The save icon beside the notes keeps what sounds; ✎ renames or deletes. SEQ numbers the cards, ▶ plays them in turn, one beat each."
/** The page's tabs and CUSTOM after them. */
private fun allTabs(custom: List<Preset>) = Presets.TABS + PresetTab("CUSTOM", CUSTOM_INTRO, custom)

/** The tuner's last reading, handed from the trainer to the auto walk without a recomposition per frame. */
private class Readout { var percent: Double? = null }

/** Where each valve is on the canvas, for drawing and for the finger. */
private class Tab(val valve: Int, val rect: Rect, val upper: Boolean)

private val MONO = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, letterSpacing = 1.5.sp)

@Composable
private fun ShrutiScreen(
    mixer: Mixer, ear: Ear, midi: Midi, micGranted: Boolean, askMic: () -> Unit, settings: Settings, startTab: Int,
    startLevels: IntArray, startCustom: List<Preset>, onCustom: (List<Preset>) -> Unit, onLevel: (Int, Int) -> Unit, onKey: (Int) -> Unit, onOctave: (Int) -> Unit, onTab: (Int) -> Unit, onSettings: () -> Unit,
    onAnyOpen: (Boolean) -> Unit, onVersion: () -> Unit
) {
    val open = remember { mutableStateListOf(*Array(Shruti.COUNT) { mixer.isOpen(it) }) }
    var key by remember { mutableIntStateOf(mixer.key) }
    // v12: whole octaves the box is moved, by ◀ ▶ or by the keyboard playing in another octave
    var octave by remember { mutableIntStateOf(mixer.octave) }
    fun moveOctave(o: Int) { val c = o.coerceIn(Shruti.LOWEST_OCTAVE, Shruti.HIGHEST_OCTAVE); if (c != octave) { octave = c; mixer.octave = c; onOctave(c) } }
    // v13: CUSTOM, his own presets, the tab after the page's; editing is the card being named, seq its sequencer
    val custom = remember { mutableStateListOf(*startCustom.toTypedArray()) }
    val tabs = allTabs(custom.toList())
    val customTab = tabs.size - 1
    var editing by remember { mutableStateOf<Int?>(null) }
    var seqMode by remember { mutableStateOf(false) }
    val seqOrder = remember { mutableStateListOf<Int>() }
    var seqRun by remember { mutableStateOf<SeqRun?>(null) }
    var tab by remember { mutableIntStateOf(startTab.coerceIn(0, tabs.size - 1)) }
    // the card that was tapped, as tab and place: ten ragas share Sa Pa Sa', so the valves alone cannot say which
    var chosen by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var inSettings by remember { mutableStateOf(false) }
    // the voice trainer: the last valve opened is the note to sing back; armed is its switch
    var target by remember { mutableStateOf<Int?>(null) }
    var armed by remember { mutableStateOf(false) }
    // auto voice (v7): AUTO on, a tap on a valve walks up the scale, note played 4 s, then sung back
    var auto by remember { mutableStateOf(false) }
    var run by remember { mutableStateOf<AutoRun?>(null) }
    var step by remember { mutableIntStateOf(0) }
    var autoDone by remember { mutableStateOf(false) }
    val readout = remember { Readout() }
    // ▶ SCALE (v7): the chosen raga's aroha and avaroha, one valve at a time, then its drone again
    var scaleRun by remember { mutableStateOf<ScaleRun?>(null) }
    // ARP (v8): with it on, a card plays its notes one after the other, round and round
    var arp by remember { mutableStateOf(false) }
    var arpRun by remember { mutableStateOf<ArpRun?>(null) }
    val pal = PALETTES[settings.palette]
    // each valve's loudness (v10): a short tap on its button beside RESET steps it full → half → a line → full
    val levels = remember { mutableStateListOf(*startLevels.toTypedArray()) }
    val chosenPreset = chosen?.let { (t, i) -> tabs.getOrNull(t)?.presets?.getOrNull(i) }
    // the chosen raga's notes, marked on the valves (v7, Marko: "yes, mark ragas"); Sa' is Sa, so it is marked too
    val marks = chosenPreset?.scale?.takeIf { it.isNotEmpty() }?.let { it.toSet() + 12 } ?: emptySet()

    val anyOpen = open.any { it }
    LaunchedEffect(anyOpen) { onAnyOpen(anyOpen) }

    fun hushAll() { scaleRun = null; arpRun = null; seqRun = null; mixer.hush(); for (i in 0 until Shruti.COUNT) open[i] = false }
    fun openValves(): List<Int> = (0 until Shruti.COUNT).filter { open[it] }
    fun sound(valves: Collection<Int>) { mixer.closeAll(); for (i in 0 until Shruti.COUNT) { open[i] = i in valves; mixer.setOpen(i, open[i]) } }
    // v13: the walk is in MIDI notes through the trainer's range; each note moves the box to its octave and opens its
    // valve (a note beyond ◀ ▶'s reach sounds as a free reed), and the trainer listens for its pitch class
    fun pitchClass(note: Int) = Math.floorMod(note - 48 - key, 12)
    fun autoPlay(r: AutoRun) {
        hushAll()
        Shruti.octaveFor(key, r.note)?.let { moveOctave(it) }
        val v = Shruti.valveInWindow(key, octave, r.note)
        if (v != null) { open[v] = true; mixer.setOpen(v, true) } else mixer.setNote(r.note, true)
        target = v ?: pitchClass(r.note); armed = false
    }
    fun autoScale() = liveScaleOrMajor()
    fun startAuto(startNote: Int) {
        if (!micGranted) askMic()
        arpRun = null; seqRun = null
        val r = AutoRun(AutoRun.walk(startNote, key, autoScale(), settings.rangeHigh), within = settings.within.toDouble())
        run = r; autoDone = false; step++; autoPlay(r)
    }
    LaunchedEffect(Unit) {
        while (isActive) withFrameNanos { now ->
            val sr = scaleRun
            if (sr != null && sr.tick(now / 1e9)) {
                if (sr.done) { scaleRun = null; sound(chosenPresetValves(tabs, chosen)) } else sound(listOf(sr.note))
            }
            val ar = arpRun
            if (ar != null && ar.tick(now / 1e9)) sound(listOf(ar.note))
            val q = seqRun
            // XFADE (v13): while the sequencer runs, one chord fades out as the next fades in, over his time
            mixer.crossfadeS = if (q != null && settings.xfade) settings.xfadeS.toDouble() else 0.0
            if (q != null) { q.bpm = settings.bpm; if (q.tick(now / 1e9)) sound(q.valves) }
            val r = run
            if (r != null && r.tick(now / 1e9, readout.percent)) {
                when (r.phase) {
                    AutoRun.Phase.PLAY -> autoPlay(r)
                    AutoRun.Phase.SING -> { hushAll(); target = Shruti.valveInWindow(key, octave, r.note) ?: pitchClass(r.note); readout.percent = null; armed = true }
                    AutoRun.Phase.DONE -> { armed = false; run = null; autoDone = true }
                }
                step++
            }
        }
    }

    // the shruti and the voice take turns (Marko, v6): opening a valve or a preset switches VOICE off
    fun toggle(v: Int) {
        // while AUTO runs, a tapped valve is the new beginning of the walk (v8)
        if (auto) { startAuto(Shruti.saNote(key, octave) + v); return }
        scaleRun = null; arpRun = null; seqRun = null
        val now = !open[v]; open[v] = now; mixer.setOpen(v, now); if (now) { target = v; armed = false }
    }
    fun closeAll() { scaleRun = null; arpRun = null; seqRun = null; mixer.closeAll(); for (i in 0 until Shruti.COUNT) open[i] = false; chosen = null }
    /** ARP on or off; a card that sounds changes at once between its chord and its arpeggio. */
    // read the chosen card at the moment of the tap, not as it was when the button was composed
    fun liveChosen(): Preset? = chosen?.let { (t, i) -> tabs.getOrNull(t)?.presets?.getOrNull(i) }
    fun liveScaleOrMajor(): List<Int> = liveChosen()?.scale?.takeIf { it.isNotEmpty() } ?: AutoRun.MAJOR
    fun toggleArp() {
        arp = !arp
        val p = liveChosen() ?: return
        val sounding = arpRun != null || openValves() == p.valves.sorted()
        if (!sounding || scaleRun != null || run != null) return
        if (arp) { val a = ArpRun(p.valves); arpRun = a; sound(listOf(a.note)) } else { arpRun = null; sound(p.valves) }
    }
    fun playScale() {
        val p = chosenPreset ?: return
        arpRun = null
        if (scaleRun != null) { scaleRun = null; sound(p.valves); return }
        if (auto) { auto = false; run = null; autoDone = false }
        armed = false
        val sr = ScaleRun(p.walk); scaleRun = sr; sound(listOf(sr.note)); target = null
    }
    fun choose(t: Int, i: Int) {
        // v13: with SEQ on, a CUSTOM card gets the next number (or loses it) instead of sounding
        if (t == customTab && seqMode) { if (i in seqOrder) seqOrder.remove(i) else seqOrder.add(i); return }
        val p = tabs[t].presets[i]
        val again = chosen == (t to i)
        run = null
        closeAll()
        if (!again) {
            chosen = t to i; target = p.valves.min(); armed = false
            if (arp) { val a = ArpRun(p.valves); arpRun = a; sound(listOf(a.note)) } else for (v in p.valves) { open[v] = true; mixer.setOpen(v, true) }
        }
    }
    val openValves = (0 until Shruti.COUNT).filter { open[it] }

    // the MIDI controller (v11, v12): a key held opens its valve, released closes it (Marko: "when I press it's open, and
    // when I stop pressing it's closed"), every key at its own pitch ("the octaves are matching, so it's not repeating").
    // A key in another octave moves the box there and lights its valve ("the view shifts to that octave automatically"),
    // unless other keys are held: then it sounds as a free reed at its own pitch, unlit, so the held notes never jump.
    // How hard a key is struck sets its loudness, on top of the note's own level ("make it velocity sensitive"). A pad in
    // Drum mode (channel 10, notes 36–51, the Launchkey's default) chooses that card of the open tab when it goes down.
    val sounding = remember { HashMap<Int, Int>() }                  // a key that is down → its valve, or -1 for a free reed
    val onMidi by rememberUpdatedState<(Int, Int, Int) -> Unit> { ch, note, vel ->
        val pad = note - 36
        if (ch == 9 && pad in 0..15) { if (vel > 0 && pad < tabs[tab].presets.size) choose(tab, pad) }
        else if (vel > 0) {
            if (Shruti.valveInWindow(key, octave, note) == null && sounding.isEmpty() && !auto) Shruti.octaveFor(key, note)?.let { moveOctave(it) }
            val v = Shruti.valveInWindow(key, octave, note)
            if (v == null) { sounding[note] = -1; mixer.setNote(note, true, Loudness.velocity(vel)) }
            else {
                sounding[note] = v
                mixer.setVolume(v, Loudness.gain(levels[v]) * Loudness.velocity(vel))
                when { auto -> startAuto(note); !open[v] -> toggle(v) }  // while AUTO runs a key is the walk's new beginning
            }
        } else {
            when (val v = sounding.remove(note)) {
                null -> Unit
                -1 -> mixer.setNote(note, false)
                else -> if (!sounding.containsValue(v)) { mixer.setVolume(v, Loudness.gain(levels[v])); if (open[v] && !auto) toggle(v) }
            }
        }
    }
    DisposableEffect(midi) { midi.onNote = { ch, note, vel -> onMidi(ch, note, vel) }; onDispose { midi.onNote = null } }

    // v13 CUSTOM: the save icon keeps what sounds as a new card and asks its name at once
    fun saveCustom() {
        val v = (0 until Shruti.COUNT).filter { open[it] }
        if (v.isEmpty()) return
        custom.add(customPreset("Custom ${custom.size + 1}", v)); onCustom(custom.toList())
        tab = customTab; onTab(customTab); editing = custom.size - 1
    }
    fun renameCustom(i: Int, name: String) {
        val p = custom.getOrNull(i) ?: return
        custom[i] = customPreset(name.trim().ifEmpty { p.name }, p.valves); onCustom(custom.toList())
    }
    fun deleteCustom(i: Int) {
        if (i !in custom.indices) return
        if (chosen?.first == customTab) closeAll()
        seqRun = null
        custom.removeAt(i); onCustom(custom.toList())
        // the numbers after it move down one place with it
        val kept = seqOrder.filter { it != i }.map { if (it > i) it - 1 else it }
        seqOrder.clear(); seqOrder.addAll(kept)
    }
    fun toggleSequence() {
        if (seqRun != null) { seqRun = null; closeAll(); return }
        val steps = seqOrder.mapNotNull { custom.getOrNull(it)?.valves }
        if (steps.isEmpty()) return
        run = null; auto = false; scaleRun = null; arpRun = null; chosen = null
        val q = SeqRun(steps, settings.bpm); sound(q.valves); seqRun = q
    }

    BackHandler(enabled = inSettings) { inSettings = false; onSettings() }

    Box(Modifier.fillMaxSize().background(BLACK).windowInsetsPadding(WindowInsets.displayCutout).padding(horizontal = 10.dp, vertical = 10.dp)) {
        if (inSettings) {
            SettingsScreen(settings, pal, midi.connected.value, key, { key = it; mixer.key = it; onKey(it) }, onVersion) { inSettings = false; onSettings() }
            return@Box
        }
        val box: @Composable () -> Unit = { ValveBox(mixer, open, key, pal, settings.showNames, marks, levels) { toggle(it) } }
        // the chosen card names what sounds (Sus2 and Sus4 on Pa are the same notes; a raga is not a chord)
        val cardName = chosen?.let { (t, i) -> tabs.getOrNull(t)?.presets?.getOrNull(i) }?.takeIf { openValves == it.valves.sorted() }?.name
        // the buttons beside RESET: the notes that sound; while ARP runs, every note of its card, not the one passing by
        val noteButtons = (openValves + (if (arpRun != null) chosenPreset?.valves.orEmpty() else emptyList())).distinct().sorted()
        val notes: @Composable () -> Unit = {
            if (settings.showNotes) NotesRow(openValves, noteButtons, levels, key, pal, anyOpen, cardName, onSave = { saveCustom() }, onLevel = { v ->
                val i = Loudness.next(levels[v]); levels[v] = i; mixer.setVolume(v, Loudness.gain(i)); onLevel(v, i)
            }) { closeAll() }
        }
        val keys: @Composable () -> Unit = { if (settings.showKeys) OctaveRow(octave, key, pal) { moveOctave(it) } }
        val presets: @Composable ColumnScope.() -> Unit = {
            PresetPanel(tabs, tab, chosen, openValves, key, pal, scaleRun != null, arp, arpRun != null, onArp = { toggleArp() }, onScale = { playScale() }, onTab = { tab = it; onTab(it) },
                seq = SeqUi(seqMode, seqOrder.toList(), seqRun != null, seqRun?.let { seqOrder.getOrNull(it.index % maxOf(1, seqOrder.size)) }, settings.bpm,
                    settings.xfade, settings.xfadeS,
                    onMode = { seqMode = !seqMode }, onPlay = { toggleSequence() }, onBpm = { settings.bpm = it.coerceIn(SeqRun.BPM.first, SeqRun.BPM.last); onSettings() },
                    onXfade = { settings.xfade = !settings.xfade; onSettings() }, onXfadeS = { settings.xfadeS = it.coerceIn(0.2f, 10f); onSettings() }),
                onEdit = { editing = it }) { t, i -> choose(t, i) }
        }
        val gear: @Composable () -> Unit = { Gear(pal) { inSettings = true } }
        val autoStatus = if (!auto) null else run?.let { r ->
            val n = "${Shruti.NAMES[pitchClass(r.note)]} ${noteName(r.note)}"
            "${r.index + 1}/${r.notes.size} " + (if (r.phase == AutoRun.Phase.PLAY) "listen $n" else "sing $n")
        } ?: if (autoDone) "scale complete · AUTO again to stop" else "starting"
        if (step < 0) Unit                                          // step only makes the status recompose
        val trainer: @Composable () -> Unit = {
            if (settings.showTrainer) Trainer(mixer, ear, target, key, pal, armed && micGranted, auto, autoStatus, readout, settings.within,
                onWithin = { settings.within = it; onSettings() },
                onAuto = {
                    // AUTO starts at once from the beginning of the scale; AUTO again stops it (v8)
                    auto = !auto
                    if (!auto) { run = null; autoDone = false; hushAll(); armed = false }
                    else startAuto(AutoRun.firstInRange(settings.rangeLow, key, liveScaleOrMajor()))
                }) {
                // VOICE stops the shruti at once: the microphone must hear the singer, never the box
                if (!armed || !micGranted) { hushAll(); chosen = null }
                if (!micGranted) { askMic(); armed = true } else armed = !armed
            }
        }

        // v13: naming a CUSTOM card, over the whole screen; DELETE removes it
        editing?.let { i ->
            custom.getOrNull(i)?.let { p ->
                NameDialog(p.name, pal, onDone = { renameCustom(i, it); editing = null }, onDelete = { deleteCustom(i); editing = null }) { editing = null }
                return@Box
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth > maxHeight && !settings.showPresets) {
                // landscape without presets: the box alone in the middle, as wide as the height leaves room for
                val reserve = 54.dp + (if (settings.showNotes) 92.dp else 0.dp) + (if (settings.showKeys) 58.dp else 0.dp) +
                    (if (settings.showTrainer) 70.dp else 0.dp)
                val boxWidth = minOf(maxWidth, (maxHeight - reserve) * (BOX_W / BOX_H))
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth()) { Spacer(Modifier.weight(1f)); gear() }
                    Spacer(Modifier.weight(1f))
                    Column(Modifier.width(boxWidth)) { box(); notes(); trainer() }
                    Spacer(Modifier.weight(1f))
                    keys()
                }
            } else if (maxWidth > maxHeight) {
                // landscape: the box, its notes and the keys on the left, the presets on the right
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(1.15f).fillMaxHeight()) {
                        Spacer(Modifier.weight(1f)); box(); notes(); trainer(); Spacer(Modifier.weight(1f)); keys()
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.fillMaxWidth()) { Spacer(Modifier.weight(1f)); gear() }
                        if (settings.showPresets) presets() else Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth()) { Spacer(Modifier.weight(1f)); gear() }
                    if (!settings.showPresets) Spacer(Modifier.weight(1f))
                    box(); notes(); trainer()
                    if (settings.showPresets) presets() else Spacer(Modifier.weight(1f))
                    keys()
                }
            }
        }
    }
}

/**
 * The settings button: Android's own settings gear (v12, Marko: "It should look exactly like Android setting icon"),
 * Material Icons' "settings" path on its 24-unit grid, filled in the scheme's light, its middle open.
 */
private const val ANDROID_SETTINGS = "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"

@Composable
private fun Gear(pal: Palette, onClick: () -> Unit) {
    val gear = remember { androidx.compose.ui.graphics.vector.PathParser().parsePathString(ANDROID_SETTINGS).toPath().apply { fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd } }
    Box(Modifier.size(48.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(26.dp)) {
            val k = size.minDimension / 24f
            withTransform({ scale(k, k, pivot = Offset.Zero) }) { drawPath(gear, pal.light) }
        }
    }
}

/** A row of pills, one lit: the scheme's light on black. Used for the presets' tabs, the keys and the settings. */
@Composable
private fun Pills(labels: List<String>, selected: Int, pal: Palette, height: Int = 44, fontSize: Int = 11, onPick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { i, name ->
            val on = i == selected
            Box(
                Modifier.weight(1f).height(height.dp)
                    .background(if (on) pal.light else BLACK, RoundedCornerShape((height / 2).dp))
                    .border(1.dp, if (on) pal.light else pal.faint, RoundedCornerShape((height / 2).dp))
                    .clickable { onPick(i) },
                contentAlignment = Alignment.Center
            ) { BasicText(name, style = MONO.copy(color = if (on) BLACK else pal.light.copy(alpha = 0.75f), fontSize = fontSize.sp, letterSpacing = 0.sp)) }
        }
    }
}

@Composable
private fun OctaveRow(octave: Int, key: Int, pal: Palette, onOctave: (Int) -> Unit) {
    // v12 (Marko: "instead of the list of all the keys at the bottom, just add one two buttons arrow left and right"):
    // ◀ ▶ move the box an octave down or up; between them, where Sa is now. The key itself is in settings.
    val low = octave > Shruti.LOWEST_OCTAVE
    val high = octave < Shruti.HIGHEST_OCTAVE
    @Composable fun Arrow(glyph: String, on: Boolean, to: Int) = Box(
        Modifier.size(width = 88.dp, height = 44.dp)
            .border(1.dp, if (on) pal.light else pal.faint, RoundedCornerShape(22.dp))
            .clickable(enabled = on) { onOctave(to) },
        contentAlignment = Alignment.Center
    ) { BasicText(glyph, style = MONO.copy(fontSize = 18.sp, letterSpacing = 0.sp, color = if (on) pal.light else pal.line)) }
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Arrow("◀", low, octave - 1)
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BasicText("Sa = ${Shruti.KEYS[key]}${3 + octave}", style = MONO.copy(fontSize = 14.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = pal.light))
        }
        Arrow("▶", high, octave + 1)
    }
}

/**
 * Under the box, as on the reference page: Reset, the notes that sound, and what they make. Each note is a button
 * (v10, Marko: "as in Fruity Loops"): its light fills it at 0 dB, half of it at −12 dB, a line at its foot at −24 dB;
 * a short tap steps it to the next.
 */
@Composable
private fun NotesRow(
    openValves: List<Int>, buttons: List<Int>, levels: List<Int>, key: Int, pal: Palette, anyOpen: Boolean, cardName: String?,
    onSave: () -> Unit, onLevel: (Int) -> Unit, onReset: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp, start = 6.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.height(40.dp)
                .border(1.dp, if (anyOpen) pal.light else pal.faint, RoundedCornerShape(20.dp))
                .clickable(enabled = anyOpen, onClick = onReset)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) { BasicText("RESET", style = MONO.copy(color = if (anyOpen) pal.light else pal.line, fontSize = 12.sp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            if (buttons.isEmpty()) BasicText("—", style = MONO.copy(fontSize = 12.sp, color = pal.light.copy(alpha = 0.85f)))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (v in buttons) LevelButton(v, levels.getOrElse(v) { 0 }, key, pal) { onLevel(v) }
                // v13: after the notes, the save icon keeps them as a CUSTOM card
                if (openValves.isNotEmpty()) SaveIcon(pal, onSave)
            }
            val named = cardName ?: Shruti.detect(key, openValves)
            if (named.isNotEmpty()) BasicText(named, style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = pal.halo, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** One note's button: an outline, and inside it as much light as the note is loud — all of it, half, or a line. */
@Composable
private fun LevelButton(valve: Int, level: Int, key: Int, pal: Palette, onTap: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier.size(width = 46.dp, height = 58.dp).clip(shape).background(BLACK).border(1.dp, pal.light, shape).clickable(onClick = onTap)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val fill = Loudness.FILL[level.coerceIn(0, Loudness.FILL.size - 1)]
            val h = maxOf(size.height * fill, 3.dp.toPx())
            drawRect(Brush.horizontalGradient(listOf(pal.light, pal.core, pal.light)), Offset(0f, size.height - h), Size(size.width, h))
        }
        val ink = if (level == 0) pal.ink else pal.light
        Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(Shruti.western(key, valve), style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Bold, color = ink))
            BasicText(Shruti.NAMES[valve], style = MONO.copy(fontSize = 10.sp, letterSpacing = 0.sp, color = ink.copy(alpha = 0.75f)))
        }
    }
}

/** The page's five tabs of presets and their cards. A card lights while it is the chosen one and its valves sound. */
@Composable
private fun ColumnScope.PresetPanel(
    tabs: List<PresetTab>, tab: Int, chosen: Pair<Int, Int>?, openValves: List<Int>, key: Int, pal: Palette, scaling: Boolean, arp: Boolean, arping: Boolean,
    onArp: () -> Unit, onScale: () -> Unit, onTab: (Int) -> Unit, seq: SeqUi, onEdit: (Int) -> Unit, onChoose: (Int, Int) -> Unit
) {
    val isCustom = tab == tabs.size - 1
    Row(Modifier.padding(top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { Pills(tabs.map { it.title }, tab, pal, height = 36, fontSize = 10, onPick = onTab) }
        Spacer(Modifier.width(6.dp))
        // ARP (v8): a switch of its own, beside the tabs; lit while it is on
        Box(
            Modifier.height(36.dp)
                .background(if (arp) pal.light else BLACK, RoundedCornerShape(18.dp))
                .border(1.dp, if (arp) pal.light else pal.faint, RoundedCornerShape(18.dp))
                .clickable(onClick = onArp)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) { BasicText("ARP", style = MONO.copy(color = if (arp) BLACK else pal.light.copy(alpha = 0.75f), fontSize = 10.sp, letterSpacing = 0.5.sp)) }
    }
    val current = tabs[tab]
    if (isCustom) SequencerBar(seq, pal)
    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
        BasicText(current.intro, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.5f)), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
        current.presets.forEachIndexed { i, p ->
            val number = if (isCustom) seq.order.indexOf(i).takeIf { it >= 0 }?.plus(1) else null
            val sounding = (chosen == (tab to i) && (openValves == p.valves.sorted() || scaling || arping)) || (isCustom && seq.playingCard == i)
            Column(
                Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .background(if (sounding) pal.cardOn else BLACK, RoundedCornerShape(16.dp))
                    .border(1.dp, if (sounding) pal.light else pal.faint, RoundedCornerShape(16.dp))
                    .clickable { onChoose(tab, i) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // v13: the sequencer's number, given by the taps in SEQ, as the Chrome profiles in Opening and Closing
                    if (isCustom && (seq.mode || number != null)) Box(
                        Modifier.padding(end = 10.dp).size(26.dp)
                            .background(if (number != null) pal.light else BLACK, RoundedCornerShape(6.dp))
                            .border(1.dp, if (number != null) pal.light else pal.line, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) { BasicText(number?.toString() ?: "·", style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Bold, color = if (number != null) pal.ink else pal.line)) }
                    BasicText(p.name, style = MONO.copy(fontSize = 14.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold, color = if (sounding) pal.core else pal.light))
                    Spacer(Modifier.weight(1f))
                    BasicText(p.valves.sorted().joinToString(" ") { Shruti.western(key, it) }, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.halo))
                    // v13: ✎ names or deletes a CUSTOM card (a button to see, not a hidden long press)
                    if (isCustom) Box(Modifier.padding(start = 6.dp).size(36.dp).clickable { onEdit(i) }, contentAlignment = Alignment.Center) {
                        BasicText("✎", style = MONO.copy(fontSize = 16.sp, letterSpacing = 0.sp, color = pal.light))
                    }
                    // the chosen raga plays its aroha and avaroha; a second tap stops it and the drone returns
                    if (sounding && p.up.isNotEmpty()) Box(
                        Modifier.padding(start = 10.dp).height(30.dp)
                            .background(if (scaling) pal.light else BLACK, RoundedCornerShape(15.dp))
                            .border(1.dp, pal.light, RoundedCornerShape(15.dp))
                            .clickable(onClick = onScale).padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { BasicText(if (scaling) "■ STOP" else "▶ SCALE", style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold, color = if (scaling) pal.ink else pal.light)) }
                }
                if (p.level.isNotEmpty()) BasicText(p.level.uppercase(), style = MONO.copy(fontSize = 9.sp, letterSpacing = 2.sp, color = pal.light.copy(alpha = 0.45f)), modifier = Modifier.padding(top = 2.dp))
                BasicText(p.about, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.75f)), modifier = Modifier.padding(top = 4.dp))
                BasicText(p.usedIn, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.45f)), modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/** Settings (v5): the colour scheme, the layout, which parts show, and the version. Back or DONE returns. */
@Composable
private fun SettingsScreen(s: Settings, pal: Palette, midiNames: List<String>, key: Int, onKey: (Int) -> Unit, onVersion: () -> Unit, onDone: () -> Unit) {
    val head = MONO.copy(fontSize = 11.sp, letterSpacing = 2.sp, color = pal.light.copy(alpha = 0.5f))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicText("SETTINGS", style = MONO.copy(fontSize = 14.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold, color = pal.light))
            Spacer(Modifier.weight(1f))
            BasicText("DONE", style = MONO.copy(fontSize = 13.sp, color = pal.light), modifier = Modifier.clickable(onClick = onDone).padding(14.dp))
        }
        BasicText("COLOUR", style = head, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
        Pills(PALETTES.map { it.name }, s.palette, pal) { s.palette = it }
        // v12: the key moved here from under the box, where ◀ ▶ are now
        BasicText("KEY", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Pills(Shruti.KEYS, key, pal, height = 40, fontSize = 10, onPick = onKey)
        // v13: the voice trainer's difficulty and range (a long press on AUTO sets the difficulty too)
        BasicText("VOICE TRAINER · DIFFICULTY", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Pills(AutoRun.DIFFICULTIES.map { "±$it %" }, AutoRun.DIFFICULTIES.indexOf(s.within), pal, height = 40, fontSize = 11) { s.within = AutoRun.DIFFICULTIES[it] }
        BasicText("VOICE TRAINER · RANGE (88 KEYS, A0 TO C8)", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        RangeStepper("FROM", s.rangeLow, pal) { s.rangeLow = it.coerceIn(AutoRun.PIANO.first, s.rangeHigh) }
        RangeStepper("TO", s.rangeHigh, pal) { s.rangeHigh = it.coerceIn(s.rangeLow, AutoRun.PIANO.last) }
        BasicText("LAYOUT", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Pills(listOf("PORTRAIT", "LANDSCAPE"), if (s.landscape) 1 else 0, pal) { s.landscape = it == 1 }
        BasicText("SHOW", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        for ((label, on, set) in listOf<Triple<String, Boolean, (Boolean) -> Unit>>(
            Triple("NAMES ON THE VALVES", s.showNames) { v: Boolean -> s.showNames = v },
            Triple("RESET AND NOTES", s.showNotes) { v: Boolean -> s.showNotes = v },
            Triple("PRESETS", s.showPresets) { v: Boolean -> s.showPresets = v },
            Triple("OCTAVE ◀ ▶", s.showKeys) { v: Boolean -> s.showKeys = v },
            Triple("VOICE TRAINER", s.showTrainer) { v: Boolean -> s.showTrainer = v },
        )) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, if (on) pal.light else pal.faint, RoundedCornerShape(22.dp))
                    .clickable { set(!on) }.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicText(label, style = MONO.copy(fontSize = 12.sp, letterSpacing = 1.sp, color = if (on) pal.light else pal.line))
                Spacer(Modifier.weight(1f))
                BasicText(if (on) "ON" else "OFF", style = MONO.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) pal.halo else pal.line))
            }
        }
        // v11: which MIDI controller plays the box, and how
        BasicText("MIDI", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        BasicText(
            if (midiNames.isEmpty()) "no controller · plug one into USB" else midiNames.joinToString("\n"),
            style = MONO.copy(fontSize = 13.sp, letterSpacing = 0.sp, color = if (midiNames.isEmpty()) pal.line else pal.light)
        )
        BasicText(
            "a key held opens its valve and released closes it, as loud as it is struck, at its own pitch; the box moves to the octave you play; its pads in Drum mode choose the cards of the open tab",
            style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.5f)),
            modifier = Modifier.padding(top = 4.dp)
        )
        BasicText("VERSION", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        BasicText(
            "v${BuildConfig.VERSION_NAME}   latest release →",
            style = MONO.copy(fontSize = 13.sp, letterSpacing = 0.sp, color = pal.light),
            modifier = Modifier.clickable(onClick = onVersion).padding(vertical = 10.dp)
        )
    }
}

/**
 * The voice trainer (v6): play a valve, close it, and when the reed has died away the microphone opens; sing the note
 * back. A row of oval LEDs as on a tuner: the middle one is the exact note, the last valve opened, named under it;
 * flat lights to the left, sharp to the right, each LED ten percent of a semitone; the number says how far, ±.
 */
@Composable
private fun Trainer(
    mixer: Mixer, ear: Ear, target: Int?, key: Int, pal: Palette, armed: Boolean, auto: Boolean, autoStatus: String?,
    readout: Readout, within: Int, onWithin: (Int) -> Unit, onAuto: () -> Unit, onArm: () -> Unit
) {
    // v13 (Marko: "A long press on auto sets its difficulty"): the three tolerances, shown under the row until one is chosen
    var choosing by remember { mutableStateOf(false) }
    val live by rememberUpdatedState(armed && target != null)
    val goal by rememberUpdatedState(target?.let { Shruti.freq(key, it) })
    var listening by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf<Double?>(null) }
    var heard by remember { mutableStateOf("") }
    var shownAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var seen = 0L
        while (isActive) withFrameNanos { now ->
            // the microphone only while the trainer is on and every reed is silent: it never hears the shruti
            val silent = (0 until Shruti.COUNT).all { mixer.level(it) == 0.0 }
            val want = live && silent && !ear.failed
            if (want != ear.on) ear.listen(want)
            listening = ear.on
            if (listening && ear.heardAt != seen) {
                seen = ear.heardAt
                val hz = ear.hz
                val g = goal
                if (hz > 0 && g != null) {
                    // a tuner's needle: it follows the voice at once, only the last few milliseconds of jitter are
                    // smoothed; a jump of more than a semitone is followed without smoothing
                    val now0 = Pitch.percentOff(hz, g)
                    val was = percent
                    percent = if (was != null && abs(now0 - was) < 100) was + 0.5 * (now0 - was) else now0
                    heard = Pitch.note(hz); shownAt = now
                }
            }
            // the last reading stays for a moment after the voice stops, then the row goes dark
            if (percent != null && now - shownAt > 600_000_000L) percent = null
            readout.percent = if (listening) percent else null
        }
    }
    DisposableEffect(Unit) { onDispose { ear.listen(false) } }

    val p = percent
    val lit = p?.let { Pitch.led(it) }
    val label = target?.let { "${Shruti.NAMES[it]} ${Shruti.western(key, it)}" } ?: "—"
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, start = 6.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.height(40.dp)
                .background(if (armed) pal.light else BLACK, RoundedCornerShape(20.dp))
                .border(1.dp, if (armed) pal.light else pal.faint, RoundedCornerShape(20.dp))
                .clickable(onClick = onArm)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) { BasicText("VOICE", style = MONO.copy(color = if (armed) BLACK else pal.light.copy(alpha = 0.75f), fontSize = 12.sp)) }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier.height(40.dp)
                .background(if (auto) pal.light else BLACK, RoundedCornerShape(20.dp))
                .border(1.dp, if (auto) pal.light else pal.faint, RoundedCornerShape(20.dp))
                .pointerInput(Unit) { detectTapGestures(onLongPress = { choosing = true }, onTap = { onAuto() }) }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText("AUTO", style = MONO.copy(color = if (auto) BLACK else pal.light.copy(alpha = 0.75f), fontSize = 12.sp))
                BasicText("±$within", style = MONO.copy(color = if (auto) BLACK else pal.light.copy(alpha = 0.5f), fontSize = 8.sp, letterSpacing = 0.sp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.fillMaxWidth().height(22.dp)) {
                val n = Pitch.SIDE * 2 + 1
                val slot = size.width / n
                for (i in 0 until n) {
                    val led = i - Pitch.SIDE
                    val middle = led == 0
                    val w = slot * (if (middle) 0.78f else 0.6f)
                    val h = size.height * (if (middle) 0.9f else 0.62f)
                    val tl = Offset(slot * i + (slot - w) / 2f, (size.height - h) / 2f)
                    if (lit == led) {
                        drawIntoCanvas { c ->
                            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                            paint.maskFilter = BlurMaskFilter(h * 0.6f, BlurMaskFilter.Blur.NORMAL)
                            paint.color = pal.halo.toArgb()
                            c.nativeCanvas.drawOval(tl.x, tl.y, tl.x + w, tl.y + h, paint)
                        }
                        drawOval(if (middle) pal.core else pal.light, tl, Size(w, h))
                    } else {
                        drawOval(pal.faint.copy(alpha = if (middle) 0.9f else 0.6f), tl, Size(w, h))
                        drawOval(if (middle) pal.line else pal.faint, tl, Size(w, h), style = Stroke(width = 1.5f))
                    }
                }
            }
            val reading = p?.let { (if (it > 0.5) "+" else if (it < -0.5) "−" else "") + "${abs(it).roundToInt()} %" }
            val words = when {
                autoStatus != null -> autoStatus + (reading?.let { "   $it" } ?: "")
                !armed -> label
                target == null -> "play a note"
                !listening -> "close the shruti, then sing $label"
                p == null -> "sing $label"
                else -> (if (p > 0.5) "+" else if (p < -0.5) "−" else "") + "${abs(p).roundToInt()} %   $label   you: $heard"
            }
            BasicText(
                words,
                style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = if (p != null && abs(p) < 5) pal.halo else pal.light.copy(alpha = 0.8f), fontWeight = if (p != null) FontWeight.Bold else FontWeight.Normal),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    if (choosing) Column(Modifier.fillMaxWidth().padding(top = 8.dp, start = 6.dp, end = 6.dp)) {
        BasicText("AUTO DIFFICULTY · how close the voice must stay", style = MONO.copy(fontSize = 10.sp, letterSpacing = 1.sp, color = pal.light.copy(alpha = 0.5f)), modifier = Modifier.padding(bottom = 6.dp))
        Pills(AutoRun.DIFFICULTIES.map { "±$it %" }, AutoRun.DIFFICULTIES.indexOf(within), pal, height = 36, fontSize = 11) { onWithin(AutoRun.DIFFICULTIES[it]); choosing = false }
    }
}

/** A MIDI note's name with its octave, as on a piano: 48 is C3, 21 is A0, 108 is C8. */
private fun noteName(note: Int): String = Shruti.KEYS[Math.floorMod(note, 12)] + (Math.floorDiv(note, 12) - 1)

/** What the sequencer bar shows and does (v13). */
private class SeqUi(
    val mode: Boolean, val order: List<Int>, val playing: Boolean, val playingCard: Int?, val bpm: Int, val xfade: Boolean, val xfadeS: Float,
    val onMode: () -> Unit, val onPlay: () -> Unit, val onBpm: (Int) -> Unit, val onXfade: () -> Unit, val onXfadeS: (Float) -> Unit
)

/** A small round button with a word or a sign, lit when [on]. */
@Composable
private fun Chip(text: String, on: Boolean, pal: Palette, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.height(36.dp)
            .background(if (on) pal.light else BLACK, RoundedCornerShape(18.dp))
            .border(1.dp, if (on) pal.light else if (enabled) pal.faint else pal.faint.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) { BasicText(text, style = MONO.copy(color = if (on) BLACK else pal.light.copy(alpha = if (enabled) 0.8f else 0.3f), fontSize = 11.sp, letterSpacing = 0.5.sp)) }
}

/**
 * The sequencer (v13): SEQ numbers the cards by taps, ▶ plays them in turn one beat each and ■ stops; − BPM + is its
 * speed; XFADE − s + fades one chord into the next over that time (Marko: "so it's not hard sequencing").
 */
@Composable
private fun SequencerBar(q: SeqUi, pal: Palette) {
    val num = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Bold, color = pal.light)
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("SEQ", q.mode, pal, onClick = q.onMode)
            Chip(if (q.playing) "■ STOP" else "▶ SEQUENCE", q.playing, pal, enabled = q.playing || q.order.isNotEmpty(), onClick = q.onPlay)
            Spacer(Modifier.weight(1f))
            Chip("−", false, pal) { q.onBpm(q.bpm - 5) }
            BasicText("${q.bpm} BPM", style = num)
            Chip("+", false, pal) { q.onBpm(q.bpm + 5) }
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("XFADE", q.xfade, pal, onClick = q.onXfade)
            Spacer(Modifier.weight(1f))
            Chip("−", false, pal, enabled = q.xfade) { q.onXfadeS(((q.xfadeS - 0.5f) * 10).roundToInt() / 10f) }
            BasicText(String.format(java.util.Locale.ROOT, "%.1f s", q.xfadeS), style = num.copy(color = if (q.xfade) pal.light else pal.line))
            Chip("+", false, pal, enabled = q.xfade) { q.onXfadeS(((q.xfadeS + 0.5f) * 10).roundToInt() / 10f) }
        }
    }
}

/** Material Icons' "save" (a floppy disk), on its 24-unit grid: after the notes, it keeps them as a CUSTOM card (v13). */
private const val ANDROID_SAVE = "M17,3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V7l-4,-4zM12,19c-1.66,0 -3,-1.34 -3,-3s1.34,-3 3,-3 3,1.34 3,3 -1.34,3 -3,3zM15,9H5V5h10v4z"

@Composable
private fun SaveIcon(pal: Palette, onSave: () -> Unit) {
    val icon = remember { androidx.compose.ui.graphics.vector.PathParser().parsePathString(ANDROID_SAVE).toPath().apply { fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd } }
    val shape = RoundedCornerShape(10.dp)
    Box(Modifier.size(width = 46.dp, height = 58.dp).clip(shape).border(1.dp, pal.light, shape).clickable(onClick = onSave), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(24.dp)) {
            val k = size.minDimension / 24f
            withTransform({ scale(k, k, pivot = Offset.Zero) }) { drawPath(icon, pal.light) }
        }
    }
}

/** Naming a CUSTOM card (v13): the name in a field, SAVE, DELETE, CANCEL. */
@Composable
private fun NameDialog(name: String, pal: Palette, onDone: (String) -> Unit, onDelete: () -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(name, androidx.compose.ui.text.TextRange(0, name.length))) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler(onBack = onCancel)
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 40.dp)) {
        BasicText("NAME THIS PRESET", style = MONO.copy(fontSize = 13.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold, color = pal.light))
        androidx.compose.foundation.text.BasicTextField(
            value = text, onValueChange = { text = it }, singleLine = true,
            textStyle = MONO.copy(fontSize = 20.sp, letterSpacing = 0.sp, color = pal.core),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(pal.light),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onDone(text.text) }),
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).border(1.dp, pal.light, RoundedCornerShape(12.dp)).padding(14.dp).focusRequester(focus)
        )
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("SAVE", true, pal) { onDone(text.text) }
            Chip("CANCEL", false, pal, onClick = onCancel)
            Spacer(Modifier.weight(1f))
            Chip("DELETE", false, pal, onClick = onDelete)
        }
    }
}

/** The box itself: the thirteen valves, drawn every frame so the glow breathes with the bellows. */
@Composable
private fun ValveBox(
    mixer: Mixer, open: List<Boolean>, key: Int, pal: Palette, showNames: Boolean, marks: Set<Int>, levels: List<Int>,
    onToggle: (Int) -> Unit
) {
    var frame by remember { mutableLongStateOf(0L) }
    val tabs = remember { mutableStateListOf<Tab>() }
    val measurer = rememberTextMeasurer()
    LaunchedEffect(Unit) { while (isActive) withFrameNanos { frame = it } }

    // the box, as on the reference page: 680 × 230 of its units, the valves drawn inside with room for their glow
    Canvas(
        Modifier.fillMaxWidth().aspectRatio(BOX_W / BOX_H).pointerInput(Unit) {
            detectTapGestures { p ->
                // the upper valves lie over the top of the lower ones, so they are asked first, as they are drawn last
                val hit = tabs.sortedByDescending { it.upper }.firstOrNull { it.rect.contains(p) }
                if (hit != null) onToggle(hit.valve)
            }
        }
    ) {
        if (frame < 0L) return@Canvas
        val laid = layValves()
        if (tabs.size != laid.size || tabs.zip(laid).any { (a, b) -> a.rect != b.rect }) { tabs.clear(); tabs.addAll(laid) }
        val breath = ((mixer.breath() - 1.0) / Mixer.BELLOWS_DEPTH).toFloat()      // -1..1
        val t = frame / 1e9
        for (upper in listOf(false, true)) for (vt in laid.filter { it.upper == upper }) {
            val on = open[vt.valve]
            drawValve(vt, on, vt.valve in marks, mixer.level(vt.valve).toFloat(), breath, t, pal)
            val r0 = vt.rect
            // a valve below 0 dB says so near its top
            val lv = levels.getOrElse(vt.valve) { 0 }
            if (lv != 0) {
                val tag = measurer.measure(Loudness.LABELS[lv].removeSuffix(" dB"), MONO.copy(fontSize = if (vt.upper) 8.sp else 10.sp, letterSpacing = 0.sp, color = if (on) pal.ink else pal.halo, fontWeight = FontWeight.Bold))
                drawText(tag, topLeft = Offset(r0.center.x - tag.size.width / 2f, if (vt.upper) r0.top + r0.height * 0.1f else r0.top + r0.height * 0.08f))
            }
            if (!showNames) continue
            // the Western note over the Indian name, in the middle of the valve, as on the page
            val ink = if (on) pal.ink else pal.light
            val size = if (vt.upper) 10.sp else 13.sp
            val west = measurer.measure(Shruti.western(key, vt.valve), MONO.copy(fontSize = size, letterSpacing = 0.sp, color = ink, fontWeight = FontWeight.Bold))
            val ind = measurer.measure(Shruti.NAMES[vt.valve], MONO.copy(fontSize = size * 0.9f, letterSpacing = 0.sp, color = ink.copy(alpha = 0.7f)))
            val r = vt.rect
            val cy = if (vt.upper) r.top + r.height * 0.5f else r.top + r.height * 0.62f
            val top = cy - (west.size.height + ind.size.height) / 2f
            drawText(west, topLeft = Offset(r.center.x - west.size.width / 2f, top))
            drawText(ind, topLeft = Offset(r.center.x - ind.size.width / 2f, top + west.size.height))
        }
    }
}

// The reference page's box (reference-v2-layout.html), in its own pixels: eight valves 70 wide every 80, 140 tall
// (Marko's 2:1); five 40 × 100 over the gaps between them, their lower part over the top of the eight. A margin of
// 20 all round leaves room for the glow.
private const val BOX_W = 680f
private const val BOX_H = 230f

/** Where the thirteen valves are, in the page's placement, scaled to the canvas. */
private fun DrawScope.layValves(): List<Tab> {
    val u = size.width / BOX_W
    val lower = listOf(0, 2, 4, 5, 7, 9, 11, 12)
    val upperAfter = mapOf(1 to 0, 3 to 1, 6 to 3, 8 to 4, 10 to 5)                  // valve to the lower slot it follows
    val tabs = ArrayList<Tab>(Shruti.COUNT)
    lower.forEachIndexed { i, v -> tabs += Tab(v, Rect(Offset((20f + 80f * i + 5f) * u, 70f * u), Size(70f * u, 140f * u)), false) }
    upperAfter.forEach { (v, after) -> tabs += Tab(v, Rect(Offset((20f + 80f * (after + 1) - 20f) * u, 20f * u), Size(40f * u, 100f * u)), true) }
    return tabs
}

/**
 * A closed valve is a thin line of light; an open one burns in the scheme's light with a white heart and a halo that
 * breathes. A valve [marked] as a note of the chosen raga has a brighter outline and a dot of light at its foot.
 */
private fun DrawScope.drawValve(tab: Tab, isOpen: Boolean, marked: Boolean, level: Float, breath: Float, t: Double, pal: Palette) {
    val r = tab.rect
    val radius = CornerRadius(minOf(r.width, r.height) * 0.22f)
    val edge = r.width * 0.035f

    // at rest: black (an upper valve hides what it lies over), a faint inner light and an outline
    drawRoundRect(BLACK, r.topLeft, r.size, radius)
    drawRoundRect(Brush.verticalGradient(listOf(pal.faint.copy(alpha = 0.7f), BLACK), r.top, r.bottom), r.topLeft, r.size, radius)
    drawRoundRect(if (isOpen) pal.light else if (marked) pal.light.copy(alpha = 0.75f) else pal.line, r.topLeft, r.size, radius, style = Stroke(width = edge))
    if (marked) drawCircle(pal.halo, r.width * 0.07f, Offset(r.center.x, r.bottom - r.width * 0.2f))
    if (level <= 0f) return

    // the halo: a real blur around the valve, wider and brighter as the bellows breathe in
    val pulse = 0.82f + 0.18f * breath
    drawIntoCanvas { c ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        for ((spread, alpha) in listOf(0.45f to 0.55f, 0.2f to 0.8f)) {
            paint.maskFilter = BlurMaskFilter(r.width * spread * pulse, BlurMaskFilter.Blur.NORMAL)
            paint.color = pal.halo.copy(alpha = alpha * level * pulse).toArgb()
            c.nativeCanvas.drawRoundRect(r.left, r.top, r.right, r.bottom, radius.x, radius.y, paint)
        }
    }
    // the body: the scheme's light, white towards the middle
    drawRoundRect(
        Brush.horizontalGradient(listOf(pal.light.copy(alpha = level), pal.core.copy(alpha = level), pal.light.copy(alpha = level)), r.left, r.right),
        r.topLeft, r.size, radius
    )
    // light rising through it, slowly, from below into the other world
    val shape = Path().apply { addRoundRect(RoundRect(r, radius)) }
    clipPath(shape) {
        val band = r.height * 0.45f
        val phase = ((t * 0.18 + tab.valve * 0.137) % 1.0).toFloat()
        val y = r.bottom + band - phase * (r.height + band * 2f)
        drawRect(
            Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.55f * level), Color.Transparent), y - band, y + band),
            Offset(r.left, y - band), Size(r.width, band * 2f)
        )
    }
    // a shimmer along the edge, never quite still
    val shimmer = 0.6f + 0.4f * sin(t * 2 * PI * 0.35 + tab.valve).toFloat()
    drawRoundRect(Color.White.copy(alpha = 0.7f * level * shimmer), r.topLeft, r.size, radius, style = Stroke(width = edge))
}

/** One end of the trainer's range: « an octave, ‹ a semitone, the note, › and » (v13). */
@Composable
private fun RangeStepper(label: String, note: Int, pal: Palette, onNote: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(label, style = MONO.copy(fontSize = 12.sp, letterSpacing = 1.sp, color = pal.light.copy(alpha = 0.7f)), modifier = Modifier.width(52.dp))
        Chip("«", false, pal) { onNote(note - 12) }
        Chip("‹", false, pal) { onNote(note - 1) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BasicText(noteName(note), style = MONO.copy(fontSize = 16.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = pal.light))
        }
        Chip("›", false, pal) { onNote(note + 1) }
        Chip("»", false, pal) { onNote(note + 12) }
    }
}
