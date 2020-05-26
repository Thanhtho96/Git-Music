package com.tt.gitmusic

import android.app.Application
import com.tt.gitmusic.di.appComponent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GitMusicApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GitMusicApp)
            androidLogger()
            modules(appComponent)
        }
    }
}