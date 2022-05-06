package com.example.alarm.manager.receiver

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.alarm.manager.R
import com.example.alarm.manager.service.AlarmService
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

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive -> action = ${intent.action}")
        when (intent.action) {
            RECEIVER_ACTION -> {
                sendNotification(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val lock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (lock.isHeld) {
                    lock.release()
                }
                //
                startForegroundService(context, false)
                //
                RefWatcher.detach()
            }
            Intent.ACTION_SCREEN_OFF -> {
                val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val lock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (!lock.isHeld) {
                    lock.acquire()
                }
                //
                startForegroundService(context, true)
                //
                val i = Intent(context, ShowActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
            }
            else -> {}
        }
    }

    private fun sendNotification(context: Context) {
        // 获取通知管理器
        val name = Context.NOTIFICATION_SERVICE
        val manager = context.getSystemService(name) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, TAG, importance)
            manager.createNotificationChannel(channel)
        }
        //
        val intent = Intent(context, MainActivity::class.java)
        val flags = PendingIntent.FLAG_CANCEL_CURRENT
        val pending = PendingIntent.getActivity(context, 0, intent, flags)
        // 创建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(getContentText())
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build()
        manager.notify(100, notification)
    }

    private fun getContentText(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }

    private fun startForegroundService(context: Context, value: Boolean) {
        val intent = Intent(context, AlarmService::class.java)
        intent.putExtra(AlarmService.EXTRA_NAME, value)
        ContextCompat.startForegroundService(context, intent)
    }
}