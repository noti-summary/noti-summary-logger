package com.example.summary_logger.jetpack_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.lifecycle.LifecycleOwner
import com.example.summary_logger.R
import com.example.summary_logger.database.firestore.FirestoreCollection
import com.example.summary_logger.database.firestore.collectionStateOf
import com.example.summary_logger.model.Summary
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@Composable
fun ShowQuestionnaireURL(lifecycleOwner: LifecycleOwner){
    val query = Firebase.firestore.collection("summary").orderBy("summary_id", Query.Direction.DESCENDING)
    val (result) = remember { collectionStateOf(query, lifecycleOwner) }

    if (result is FirestoreCollection.Snapshot) {
//        Log.i("firestore", result.list.get(0).toObject<Summary>().toString())
        QuestionnaireURL(result.list)
    }
}


@Composable
fun QuestionnaireURL(urls: List<DocumentSnapshot>){

    val uriHandler = LocalUriHandler.current

    LazyColumn(modifier = Modifier.fillMaxHeight()) {

        items(urls){ documentSnapshot  ->

            val summaryId : String = documentSnapshot.toObject<Summary>()?.summary_id ?: ""
            val userId : String = documentSnapshot.toObject<Summary>()?.user_id ?: ""
            val item  = "https://noti-summary.vercel.app/$userId/$summaryId"

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