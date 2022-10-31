package com.example.summary_logger.service

import android.app.Notification
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.content.Context
import android.content.pm.ApplicationInfo
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.summary_logger.util.TAG
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class NotiItem(context:Context,
               sbn: StatusBarNotification?,
               private val userId: String,
               private val notificationId: String) {

    private var appName: String? = null
    private var title: String? = null
    private var content: String? = null
    private var category: String? = null
    private var packageName: String? = null

    private var notification: Notification? = null
    private var key: String? = null
    private var unixTime: Long? = null
    private var postTime: String? = null
    private var group: String? = null

    private var sbnId: Int? = null

    init {
        this.title = sbn?.notification?.extras?.getString(EXTRA_TITLE)
        this.content = sbn?.notification?.extras?.getString(EXTRA_TEXT)
        this.category = sbn?.notification?.category
        this.packageName = sbn?.packageName

        this.notification = sbn?.notification
        this.key = sbn?.key
        this.unixTime = sbn?.postTime
        this.postTime = sbn?.postTime?.let { Date(it).toString() }
        this.group = sbn?.notification?.group

        this.sbnId = sbn?.id

        val applicationInfo: ApplicationInfo? =
            sbn?.packageName?.let {
                packageManager(context.applicationContext).getApplicationInfo(it, 0)
            }

        this.appName = (if (applicationInfo != null) {
            val s = packageManager(context.applicationContext).getApplicationLabel(
                sbn.packageName!!
            ).toString()
            s
        } else {
            this.packageName
        })
    }

    fun logProperty() {
        Log.d(TAG, "############# NotiItem Property Start #############")

        Log.d(TAG, "userId=${this.userId}")
        Log.d(TAG, "notificationId=${this.notificationId}")
        Log.d(TAG, "postTime=${this.postTime}")

        Log.d(TAG, "appName=${this.appName}")
        Log.d(TAG, "title=${this.title}")
        Log.d(TAG, "content=${this.content}")
        Log.d(TAG, "category=${this.category}")

//        Log.d(TAG, "packageName=${this.packageName}")
//        Log.d(TAG, "notification=${this.notification}")
//        Log.d(TAG, "key=${this.key}")
//        Log.d(TAG, "group=${this.group}")

        Log.d(TAG, "############# NotiItem Property End #############")
    }

    fun upload() {
        val db = Firebase.firestore
        val noti = hashMapOf(
            "userId" to this.userId,
            "notificationId" to this.notificationId,
            "unixTime" to this.postTime,
            "postTime" to this.postTime,
            "appName" to this.appName,
            "title" to this.title,
            "content" to this.title,
            "category" to this.category
        )

        db.collection("notification")
            .document(this.notificationId)
            .set(noti)
            .addOnSuccessListener {
                Log.d(TAG, "upload ${this.userId}'s noti with docID ${this.notificationId}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding noti to firestore", e)
            }
    }
}
