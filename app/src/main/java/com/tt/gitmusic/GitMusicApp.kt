package com.tt.gitmusic

import android.app.Application
import appComponent
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GitMusicApp : Application() {

    companion object{
         lateinit var mSelf: GitMusicApp
    }

    private lateinit var mGSon: Gson

    fun getInstance(): GitMusicApp {
        return GitMusicApp.mSelf
    }

    public fun getGson(): Gson{
        return mGSon
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GitMusicApp)
            androidLogger()
            modules(appComponent)
        }
    }
}