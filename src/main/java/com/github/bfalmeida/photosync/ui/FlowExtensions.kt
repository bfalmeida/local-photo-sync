package com.github.bfalmeida.photosync.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.Flow.Subscriber

fun <T> Publisher<T>.asFlow(): Flow<T> = callbackFlow {
    val subscriber = object : Subscriber<T> {
        override fun onSubscribe(subscription: java.util.concurrent.Flow.Subscription) {
            subscription.request(Long.MAX_VALUE)
        }

        override fun onNext(item: T) {
            trySend(item)
        }

        override fun onError(throwable: Throwable) {
            close(throwable)
        }

        override fun onComplete() {
            close()
        }
    }
    this@asFlow.subscribe(subscriber)
    awaitClose {
        // In a real implementation, we'd need to cancel the subscription
    }
}
