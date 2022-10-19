package com.example.summary_logger.service

import android.app.Notification
import android.app.Notification.*
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.summary_logger.util.TAG

class NotiItem(sbn: StatusBarNotification?, private val userId: String, private val notificationId: String) {

    private var title: String? = null
    private var content: String? = null
    private var category: String? = null
    private var packageName: String? = null

    private var notification: Notification? = null
    private var key: String? = null
    private var postTime: Long? = null
    private var group: String? = null

    private var sbnId: Int? = null

    init {
        this.title = sbn?.notification?.extras?.getString(EXTRA_TITLE)
        this.content = sbn?.notification?.extras?.getString(EXTRA_TEXT)
        this.category = sbn?.notification?.category
        this.packageName = sbn?.packageName

        this.notification = sbn?.notification
        this.key = sbn?.key
        this.postTime = sbn?.postTime
        this.group = sbn?.notification?.group

        this.sbnId = sbn?.id
    }

    fun logProperty() {
        Log.d(TAG, "############# NotiItem Property Start #############")

        Log.d(TAG, "userId=${this.userId}")
        Log.d(TAG, "notificationId=${this.notificationId}")

        Log.d(TAG, "title=${this.title}")
        Log.d(TAG, "content=${this.content}")
        Log.d(TAG, "category=${this.category}")
        Log.d(TAG, "packageName=${this.packageName}")

        Log.d(TAG, "notification=${this.notification}")
        Log.d(TAG, "key=${this.key}")
        Log.d(TAG, "postTime=${this.postTime}")
        Log.d(TAG, "group=${this.group}")

        Log.d(TAG, "############# NotiItem Property End #############")
    }

    fun upload() {
        val content = if (this.content?.length!! <= 20) this.content else this.content?.substring(0, 20)
        println("${this.userId} ${this.notificationId} $content")
        Log.d("NotiItem", "upload")

        val db = Firebase.firestore
        val noti = hashMapOf(
            "user_id" to this.userId,
            "notification_id" to this.notificationId,
        )

        db.collection("notification")
            .document(this.notificationId)
            .set(noti)
            .addOnSuccessListener { documentReference ->
                Log.d("NotiItem", "Uploaded noti to firestore with document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("NotiItem", "Error adding noti to firestore", e)
            }
    }
}
