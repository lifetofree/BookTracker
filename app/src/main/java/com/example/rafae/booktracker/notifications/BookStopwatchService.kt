package com.example.rafae.booktracker.notifications

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.util.Log
import com.example.android.notificationchannels.MainActivity
import com.example.android.notificationchannels.NotificationHelper
import com.example.rafae.booktracker.helpers.TimeHelpers
import java.util.*
import com.example.rafae.booktracker.notifications.messages.MessageEvent
import org.greenrobot.eventbus.EventBus


class BookStopwatchService : Service() {

    private val mBinder = MyBinder()
    private val resultList = ArrayList<String>()
    var startTime: Long = 0
    var stopTime: Long = 0
    private var running = false
    // elapsed seconds since started to read
    var counter = 0

    var bookName: String? = null

    val wordList: List<String> get() = resultList

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        start()
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        running = false
        stopForeground(true)
        stopSelf()
        Log.d("Service", "unbind")
        return super.onUnbind(intent)
    }

    private lateinit var mNotificationHelper: NotificationHelper


    private lateinit var timer: Timer

    fun start() {
        this.startTime = System.currentTimeMillis()
        this.running = true

        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                when (running) {
                    true -> {
                        counter++
                        // launch notification
                        mNotificationHelper = NotificationHelper(applicationContext)

                        val counterText: String


                        mNotificationHelper.notify(MainActivity.NOTIFICATION_FOLLOW, mNotificationHelper.getNotificationElapsedTime(
                                "Reading " + bookName,
                                TimeHelpers.convertSecondsToTimeString(counter)))

                        // launch Event
                        EventBus.getDefault().post(MessageEvent(counter))
                    }
                    false -> {
                        // stop timer
                        timer.cancel()
                        timer.purge()

                        // dismiss notification
                        mNotificationHelper.cancelNotification(MainActivity.NOTIFICATION_FOLLOW)
                    }
                }


            }
        }, 0, 1000)
    }

    inner class MyBinder : Binder() {
        internal val service: BookStopwatchService
            get() = this@BookStopwatchService
    }


    // elaspsed time in milliseconds
    fun getElapsedTime(): Long {
        return if (running) {
            System.currentTimeMillis() - startTime
        } else stopTime - startTime
    }


    // elaspsed time in seconds
    fun getElapsedTimeSecs(): Long {
        return if (running) {
            (System.currentTimeMillis() - startTime) / 1000
        } else (stopTime - startTime) / 1000
    }
}
