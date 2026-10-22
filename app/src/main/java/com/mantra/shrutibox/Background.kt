package com.mantra.shrutibox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * v26 (Marko: "it can work in the background fully functional if it's not in the focus ... add X next to the gear icon
 * ... you just press X and everything is released"). The sound and the MIDI keyboard belong to the process, not to the
 * screen: leaving the app (Home, another app, the screen off, Back) keeps the reeds or the piano sounding, the keyboard
 * playing and the sequencer running, held up by [ShrutiService], a media-playback foreground service with its
 * notification. [releaseAll] (the ✕ beside the gear, or Stop in the notification) lets everything go and closes the app.
 */
object Background {
    val engine by lazy { Engine() }
    @Volatile private var midiInstance: Midi? = null
    fun midi(context: Context): Midi = midiInstance ?: Midi(context.applicationContext).also { midiInstance = it }
    /** The piano's recordings are decoded once per process. */
    @Volatile var pianoLoading = false
    /** The screen closes itself when everything is released. */
    var onRelease: (() -> Unit)? = null

    fun start(context: Context) {
        val i = Intent(context, ShrutiService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i) else context.startService(i)
    }

    /** Everything let go: the sound, the MIDI (the Launchkey back to standalone), the service; the screen closes. */
    fun releaseAll(context: Context) {
        engine.mixer.hush()
        midiInstance?.stop()
        engine.stop()
        context.stopService(Intent(context, ShrutiService::class.java))
        onRelease?.invoke()
    }
}

/** Keeps the process and its sound alive while the app is out of focus; its notification opens the app or stops it. */
class ShrutiService : Service() {
    companion object {
        const val CHANNEL = "playing"
        const val ACTION_STOP = "com.mantra.shrutibox.STOP"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Background.releaseAll(this)
            stopSelf()
            return START_NOT_STICKY
        }
        val n = notification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(1, n)
        return START_NOT_STICKY
    }

    private fun notification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(CHANNEL, "Playing in the background", NotificationManager.IMPORTANCE_LOW))
        }
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(this, 1, Intent(this, ShrutiService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Playing · tap to open · Stop releases everything")
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
