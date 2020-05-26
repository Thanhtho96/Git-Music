package com.tt.gitmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.tt.gitmusic.R
import com.tt.gitmusic.receiver.MusicReceiver


class PlayMusic : MediaPlayer.OnPreparedListener, Service() {

    private var mMediaPlayer: MediaPlayer? = null
    private var wifiLock: WifiManager.WifiLock? = null

    companion object {
        const val ACTION_PLAY: String = "com.tt.music.action.PLAY"
        const val ACTION_PREVIOUS: String = "com.tt.music.action.PREVIOUS"
        const val ACTION_PAUSE: String = "com.tt.music.action.PAUSE"
        const val ACTION_NEXT: String = "com.tt.music.action.NEXT"
        const val ACTION_STOP: String = "com.tt.music.action.STOP"
    }

    override fun onStartCommand(intent: Intent,
                                flags: Int,
                                startId: Int): Int {
        val action = intent.action
        val token = intent.getStringExtra("token")
        val name = intent.getStringExtra("name")
        val uri = Uri.parse(intent.getStringExtra("url"))
        val map: HashMap<String, String> = HashMap()
        if (token != null) {
            map["Authorization"] = token
            map["Accept"] = "application/vnd.github.v3.raw+json"
        }

        when (action) {
            ACTION_PLAY -> {
                val wifiManager = this@PlayMusic.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock")
                if (mMediaPlayer != null) {
                    if (mMediaPlayer!!.isPlaying) {
                        mMediaPlayer?.stop()
                    }
                }
                mMediaPlayer = MediaPlayer() // initialize it here
                mMediaPlayer?.apply {
                    wifiLock?.acquire()
                    setDataSource(this@PlayMusic, uri, map)
                    setWakeMode(this@PlayMusic, PowerManager.PARTIAL_WAKE_LOCK)
                    setOnPreparedListener(this@PlayMusic)
                    prepareAsync() // prepare async to not block main thread
                }

                Log.d("mediaPlayer", "${mMediaPlayer?.duration}")

                val mediaSession = MediaSessionCompat(this@PlayMusic, "GitMusicSession")

                createNotificationChannel("Music", "Play music", "Music")

                val intent1 = Intent(this, MusicReceiver::class.java)
                intent1.action = ACTION_PREVIOUS

                val pdPrev = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )

                val intent2 = Intent(this, MusicReceiver::class.java)
                intent2.action = ACTION_PAUSE

                val pdPause = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent2,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )

                val intent3 = Intent(this, MusicReceiver::class.java)
                intent3.action = ACTION_NEXT

                val pdNext = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent3,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )

                val intent4 = Intent(this, MusicReceiver::class.java)
                intent4.action = ACTION_STOP

                val pdStop = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent4,
                        PendingIntent.FLAG_CANCEL_CURRENT
                )
                val builder = NotificationCompat.Builder(this, "Music")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(name)
                        .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pdPrev)
                        .addAction(R.drawable.ic_pause_black_24dp, "Pause", pdPause)
                        .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pdNext)
                        .addAction(R.drawable.ic_close_black_24dp, "Stop", pdStop)
                        .setStyle(
                                androidx.media.app.NotificationCompat.MediaStyle()
                                        .setShowActionsInCompactView(0, 1, 2)
                                        .setMediaSession(mediaSession.sessionToken)
                        )

                startForeground(1, builder.build())
                registerReceiver(broadcastReceiver, IntentFilter("MusicFilter"))
            }
        }
        return START_STICKY
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?,
                               intent: Intent?) {
            if (intent?.getStringExtra("Action").equals(ACTION_STOP))
                onDestroy()
            else
                Toast.makeText(context, intent?.getStringExtra("Action"), Toast.LENGTH_SHORT)
                        .show()
        }
    }

    private fun createNotificationChannel(name: CharSequence,
                                          description: String,
                                          channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /** Called when MediaPlayer is ready */
    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        if (wifiLock?.isHeld!!)
            wifiLock?.release()
        stopSelf()
    }
}
