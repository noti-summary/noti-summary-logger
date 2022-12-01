package com.example.summary_logger.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.summary_logger.database.room.CurrentDrawerDatabase
import com.example.summary_logger.util.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotiListenerService : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, NotiListenerService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + 10000, restartServicePendingIntent);
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

        try {

            val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", "000").toString()
            val notiItem = NotiItem(this, sbn, userId)

            val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
            val drawerNoti = notiItem.makeDrawerNoti()
            GlobalScope.launch {
                if (notiItem.isOnGoing()!!)
                    currentDrawerDao.deleteByPackageKey(drawerNoti.packageName, drawerNoti.groupKey)
                currentDrawerDao.insert(drawerNoti)
                Log.d(TAG, "insert drawerNoti")
                notiItem.logProperty()
            }

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
        val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()
        val notiItem = NotiItem(this, sbn, userId)

        val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
        val drawerNoti = notiItem.makeDrawerNoti()
        GlobalScope.launch {
            currentDrawerDao.deleteByPackageKey(drawerNoti.packageName, drawerNoti.groupKey)
            Log.d(TAG, "remove drawerNoti")
            notiItem.logProperty()
        }
    }
}