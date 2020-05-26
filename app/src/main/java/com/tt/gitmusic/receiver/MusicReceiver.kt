package com.tt.gitmusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val intent1 = Intent("MusicFilter")
        intent1.putExtra("Action", intent.action)
        context.sendBroadcast(intent1)
    }
}
