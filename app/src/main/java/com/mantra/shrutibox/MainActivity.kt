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
private const val LATEST = "https://github.com/markoboskoauroville/MANTRA_SHRUTI_BOX/releases/latest"
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

    fun save(prefs: SharedPreferences) = prefs.edit {
        putInt("palette", palette); putBoolean("landscape", landscape); putBoolean("showNotes", showNotes)
        putBoolean("showPresets", showPresets); putBoolean("showKeys", showKeys); putBoolean("showNames", showNames)
        putBoolean("showTrainer", showTrainer)
    }
}

class MainActivity : ComponentActivity() {
    private val engine = Engine()
    private val ear = Ear()
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
        val settings = Settings(prefs)
        micGranted.value = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        fixLayout(settings.landscape)
        setContent {
            ShrutiScreen(
                mixer = engine.mixer,
                ear = ear,
                micGranted = micGranted.value,
                askMic = { askMic.launch(Manifest.permission.RECORD_AUDIO) },
                settings = settings,
                startTab = prefs.getInt("tab", 0),
                onKey = { prefs.edit { putInt("key", it) } },
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

    override fun onStart() { super.onStart(); engine.start() }
    override fun onStop() { ear.listen(false); engine.stop(); super.onStop() }
}

private fun openLatest(c: Context) {
    try {
        c.startActivity(Intent(Intent.ACTION_VIEW, LATEST.toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(c, "No browser", Toast.LENGTH_SHORT).show()
    }
}

/** The valves of the card [chosen], to sound again when ▶ SCALE ends. */
private fun chosenPresetValves(chosen: Pair<Int, Int>?): List<Int> =
    chosen?.let { (t, i) -> Presets.TABS.getOrNull(t)?.presets?.getOrNull(i)?.valves }.orEmpty()

/** The tuner's last reading, handed from the trainer to the auto walk without a recomposition per frame. */
private class Readout { var percent: Double? = null }

/** Where each valve is on the canvas, for drawing and for the finger. */
private class Tab(val valve: Int, val rect: Rect, val upper: Boolean)

private val MONO = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, letterSpacing = 1.5.sp)

@Composable
private fun ShrutiScreen(
    mixer: Mixer, ear: Ear, micGranted: Boolean, askMic: () -> Unit, settings: Settings, startTab: Int, onKey: (Int) -> Unit, onTab: (Int) -> Unit, onSettings: () -> Unit,
    onAnyOpen: (Boolean) -> Unit, onVersion: () -> Unit
) {
    val open = remember { mutableStateListOf(*Array(Shruti.COUNT) { mixer.isOpen(it) }) }
    var key by remember { mutableIntStateOf(mixer.key) }
    var tab by remember { mutableIntStateOf(startTab.coerceIn(0, Presets.TABS.size - 1)) }
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
    val chosenPreset = chosen?.let { (t, i) -> Presets.TABS.getOrNull(t)?.presets?.getOrNull(i) }
    // the chosen raga's notes, marked on the valves (v7, Marko: "yes, mark ragas"); Sa' is Sa, so it is marked too
    val marks = chosenPreset?.scale?.takeIf { it.isNotEmpty() }?.let { it.toSet() + 12 } ?: emptySet()

    val anyOpen = open.any { it }
    LaunchedEffect(anyOpen) { onAnyOpen(anyOpen) }

    fun hushAll() { scaleRun = null; arpRun = null; mixer.hush(); for (i in 0 until Shruti.COUNT) open[i] = false }
    fun openValves(): List<Int> = (0 until Shruti.COUNT).filter { open[it] }
    fun sound(valves: Collection<Int>) { mixer.closeAll(); for (i in 0 until Shruti.COUNT) { open[i] = i in valves; mixer.setOpen(i, open[i]) } }
    fun autoPlay(r: AutoRun) { hushAll(); open[r.note] = true; mixer.setOpen(r.note, true); target = r.note; armed = false }
    fun startAuto(v: Int) {
        if (!micGranted) askMic()
        arpRun = null
        // the chosen raga's notes, or Sa Re Ga ma Pa Dha Ni when no raga is chosen
        val scale = chosen?.let { (t, i) -> Presets.TABS.getOrNull(t)?.presets?.getOrNull(i)?.scale }?.takeIf { it.isNotEmpty() } ?: AutoRun.MAJOR
        val r = AutoRun(AutoRun.from(v, scale)); run = r; autoDone = false; step++; autoPlay(r)
    }
    LaunchedEffect(Unit) {
        while (isActive) withFrameNanos { now ->
            val sr = scaleRun
            if (sr != null && sr.tick(now / 1e9)) {
                if (sr.done) { scaleRun = null; sound(chosenPresetValves(chosen)) } else sound(listOf(sr.note))
            }
            val ar = arpRun
            if (ar != null && ar.tick(now / 1e9)) sound(listOf(ar.note))
            val r = run
            if (r != null && r.tick(now / 1e9, readout.percent)) {
                when (r.phase) {
                    AutoRun.Phase.PLAY -> autoPlay(r)
                    AutoRun.Phase.SING -> { hushAll(); target = r.note; readout.percent = null; armed = true }
                    AutoRun.Phase.DONE -> { armed = false; run = null; autoDone = true }
                }
                step++
            }
        }
    }

    // the shruti and the voice take turns (Marko, v6): opening a valve or a preset switches VOICE off
    fun toggle(v: Int) {
        // while AUTO runs, a tapped valve is the new beginning of the walk (v8)
        if (auto) { startAuto(v); return }
        scaleRun = null; arpRun = null
        val now = !open[v]; open[v] = now; mixer.setOpen(v, now); if (now) { target = v; armed = false }
    }
    fun closeAll() { scaleRun = null; arpRun = null; mixer.closeAll(); for (i in 0 until Shruti.COUNT) open[i] = false; chosen = null }
    /** ARP on or off; a card that sounds changes at once between its chord and its arpeggio. */
    // read the chosen card at the moment of the tap, not as it was when the button was composed
    fun liveChosen(): Preset? = chosen?.let { (t, i) -> Presets.TABS.getOrNull(t)?.presets?.getOrNull(i) }
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
        val p = Presets.TABS[t].presets[i]
        val again = chosen == (t to i)
        run = null
        closeAll()
        if (!again) {
            chosen = t to i; target = p.valves.min(); armed = false
            if (arp) { val a = ArpRun(p.valves); arpRun = a; sound(listOf(a.note)) } else for (v in p.valves) { open[v] = true; mixer.setOpen(v, true) }
        }
    }
    val openValves = (0 until Shruti.COUNT).filter { open[it] }

    BackHandler(enabled = inSettings) { inSettings = false; onSettings() }

    Box(Modifier.fillMaxSize().background(BLACK).windowInsetsPadding(WindowInsets.displayCutout).padding(horizontal = 10.dp, vertical = 10.dp)) {
        if (inSettings) {
            SettingsScreen(settings, pal, onVersion) { inSettings = false; onSettings() }
            return@Box
        }
        val box: @Composable () -> Unit = { ValveBox(mixer, open, key, pal, settings.showNames, marks) { toggle(it) } }
        // the chosen card names what sounds (Sus2 and Sus4 on Pa are the same notes; a raga is not a chord)
        val cardName = chosen?.let { (t, i) -> Presets.TABS.getOrNull(t)?.presets?.getOrNull(i) }?.takeIf { openValves == it.valves.sorted() }?.name
        val notes: @Composable () -> Unit = { if (settings.showNotes) NotesRow(openValves, key, pal, anyOpen, cardName) { closeAll() } }
        val keys: @Composable () -> Unit = { if (settings.showKeys) KeyRow(key, pal) { key = it; mixer.key = it; onKey(it) } }
        val presets: @Composable ColumnScope.() -> Unit = {
            PresetPanel(tab, chosen, openValves, key, pal, scaleRun != null, arp, arpRun != null, onArp = { toggleArp() }, onScale = { playScale() }, onTab = { tab = it; onTab(it) }) { t, i -> choose(t, i) }
        }
        val gear: @Composable () -> Unit = { Gear(pal) { inSettings = true } }
        val autoStatus = if (!auto) null else run?.let { r ->
            val n = "${Shruti.NAMES[r.note]} ${Shruti.western(key, r.note)}"
            "${r.index + 1}/${r.notes.size} " + (if (r.phase == AutoRun.Phase.PLAY) "listen $n" else "sing $n")
        } ?: if (autoDone) "scale complete · AUTO again to stop" else "starting"
        if (step < 0) Unit                                          // step only makes the status recompose
        val trainer: @Composable () -> Unit = {
            if (settings.showTrainer) Trainer(mixer, ear, target, key, pal, armed && micGranted, auto, autoStatus, readout,
                onAuto = {
                    // AUTO starts at once from the beginning of the scale; AUTO again stops it (v8)
                    auto = !auto
                    if (!auto) { run = null; autoDone = false; hushAll(); armed = false }
                    else startAuto((liveChosen()?.scale?.takeIf { it.isNotEmpty() } ?: AutoRun.MAJOR).min())
                }) {
                // VOICE stops the shruti at once: the microphone must hear the singer, never the box
                if (!armed || !micGranted) { hushAll(); chosen = null }
                if (!micGranted) { askMic(); armed = true } else armed = !armed
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth > maxHeight && !settings.showPresets) {
                // landscape without presets: the box alone in the middle, as wide as the height leaves room for
                val reserve = 54.dp + (if (settings.showNotes) 52.dp else 0.dp) + (if (settings.showKeys) 58.dp else 0.dp) +
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

/** The settings button: a small gear drawn in the scheme's light, where "SHRUTI" and the version were. */
@Composable
private fun Gear(pal: Palette, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(22.dp)) {
            val c = center
            val r = size.minDimension / 2f
            for (i in 0 until 8) {
                val a = i * PI / 4
                drawLine(pal.light.copy(alpha = 0.8f), Offset(c.x + (r * 0.55f) * cos(a).toFloat(), c.y + (r * 0.55f) * sin(a).toFloat()),
                    Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()), strokeWidth = r * 0.28f, cap = StrokeCap.Round)
            }
            drawCircle(pal.light.copy(alpha = 0.8f), r * 0.62f, c)
            drawCircle(BLACK, r * 0.28f, c)
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
private fun KeyRow(key: Int, pal: Palette, onKey: (Int) -> Unit) {
    Box(Modifier.padding(top = 10.dp)) { Pills(Shruti.KEYS, key, pal, onPick = onKey) }
}

/** Under the box, as on the reference page: Reset, the notes that sound, and what they make. */
@Composable
private fun NotesRow(openValves: List<Int>, key: Int, pal: Palette, anyOpen: Boolean, cardName: String?, onReset: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, start = 6.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.height(40.dp)
                .border(1.dp, if (anyOpen) pal.light else pal.faint, RoundedCornerShape(20.dp))
                .clickable(enabled = anyOpen, onClick = onReset)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) { BasicText("RESET", style = MONO.copy(color = if (anyOpen) pal.light else pal.line, fontSize = 12.sp)) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            BasicText(
                openValves.joinToString("  ") { "${Shruti.NAMES[it]} ${Shruti.western(key, it)}" }.ifEmpty { "—" },
                style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.85f))
            )
            val named = cardName ?: Shruti.detect(key, openValves)
            if (named.isNotEmpty()) BasicText(named, style = MONO.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = pal.halo, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** The page's five tabs of presets and their cards. A card lights while it is the chosen one and its valves sound. */
@Composable
private fun ColumnScope.PresetPanel(
    tab: Int, chosen: Pair<Int, Int>?, openValves: List<Int>, key: Int, pal: Palette, scaling: Boolean, arp: Boolean, arping: Boolean,
    onArp: () -> Unit, onScale: () -> Unit, onTab: (Int) -> Unit, onChoose: (Int, Int) -> Unit
) {
    Row(Modifier.padding(top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { Pills(Presets.TABS.map { it.title }, tab, pal, height = 36, fontSize = 10, onPick = onTab) }
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
    val current = Presets.TABS[tab]
    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
        BasicText(current.intro, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.light.copy(alpha = 0.5f)), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
        current.presets.forEachIndexed { i, p ->
            val sounding = chosen == (tab to i) && (openValves == p.valves.sorted() || scaling || arping)
            Column(
                Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .background(if (sounding) pal.cardOn else BLACK, RoundedCornerShape(16.dp))
                    .border(1.dp, if (sounding) pal.light else pal.faint, RoundedCornerShape(16.dp))
                    .clickable { onChoose(tab, i) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(p.name, style = MONO.copy(fontSize = 14.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold, color = if (sounding) pal.core else pal.light))
                    Spacer(Modifier.weight(1f))
                    BasicText(p.valves.sorted().joinToString(" ") { Shruti.western(key, it) }, style = MONO.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = pal.halo))
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
private fun SettingsScreen(s: Settings, pal: Palette, onVersion: () -> Unit, onDone: () -> Unit) {
    val head = MONO.copy(fontSize = 11.sp, letterSpacing = 2.sp, color = pal.light.copy(alpha = 0.5f))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicText("SETTINGS", style = MONO.copy(fontSize = 14.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold, color = pal.light))
            Spacer(Modifier.weight(1f))
            BasicText("DONE", style = MONO.copy(fontSize = 13.sp, color = pal.light), modifier = Modifier.clickable(onClick = onDone).padding(14.dp))
        }
        BasicText("COLOUR", style = head, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
        Pills(PALETTES.map { it.name }, s.palette, pal) { s.palette = it }
        BasicText("LAYOUT", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        Pills(listOf("PORTRAIT", "LANDSCAPE"), if (s.landscape) 1 else 0, pal) { s.landscape = it == 1 }
        BasicText("SHOW", style = head, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
        for ((label, on, set) in listOf<Triple<String, Boolean, (Boolean) -> Unit>>(
            Triple("NAMES ON THE VALVES", s.showNames) { v: Boolean -> s.showNames = v },
            Triple("RESET AND NOTES", s.showNotes) { v: Boolean -> s.showNotes = v },
            Triple("PRESETS", s.showPresets) { v: Boolean -> s.showPresets = v },
            Triple("KEYS", s.showKeys) { v: Boolean -> s.showKeys = v },
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
    readout: Readout, onAuto: () -> Unit, onArm: () -> Unit
) {
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
                .clickable(onClick = onAuto)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) { BasicText("AUTO", style = MONO.copy(color = if (auto) BLACK else pal.light.copy(alpha = 0.75f), fontSize = 12.sp)) }
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
}

/** The box itself: the thirteen valves, drawn every frame so the glow breathes with the bellows. */
@Composable
private fun ValveBox(mixer: Mixer, open: List<Boolean>, key: Int, pal: Palette, showNames: Boolean, marks: Set<Int>, onToggle: (Int) -> Unit) {
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
