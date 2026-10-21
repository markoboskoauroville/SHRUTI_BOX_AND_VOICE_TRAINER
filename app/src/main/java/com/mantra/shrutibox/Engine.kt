package com.mantra.shrutibox

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * The sound: one AudioTrack, mono float at 48 kHz, fed by [Mixer] on its own thread. It runs while the app is on
 * screen ([start] in onStart, [stop] in onStop); the loop ends when [running] goes false, so it is never unbounded.
 */
class Engine {
    companion object { const val RATE = 48_000 }

    val mixer = Mixer(RATE)
    @Volatile private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread({ loop() }, "shruti-reeds").apply { priority = Thread.MAX_PRIORITY; start() }
    }

    fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }

    private fun loop() {
        val min = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(min, 4096 * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        val block = FloatArray(1024)
        track.play()
        try {
            while (running) {
                mixer.render(block)
                track.write(block, 0, block.size, AudioTrack.WRITE_BLOCKING)
            }
        } finally {
            track.stop()
            track.release()
        }
    }
}
