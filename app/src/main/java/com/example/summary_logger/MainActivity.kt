package com.example.summary_logger

import android.Manifest
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.summary_logger.jetpack_compose.QRCodeScanner
import com.example.summary_logger.jetpack_compose.ShowQuestionnaireURL
import com.example.summary_logger.jetpack_compose.UserIdAlertDialog
import com.example.summary_logger.service.ContextListenerService
import com.example.summary_logger.service.NotiListenerService
import com.example.summary_logger.ui.theme.SummaryloggerTheme
import com.example.summary_logger.util.TAG
import com.example.summary_logger.util.channelId
import com.example.summary_logger.util.pushNoti
import com.example.summary_logger.util.upload
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var notificationManager: NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNotiListenerEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        if (!isUsageEnabled()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)
        startService(notiListenerIntent)

        setContent {
            SummaryloggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UserIdAlertDialog(this)

                    ShowQuestionnaireURL(this, this)

                    QRCodeScanner(this)
                }
//                Box {
//                    NotiButton(this@MainActivity)
//                }
                Box {
                    UploadButton(this@MainActivity, packageManager)
                }
            }
        }

        val contextListenerServiceIntent = Intent(this@MainActivity, ContextListenerService::class.java)
        startService(contextListenerServiceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "summary_log"
            val descriptionText = "summary_reminder"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            this.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            this.notificationManager.createNotificationChannel(channel)
        }

    }

    private fun isNotiListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotiListenerService::class.java)
        val flat: String =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return cn.flattenToString() in flat
    }

    private fun chkPermissionOps(opString: String, permission: String): Boolean {
        val appOps = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(opString, Process.myUid(), this.packageName)
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(permission) === PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun isUsageEnabled(): Boolean {
        return chkPermissionOps(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Manifest.permission.PACKAGE_USAGE_STATS
        )
    }

}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SummaryloggerTheme {
        Greeting("Android")
    }
}

@Composable
fun NotiButton(context: Context) {
    Button(onClick = {
        pushNoti("TitleTitle", "ContentContent", context)
    }) {
        Text(text = "Send the Notification")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UploadButton(context: Context, packageManager: PackageManager) {
    Button(onClick = {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60000
        upload(context, packageManager, startTime, endTime)
    }) {
        Text(text = "Upload Summary")
    }
}