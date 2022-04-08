package com.example.alarm.manager.watcher

import android.app.Activity
import java.lang.ref.WeakReference

object RefWatcher {

    private var mReference: WeakReference<Activity>? = null

    fun attach(activity: Activity) {
        mReference = WeakReference(activity)
    }

    fun detach() {
        val activity: Activity? = mReference?.get()
        activity?.finish()
    }
}