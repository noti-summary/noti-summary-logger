package com.example.summary_logger.jetpack_compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.summary_logger.R


@Composable
fun NoPaddingAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                title?.let {
                    CompositionLocalProvider() {
                        val textStyle = MaterialTheme.typography.titleLarge
                        ProvideTextStyle(textStyle, it)
                    }
                }
                text?.let {
                    CompositionLocalProvider() {
                        val textStyle = MaterialTheme.typography.labelSmall
                        ProvideTextStyle(textStyle, it)
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(all = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserIdAlertDialog(context: Context) {

    val openDialog = remember { mutableStateOf(true) }
    var inputId by remember { mutableStateOf("") }
    val sharedPref = context.getSharedPreferences("user_id", Context.MODE_PRIVATE)

    if(sharedPref.getString("user_id", "000").toString() == "000") {
        if (openDialog.value) {
            NoPaddingAlertDialog(
                onDismissRequest = { },
                title = {
                    Image(
                        modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 20.dp).height(70.dp),
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "user_icon",
                    )
                },
                text = {
                    OutlinedTextField(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                        value = inputId,
                        onValueChange = { inputId = it },
                        label = { Text("User ID") },
                    )
                },
                confirmButton = {
                    TextButton(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                        onClick = {
                            if(inputId != "") {
                                with(sharedPref.edit()) {
                                    putString("user_id", inputId)
                                    apply()
                                }

                                openDialog.value = false

                                Toast.makeText(
                                    context,
                                    "user_id = ${sharedPref.getString("user_id", "000").toString()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                    { Text(text = "OK") }
                }
            )
        }
    }
}
