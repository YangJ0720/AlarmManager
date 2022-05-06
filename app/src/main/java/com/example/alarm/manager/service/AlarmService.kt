package com.example.alarm.manager.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarm.manager.R
import com.example.alarm.manager.receiver.AlarmReceiver
import com.example.alarm.manager.ui.MainActivity
import com.example.alarm.manager.utils.FileUtils
import com.xdandroid.hellodaemon.AbsWorkService
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AlarmService : AbsWorkService() {

    companion object {
        const val EXTRA_NAME = "startForegroundService"
        //
        private const val TAG = "AlarmService"
        private const val CHANNEL_ID = "id"
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
        val value = intent.getBooleanExtra(EXTRA_NAME, false)
        if (value) {
            player()
        } else {
            val manager = getSystemService(Context.POWER_SERVICE) as PowerManager
            Log.i(TAG, "manager.isInteractive = ${manager.isInteractive}")
            if (manager.isInteractive) {
                mPlayer?.stop()
            } else {
                player()
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
        return mBinder
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

    private fun player() {
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
            } catch (e: IllegalStateException) {
                Log.e(TAG, "e = ${e.message}")
            } catch (e: IOException) {
                Log.e(TAG, "e = ${e.message}")
            }
        }
    }

    private fun sendNotification() {
        // 获取通知管理器
        val name = Context.NOTIFICATION_SERVICE
        val manager = getSystemService(name) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, TAG, importance)
            manager.createNotificationChannel(channel)
        }
        //
        val intent = Intent(this, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        val pending = PendingIntent.getActivity(this, 0, intent, flags)
        // 创建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getContentText())
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build()
        manager.notify(100, notification)
        // 播放音乐
        player()
    }

    private fun getContentText(): String {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date())
    }

    inner class MainBinder : Binder() {
        fun refresh(): Long {
            return System.currentTimeMillis()
        }
    }

    inner class AlarmHandler(looper: Looper) : Handler(looper) {

        init {
            sendHandler()
        }

        private fun sendHandler() {
            val msg = Message.obtain()
            msg.what = WHAT_NOTIFICATION
            sendMessageDelayed(msg, DELAY_NOTIFICATION)
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i(TAG, "handleMessage")
            sendNotification()
            //
            sendHandler()
        }
    }

}