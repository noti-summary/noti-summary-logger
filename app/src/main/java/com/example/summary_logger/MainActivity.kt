package com.example.summary_logger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.summary_logger.jetpack_compose.ShowQuestionnaireURL
import com.example.summary_logger.service.ContextListenerService
import com.example.summary_logger.ui.theme.SummaryloggerTheme
import com.example.summary_logger.jetpack_compose.UserIdAlertDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SummaryloggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    UserIdAlertDialog(this)

                    ShowQuestionnaireURL(this, this)
                }
            }
        }

        val contextListenerServiceIntent = Intent(this@MainActivity, ContextListenerService::class.java)
        startService(contextListenerServiceIntent)
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