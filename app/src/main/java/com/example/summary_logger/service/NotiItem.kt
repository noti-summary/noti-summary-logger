package com.example.summary_logger.service

import android.app.Notification
import android.app.Notification.*
import android.content.Context
import android.content.pm.ApplicationInfo
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.summary_logger.model.CurrentDrawer
import com.example.summary_logger.util.TAG
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.jvm.internal.impl.metadata.deserialization.Flags

class NotiItem(context:Context,
               sbn: StatusBarNotification?,
               private val userId: String) {

    private var appName: String? = null
    private var title: String = ""
    private var content: String = ""
    private var category: String = ""
    private lateinit var packageName: String

    private var notification: Notification? = null
    private lateinit var key: String
    private var unixTime: Long? = null
    private var postTime: String? = null
    private var group: String? = null
    private var onGoing: Boolean? = null
    private var flags: Int? = null
    private lateinit var sortKey: String

    private lateinit var notificationId: String

    private var sbnId: Int? = null

    init {

        this.title = sbn?.notification?.extras?.getCharSequence(EXTRA_TITLE).toString()

        this.content = if (sbn?.notification?.extras?.getCharSequence(EXTRA_TEXT).toString().isEmpty()) {
            sbn?.notification?.extras?.getCharSequence(EXTRA_BIG_TEXT).toString()
        } else {
            sbn?.notification?.extras?.getCharSequence(EXTRA_TEXT).toString()
        }
        this.category = sbn?.notification?.category ?: "others"
        this.packageName = sbn?.packageName.toString()

        this.notification = sbn?.notification
        this.key = sbn?.key.toString()
        this.unixTime = sbn?.postTime
        this.postTime = sbn?.postTime?.let { Date(it).toString() }
        this.group = sbn?.notification?.group
        this.flags = sbn?.notification?.flags
        this.sortKey = sbn?.notification?.sortKey.toString()

        this.notificationId = "${this.userId}_${this.unixTime}"

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
        Log.d(TAG, "postTime=${this.unixTime}")

        Log.d(TAG, "appName=${this.appName}")
        Log.d(TAG, "title=${this.title}")
        Log.d(TAG, "content=${this.content}")
        Log.d(TAG, "category=${this.category}")

        Log.d(TAG, "packageName=${this.packageName}")
        Log.d(TAG, "notification=${this.notification}")
        Log.d(TAG, "key=${this.key}")
        Log.d(TAG, "group=${this.group}")

        Log.d(TAG, "############# NotiItem Property End #############")
    }

    fun upload() {
        val db = Firebase.firestore
        val noti = hashMapOf(
            "userId" to this.userId,
            "notificationId" to this.notificationId,
            "postTime" to this.postTime,
            "appName" to this.appName,
            "title" to this.title,
            "content" to this.content,
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

    fun getFlags(): Int? {
        return flags
    }

    fun getPackageName(): String? {
        return packageName
    }

    fun getSbnId(): Int? {
        return sbnId
    }

    fun getTitle(): String? {
        return title
    }

    fun getContent(): String? {
        return content
    }

    fun makeDrawerNoti(): CurrentDrawer {
        return CurrentDrawer(0, this.notificationId, this.packageName, this.key, this.sortKey)
    }

}
