package com.tt.gitmusic.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tt.gitmusic.R
import com.tt.gitmusic.receiver.MusicReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayMusic : MediaPlayer.OnPreparedListener, Service(), AudioManager.OnAudioFocusChangeListener {

    private var mMediaPlayer: MediaPlayer? = null
    private var metadataRetriever: MediaMetadataRetriever? = null
    private var audioManager: AudioManager? = null
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var uri: Uri
    private lateinit var headerMap: HashMap<String, String>
    private lateinit var songName: String
    private lateinit var audioFocusRequest: AudioFocusRequest
    private var title: String? = null
    private var artist: String? = null
    private var album: String? = null
    private var duration: Long? = null
    private var bitmap: Bitmap? = null

    companion object {
        const val ACTION_PLAY: String = "com.tt.music.action.PLAY"
        const val ACTION_PREVIOUS: String = "com.tt.music.action.PREVIOUS"
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
            headerMap["Accept"] = "application/vnd.github.v3.raw"
        }

        val wifiManager = this@PlayMusic.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock")
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.release()
            }
        }

        title = "Loading.."
        artist = "Loading.."
        album = "Loading.."
        duration = -1
        bitmap = null
        startForeground(1, buildNotification(R.drawable.ic_play_arrow_black_24dp).build())
        initMediaPlayer(uri, headerMap)

        createNotificationChannel("Music", "Play music", "Music")

        registerReceiver(broadcastReceiver, IntentFilter("MusicFilter"))

        return START_STICKY
    }

    private fun buildNotification(iconPlay: Int): NotificationCompat.Builder {
        val mediaSession = MediaSessionCompat(this@PlayMusic, "GitMusicSession")
        val metadata = MediaMetadataCompat.Builder().apply {
            putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)
        }

        mediaSession.setMetadata(metadata.build())

        val intentPrev = Intent(this, MusicReceiver::class.java)
        intentPrev.action = ACTION_PREVIOUS

        val pdPrev = PendingIntent.getBroadcast(
                this,
                0,
                intentPrev,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentPause = Intent(this, MusicReceiver::class.java)
        intentPause.action = ACTION_PLAY

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
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(artist)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", pdPrev)
                .addAction(iconPlay, "Pause", pdPause)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", pdNext)
                .addAction(R.drawable.ic_close_black_24dp, "Stop", pdStop)
                .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setMediaSession(mediaSession.sessionToken)
                )
                .setLargeIcon(bitmap)
                .setShowWhen(false)
                .setSubText(album)
    }

    private fun initMediaPlayer(uri: Uri,
                                headerMap: HashMap<String, String>) {
        if (metadataRetriever == null) {
            metadataRetriever = MediaMetadataRetriever()
        }
        GlobalScope.launch(Dispatchers.IO) {
            metadataRetriever?.apply {
                setDataSource(uri.toString(), headerMap)
            }

            metadataRetriever?.let { metadata ->
                bitmap = null
                metadata.embeddedPicture?.let {
                    bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                }
                title = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?: songName
                artist = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?: "Unknown"
                album = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        ?: "Unknown"
                val du = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                duration = if (du == 0L) {
                    -1
                } else {
                    du
                }

                withContext(Dispatchers.Main) {
                    mMediaPlayer = MediaPlayer() // initialize it here
                    mMediaPlayer?.apply {
                        wifiLock.acquire()
                        setAudioAttributes(AudioAttributes.Builder().apply {
                            setUsage(AudioAttributes.USAGE_MEDIA)
                            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        }.build())
                        setDataSource(this@PlayMusic, uri, headerMap)
                        setWakeMode(this@PlayMusic, PowerManager.PARTIAL_WAKE_LOCK)
                        setOnPreparedListener(this@PlayMusic)
                        prepareAsync() // prepare async to not block main thread
                    }
                }
                Log.d("metadata", "${title}||${artist}||${duration}||${album}")
            }
        }
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?,
                               intent: Intent?) {
            when (intent?.getStringExtra("Action")) {
                ACTION_PLAY -> {
                    if (mMediaPlayer == null) {
                        initMediaPlayer(uri, headerMap)
                    } else {
                        if (mMediaPlayer?.isPlaying!!) {
                            onPrepared(mMediaPlayer!!)
                            mMediaPlayer?.pause()
                            stopForeground(false)
                            with(NotificationManagerCompat.from(this@PlayMusic)) {
                                notify(1, buildNotification(R.drawable.ic_play_arrow_black_24dp).build())
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                audioManager?.requestAudioFocus(audioFocusRequest)
                            } else {
                                audioManager?.requestAudioFocus(this@PlayMusic, AudioManager.STREAM_MUSIC,
                                        AudioManager.AUDIOFOCUS_GAIN)
                            }
                            mMediaPlayer?.setVolume(1f, 1f)
                            mMediaPlayer?.start()
                            startForeground(1, buildNotification(R.drawable.ic_pause_black_24dp).build())
                        }
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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = (AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this@PlayMusic)
                    .build())

            audioManager?.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
        }

        mediaPlayer.start()
        startForeground(1, buildNotification(R.drawable.ic_pause_black_24dp).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mMediaPlayer?.release()
        metadataRetriever?.release()
        audioManager?.abandonAudioFocus(this)
        if (wifiLock.isHeld)
            wifiLock.release()
        stopSelf()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mMediaPlayer?.setVolume(1f, 1f)
                mMediaPlayer?.start()

                startForeground(1, buildNotification(R.drawable.ic_pause_black_24dp).build())
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                mMediaPlayer?.pause()
                stopForeground(false)
                with(NotificationManagerCompat.from(this@PlayMusic)) {
                    notify(1, buildNotification(R.drawable.ic_play_arrow_black_24dp).build())
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mMediaPlayer?.pause()
                stopForeground(false)
                with(NotificationManagerCompat.from(this@PlayMusic)) {
                    notify(1, buildNotification(R.drawable.ic_play_arrow_black_24dp).build())
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mMediaPlayer?.let {
                    if (it.isPlaying)
                        it.setVolume(0.1f, 0.1f)
                }
            }
        }
    }
}
