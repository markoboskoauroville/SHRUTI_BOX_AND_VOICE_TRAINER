package com.mantra.shrutibox

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf

/**
 * A MIDI controller plays the box (v11, Marko 22.10.2026: "make this MIDI controller control my Shruti box"). Every
 * USB or Bluetooth MIDI device that sends is opened as it appears; each key going down or up is handed to [onNote] on
 * the main thread as (channel 0–15, note, velocity): 1–127 a key struck, 0 a key released (a note-off, or a note-on of
 * velocity 0, as the Launchkey sends it).
 * First player: a Novation Launchkey Mini MK4 25 on the Pixel 7's USB (its keys on channel 1, its pads in Drum mode on
 * channel 10, notes 36–51).
 */
class Midi(context: Context) {
    /** The names of the controllers that are open now, for settings; empty when none. */
    val connected = mutableStateOf<List<String>>(emptyList())
    var onNote: ((Int, Int, Int) -> Unit)? = null

    private val main = Handler(Looper.getMainLooper())
    private val manager: MidiManager? =
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) context.getSystemService(MidiManager::class.java) else null
    private val open = HashMap<Int, Pair<MidiDevice, List<MidiOutputPort>>>()
    private val names = HashMap<Int, String>()
    // v16: the ports the app SENDS on, to write on a Launchkey's display
    private val inputs = HashMap<Int, List<MidiInputPort>>()

    private val callback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(info: MidiDeviceInfo) = attach(info)
        override fun onDeviceRemoved(info: MidiDeviceInfo) = detach(info.id)
    }

    fun start() {
        val m = manager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            m.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM).forEach { attach(it) }
            m.registerDeviceCallback(MidiManager.TRANSPORT_MIDI_BYTE_STREAM, { main.post(it) }, callback)
        } else startBefore33(m)
    }

    // the calls of Android 8 to 12, deprecated from 13 (this app runs from 26)
    @Suppress("DEPRECATION")
    private fun startBefore33(m: MidiManager) {
        m.devices.forEach { attach(it) }
        m.registerDeviceCallback(callback, main)
    }

    fun stop() {
        manager?.unregisterDeviceCallback(callback)
        open.keys.toList().forEach { detach(it) }
    }

    private fun attach(info: MidiDeviceInfo) {
        val m = manager ?: return
        if (info.outputPortCount == 0 || open.containsKey(info.id)) return
        val name = info.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
            ?: info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "MIDI ${info.id}"
        m.openDevice(info, { device ->
            if (device == null) { Log.w(TAG, "could not open $name"); return@openDevice }
            // every port it sends on: the Launchkey's keys and Drum-mode pads come on the first, its DAW mode on the second
            val ports = (0 until info.outputPortCount).mapNotNull { p -> device.openOutputPort(p)?.also { it.connect(Parser(p)) } }
            open[info.id] = device to ports; names[info.id] = name
            inputs[info.id] = (0 until info.inputPortCount).mapNotNull { p -> device.openInputPort(p) }
            connected.value = names.values.sorted()
            Log.i(TAG, "open: $name, ${ports.size} port(s)")
        }, main)
    }

    private fun detach(id: Int) {
        inputs.remove(id)?.forEach { runCatching { it.close() } }
        open.remove(id)?.let { (device, ports) -> ports.forEach { it.close() }; device.close() }
        names.remove(id)?.let { Log.i(TAG, "closed: $it") }
        connected.value = names.values.sorted()
    }

    /**
     * Two lines on a Launchkey MK4's display (v16), as its own Octave buttons show them ("Octave" over "+1 (C3 - C5)").
     * Novation's Programmer's Reference: configure the global temporary display (target 21h, arrangement 1 = name and
     * text value, bit 6 kept), fill field 0 and 1, trigger it (7Fh). Mini SKUs use the header F0 00 20 29 02 13. It goes
     * to every port the Launchkey listens on; the keyboard's own octave cannot be set over MIDI (no such message).
     */
    fun launchkeyText(name: String, value: String) {
        val head = byteArrayOf(0xF0.toByte(), 0x00, 0x20, 0x29, 0x02, 0x13)
        fun text(field: Int, t: String) = head + byteArrayOf(0x06, 0x21, field.toByte()) +
            t.filter { it.code in 0x20..0x7E }.toByteArray(Charsets.US_ASCII) + byteArrayOf(0xF7.toByte())
        val msgs = listOf(
            head + byteArrayOf(0x04, 0x21, 0x41, 0xF7.toByte()),
            text(0, name), text(1, value),
            head + byteArrayOf(0x04, 0x21, 0x7F, 0xF7.toByte())
        )
        for ((id, ports) in inputs) {
            if (names[id]?.contains("Launchkey", ignoreCase = true) != true) continue
            for (p in ports) for (m in msgs) runCatching { p.send(m, 0, m.size) }.onFailure { Log.w(TAG, "display: ${it.message}") }
        }
        Log.i(TAG, "display: $name / $value")
    }

    /** Reads the bytes of one port into notes down and up (running status included); everything else is passed over. */
    private inner class Parser(private val port: Int) : MidiReceiver() {
        private var status = 0
        private var first = -1
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            for (i in offset until offset + count) {
                val b = msg[i].toInt() and 0xFF
                when {
                    b >= 0xF8 -> Unit                                  // clock and other real-time bytes, between any two
                    b >= 0x80 -> { status = b; first = -1 }
                    status and 0xF0 == 0x90 || status and 0xF0 == 0x80 -> {
                        if (first < 0) first = b
                        else {
                            val ch = status and 0x0F; val note = first; val vel = b; first = -1
                            Log.d(TAG, "port $port ch ${ch + 1} note $note vel $vel")
                            val struck = if (status and 0xF0 == 0x90) vel else 0
                            main.post { onNote?.invoke(ch, note, struck) }
                        }
                    }
                    else -> first = -1
                }
            }
        }
    }

    companion object { const val TAG = "ShrutiMidi" }
}
