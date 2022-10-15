package com.example.summary_logger.service

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotiListenerService : NotificationListenerService() {

//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }
    override fun onCreate() {
        super.onCreate()
        Log.d("NotiListenerService", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotiListenerService", "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d("NotiListenerService", "onNotificationPosted")

        if (sbn?.isOngoing == true) {
            Log.d("NotiListenerService", "posted ongoing noti")
            return
        }

        try {
            val userId = "000"
            val notificationId: String = userId + System.currentTimeMillis().toString()
            val notiItem = NotiItem(sbn, userId, notificationId)
            notiItem.printProperty()
            Log.d("NotiListenerService", "${sbn?.postTime}")
            Log.d("NotiListenerService", "${notiItem.title}")
            TODO("double noti in Messenger")
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
        Log.d("NotiListenerService", "onNotificationRemoved")
    }
}