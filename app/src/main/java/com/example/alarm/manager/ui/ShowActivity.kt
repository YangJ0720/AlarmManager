package com.example.alarm.manager.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import com.example.alarm.manager.R
import com.example.alarm.manager.service.AlarmService
import com.example.alarm.manager.watcher.RefWatcher

class ShowActivity : Activity() {

    companion object {
        private const val TAG = "ShowActivity"
    }

    private val mConnect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val binder = binder as AlarmService.MainBinder
            val currentTimeMillis = binder.refresh()
            Log.i(TAG, "currentTimeMillis = $currentTimeMillis")
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        initData()
        initView()
        initService()
        Log.i(TAG, "onCreate")
    }

    private fun initData() {
        RefWatcher.attach(this)
    }

    private fun initView() {
        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val attributes = window.attributes
        attributes.x = 0
        attributes.y = 0
        attributes.width = 10
        attributes.height = 10
        window.attributes = attributes
    }

    private fun initService() {
        val intent = Intent(this, AlarmService::class.java)
        bindService(intent, mConnect, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnect)
        Log.e(TAG, "onDestroy")
    }
}