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
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
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
                onKey = { prefs.edit { putInt("key", it) } },
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
private fun ShrutiScreen(mixer: Mixer, onKey: (Int) -> Unit, onAnyOpen: (Boolean) -> Unit, onVersion: () -> Unit) {
    val open = remember { mutableStateListOf(*Array(Shruti.COUNT) { mixer.isOpen(it) }) }
    var key by remember { mutableIntStateOf(mixer.key) }
    var frame by remember { mutableLongStateOf(0L) }
    val tabs = remember { mutableStateListOf<Tab>() }
    val measurer = rememberTextMeasurer()
    val mono = TextStyle(fontFamily = FontFamily.Monospace, color = ANGEL, fontSize = 13.sp, letterSpacing = 1.5.sp)

    // the glow breathes with the bellows: one redraw per frame while the app is on screen
    LaunchedEffect(Unit) { while (isActive) withFrameNanos { frame = it } }
    val anyOpen = open.any { it }
    LaunchedEffect(anyOpen) { onAnyOpen(anyOpen) }

    fun toggle(v: Int) { val now = !open[v]; open[v] = now; mixer.setOpen(v, now) }

    Column(
        Modifier.fillMaxSize().background(BLACK).windowInsetsPadding(WindowInsets.displayCutout)
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText("SHRUTI", style = mono.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 4.sp))
            Spacer(Modifier.weight(1f))
            BasicText(
                "OFF",
                style = mono.copy(color = if (anyOpen) ANGEL else LINE),
                modifier = Modifier.clickable(enabled = anyOpen) {
                    mixer.closeAll(); for (i in 0 until Shruti.COUNT) open[i] = false
                }.padding(horizontal = 14.dp, vertical = 8.dp)
            )
            BasicText(
                "v${BuildConfig.VERSION_NAME}",
                style = mono.copy(color = LINE, fontSize = 11.sp),
                modifier = Modifier.clickable { onVersion() }.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 10.dp)) {
            Canvas(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures { p ->
                        // the valve under the finger, or the nearest one if the finger lands in a gap
                        val hit = tabs.firstOrNull { it.rect.contains(p) }
                            ?: tabs.minByOrNull { (it.rect.center - p).getDistanceSquared() }
                                ?.takeIf { t -> Rect(t.rect.left - t.rect.width * 0.3f, t.rect.top - 40f, t.rect.right + t.rect.width * 0.3f, t.rect.bottom + 40f).contains(p) }
                        if (hit != null) toggle(hit.valve)
                    }
                }
            ) {
                if (frame < 0L) return@Canvas
                val laid = layValves()
                if (tabs.size != laid.size || tabs.zip(laid).any { (a, b) -> a.rect != b.rect }) { tabs.clear(); tabs.addAll(laid) }
                val breath = ((mixer.breath() - 1.0) / Mixer.BELLOWS_DEPTH).toFloat()      // -1..1
                val t = frame / 1e9
                for (tab in laid) drawValve(tab, open[tab.valve], mixer.level(tab.valve).toFloat(), breath, t)
                for (tab in laid) {
                    val on = open[tab.valve]
                    val label = measurer.measure(
                        Shruti.NAMES[tab.valve],
                        mono.copy(fontSize = 12.sp, letterSpacing = 0.sp, color = if (on) Color(0xFF05222E) else ANGEL.copy(alpha = 0.7f), fontWeight = if (on) FontWeight.Bold else FontWeight.Normal)
                    )
                    val r = tab.rect
                    val y = if (tab.upper) r.top + r.width * 0.45f else r.bottom - r.width * 0.45f - label.size.height
                    drawText(label, topLeft = Offset(r.center.x - label.size.width / 2f, y))
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

/**
 * The placement of a shruti box's reed plate, kept from v1 and from the reference page: the eight of the scale along
 * the bottom, the five between them above, each centred on the gap of the two it lies between. Now they fill the
 * screen: the lower ones tall capsules from the bottom, the upper ones from the top.
 */
private fun DrawScope.layValves(): List<Tab> {
    val w = size.width
    val h = size.height
    val slot = w / 8f
    val lowerW = slot * 0.80f
    val upperW = slot * 0.64f
    val gap = h * 0.035f
    val upperH = h * 0.38f
    val lowerTop = upperH + gap
    val lower = listOf(0, 2, 4, 5, 7, 9, 11, 12)
    val upperAfter = mapOf(1 to 0, 3 to 1, 6 to 3, 8 to 4, 10 to 5)                  // valve to the lower slot it follows
    val tabs = ArrayList<Tab>(Shruti.COUNT)
    lower.forEachIndexed { i, v -> tabs += Tab(v, Rect(Offset(slot * i + (slot - lowerW) / 2f, lowerTop), Size(lowerW, h - lowerTop)), false) }
    upperAfter.forEach { (v, after) -> tabs += Tab(v, Rect(Offset(slot * (after + 1) - upperW / 2f, 0f), Size(upperW, upperH)), true) }
    return tabs
}

/** A closed valve is a thin line of light; an open one burns light blue with a white heart and a halo that breathes. */
private fun DrawScope.drawValve(tab: Tab, isOpen: Boolean, level: Float, breath: Float, t: Double) {
    val r = tab.rect
    val radius = CornerRadius(r.width / 2f)
    val edge = r.width * 0.035f

    // at rest: a faint inner light and an outline, so the hand knows where the valve is
    drawRoundRect(Brush.verticalGradient(listOf(FAINT.copy(alpha = 0.55f), BLACK), r.top, r.bottom), r.topLeft, r.size, radius)
    drawRoundRect(LINE, r.topLeft, r.size, radius, style = Stroke(width = edge))
    if (level <= 0f) return

    // the halo: a real blur around the valve, wider and brighter as the bellows breathe in
    val pulse = 0.82f + 0.18f * breath
    drawIntoCanvas { c ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        for ((spread, alpha) in listOf(0.55f to 0.55f, 0.25f to 0.8f)) {
            val blur = r.width * spread * pulse
            paint.maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            paint.color = HALO.copy(alpha = alpha * level * pulse).toArgb()
            c.nativeCanvas.drawRoundRect(r.left, r.top, r.right, r.bottom, r.width / 2f, r.width / 2f, paint)
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
