package com.example.alarm.manager.base

import android.app.Application
import com.example.alarm.manager.service.AlarmService
import com.xdandroid.hellodaemon.DaemonEnv

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val cls = AlarmService::class.java
        DaemonEnv.initialize(this, cls, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL)
        DaemonEnv.startServiceMayBind(cls)
    }
}