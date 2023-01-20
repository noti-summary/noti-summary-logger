package com.example.summary_logger.service

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
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
import com.example.summary_logger.database.room.ActiveContextDatabase
import com.example.summary_logger.database.room.PeriodicContextDatabase
import com.example.summary_logger.model.ActiveContext
import com.example.summary_logger.model.PeriodicContext
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.reflect.full.memberProperties
import com.example.summary_logger.util.TAG
import com.example.summary_logger.util.upload
import kotlin.collections.HashMap

class ContextListenerService : Service() {

    private lateinit var audioManager: AudioManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var locationManager: LocationManager
    private lateinit var powerManager: PowerManager
    private lateinit var sensorManager: SensorManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var usageStatsManager: UsageStatsManager

    private val pullActiveInterval: Long = 1000
    private val pullPeriodicInterval: Long = 60000

    companion object {
        var latestPeriodicContext: PeriodicContext = PeriodicContext()
        var latestActiveContext: ActiveContext = ActiveContext()
    }

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
        Log.d(TAG, "CLS onCreate!")
    }

    // Public for potential external usage
    public fun log(activeContext: ActiveContext, periodicContext: PeriodicContext) {

        val activeSpecs: MutableList<String> = arrayListOf()
        for (property in ActiveContext::class.memberProperties) {
            activeSpecs.add("${property.name}: ${property.get(activeContext).toString()};")
        }

        val periodicSpecs: MutableList<String> = arrayListOf()
        for (property in PeriodicContext::class.memberProperties)
            periodicSpecs.add("${property.name}: ${property.get(periodicContext).toString()};")

        val logString = buildString {
            append("ActiveContext\n")
            for (spec in activeSpecs)
                append("$spec\n")
            append("\n")
            append("PeriodicContext\n")
            for (spec in periodicSpecs)
                append("$spec\n")
        }
        Log.d(TAG, logString)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun updateActiveContext() {

        var contextUpdated = false

        latestActiveContext.time = System.currentTimeMillis()

        val ringerMode = when(audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            else -> "ERROR"
        }
        if (latestActiveContext.ringerMode != ringerMode) {
            Log.d(TAG, "Ringer Mode Changed")
            contextUpdated = true
        }
        latestActiveContext.ringerMode = ringerMode

        if (latestActiveContext.batteryCharging != batteryManager.isCharging) {
            Log.d(TAG, "Battery Charge State Changed")
            contextUpdated = true
        }
        latestActiveContext.batteryCharging = batteryManager.isCharging

        if (latestActiveContext.isDeviceIdle != powerManager.isDeviceIdleMode) {
            Log.d(TAG, "Device Idle State Changed")
            // Device Idleness currently optional (too user/device dependent)
            // contextUpdated = true;
        }
        latestActiveContext.isDeviceIdle = powerManager.isDeviceIdleMode

        if (latestActiveContext.isInteractive != powerManager.isInteractive) {
            Log.d(TAG, "Device Interactive State Changed")
            contextUpdated = true
        }
        latestActiveContext.isInteractive = powerManager.isInteractive

        if (latestActiveContext.isPowerSave != powerManager.isPowerSaveMode) {
            Log.d(TAG, "Power Save Mode Changed")
            // Power Save Mode currently optional (too vague in reasoning)
            // contextUpdated = true;
        }
        latestActiveContext.isPowerSave = powerManager.isPowerSaveMode

        val recentApp = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
            latestActiveContext.time - pullActiveInterval, latestActiveContext.time)
        val usageStats = buildString {
            for (u in recentApp) {
                // Currently set to 30 minutes
                if (u.lastTimeUsed == 0L || latestActiveContext.time - u.lastTimeUsed > 1800000)
                    continue
                val packageName = u.packageName
                val lastTimeUsed = u.lastTimeUsed // TODO: lastTimeForegroundServiceUsed
                append("$packageName: $lastTimeUsed;")
            }
        }
        if (latestActiveContext.usageStats != usageStats) {
            Log.d(TAG, "Device Usage Changed")
            contextUpdated = true
        }
        latestActiveContext.usageStats = usageStats

        if (contextUpdated)
            insertActiveContext()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun updatePeriodicContext() {

        latestPeriodicContext.time = System.currentTimeMillis()

        latestPeriodicContext.batteryLevel = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY)

        var gpsLoc: Location? = null; var netLoc: Location? = null; val loc: Location
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            latestPeriodicContext.longitude = -1.0; latestPeriodicContext.latitude = -1.0
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
            latestPeriodicContext.longitude = loc.longitude; latestPeriodicContext.latitude = loc.latitude
        }

        val network = connectivityManager.activeNetwork
        val activeNetwork: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)

        latestPeriodicContext.network =  if (activeNetwork == null || network == null) {
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

        insertPeriodicContext()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Public for potential external usage
    public fun insertActiveContext() {
        val activeDao = ActiveContextDatabase.getInstance(applicationContext).activeContextDao()
        activeDao.insert(latestActiveContext)
        // Log.d(TAG, "ActiveContext Stored")
    }

    // Public for potential external usage
    public fun insertPeriodicContext() {
        val periodicDao = PeriodicContextDatabase.getInstance(applicationContext).periodicContextDao()
        periodicDao.insert(latestPeriodicContext)
        // Log.d(TAG, "PeriodicContext Stored")
    }

    // Room size checking
    private fun logContextRoom(interval: Long) {
        Timer().scheduleAtFixedRate(timerTask {
            val activeDao = ActiveContextDatabase.getInstance(applicationContext).activeContextDao()
            val periodicDao = PeriodicContextDatabase.getInstance(applicationContext).periodicContextDao()
            val currentTime = System.currentTimeMillis()

            val activeContexts = activeDao.queryByTimeInterval(currentTime - interval, currentTime)
            val periodicContexts = periodicDao.queryByTimeInterval(currentTime - interval, currentTime)

            Log.d(TAG, "Context in recent ${interval / 1000} seconds: Active ${activeContexts.size} / Periodic ${periodicContexts.size}")
            Log.d(TAG, "Context stored overall: Active ${activeDao.getAll().size} / Periodic ${periodicDao.getAll().size}")
        }, 0, interval)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timer().scheduleAtFixedRate(timerTask {
            updateActiveContext()
        }, 0, pullActiveInterval)

        Timer().scheduleAtFixedRate(timerTask {
            updatePeriodicContext()
        }, 0, pullPeriodicInterval)

        Timer().scheduleAtFixedRate(timerTask {
            // log(latestActiveContext, latestPeriodicContext)
        }, 0, 10000)

        // logContextRoom(30000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            /*
            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        val callState = when(state) {
                            TelephonyManager.CALL_STATE_IDLE -> "Idle"
                            TelephonyManager.CALL_STATE_OFFHOOK -> "Off-hook"
                            TelephonyManager.CALL_STATE_RINGING -> "Ringing"
                            else -> "ERROR"
                        }
                        if (latestActiveContext.callState != callState)
                            Log.d(TAG, "Call State Changed")
                        latestActiveContext.callState = callState
                        updateActiveContext()
                    }
                })
             */
        }

        val sensorListener = object:SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                if(event != null)
                    latestPeriodicContext.light = event.values[0]
            }
        }

        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        return START_STICKY
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, ContextListenerService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + 10000, restartServicePendingIntent);
        Log.d(TAG, "onDestroy")
    }
}