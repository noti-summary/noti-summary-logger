package com.example.summary_logger.service

import android.Manifest
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.reflect.full.memberProperties


class ContextListenerService : Service() {

    private lateinit var audioManager: AudioManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var locationManager: LocationManager
    private lateinit var powerManager: PowerManager
    private lateinit var sensorManager: SensorManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var usageStatsManager: UsageStatsManager

    private val pullInterval: Long = 1000

    override fun onCreate() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        println("CLS onCreate!")
    }

    companion object {
        public var latestContext: Contexts = Contexts()
    }

    class Contexts {

        var ringerMode: String = "Unknown"
            set(value) {
                if (field != value)
                    Log.d("Context", "Ringer Mode Change")
                field = value
            }

        var batteryLevel: Int = -1
        var batteryCharging: Boolean = false
            set(value) {
                if (field != value)
                    Log.d("Context", "Battery Charge Status Change")
                field = value
            }

        var isDeviceIdle: Boolean = false
            set(value) {
                if (field != value)
                    Log.d("Context", "Idle Status Change")
                field = value
            }

        var isInteractive: Boolean = false
            set(value) {
                if (field != value)
                    Log.d("Context", "Interactive Status Change")
                field = value
            }
        var isPowerSave: Boolean = false
            set(value) {
                if (field != value)
                    Log.d("Context", "Power Saving Status Change")
                field = value
            }

        var callState: String = "Idle"
            set(value) {
                if (field != value)
                    Log.d("Context", "Call State Change")
                field = value
            }

        var usageStats: String = "Unknown"
        var light: Float = 0F

        var longitude: Double = 0.0
        var latitude: Double = 0.0

        var network: String = "Unknown"
            set(value) {
                if (field != value)
                    Log.d("Context", "Network Type Change")
                field = value
            }

        fun log(): String {

            val specs: MutableList<String> = arrayListOf()
            for (property in Contexts::class.memberProperties) {
                val field: String = property.name
                val value: String = property.get(this).toString()
                if (field == "usageStats")
                    specs.add("$field: $value;")
            }

            return buildString {
                for (spec in specs) {
                    append(spec)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun fetchContext() {

        latestContext.ringerMode = when(audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            else -> "ERROR"
        }

        latestContext.batteryLevel = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY)
        latestContext.batteryCharging = batteryManager.isCharging

        latestContext.isDeviceIdle = powerManager.isDeviceIdleMode
        latestContext.isInteractive = powerManager.isInteractive
        latestContext.isPowerSave = powerManager.isPowerSaveMode

        val recentApp = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
            System.currentTimeMillis() - pullInterval, System.currentTimeMillis())
        latestContext.usageStats = buildString {
            for (u in recentApp) {
                if (u.lastTimeUsed == 0L)
                    continue
                val packageName = u.packageName
                val lastTimeUsed = u.lastTimeUsed // TODO: lastTimeForegroundServiceUsed
                append("$packageName: $lastTimeUsed;")
            }
        }

        var gpsLoc: Location? = null; var netLoc: Location? = null; val loc: Location
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            latestContext.longitude = -1.0; latestContext.latitude = -1.0
        } else {
            if (gpsEnabled)
                gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
            if (netEnabled)
                netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
            loc = if (gpsEnabled && netEnabled)
                if (gpsLoc!!.accuracy > netLoc!!.accuracy) gpsLoc else netLoc
            else if (gpsEnabled)
                gpsLoc!!
            else
                netLoc!!
            latestContext.longitude = loc.longitude; latestContext.latitude = loc.latitude
        }
        Log.d("Context", latestContext.log())

        val network = connectivityManager.activeNetwork
        val activeNetwork: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)

        latestContext.network =  if (activeNetwork == null || network == null) {
            "Disconnected"
        } else if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            "Cellular"
        } else if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            "Wi-Fi"
        } else if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
            "Ethernet"
        } else if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
            "VPN"
        } else
            "Unknown"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timer().scheduleAtFixedRate(timerTask {
            fetchContext()
        }, 0, pullInterval)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        latestContext.callState = when(state) {
                            TelephonyManager.CALL_STATE_IDLE -> "Idle"
                            TelephonyManager.CALL_STATE_OFFHOOK -> "Off-hook"
                            TelephonyManager.CALL_STATE_RINGING -> "Ringing"
                            else -> "ERROR"
                        }
                    }
                })
        }

        val sensorListener = object:SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                if(event != null) {
                    latestContext.light = event.values[0]
                }
            }
        }

        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("Context", "CLS onDestroy")
    }
}