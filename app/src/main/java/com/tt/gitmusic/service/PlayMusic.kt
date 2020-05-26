package com.tt.gitmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tt.gitmusic.R
import com.tt.gitmusic.receiver.MusicReceiver

class PlayMusic : MediaPlayer.OnPreparedListener, Service(), AudioManager.OnAudioFocusChangeListener {

    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var uri: Uri
    private lateinit var headerMap: HashMap<String, String>
    private lateinit var songName: String
    private lateinit var audioManager: AudioManager

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
        val token = intent.getStringExtra("token")
        songName = intent.getStringExtra("name")!!
        uri = Uri.parse(intent.getStringExtra("url"))
        headerMap = HashMap()
        if (token != null) {
            headerMap["Authorization"] = token
            headerMap["Accept"] = "application/vnd.github.v3.raw+json"
        }

        val wifiManager = this@PlayMusic.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock")
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        initMediaPlayer(uri, headerMap)

        createNotificationChannel("Music", "Play music", "Music")

        startForeground(1, buildNotification(songName, R.drawable.ic_pause_black_24dp, true).build())
        registerReceiver(broadcastReceiver, IntentFilter("MusicFilter"))

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)

        return START_STICKY
    }

    private fun buildNotification(songName: String?,
                                  iconPlay: Int,
                                  wantToPause: Boolean): NotificationCompat.Builder {
        val mediaSession = MediaSessionCompat(this@PlayMusic, "GitMusicSession")

        val intentPrev = Intent(this, MusicReceiver::class.java)
        intentPrev.action = ACTION_PREVIOUS

        val pdPrev = PendingIntent.getBroadcast(
                this,
                0,
                intentPrev,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentPause = Intent(this, MusicReceiver::class.java)
        if (wantToPause) {
            intentPause.action = ACTION_PAUSE
        } else {
            intentPause.action = ACTION_PLAY
        }

        val pdPause = PendingIntent.getBroadcast(
                this,
                0,
                intentPause,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentNext = Intent(this, MusicReceiver::class.java)
        intentNext.action = ACTION_NEXT

        val pdNext = PendingIntent.getBroadcast(
                this,
                0,
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentStop = Intent(this, MusicReceiver::class.java)
        intentStop.action = ACTION_STOP

        val pdStop = PendingIntent.getBroadcast(
                this,
                0,
                intentStop,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "Music")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(songName)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pdPrev)
                .addAction(iconPlay, "Pause", pdPause)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pdNext)
                .addAction(R.drawable.ic_close_black_24dp, "Stop", pdStop)
                .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSession.sessionToken)
                )
    }

    private fun initMediaPlayer(uri: Uri,
                                headerMap: HashMap<String, String>) {
        mMediaPlayer = MediaPlayer() // initialize it here
        mMediaPlayer?.apply {
            wifiLock.acquire()
            setAudioAttributes(AudioAttributes.Builder().apply {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            }.build())
            setDataSource(this@PlayMusic, uri, headerMap)
            setWakeMode(this@PlayMusic, PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener(this@PlayMusic)
            prepareAsync() // prepare async to not block main thread
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?,
                               intent: Intent?) {
            when (intent?.getStringExtra("Action")) {
                ACTION_PLAY -> {
                    if (mMediaPlayer == null)
                        initMediaPlayer(uri, headerMap)
                    else
                        mMediaPlayer?.start()

                    startForeground(1, buildNotification(songName, R.drawable.ic_pause_black_24dp, true).build())
                }
                ACTION_PAUSE -> {
                    onPrepared(mMediaPlayer!!)
                    mMediaPlayer?.pause()
                    with(NotificationManagerCompat.from(this@PlayMusic)) {
                        notify(1, buildNotification(songName, R.drawable.ic_play_arrow_black_24dp, false).build())
                    }
                }
                ACTION_PREVIOUS -> {
                    mMediaPlayer?.let {
                        if (it.isPlaying) {
                            it.stop()
                        }
                    }

                    // Todo: add Prev
                }
                ACTION_NEXT -> {
                    mMediaPlayer?.let {
                        if (it.isPlaying) {
                            it.stop()
                        }
                    }

                    // Todo: add Next
                }
                ACTION_STOP -> {
                    onDestroy()
                }
            }
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
        audioManager.abandonAudioFocus(this)
        if (wifiLock.isHeld)
            wifiLock.release()
        stopSelf()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mMediaPlayer == null) initMediaPlayer(uri, headerMap)
                else if (!mMediaPlayer?.isPlaying!!) mMediaPlayer?.start()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mMediaPlayer?.isPlaying!!) mMediaPlayer?.stop()
                mMediaPlayer?.release()
                mMediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mMediaPlayer?.isPlaying!!) mMediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mMediaPlayer?.isPlaying!!) mMediaPlayer?.setVolume(0.1f, 0.1f)
            }
        }
    }
}
