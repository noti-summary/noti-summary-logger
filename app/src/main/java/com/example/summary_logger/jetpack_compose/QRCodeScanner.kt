package com.example.summary_logger.jetpack_compose

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.summary_logger.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

@Composable
fun QRCodeScanner(context: Context){

    val barcodeLauncher = rememberLauncherForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)
            val currentUserId = sharedPref.getString("user_id", "000").toString()
//            Toast.makeText(context, "user id: $currentUserId, Scanned: ${result.contents}", Toast.LENGTH_LONG).show()

            loginToWeb(currentUserId, result.contents, context)
        }
    }

    Column(
        modifier = Modifier.padding(25.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ){
        FloatingActionButton(
            onClick = {
                val options = ScanOptions()
                options.setPrompt("—— QR Codes ——")
                options.setBeepEnabled(false)
                options.setOrientationLocked(true)
                barcodeLauncher.launch(options)
            },
        ) {
            Icon(painter = painterResource(id = R.drawable.scan), "", modifier = Modifier.size(50.dp).padding(3.dp))
        }
    }
}


fun loginToWeb(currentUserId: String, accessToken: String, context: Context){
    val dotenv = dotenv()
    val SERVER_IP = dotenv["SERVER"] ?: "http://localhost:5000"

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("$SERVER_IP/login/$currentUserId")
        .post(accessToken.toRequestBody())
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.d("http_request", e.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            val res = response.body?.string()
            if(res == "true"){
                Toast.makeText(context, "登入成功", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(context, "登入失敗 請稍後再試", Toast.LENGTH_LONG).show()
            }
        }
    })
}

