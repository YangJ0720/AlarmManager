package com.example.alarm.manager.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarm.manager.R
import com.example.alarm.manager.service.DaemonService
import com.example.alarm.manager.ui.MainActivity
import com.example.alarm.manager.ui.ShowActivity
import com.example.alarm.manager.watcher.RefWatcher
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "id"
        const val RECEIVER_ACTION = "receiver_action"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive -> action = ${intent.action}")
        when (intent.action) {
            RECEIVER_ACTION -> {
                // 获取通知管理器
                val name = Context.NOTIFICATION_SERVICE
                val manager = context.getSystemService(name) as NotificationManager
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
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .build()
                manager.notify(100, notification)
            }
            Intent.ACTION_SCREEN_ON -> {
                RefWatcher.detach()
            }
            Intent.ACTION_SCREEN_OFF -> {
                val i = Intent(context, ShowActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
            }
            else -> {}
        }
    }

    private fun getContentText(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
}