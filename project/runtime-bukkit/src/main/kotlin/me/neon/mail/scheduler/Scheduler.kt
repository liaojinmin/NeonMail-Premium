package me.neon.mail.scheduler

import me.neon.mail.api.io.syncRunner
import me.neon.mail.template.TemplateManager
import org.apache.logging.log4j.core.util.CronExpression
import org.bukkit.Bukkit

/**
 * NeonMail
 * me.neon.mail.scheduler
 *
 * @author 老廖
 * @since 2024/2/21 17:12
 */
data class Scheduler(
    val uniqueID: String,
    val target: String,
    val template: String,
    val commands: List<String>,
    val start: Long = -1,
    val end: Long = -1,
    val period: Long = -1,
    val cron: String = ""
) {
    var timer: Long = 0

    val cronExpression by lazy {
        CronExpression(cron)
    }

    fun call() {
        TemplateManager.getTemplatePack(template)?.let {
            if (target.equals("online", true)) {
                it.sendToAllOnLinePlayer()
            } else if (target.equals("offline", true)) {
                it.sendToAllOffLinePlayer()
            }
            evalCommand()
        }
    }

    fun evalCommand() {
        if (commands.isEmpty()) return
        syncRunner {
            commands.forEach {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it)
            }
        }
    }


}
