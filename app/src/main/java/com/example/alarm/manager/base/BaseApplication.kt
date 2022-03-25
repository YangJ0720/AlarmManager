package com.example.alarm.manager.base

import android.app.Application
import com.dev.daemon.helper.DaemonHelper
import com.example.alarm.manager.service.DaemonService

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DaemonHelper.setup(this, DaemonService::class.java)
    }
}