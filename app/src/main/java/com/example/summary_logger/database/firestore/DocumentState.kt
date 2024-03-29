package com.example.summary_logger.database.firestore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration

/**
 * A value holder where reads to the [value] property during the execution of a [Composable]
 * function, the current [RecomposeScope] will be subscribed to changes of that value. When the
 * [value] property is changed in that Firestore Document, a recomposition of any subscribed
 * [RecomposeScope]s will be scheduled.
 *
 * @see [State]
 * @see [documentStateOf]
 */
interface DocumentState : State<FirestoreDocument>, LifecycleObserver {
    override val value: FirestoreDocument
    fun startListening()
    fun stopListening()
    operator fun component1(): FirestoreDocument
}

/**
 * Return a new [DocumentState] initialized with the passed [documentReference]
 *
 * The DocumentState class is a single value holder whose reads are observed by
 * Compose.
 *
 * @param documentReference the document to be observed
 * @param lifecycleOwner the lifecycle owner that the state should react to
 *
 * @see State
 * @see DocumentState
 */
fun documentStateOf(
    documentReference: DocumentReference,
    lifecycleOwner: LifecycleOwner? = null
) = object : DocumentState {
    private var listener: ListenerRegistration? = null
    private var snapshotState: FirestoreDocument by mutableStateOf(FirestoreDocument.Loading)

    override val value: FirestoreDocument
        get() = snapshotState

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun startListening() {
        if (listener == null) {
            listener = documentReference.addSnapshotListener { value, error ->
                value?.let { snapshot ->
                    snapshotState = FirestoreDocument.Snapshot(snapshot)
                }
                error?.let {  exception ->
                    snapshotState = FirestoreDocument.Error(exception)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun stopListening() {
        listener?.remove()
    }

    override fun component1() = value
}
