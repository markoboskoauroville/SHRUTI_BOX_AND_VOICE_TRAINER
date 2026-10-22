package com.mantra.shrutibox

import android.content.res.AssetManager
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteOrder

/**
 * Decodes the piano's recordings (v22) from the app's assets, piano/<note>v<layer>.m4a (A0v5.m4a … C8v13.m4a; a sharp
 * is written "s": Ds4v5.m4a; AAC 64 kbps mono 32 kHz, 5 s, 2.5 MB for all 60), into 16-bit mono sound for [Piano].
 * Runs once, off the main thread, at the app's start.
 */
object PianoLoader {
    private val PITCH = mapOf("C" to 0, "Cs" to 1, "D" to 2, "Ds" to 3, "E" to 4, "F" to 5, "Fs" to 6, "G" to 7, "Gs" to 8, "A" to 9, "As" to 10, "B" to 11)
    private val NAME = Regex("""([A-G]s?)(\d)v(\d+)\.m4a""")

    fun load(assets: AssetManager): List<PianoSample> {
        val out = ArrayList<PianoSample>()
        for (f in assets.list("piano").orEmpty()) {
            val m = NAME.matchEntire(f) ?: continue
            val (n, o, v) = m.destructured
            val midi = 12 * (o.toInt() + 1) + (PITCH[n] ?: continue)
            val layer = if (v.toInt() <= 8) 0 else 1
            runCatching { decode(assets, "piano/$f") }
                .onSuccess { (data, rate) -> out += PianoSample(midi, layer, data, rate) }
                .onFailure { Log.w("ShrutiPiano", "could not decode $f: ${it.message}") }
        }
        Log.i("ShrutiPiano", "loaded ${out.size} piano samples")
        return out
    }

    /** One file to 16-bit mono, with its sample rate. */
    private fun decode(assets: AssetManager, path: String): Pair<ShortArray, Int> {
        val afd = assets.openFd(path)
        val ex = MediaExtractor()
        ex.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        ex.selectTrack(0)
        val fmt = ex.getTrackFormat(0)
        val rate = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val codec = MediaCodec.createDecoderByType(fmt.getString(MediaFormat.KEY_MIME)!!)
        codec.configure(fmt, null, null, 0)
        codec.start()
        var pcm = ShortArray(rate * 6)
        var n = 0
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        try {
            // bounded (the project's gate allows no unbounded loop): a 5 s file needs a few hundred rounds
            var rounds = 0
            var done = false
            while (!done && rounds++ < 20_000) {
                if (!inputDone) {
                    val ii = codec.dequeueInputBuffer(10_000)
                    if (ii >= 0) {
                        val size = ex.readSampleData(codec.getInputBuffer(ii)!!, 0)
                        if (size < 0) { codec.queueInputBuffer(ii, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inputDone = true }
                        else { codec.queueInputBuffer(ii, 0, size, ex.sampleTime, 0); ex.advance() }
                    }
                }
                val oi = codec.dequeueOutputBuffer(info, 10_000)
                if (oi >= 0) {
                    val sb = codec.getOutputBuffer(oi)!!.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                    val frames = sb.remaining() / channels
                    if (n + frames > pcm.size) pcm = pcm.copyOf(maxOf(pcm.size * 2, n + frames))
                    for (k in 0 until frames) {
                        var acc = 0
                        for (c in 0 until channels) acc += sb.get()
                        pcm[n++] = (acc / channels).toShort()
                    }
                    codec.releaseOutputBuffer(oi, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) done = true
                }
            }
        } finally {
            codec.stop(); codec.release(); ex.release(); afd.close()
        }
        return pcm.copyOf(n) to rate
    }
}
