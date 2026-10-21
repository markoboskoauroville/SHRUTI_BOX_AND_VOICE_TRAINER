package com.mantra.shrutibox

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.isActive

// The ground every Mantra app shares (mantra-design-language.md §1): dark ground, sand words, monospace.
private val GROUND = Color(0xFF0B0D10)
private val SAND = Color(0xFFF2DDB4)
private val DIM = Color(0xFF6E6658)
private val GLOW = Color(0xFFFFB347)
// The box, from Marko's photo (reference-shruti-box.png): pale teak, a darker frame, silver corners, a rosewood
// reed plate with pale tabs, blue and violet bellows cloth.
private val WOOD_LIGHT = Color(0xFFDCA15A)
private val WOOD_DARK = Color(0xFFB8762F)
private val FRAME = Color(0xFF7A4A1E)
private val BACK = Color(0xFF8E5A26)
private val PLATE = Color(0xFF3A2317)
private val TAB = Color(0xFFEBC58C)
private val TAB_EDGE = Color(0xFF9A6A35)
private val CLOTH = Color(0xFF2F8FA8)
private val CLOTH_FOLD = Color(0xFF4B3AA0)
private val SILVER = Color(0xFFCBCBCB)

/** version-visible.md §3: the number in the header opens the latest build. */
private const val LATEST = "https://github.com/markoboskoauroville/MANTRA_SHRUTI_BOX/releases/latest"
private const val PREFS = "shruti"

class MainActivity : ComponentActivity() {
    private val engine = Engine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

/** Where each valve tab is on the canvas, for drawing and for the finger. */
private class Tab(val valve: Int, val rect: Rect, val upper: Boolean)

@Composable
private fun ShrutiScreen(mixer: Mixer, onKey: (Int) -> Unit, onAnyOpen: (Boolean) -> Unit, onVersion: () -> Unit) {
    val open = remember { mutableStateListOf(*Array(Shruti.COUNT) { mixer.isOpen(it) }) }
    var key by remember { mutableIntStateOf(mixer.key) }
    var frame by remember { mutableLongStateOf(0L) }
    val tabs = remember { mutableStateListOf<Tab>() }
    val measurer = rememberTextMeasurer()
    val mono = TextStyle(fontFamily = FontFamily.Monospace, color = SAND, fontSize = 13.sp, letterSpacing = 1.5.sp)

    // the bellows and the glow move with the sound: one redraw per frame while the app is on screen
    LaunchedEffect(Unit) { while (isActive) withFrameNanos { frame = it } }
    val anyOpen = open.any { it }
    LaunchedEffect(anyOpen) { onAnyOpen(anyOpen) }

    fun toggle(v: Int) { val now = !open[v]; open[v] = now; mixer.setOpen(v, now) }

    Column(
        Modifier.fillMaxSize().background(GROUND).windowInsetsPadding(WindowInsets.systemBars).padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicText("SHRUTI BOX", style = mono.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp))
            Spacer(Modifier.weight(1f))
            BasicText(
                "OFF",
                style = mono.copy(color = if (anyOpen) SAND else DIM),
                modifier = Modifier.clickable(enabled = anyOpen) {
                    mixer.closeAll(); for (i in 0 until Shruti.COUNT) open[i] = false
                }.padding(horizontal = 14.dp, vertical = 8.dp)
            )
            BasicText(
                "v${BuildConfig.VERSION_NAME}",
                style = mono.copy(color = DIM, fontSize = 11.sp),
                modifier = Modifier.clickable { onVersion() }.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
            )
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures { p ->
                        // the nearest tab whose generous box holds the finger: fingers are wider than tabs
                        val hit = tabs.filter { t ->
                            val r = t.rect
                            Rect(r.left - r.width * 0.25f, r.top - r.height * 0.3f, r.right + r.width * 0.25f, r.bottom + r.height * 0.3f).contains(p)
                        }.minByOrNull { (it.rect.center - p).getDistanceSquared() }
                        if (hit != null) toggle(hit.valve)
                    }
                }
            ) {
                if (frame < 0L) return@Canvas
                val laid = drawShrutiBox(mixer, open)
                if (tabs.size != laid.size || tabs.zip(laid).any { (a, b) -> a.rect != b.rect }) { tabs.clear(); tabs.addAll(laid) }
                // the names under the lower row and over the upper one
                for (t in laid) {
                    val label = measurer.measure(
                        Shruti.NAMES[t.valve],
                        mono.copy(fontSize = 11.sp, color = if (open[t.valve]) GLOW else SAND.copy(alpha = 0.75f), letterSpacing = 0.sp, textAlign = TextAlign.Center)
                    )
                    val y = if (t.upper) t.rect.top - label.size.height - t.rect.height * 0.55f else t.rect.bottom + t.rect.height * 0.55f
                    drawText(label, topLeft = Offset(t.rect.center.x - label.size.width / 2f, y))
                }
            }
        }

        BasicText("KEY", style = mono.copy(color = DIM, fontSize = 11.sp), modifier = Modifier.padding(bottom = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Shruti.KEYS.forEachIndexed { i, name ->
                val on = i == key
                Box(
                    Modifier.weight(1f).height(40.dp)
                        .background(if (on) SAND else GROUND, RoundedCornerShape(6.dp))
                        .border(1.dp, if (on) SAND else DIM, RoundedCornerShape(6.dp))
                        .clickable { key = i; mixer.key = i; onKey(i) },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(name, style = mono.copy(color = if (on) GROUND else SAND, fontSize = 11.sp, letterSpacing = 0.sp))
                }
            }
        }
        Spacer(Modifier.width(1.dp).height(4.dp))
    }
}

/** Draws the box, front on, the bellows breathing behind it, and returns where the tabs are. */
private fun DrawScope.drawShrutiBox(mixer: Mixer, open: List<Boolean>): List<Tab> {
    val w = size.width
    val h = size.height
    val bw = minOf(w * 0.84f, h * 0.95f / 0.8f)
    val bh = bw * 0.8f
    val fx = (w - bw) / 2f + bw * 0.05f
    val fy = (h - bh) / 2f + bw * 0.04f
    val breath = ((mixer.breath() - 1.0) / Mixer.BELLOWS_DEPTH).toFloat()            // -1..1
    val d = bw * (0.07f + 0.025f * breath)                                            // how far the bellows open
    val bx = fx - d
    val by = fy - d * 0.55f

    // the back board
    drawRect(BACK, Offset(bx, by), Size(bw, bh))
    // the bellows cloth: the left side and the top side between the two boards, folded
    val left = Path().apply { moveTo(bx, by); lineTo(fx, fy); lineTo(fx, fy + bh); lineTo(bx, by + bh); close() }
    val top = Path().apply { moveTo(bx, by); lineTo(bx + bw, by); lineTo(fx + bw, fy); lineTo(fx, fy); close() }
    drawPath(left, CLOTH)
    drawPath(top, CLOTH.copy(red = 0.25f))
    val folds = 5
    for (i in 1 until folds) {
        val f = i / folds.toFloat()
        val x = bx + (fx - bx) * f
        drawLine(CLOTH_FOLD, Offset(x, by + (fy - by) * f), Offset(x, by + bh + (fy - by) * f), strokeWidth = bw * 0.006f)
        val y = by + (fy - by) * f
        drawLine(CLOTH_FOLD, Offset(bx + (fx - bx) * f, y), Offset(bx + bw + (fx - bx) * f, y), strokeWidth = bw * 0.006f)
    }

    // the front board: teak with its grain, a darker frame, silver corners
    drawRect(Brush.linearGradient(listOf(WOOD_LIGHT, WOOD_DARK), Offset(fx, fy), Offset(fx + bw, fy + bh)), Offset(fx, fy), Size(bw, bh))
    for (i in 1..22) {
        val y = fy + bh * i / 23f + (if (i % 3 == 0) bh * 0.008f else 0f)
        drawLine(FRAME.copy(alpha = 0.10f + 0.06f * (i % 4)), Offset(fx + bw * 0.03f, y), Offset(fx + bw * 0.97f, y + bh * 0.004f), strokeWidth = bw * 0.0025f)
    }
    drawRect(FRAME, Offset(fx, fy), Size(bw, bh), style = Stroke(width = bw * 0.03f))
    val c = bw * 0.075f
    for ((cx, cy, sx, sy) in listOf(
        listOf(fx, fy, 1f, 1f), listOf(fx + bw, fy, -1f, 1f), listOf(fx, fy + bh, 1f, -1f), listOf(fx + bw, fy + bh, -1f, -1f)
    )) {
        val p = Path().apply { moveTo(cx, cy); lineTo(cx + sx * c, cy); lineTo(cx, cy + sy * c); close() }
        drawPath(p, SILVER)
        drawPath(p, Color(0xFF8C8C8C), style = Stroke(width = bw * 0.004f))
    }
    // the handle on top
    val hw = bw * 0.2f
    val hx = fx + bw / 2f - hw / 2f
    drawArc(SILVER, 180f, 180f, false, Offset(hx, fy - bh * 0.09f), Size(hw, bh * 0.16f), style = Stroke(width = bw * 0.014f, cap = StrokeCap.Round))
    for (x in listOf(hx, hx + hw)) drawRect(SILVER, Offset(x - bw * 0.018f, fy - bh * 0.012f), Size(bw * 0.036f, bh * 0.03f))

    // the reed plate and its thirteen tabs, laid out like a keyboard
    val pw = bw * 0.7f
    val ph = bh * 0.42f
    val px = fx + (bw - pw) / 2f
    val py = fy + (bh - ph) / 2f
    drawRoundRect(PLATE, Offset(px, py), Size(pw, ph), CornerRadius(bw * 0.01f))
    drawRoundRect(Color(0xFF1E120B), Offset(px, py), Size(pw, ph), CornerRadius(bw * 0.01f), style = Stroke(width = bw * 0.005f))
    val lower = listOf(0, 2, 4, 5, 7, 9, 11, 12)
    val upperAfter = mapOf(1 to 0, 3 to 1, 6 to 3, 8 to 4, 10 to 5)                  // valve to the lower slot it follows
    val slot = pw / 8f
    val tw = slot * 0.56f
    val th = ph * 0.34f
    val tabs = ArrayList<Tab>(Shruti.COUNT)
    lower.forEachIndexed { i, v -> tabs += Tab(v, Rect(Offset(px + slot * i + (slot - tw) / 2f, py + ph * 0.56f), Size(tw, th)), false) }
    upperAfter.forEach { (v, after) -> tabs += Tab(v, Rect(Offset(px + slot * (after + 1) - tw / 2f, py + ph * 0.1f), Size(tw, th)), true) }
    for (t in tabs) {
        val isOpen = open[t.valve]
        val lvl = mixer.level(t.valve).toFloat()
        // an open valve slides away from the middle of the plate and shows the dark slot of its reed
        val shift = if (isOpen) th * 0.32f * (if (t.upper) -1f else 1f) else 0f
        drawRoundRect(Color(0xFF120A06), t.rect.topLeft, t.rect.size, CornerRadius(tw * 0.25f))
        if (lvl > 0f) drawRoundRect(GLOW.copy(alpha = 0.35f * lvl), Offset(t.rect.left - tw * 0.18f, t.rect.top - tw * 0.18f + shift), Size(tw * 1.36f, th + tw * 0.36f), CornerRadius(tw * 0.4f))
        drawRoundRect(if (isOpen) GLOW else TAB, Offset(t.rect.left, t.rect.top + shift), t.rect.size, CornerRadius(tw * 0.25f))
        drawRoundRect(TAB_EDGE, Offset(t.rect.left, t.rect.top + shift), t.rect.size, CornerRadius(tw * 0.25f), style = Stroke(width = bw * 0.004f))
        drawLine(TAB_EDGE, Offset(t.rect.center.x, t.rect.top + shift + th * 0.22f), Offset(t.rect.center.x, t.rect.bottom + shift - th * 0.22f), strokeWidth = bw * 0.004f, cap = StrokeCap.Round)
    }
    return tabs
}
