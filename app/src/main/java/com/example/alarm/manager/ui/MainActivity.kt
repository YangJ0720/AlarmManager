package com.example.alarm.manager.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.alarm.manager.R
import com.example.alarm.manager.service.AlarmService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var mBinder: AlarmService.MainBinder? = null
    private val mConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            mBinder = binder as AlarmService.MainBinder?
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initView()
        initService()
        Log.i(TAG, "onCreate")
    }

    override fun onResume() {
        super.onResume()
        val currentTimeMillis = mBinder?.refresh()
        Log.i(TAG, "onResume -> currentTimeMillis = $currentTimeMillis")
    }

    override fun onStop() {
        super.onStop()
        val currentTimeMillis = mBinder?.refresh()
        Log.i(TAG, "onStop -> currentTimeMillis = $currentTimeMillis")
    }

    override fun onDestroy() {
        unbindService(mConn)
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }

    private fun initData() {

    }

    private fun initView() {
        findViewById<View>(R.id.btn_ignore).setOnClickListener {
            ignoreBatteryOptimizations()
        }
    }

    private fun initService() {
        val intent = Intent(this, AlarmService::class.java)
        bindService(intent, mConn, Context.BIND_AUTO_CREATE)
    }

    private fun ignoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

}