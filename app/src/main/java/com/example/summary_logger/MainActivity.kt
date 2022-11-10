package com.example.summary_logger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.summary_logger.service.NotiListenerService
import com.example.summary_logger.ui.theme.SummaryloggerTheme
import com.example.summary_logger.util.channelId
import com.example.summary_logger.util.pushNoti

class MainActivity : ComponentActivity() {
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNotiListenerEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)
        startService(notiListenerIntent)

        setContent {
            SummaryloggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
//                Box {
//                    NotiButton(this@MainActivity)
//                }
            }
        }

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