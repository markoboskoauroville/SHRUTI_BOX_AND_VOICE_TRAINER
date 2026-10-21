package com.mantra.shrutibox

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

// v2 (Marko, 21.10.2026): not a wooden box any more. Black ground, the whole portrait screen is the instrument, the
// valves light blue and glowing, "as if coming from another dimension, from the reality of angels".
private val BLACK = Color(0xFF000000)
private val ANGEL = Color(0xFF9FE3FF)       // the light blue of a valve
private val CORE = Color(0xFFF2FCFF)        // the white heart of an open one
private val HALO = Color(0xFF5CC8FF)        // the light it throws around itself
private val LINE = Color(0xFF2E5F78)        // a closed valve's outline, the words at rest
private val FAINT = Color(0xFF16303D)

/** version-visible.md §3: the number in the header opens the latest build. */
private const val LATEST = "https://github.com/markoboskoauroville/MANTRA_SHRUTI_BOX/releases/latest"
private const val PREFS = "shruti"

class MainActivity : ComponentActivity() {
    private val engine = Engine()

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
        setContent {
            ShrutiScreen(
                mixer = engine.mixer,
                startTab = prefs.getInt("tab", 0),
                onKey = { prefs.edit { putInt("key", it) } },
                onTab = { prefs.edit { putInt("tab", it) } },
                onAnyOpen = { open ->
                    // a drone is played with the hands busy: the screen stays on while a reed sounds
                    if (open) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                },
                onVersion = { openLatest(this) }
            )
        }
    }

    override fun onStart() { super.onStart(); engine.start() }
    override fun onStop() { engine.stop(); super.onStop() }
}

private fun openLatest(c: Context) {
    try {
        c.startActivity(Intent(Intent.ACTION_VIEW, LATEST.toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(c, "No browser", Toast.LENGTH_SHORT).show()
    }
}

/** Where each valve is on the canvas, for drawing and for the finger. */
private class Tab(val valve: Int, val rect: Rect, val upper: Boolean)

@Composable
private fun ShrutiScreen(
    mixer: Mixer, startTab: Int, onKey: (Int) -> Unit, onTab: (Int) -> Unit, onAnyOpen: (Boolean) -> Unit, onVersion: () -> Unit
) {
    val open = remember { mutableStateListOf(*Array(Shruti.COUNT) { mixer.isOpen(it) }) }
    var key by remember { mutableIntStateOf(mixer.key) }
    var tab by remember { mutableIntStateOf(startTab.coerceIn(0, Presets.TABS.size - 1)) }
    var frame by remember { mutableLongStateOf(0L) }
    val tabs = remember { mutableStateListOf<Tab>() }
    val measurer = rememberTextMeasurer()
    val mono = TextStyle(fontFamily = FontFamily.Monospace, color = ANGEL, fontSize = 13.sp, letterSpacing = 1.5.sp)

    // the glow breathes with the bellows: one redraw per frame while the app is on screen
    LaunchedEffect(Unit) { while (isActive) withFrameNanos { frame = it } }
    val anyOpen = open.any { it }
    LaunchedEffect(anyOpen) { onAnyOpen(anyOpen) }

    fun toggle(v: Int) { val now = !open[v]; open[v] = now; mixer.setOpen(v, now) }
    fun closeAll() { mixer.closeAll(); for (i in 0 until Shruti.COUNT) open[i] = false }
    val openValves = (0 until Shruti.COUNT).filter { open[it] }

    Column(
        Modifier.fillMaxSize().background(BLACK).windowInsetsPadding(WindowInsets.displayCutout)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText("SHRUTI", style = mono.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 4.sp))
            Spacer(Modifier.weight(1f))
            BasicText(
                "v${BuildConfig.VERSION_NAME}",
                style = mono.copy(color = LINE, fontSize = 11.sp),
                modifier = Modifier.clickable { onVersion() }.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        // the box, as on the reference page: 680 × 230 of its units, the valves drawn inside with room for their glow
        Canvas(
            Modifier.fillMaxWidth().aspectRatio(BOX_W / BOX_H).pointerInput(Unit) {
                detectTapGestures { p ->
                    // the upper valves lie over the top of the lower ones, so they are asked first, as they are drawn last
                    val hit = tabs.sortedByDescending { it.upper }.firstOrNull { it.rect.contains(p) }
                    if (hit != null) toggle(hit.valve)
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
                drawValve(vt, on, mixer.level(vt.valve).toFloat(), breath, t)
                // the Western note over the Indian name, in the middle of the valve, as on the page
                val ink = if (on) Color(0xFF05222E) else ANGEL
                val size = if (vt.upper) 10.sp else 13.sp
                val west = measurer.measure(Shruti.western(key, vt.valve), mono.copy(fontSize = size, letterSpacing = 0.sp, color = ink, fontWeight = FontWeight.Bold))
                val ind = measurer.measure(Shruti.NAMES[vt.valve], mono.copy(fontSize = size * 0.9f, letterSpacing = 0.sp, color = ink.copy(alpha = 0.7f)))
                val r = vt.rect
                val cy = if (vt.upper) r.top + r.height * 0.5f else r.top + r.height * 0.62f
                val top = cy - (west.size.height + ind.size.height) / 2f
                drawText(west, topLeft = Offset(r.center.x - west.size.width / 2f, top))
                drawText(ind, topLeft = Offset(r.center.x - ind.size.width / 2f, top + west.size.height))
            }
        }

        // under the box, as on the page: Reset, the notes that sound, and what they make
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, start = 6.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.height(40.dp)
                    .background(BLACK, RoundedCornerShape(20.dp))
                    .border(1.dp, if (anyOpen) ANGEL else FAINT, RoundedCornerShape(20.dp))
                    .clickable(enabled = anyOpen) { closeAll() }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) { BasicText("RESET", style = mono.copy(color = if (anyOpen) ANGEL else LINE, fontSize = 12.sp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                BasicText(
                    openValves.joinToString("  ") { "${Shruti.NAMES[it]} ${Shruti.western(key, it)}" }.ifEmpty { "—" },
                    style = mono.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = ANGEL.copy(alpha = 0.85f))
                )
                val named = Shruti.detect(key, openValves)
                if (named.isNotEmpty()) BasicText(named, style = mono.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = HALO, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
            }
        }

        // the page's five tabs of presets
        Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Presets.TABS.forEachIndexed { i, pt ->
                val on = i == tab
                Box(
                    Modifier.weight(1f).height(36.dp)
                        .background(if (on) ANGEL else BLACK, RoundedCornerShape(18.dp))
                        .border(1.dp, if (on) ANGEL else FAINT, RoundedCornerShape(18.dp))
                        .clickable { tab = i; onTab(i) },
                    contentAlignment = Alignment.Center
                ) { BasicText(pt.title, style = mono.copy(color = if (on) BLACK else ANGEL.copy(alpha = 0.75f), fontSize = 10.sp, letterSpacing = 0.5.sp)) }
            }
        }

        // its cards: a tap closes every valve and opens the preset's; a tap on the one that sounds closes it again
        val current = Presets.TABS[tab]
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            BasicText(current.intro, style = mono.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = ANGEL.copy(alpha = 0.5f)), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
            for (p in current.presets) {
                val sounding = openValves == p.valves.sorted()
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .background(if (sounding) Color(0xFF0A2633) else BLACK, RoundedCornerShape(16.dp))
                        .border(1.dp, if (sounding) ANGEL else FAINT, RoundedCornerShape(16.dp))
                        .clickable {
                            closeAll()
                            if (!sounding) for (v in p.valves) { open[v] = true; mixer.setOpen(v, true) }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText(p.name, style = mono.copy(fontSize = 14.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold, color = if (sounding) CORE else ANGEL))
                        Spacer(Modifier.weight(1f))
                        BasicText(p.valves.sorted().joinToString(" ") { Shruti.western(key, it) }, style = mono.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = HALO))
                    }
                    if (p.level.isNotEmpty()) BasicText(p.level.uppercase(), style = mono.copy(fontSize = 9.sp, letterSpacing = 2.sp, color = ANGEL.copy(alpha = 0.45f)), modifier = Modifier.padding(top = 2.dp))
                    BasicText(p.about, style = mono.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = ANGEL.copy(alpha = 0.75f)), modifier = Modifier.padding(top = 4.dp))
                    BasicText(p.usedIn, style = mono.copy(fontSize = 11.sp, letterSpacing = 0.sp, color = ANGEL.copy(alpha = 0.45f)), modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Shruti.KEYS.forEachIndexed { i, name ->
                val on = i == key
                Box(
                    Modifier.weight(1f).height(44.dp)
                        .background(if (on) ANGEL else BLACK, RoundedCornerShape(22.dp))
                        .border(1.dp, if (on) ANGEL else FAINT, RoundedCornerShape(22.dp))
                        .clickable { key = i; mixer.key = i; onKey(i) },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(name, style = mono.copy(color = if (on) BLACK else ANGEL.copy(alpha = 0.75f), fontSize = 11.sp, letterSpacing = 0.sp))
                }
            }
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

/** A closed valve is a thin line of light; an open one burns light blue with a white heart and a halo that breathes. */
private fun DrawScope.drawValve(tab: Tab, isOpen: Boolean, level: Float, breath: Float, t: Double) {
    val r = tab.rect
    val radius = CornerRadius(minOf(r.width, r.height) * 0.22f)
    val edge = r.width * 0.035f

    // at rest: black (an upper valve hides what it lies over), a faint inner light and an outline
    drawRoundRect(BLACK, r.topLeft, r.size, radius)
    drawRoundRect(Brush.verticalGradient(listOf(FAINT.copy(alpha = 0.7f), BLACK), r.top, r.bottom), r.topLeft, r.size, radius)
    drawRoundRect(if (isOpen) ANGEL else LINE, r.topLeft, r.size, radius, style = Stroke(width = edge))
    if (level <= 0f) return

    // the halo: a real blur around the valve, wider and brighter as the bellows breathe in
    val pulse = 0.82f + 0.18f * breath
    drawIntoCanvas { c ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        for ((spread, alpha) in listOf(0.45f to 0.55f, 0.2f to 0.8f)) {
            paint.maskFilter = BlurMaskFilter(r.width * spread * pulse, BlurMaskFilter.Blur.NORMAL)
            paint.color = HALO.copy(alpha = alpha * level * pulse).toArgb()
            c.nativeCanvas.drawRoundRect(r.left, r.top, r.right, r.bottom, radius.x, radius.y, paint)
        }
    }
    // the body: light blue, white towards the middle
    drawRoundRect(
        Brush.horizontalGradient(listOf(ANGEL.copy(alpha = level), CORE.copy(alpha = level), ANGEL.copy(alpha = level)), r.left, r.right),
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
