package com.tt.gitmusic.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.IBinder
import android.os.PowerManager

private const val ACTION_PLAY: String = "com.example.action.PLAY"

class PlayMusic : Service(), MediaPlayer.OnPreparedListener {

    private var mMediaPlayer: MediaPlayer? = null
    private var wifiLock: WifiManager.WifiLock? = null
    override fun onStartCommand(intent: Intent,
                                flags: Int,
                                startId: Int): Int {
        val action = intent.action
        val token = intent.getStringExtra("token")
        val uri = Uri.parse(intent.getStringExtra("url"))
        val map: HashMap<String, String> = HashMap()
        if (token != null) {
            map["Authorization"] = token
            map["Accept"] = "application/vnd.github.v3.raw+json"
        }

        when (action) {
            ACTION_PLAY -> {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock")
                if (mMediaPlayer != null) {
                    if (mMediaPlayer!!.isPlaying) {
                        mMediaPlayer?.stop()
                    }
                }
                mMediaPlayer = MediaPlayer() // initialize it here
                mMediaPlayer?.apply {
                    wifiLock?.acquire()
                    setDataSource(applicationContext, uri, map)
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                    setOnPreparedListener(this@PlayMusic)
                    prepareAsync() // prepare async to not block main thread
                }
                // Todo: create media player controller notification
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    /** Called when MediaPlayer is ready */
    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        wifiLock?.release()
    }
}
