package com.example.summary_logger.service

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.summary_logger.util.TAG

class NotiListenerService : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "onNotificationPosted")

        if (sbn?.isOngoing == true) {
            Log.d(TAG, "posted ongoing noti")
            return
        }

        try {
            val userId = "000" // TODO("get userId from sharePreference")
            val notificationId: String = userId + "_" + System.currentTimeMillis().toString()
            val notiItem = NotiItem(this, sbn, userId, notificationId)
            notiItem.logProperty()
//            notiItem.upload()
//            TODO("double noti in Messenger")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
//        super.onNotificationRemoved(sbn, rankingMap, reason)
        Log.d(TAG, "onNotificationRemoved, Remove reason: $reason")
    }
}