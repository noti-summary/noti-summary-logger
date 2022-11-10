package com.example.summary_logger.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.Toast
import java.util.*

class ContextListenerService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var contContextLogger: ContinuousContextLogger

    class ContinuousContextLogger(sensorManager: SensorManager) : TimerTask() {
        private val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        override fun run() {
            println(deviceSensors)
        }
    }

    override fun onCreate() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        contContextLogger = ContinuousContextLogger(sensorManager)
        println("CLS onCreate!")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timer().schedule(contContextLogger, Date(), 1000)
        return START_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "CLS onDestroy!", Toast.LENGTH_LONG).show()
    }
}