package com.mantra.shrutibox

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 * The voice trainer's microphone (v6): one AudioRecord, mono float at 48 kHz, on its own thread, read 4096 samples
 * at a time (85 ms) and given to [Pitch.detect]. It runs only while the screen asks for it ([listen]); the loop ends
 * when [running] goes false. Nothing is kept: each frame is heard and dropped.
 */
class Ear {
    companion object { const val RATE = 48_000; const val FRAME = 4096 }

    /** The last pitch heard, in Hz, or 0 for silence; and when it was heard (System.nanoTime). */
    @Volatile var hz = 0.0
        private set
    @Volatile var heardAt = 0L
        private set
    @Volatile private var running = false
    /** True when the microphone could not be opened; the screen then says so instead of asking again. */
    @Volatile var failed = false
        private set
    val on: Boolean get() = running
    private var thread: Thread? = null

    // The screen asks for the microphone only after RECORD_AUDIO is granted (MainActivity), so lint is told so.
    @SuppressLint("MissingPermission")
    fun listen(on: Boolean) {
        if (on == running) return
        if (!on) { running = false; thread?.join(500); thread = null; hz = 0.0; return }
        running = true
        thread = Thread({
            val min = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val rec = try {
                AudioRecord(MediaRecorder.AudioSource.MIC, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, maxOf(min, FRAME * 4 * 2))
            } catch (_: Exception) { null }
            if (rec == null || rec.state != AudioRecord.STATE_INITIALIZED) { rec?.release(); failed = true; running = false; return@Thread }
            val buf = FloatArray(FRAME)
            rec.startRecording()
            try {
                while (running) {
                    val got = rec.read(buf, 0, FRAME, AudioRecord.READ_BLOCKING)
                    if (got < 0) break                                   // the microphone was taken away: stop
                    if (got == 0) continue
                    hz = Pitch.detect(buf, got, RATE) ?: 0.0
                    heardAt = System.nanoTime()
                }
            } finally {
                rec.stop(); rec.release()
                if (running) { failed = true; running = false }          // left by the break, not by listen(false)
            }
        }, "shruti-ear").apply { start() }
    }
}
