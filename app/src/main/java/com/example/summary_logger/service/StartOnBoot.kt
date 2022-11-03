package com.example.summary_logger.service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class StartOnBoot : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Receiver", "Received")
        val startIntent = Intent(context, ContextListenerService::class.java)
        context!!.startService(startIntent)
    }
}