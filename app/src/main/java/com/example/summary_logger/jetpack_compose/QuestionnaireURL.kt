package com.example.summary_logger.jetpack_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.summary_logger.R

@Composable
fun QuestionnaireURL(urls: List<String>){

    val uriHandler = LocalUriHandler.current

    LazyColumn(modifier = Modifier.fillMaxHeight()) {

        items(urls){ item ->

            val annotatedLinkString: AnnotatedString = buildAnnotatedString {
                val startIndex = 0
                val endIndex = item.length
                append(item)
                addStyle(
                    style = SpanStyle(
                        color = Color(0xff64B5F6),
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline
                    ), start = startIndex, end = endIndex
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = item,
                    start = startIndex,
                    end = endIndex
                )
            }

            Card(
                modifier = Modifier.padding(3.dp).fillMaxWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.url),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).padding(12.dp),
                        contentScale = ContentScale.Fit,
                    )

                    ClickableText(
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp).fillMaxWidth(),
                        text = annotatedLinkString,
                        onClick = {
                            annotatedLinkString
                                .getStringAnnotations("URL", it, it)
                                .firstOrNull()?.let { stringAnnotation ->
                                    uriHandler.openUri(stringAnnotation.item)
                                }
                        }
                    )
                }
            }
        }
    }
}