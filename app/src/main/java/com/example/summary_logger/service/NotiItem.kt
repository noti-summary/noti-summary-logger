package com.example.summary_logger.service

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log

class NotiItem(sbn: StatusBarNotification?, private val userId: String, private val notificationId: String) {
    private var notification: Notification? = null
    private var sbnId: Int? = null
    private var postTime: Long? = null

    init {
        postTime = sbn?.postTime
        notification = sbn?.notification
        sbnId = sbn?.id
        Log.d("NotiItem", "init")
    }

    fun printProperty() {
        Log.d("NotiItem", "notification: ${this.notification}")
        Log.d("NotiItem", "userId: ${this.userId}")
        Log.d("NotiItem", "postTime: ${this.postTime}")
        Log.d("NotiItem", "sbnId: ${this.sbnId}")
    }

    fun upload() {
        println("${this.userId} ${this.notificationId}")
        Log.d("NotiItem", "upload")
    }
}
