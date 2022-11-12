package com.example.summary_logger.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.summary_logger.MainActivity

class StartOnBoot : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            val startIntent = Intent(context, MainActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(startIntent)
        }
    }
}