package com.example.alarm.manager.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.alarm.manager.receiver.AlarmReceiver
import java.util.*

class AlarmService : Service() {

    companion object {
        private const val TAG = "AlarmService"
        private const val RECEIVER_ACTION = "receiver_action"

        fun launch(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, AlarmService::class.java))
            } else {
                context.startService(Intent(context, AlarmService::class.java))
            }
        }
    }

    private lateinit var mAlarmReceiver: AlarmReceiver

    override fun onBind(intent: Intent): IBinder {
        return AlarmBinder()
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val receiver = AlarmReceiver()
            registerReceiver(receiver, IntentFilter(RECEIVER_ACTION))
            mAlarmReceiver = receiver
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        setAlarm()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unregisterReceiver(mAlarmReceiver)
        }
    }

    private fun setAlarm() {
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        val intent = Intent(RECEIVER_ACTION)
        val requestCode = 0
        val flags = PendingIntent.FLAG_CANCEL_CURRENT
        val operation = PendingIntent.getBroadcast(this, requestCode, intent, flags)
        manager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, operation)
    }

    class AlarmBinder : Binder()
}