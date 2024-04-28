package me.neon.mail.scheduler

import me.neon.mail.utils.forFile
import me.neon.mail.utils.syncRunner
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitTask
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.expansion.geek.ExpIryBuilder.Companion.parseStringTimerToLong
import taboolib.platform.util.bukkitPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.measureTimeMillis


/**
 * NeonMail
 * me.neon.mail.scheduler
 *
 * @author 老廖
 * @since 2024/2/21 17:12
 */
object SchedulerLoader {

    private var schedulerCache: CopyOnWriteArrayList<Scheduler> = CopyOnWriteArrayList()

    private val formatsTime: SimpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss") }

    private val schedulerFile by lazy {
        val dir = File(getDataFolder(), "scheduler")
        if (!dir.exists()) {
            arrayOf(
                "scheduler/def.yml",
                "scheduler/def-2.yml",
            ).forEach { bukkitPlugin.saveResource(it, true) }
        }
        dir
    }

    private var bukkitTask: BukkitTask? = null


    @Awake(LifeCycle.ENABLE)
    fun loader() {
        measureTimeMillis {
            val list = CopyOnWriteArrayList<Scheduler>()
            val timer = System.currentTimeMillis()
            schedulerFile.forFile(".yml").forEach {
                val yaml = YamlConfiguration.loadConfiguration(it)
                val uniqueID = yaml.getString("scheduler.uniqueId") ?: error("缺少唯一ID")
                if (yaml.getBoolean("scheduler.enable")) {

                    val cron = yaml.getString("scheduler.cron") ?: ""
                    val start = parseTimer(yaml.getString("scheduler.start") ?: "")
                    val end = parseTimer(yaml.getString("scheduler.end") ?: "")

                    val target = yaml.getString("scheduler.target")!!
                    val template = yaml.getString("scheduler.template")!!
                    val command = yaml.getStringList("scheduler.command")
                    if ((end != -1L && timer < end) || cron.isNotEmpty()) {
                        val period = parseStringTimerToLong(yaml.getString("scheduler.period") ?: "")
                        list.add(Scheduler(uniqueID, target, template, command, start, end, period, cron))
                    }
                }
            }
            schedulerCache = list

        }.also { info("加载 ${schedulerCache.size} 个定时任务配置... (耗时 $it ms)") }
    }


    @Awake(LifeCycle.ENABLE)
    private fun startTask() {
        bukkitTask?.cancel()
        syncRunner(20, 20) {
            val iterator = schedulerCache.listIterator()
            val timer = System.currentTimeMillis()
            val list: MutableSet<Scheduler> = mutableSetOf()
            while (iterator.hasNext()) {
                val dto = iterator.next()
                if (dto.cron.isNotEmpty()) {
                    if (dto.cronExpression.isSatisfiedBy(Date())) {
                        dto.call()
                    }
                } else if (dto.start == -1L || timer >= dto.start) {
                    if (dto.end != -1L && timer > dto.end) {
                        // 移除
                        list.add(dto)
                        continue
                    }
                    if (dto.period != -1L) {
                        if (dto.timer >= dto.period) {
                            dto.timer = 0
                            dto.call()
                        } else {
                            dto.timer++
                        }
                    } else {
                        dto.call()
                        list.add(dto)
                    }
                }
            }
            schedulerCache.removeAll(list)
        }
    }

    @Awake(LifeCycle.DISABLE)
    private fun disable() {
        bukkitTask?.cancel()
    }

    private fun parseTimer(data: String): Long {
        if (data.isEmpty()) return -1
        return formatsTime.parse(data).time
    }


}