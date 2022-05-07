package com.example.alarm.manager.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import com.example.alarm.manager.receiver.AlarmReceiver
import com.example.alarm.manager.utils.FileUtils
import com.xdandroid.hellodaemon.AbsWorkService
import java.io.IOException

class AlarmService : AbsWorkService() {

    companion object {
        const val EXTRA_NAME = "startForegroundService"

        //
        private const val TAG = "AlarmService"
        private const val WHAT_NOTIFICATION = 1
        private const val DELAY_NOTIFICATION = 30000L
        private var sIsRunning = false
    }

    private var mPlayer: MediaPlayer? = null
    private val mBinder: MainBinder = MainBinder()
    private lateinit var mHandler: AlarmHandler
    private lateinit var mReceiver: AlarmReceiver

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        //
        FileUtils.writeFileToSDCard(this, assets.open("di.ogg"))
        // handler
        this.mHandler = AlarmHandler(Looper.getMainLooper())
        // receiver
        val receiver = AlarmReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiver, filter)
        this.mReceiver = receiver
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val value = intent.getBooleanExtra(EXTRA_NAME, true)
        if (value) {
            startPlayer()
        } else {
            val manager = getSystemService(Context.POWER_SERVICE) as PowerManager
            Log.i(TAG, "manager.isInteractive = ${manager.isInteractive}")
            if (manager.isInteractive) {
                stopPlayer()
            } else {
                startPlayer()
            }
        }
        Log.i(TAG, "onStartCommand")
        return super.onStartCommand(intent, START_FLAG_RETRY, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
        unregisterReceiver(mReceiver)
    }

    override fun onBind(intent: Intent?, alwaysNull: Void?): IBinder {
        Log.i(TAG, "onBind")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun shouldStopService(intent: Intent?, flags: Int, startId: Int): Boolean {
        Log.i(TAG, "shouldStopService")
        return sIsRunning
    }

    override fun startWork(intent: Intent?, flags: Int, startId: Int) {
        Log.i(TAG, "startWork")
        sIsRunning = true
    }

    override fun stopWork(intent: Intent?, flags: Int, startId: Int) {
        Log.i(TAG, "stopWork")
        sIsRunning = false
        cancelJobAlarmSub()
    }

    override fun isWorkRunning(intent: Intent?, flags: Int, startId: Int): Boolean {
        Log.i(TAG, "isWorkRunning")
        return sIsRunning
    }

    override fun onServiceKilled(rootIntent: Intent?) {
        Log.i(TAG, "onServiceKilled")
    }

    /**
     * 播放音乐
     */
    private fun startPlayer() {
        // 播放音乐
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
        }
        mPlayer?.let {
            it.reset()
            it.setDataSource(FileUtils.getPath(this))
            it.setOnPreparedListener { player ->
                player.start()
            }
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.prepareAsync()
            } catch (e: IOException) {
                Log.e(TAG, "e = ${e.message}")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "e = ${e.message}")
            }
        }
    }

    private fun stopPlayer() {
        mPlayer?.stop()
    }

    inner class MainBinder : Binder() {
        fun refresh(): Long {
            return System.currentTimeMillis()
        }
    }

    inner class AlarmHandler(looper: Looper) : Handler(looper) {

        init {
            sendHandler(delayMillis = 0)
        }

        private fun sendHandler(delayMillis: Long = DELAY_NOTIFICATION) {
            val msg = Message.obtain()
            msg.what = WHAT_NOTIFICATION
            sendMessageDelayed(msg, delayMillis)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i(TAG, "handleMessage")
            startPlayer()
            //
            sendHandler()
        }
    }

}