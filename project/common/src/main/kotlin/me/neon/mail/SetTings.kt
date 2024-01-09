package me.neon.mail

import me.neon.mail.service.channel.RedisConfig
import me.neon.mail.service.sql.ConfigSql
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration.Companion.getObject
import taboolib.module.nms.MinecraftVersion

/**
 * 作者: 老廖
 * 时间: 2022/12/1
 *
 **/
object SetTings {

    private val expiryRegex: Regex by lazy { Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?") }

    @Config(value = "settings.yml", autoReload = true)
    lateinit var config: ConfigFile
        private set

    @Awake(LifeCycle.ENABLE)
    fun loader() {
        redisConfig = config.getObject("Redis", false)
        sqlConfig = config.getObject("data_storage", false)
    }

    lateinit var redisConfig: RedisConfig

    lateinit var sqlConfig: ConfigSql

    @ConfigNode("debug", bind = "settings.yml")
    private var deBug: Boolean = false

    @ConfigNode("expiryTimer", bind = "settings.yml")
    private var expiryTimer: String = "2d"

    @ConfigNode("expiryTimer", bind = "settings.yml")
    private var useBundle: Boolean = false

    @ConfigNode("mailDisMiss", bind = "settings.yml")
    var mailDisMiss: String = "§7剩余 §6{0} §7项未显示..."
        private set
    @ConfigNode("mailDisAppend", bind = "settings.yml")
    var mailDisAppend: String = "{0} §7* §f{1} ;"
        private set

    const val clusterId: String = "NeonMail"


    fun debug(text: String) {
        if(deBug) {
            console().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8][§cDEBUG§8] §f$text")
        }
    }
    fun getExpiryTimer(add: Long = 0L): Long {
        return parseStringTimerToLong(expiryTimer) + add
    }

    fun getUseBundle(): Boolean {
        return if (MinecraftVersion.major >= 9) {
            useBundle
        } else false;
    }



    private fun parseStringTimerToLong(time: String): Long {
        if (time.isEmpty() || time == "-1") return -1
        var timer: Long = 0
        expiryRegex.findAll(time).forEach {
            val data = it.groupValues[0].substringBefore(it.groupValues[1]).toLong()
            timer += when (it.groupValues[1]) {
                "d","天" -> {
                    data * 60 * 60 * 24
                }
                "h","时" -> {
                    data * 60 * 60
                }
                "m","分" -> {
                    data * 60
                }
                else -> data
            }
        }
        return timer
    }



}