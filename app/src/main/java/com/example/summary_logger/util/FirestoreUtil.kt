package com.example.summary_logger.util

import android.content.Context
import android.util.Log
import com.example.summary_logger.database.room.ActiveContextDatabase
import com.example.summary_logger.database.room.CurrentDrawerDatabase
import com.example.summary_logger.database.room.PeriodicContextDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val TAG = "Firestore"

fun uploadSummary(appContext: Context, userId: String, summaryId: String, startTime: Long, endTime: Long) {

    val currentDrawerDao = CurrentDrawerDatabase.getInstance(appContext).currentDrawerDao()

    GlobalScope.launch {

        val drawerList = currentDrawerDao.getAll()
        val db = Firebase.firestore
        val summary = hashMapOf(
            "userId" to userId,
            "summaryId" to summaryId,
            "startTime" to startTime,
            "endTime" to endTime,
            "submitTime" to -1,
            "notifications" to drawerList,
            // TODO: notifications
            "summary" to "",
            "reason" to "",
            "selectedNotifications" to listOf<String>()
        )

        db.collection("summary")
            .document(summaryId)
            .set(summary)
            .addOnSuccessListener {
                Log.d(TAG, "upload ${userId}'s summary with docID $summaryId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding context to Firestore", e)
            }

    }
}

fun uploadContexts(appContext: Context, userId: String, summaryId: String, startTime: Long, endTime: Long) {
    val activeDao = ActiveContextDatabase.getInstance(appContext).activeContextDao()
    val periodicDao = PeriodicContextDatabase.getInstance(appContext).periodicContextDao()
    GlobalScope.launch {
        val activeContexts = activeDao.queryByTimeInterval(startTime, endTime)
        val periodicContexts = periodicDao.queryByTimeInterval(startTime, endTime)
        val db = Firebase.firestore

        val contexts = hashMapOf(
            "userId" to userId,
            "summaryId" to summaryId,
            "beginTime" to startTime,
            "endTime" to endTime,
            "activeContexts" to activeContexts,
            "periodicContexts" to periodicContexts,
        )

        db.collection("context")
            .document(summaryId)
            .set(contexts)
            .addOnSuccessListener {
                Log.d(TAG, "upload ${userId}'s contexts with docID $summaryId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding context to Firestore", e)
            }

    }
}

fun upload(context: Context, beginTime: Long, endTime: Long) {
    val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val time = System.currentTimeMillis()
    val summaryId = "${userId}_$time"
    uploadSummary(context, userId, summaryId, beginTime, endTime)
    uploadContexts(context, userId, summaryId, beginTime, endTime)
}

