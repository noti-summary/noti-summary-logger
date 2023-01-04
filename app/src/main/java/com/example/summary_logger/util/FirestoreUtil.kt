package com.example.summary_logger.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.example.summary_logger.database.room.ActiveContextDatabase
import com.example.summary_logger.database.room.CurrentDrawerDatabase
import com.example.summary_logger.database.room.PeriodicContextDatabase
import com.example.summary_logger.service.ContextListenerService
import com.example.summary_logger.service.NotiListenerService
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val TAG = "Firestore"

@RequiresApi(Build.VERSION_CODES.O)
fun datetimeFormat(time: Long): String {
    val zoneName = "Asia/Taipei"
    val zoneID = ZoneId.of(zoneName)
    val instant = Instant.ofEpochMilli(time)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return instant.atZone(zoneID).format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun uploadIcons(appContext: Context, packageManager: PackageManager) {

    val currentDrawerDao = CurrentDrawerDatabase.getInstance(appContext).currentDrawerDao()

    GlobalScope.launch {

        val packageList = currentDrawerDao.getAllPackages()

        fun getAppName(packageName: String): String {
            val ai: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            return packageManager.getApplicationLabel(ai).toString()
        }

        fun getIconString(packageName: String): String {
            val iconBitmap = packageManager.getApplicationIcon(packageName).toBitmap()
            val byteStream = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
            val byteArray: ByteArray = byteStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        val db = Firebase.firestore
        val iconStrings = packageList.map { getAppName(it) to getIconString(it) }.toMap()

        db.collection("appicon")
            .document("appicons")
            .update(iconStrings)
            .addOnSuccessListener {
                Log.d(TAG, "Uploaded icons")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding icon to Firestore", e)
            }

    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun uploadSummary(appContext: Context, userId: String, summaryId: String, startTime: Long, endTime: Long) {

    val currentDrawerDao = CurrentDrawerDatabase.getInstance(appContext).currentDrawerDao()

    GlobalScope.launch {

        val drawerList = currentDrawerDao.getAll()
        val db = Firebase.firestore
        val summary = hashMapOf(
            "userId" to userId,
            "summaryId" to summaryId,
            "startTime" to datetimeFormat(startTime),
            "endTime" to datetimeFormat(endTime),
            "submitTime" to datetimeFormat(0),
            "notifications" to drawerList,
            "esm" to hashMapOf<Any, Any>(),
            "summary" to "",
            "reason" to "",
            "selectedNotifications" to listOf<String>(),
            // TODO: Get live locations
            "longitude" to ContextListenerService.latestPeriodicContext.longitude,
            "latitude" to ContextListenerService.latestPeriodicContext.latitude,
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

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
fun upload(context: Context, packageManager: PackageManager, beginTime: Long, endTime: Long) {
    val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val time = System.currentTimeMillis()
    val summaryId = "${userId}_$time"
    uploadSummary(context, userId, summaryId, beginTime, endTime)
    uploadContexts(context, userId, summaryId, beginTime, endTime)
    uploadIcons(context, packageManager)
}

