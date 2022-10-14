package com.example.summary_logger.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotiListenerService : NotificationListenerService() {

//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.isOngoing == true) {
            Log.d("NotiListenerService", "posted ongoing noti")
            return
        }
    }
}