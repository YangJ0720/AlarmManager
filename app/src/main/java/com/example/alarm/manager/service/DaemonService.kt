package com.example.alarm.manager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import android.widget.Toast
import com.dev.daemon.service.BaseHeartBeatService
import com.example.alarm.manager.receiver.AlarmReceiver
import com.nlf.calendar.Lunar
import java.text.SimpleDateFormat
import java.util.*


class DaemonService : BaseHeartBeatService() {

    companion object {
        private const val TAG = "DaemonService"
    }

    private val mBinder: MainBinder = MainBinder()
    private lateinit var mAlarmReceiver: AlarmReceiver

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val receiver = AlarmReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(AlarmReceiver.RECEIVER_ACTION)
            registerReceiver(receiver, filter)
            mAlarmReceiver = receiver
        } else {
            val receiver = AlarmReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(receiver, filter)
            mAlarmReceiver = receiver
        }
        Log.i(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, START_FLAG_RETRY, startId)
    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mAlarmReceiver)
        Log.i(TAG, "onDestroy")
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
        return 5 * 1000
    }

    override fun onHeartBeat() {
        Log.i(TAG, "onHeartBeat")
        //
        val date: Date = setAlarm() ?: return
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val text = format.format(date)
        Log.i(TAG, "setAlarm -> $text")
    }

    private fun setAlarm(): Date? {
        val manager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.MINUTE, 20)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
        val date = calendar.time
        // 判断设置闹钟日期是否在当前日期之后
        if (System.currentTimeMillis() > date.time) {
            return null
        }
        // 设置闹钟
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.RECEIVER_ACTION
        val requestCode = 0
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        val operation = PendingIntent.getBroadcast(this, requestCode, intent, flags)
        manager.set(AlarmManager.RTC_WAKEUP, date.time, operation)
        return date
    }

    inner class MainBinder : Binder() {
        fun refresh() {
            
        }
    }

}