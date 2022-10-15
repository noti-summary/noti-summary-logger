package com.example.summary_logger.service

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log

class NotiItem(sbn: StatusBarNotification?, private val userId: String, private val notificationId: String) {
    private var notification: Notification? = null
    var title: String? = null
    private var sbnId: Int? = null
    private var postTime: Long? = null

    init {
        postTime = sbn?.postTime
        notification = sbn?.notification
        title = sbn?.notification?.extras?.getString("android.title")
        sbnId = sbn?.id
    }

    fun printProperty() {
        Log.d("NotiItem", "notification: ${this.notification}")
        Log.d("NotiItem", "userId: ${this.userId}")
        Log.d("NotiItem", "postTime: ${this.postTime}")
        Log.d("NotiItem", "sbnId: ${this.sbnId}")
        TODO("fix logging information from here")
    }

    fun upload() {
        println("${this.userId} ${this.notificationId}")
        Log.d("NotiItem", "upload")
    }
}
