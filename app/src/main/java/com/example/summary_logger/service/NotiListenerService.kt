package com.example.summary_logger.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.example.summary_logger.MainActivity
import com.example.summary_logger.database.room.CurrentDrawerDatabase
import com.example.summary_logger.util.TAG
import com.example.summary_logger.util.upload
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.concurrent.timerTask

class NotiListenerService : NotificationListenerService() {

    companion object {
        var currentNotiCount: Int = 0
        val notiThreshold: Int = 0
        val timeThreshold: Long = 600000
        val uptimeThreshold: Long = 300000
        var prevUpload: Long = 0L
        var connected: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, NotiListenerService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + 10000, restartServicePendingIntent)
        Log.d(TAG, "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    private fun adHocRemove(notiItem: NotiItem): Boolean {
        val title = notiItem.getTitle()
        val content = notiItem.getContent()
        val flags = notiItem.getFlags()
        val packageName = notiItem.getPackageName()
        val notiId = notiItem.getSbnId()

        if (title == "null" && content == "null")
            return true
        if (packageName == "jp.naver.line.android" && notiId == 16880000)
            return true
        if (packageName == "com.google.android.gm" && flags?.and(512) != 0)
            return true
        if (packageName == "com.Slack" && flags != 16)
            return true

        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "onNotificationPosted")
        appendNotifications(sbn)
    }

    private fun appendNotifications(sbn: StatusBarNotification?) {
        try {

            if (sbn?.tag == null)
                return

            if (sbn.isOngoing) {
                Log.d(TAG, "posted ongoing noti")
                return
            }

            val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", "000").toString()
            val notiItem = NotiItem(this, sbn, userId)

            if (adHocRemove(notiItem))
                return

            val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
            val drawerNoti = notiItem.makeDrawerNoti()
            GlobalScope.launch {
                if (drawerNoti.sortKey != "null")
                    currentDrawerDao.deleteByPackageSortKey(drawerNoti.packageName, drawerNoti.groupKey, drawerNoti.sortKey)
                currentDrawerDao.insert(drawerNoti)
                Log.d(TAG, "insert drawerNoti")
                notiItem.logProperty()
            }
            notiItem.upload()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removedBySystem() {
        val reasons: Set<Int> = setOf(
            NotificationListenerService.REASON_APP_CANCEL,
            NotificationListenerService.REASON_LISTENER_CANCEL,
        )
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()
        val notiItem = NotiItem(this, sbn, userId)

        // if ()

        val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
        val drawerNoti = notiItem.makeDrawerNoti()
        GlobalScope.launch {
            currentDrawerDao.deleteByPackageGroup(drawerNoti.packageName, drawerNoti.groupKey)
            Log.d(TAG, "remove drawerNoti")
            notiItem.logProperty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onListenerConnected() {
        super.onListenerConnected()
        connected = true

        if (SystemClock.uptimeMillis() < uptimeThreshold) {
            val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
            GlobalScope.launch {
                currentDrawerDao.deleteAll()
                activeNotifications.forEach {
                    appendNotifications(it)
                }
            }
        }

        Timer().scheduleAtFixedRate(timerTask {

            val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
            GlobalScope.launch {
                currentNotiCount = currentDrawerDao.getAll().size
                val currentTime = System.currentTimeMillis()
                val timeDelta = currentTime - prevUpload
                if (currentNotiCount > notiThreshold && timeDelta > timeThreshold) {
                    // upload(applicationContext, prevUpload, currentTime)
                    prevUpload = currentTime
                }
            }
        }, 1800000, 10000)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        connected = false
    }
}