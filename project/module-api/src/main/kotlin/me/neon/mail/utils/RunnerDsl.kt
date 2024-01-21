package me.neon.mail.utils

import java.util.concurrent.CompletableFuture

/**
 * NeonMail-Premium
 * me.neon.mail.utils
 *
 * @author 老廖
 * @since 2024/1/15 0:56
 */


fun asyncRunner(complete: AsyncRunner<Unit>.() -> Unit) {
    AsyncRunner<Unit>().also(complete).executeAsync()
}
fun asyncMultiRunner(complete: MultiAsyncRunner.() -> Unit) {
    MultiAsyncRunner().also(complete).executeAllAsync()
}

fun <T> asyncRunnerWithResult(complete: AsyncRunner<T>.() -> Unit){
    AsyncRunner<T>().also(complete).executeAsync()
}



class MultiAsyncRunner {
    private val tasks = mutableListOf<AsyncRunner<*>>()

    infix fun <T> task(configure: AsyncRunner<T>.() -> Unit) {
        val task = AsyncRunner<T>().also(configure)
        tasks.add(task)
    }

    fun executeAllAsync(complete: () -> Unit = {}): CompletableFuture<Unit> {
        val allOf = CompletableFuture.allOf(*tasks.map { it.executeAsync() }.toTypedArray())
        return allOf.thenApplyAsync { complete.invoke() }
    }

    fun executeAllSync(complete: () -> Unit = {}): CompletableFuture<Unit> {
        val allOf = CompletableFuture.allOf(*tasks.map { it.executeSync() }.toTypedArray())
        return allOf.thenApply { complete.invoke() }
    }

}

class AsyncRunner<T> {
    private var onError: ((Throwable) -> Unit)? = null
    private var onComplete: ((T?, Long) -> Unit)? = null
    private var onJobBlock: (() -> T?)? = null

    private fun defaultOnError(ex: Throwable) {
        ex.printStackTrace()
    }

    fun onError(action: (Throwable) -> Unit) {
        onError = action
    }

    fun onComplete(action: (T?, Long) -> Unit) {
        onComplete = action
    }

    fun onJob(block: () -> T?) {
        onJobBlock = block
    }

    fun executeSync(): CompletableFuture<T?> {
        val startTime = System.currentTimeMillis()
        val future = CompletableFuture<T?>()
        try {
            future.complete(onJobBlock?.invoke())
        } catch (ex: Throwable) {
            onError?.invoke(ex) ?: defaultOnError(ex)
            future.complete(null)
        }
        val endTime = System.currentTimeMillis()
        future.thenAccept {
            onComplete?.invoke(it, endTime - startTime)
        }
        return future
    }

    fun executeAsync(): CompletableFuture<T> {
        val startTime = System.currentTimeMillis()
        return CompletableFuture.supplyAsync {
            try {
                onJobBlock?.invoke()
            } catch (ex: Throwable) {
                onError?.invoke(ex) ?: defaultOnError(ex)
                null
            }
        }.thenApplyAsync {
            val endTime = System.currentTimeMillis()
            onComplete?.invoke(it, endTime - startTime)
            it
        }
    }
}


