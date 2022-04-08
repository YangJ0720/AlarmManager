package com.example.alarm.manager.ui

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import com.example.alarm.manager.R
import com.example.alarm.manager.service.DaemonService
import com.example.alarm.manager.watcher.RefWatcher

class ShowActivity : Activity() {

    companion object {
        private const val TAG = "ShowActivity"
    }

    private lateinit var mBinder: DaemonService.MainBinder
    private val mConnect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            mBinder = binder as DaemonService.MainBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        //
        RefWatcher.attach(this)
        //
        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val attributes = window.attributes
        attributes.x = 0
        attributes.y = 0
        attributes.width = 10
        attributes.height = 10
        window.attributes = attributes
        //
        mBinder.refresh()
        //
        Log.i(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnect)
        Log.e(TAG, "onDestroy")
    }
}