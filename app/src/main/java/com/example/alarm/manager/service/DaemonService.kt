package com.example.alarm.manager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dev.daemon.service.BaseHeartBeatService
import com.example.alarm.manager.R
import com.example.alarm.manager.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class DaemonService : BaseHeartBeatService() {

    companion object {
        private const val TAG = "DaemonService"
        private const val CHANNEL_ID = "id"
    }

    override fun onStartService() {
        Log.i(TAG, "onStartService")
    }

    override fun onStopService() {
        Log.i(TAG, "onStopService")
    }

    override fun getDelayExecutedMillis(): Long {
        Log.i(TAG, "getDelayExecutedMillis")
        return 0
    }

    override fun getHeartBeatMillis(): Long {
        Log.i(TAG, "getHeartBeatMillis")
        return 10 * 1000
    }

    override fun onHeartBeat() {
        Log.i(TAG, "onHeartBeat")
        //
        Handler(Looper.getMainLooper()).post {
            val context = applicationContext
            //
            // 获取通知管理器
            val name = Context.NOTIFICATION_SERVICE
            val manager = context?.getSystemService(name) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 创建通知渠道
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, TAG, importance)
                manager.createNotificationChannel(channel)
            }
            // 创建通知
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getContentText())
                .setAutoCancel(true)
                .build()
            manager.notify(100, notification)
        }
    }

    private fun getContentText(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
}