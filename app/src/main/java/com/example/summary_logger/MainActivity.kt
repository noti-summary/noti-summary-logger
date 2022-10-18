package com.example.summary_logger

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.summary_logger.database.UserDao
import com.example.summary_logger.database.UserDatabase
import com.example.summary_logger.model.User
import com.example.summary_logger.ui.theme.SummaryloggerTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SummaryloggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }

        val userDao: UserDao = UserDatabase.getInstance(this).userDao()

        GlobalScope.launch {

            // wait for database pre-populate
            Log.i("users", userDao.getAllUser().toString())
            delay(500)

            var userID : String = userDao.getCurrentUserId()
            if(userID == "000"){
                withContext(Dispatchers.Main) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setMessage("Please enter your user id")

                    var input = EditText(this@MainActivity)
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    builder.setView(input)

                    builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        userID = input.text.toString()
                        GlobalScope.launch {
                            userDao.setUser(User(user_id=userID))
                        }
                        Toast.makeText(this@MainActivity, "user_id = $userID", Toast.LENGTH_LONG).show()
                    })
                    builder.show()
                }
            }
        }

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