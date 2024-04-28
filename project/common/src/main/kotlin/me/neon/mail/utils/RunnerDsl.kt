
package me.neon.mail.utils

import org.bukkit.Bukkit
import taboolib.platform.util.bukkitPlugin
import java.util.concurrent.CompletableFuture

/**
 * NeonMail-Premium
 * me.neon.mail.utils
 *
 * @author 老廖
 * @since 2024/1/15 0:56
 */

fun syncRunner(complete: Runnable) {
    Bukkit.getScheduler().runTask(bukkitPlugin, complete)
}

fun syncRunner(delay: Long, complete: Runnable) {
    Bukkit.getScheduler().runTaskLater(bukkitPlugin, complete, delay)
}

fun syncRunner(delay: Long, period: Long, complete: Runnable) {
    Bukkit.getScheduler().runTaskTimer(bukkitPlugin, complete, delay, period)
}

fun asyncRunner(complete: AsyncRunner<Unit>.() -> Unit) {
    AsyncRunner<Unit>().also(complete).executeAsync()
}

fun asyncRunner(delay: Long, complete: AsyncRunner<Unit>.() -> Unit) {
    AsyncRunner<Unit>(delay).also(complete).executeAsync()
}

fun asyncRunner(delay: Long, period: Long, complete: AsyncRunner<Unit>.() -> Unit) {
    Bukkit.getScheduler().runTaskTimerAsynchronously(bukkitPlugin, AsyncRunner<Unit>().also(complete).runnable(), delay, period)
}

fun asyncMultiRunner(complete: MultiAsyncRunner.() -> Unit) {
    MultiAsyncRunner().also(complete).executeAllAsync()
}

fun <T> asyncRunnerWithResult(complete: AsyncRunner<T>.() -> Unit) {
    AsyncRunner<T>().also(complete).executeAsync()
}

fun <T> asyncRunnerResult(complete: AsyncRunner<T>.() -> Unit): CompletableFuture<T> {
    return AsyncRunner<T>().also(complete).executeAsync()
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

}

class AsyncRunner<T>(
    private var delay: Long = -1,
    // TODO 不在这实现循环任务，暂无方法取消任务
    private var period: Long = -1
) {

    private var onError: ((Throwable) -> Unit)? = null

    private var onComplete: ((T?, Long) -> Unit)? = null

    private var onJobBlock: (() -> T?)? = null

    private var periodTick = 0

    private fun defaultOnError(ex: Throwable) {
        ex.printStackTrace()
    }

    infix fun onError(action: (Throwable) -> Unit) {
        onError = action
    }

    infix fun onComplete(action: (T?, Long) -> Unit) {
        onComplete = action
    }

    infix fun onJob(block: () -> T?) {
        onJobBlock = block
    }

    fun runnable(): Runnable {
        return Runnable {
            try {
                val startTime = System.currentTimeMillis()
                onJobBlock?.invoke()
                val endTime = System.currentTimeMillis()
                onComplete?.invoke(null, endTime - startTime)
            } catch (ex: Throwable) {
                onError?.invoke(ex) ?: defaultOnError(ex)
            }
        }
    }

    fun executeAsync(): CompletableFuture<T> {
        val startTime = System.currentTimeMillis()
        return CompletableFuture.supplyAsync {
            try {
                while (delay >= 0) {
                    delay--
                    Thread.sleep(50)
                }
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


