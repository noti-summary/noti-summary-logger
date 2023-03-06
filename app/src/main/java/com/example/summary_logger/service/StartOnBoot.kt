package com.example.summary_logger.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.RoomDatabase
import com.example.summary_logger.MainActivity
import com.example.summary_logger.database.room.CurrentDrawerDatabase
import com.example.summary_logger.util.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StartOnBoot : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val startIntent = Intent(context, MainActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(startIntent)
        }
    }
}