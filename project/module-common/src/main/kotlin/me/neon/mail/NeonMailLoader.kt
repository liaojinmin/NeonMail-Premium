package me.neon.mail

import com.google.gson.GsonBuilder
import me.neon.mail.common.IMailNormalImpl
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.common.IMailEmptyImpl
import me.neon.mail.service.channel.RedisConfig
import me.neon.mail.service.sql.ConfigSql
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.submitAsync
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration.Companion.getObject
import taboolib.module.configuration.util.getMap
import taboolib.module.metrics.Metrics
import taboolib.module.nms.MinecraftVersion
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 21:43
 */

@PlatformSide([Platform.BUKKIT])
object NeonMailLoader {

    private val expiryRegex: Regex = Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?")
    var typeTranslate: MutableMap<String, String> = mutableMapOf()
        private set
    var inputCheck: MutableList<Regex> = mutableListOf()
        private set

    internal val clusterId: String = Bukkit.getPort().toString()

    @Config(value = "settings.yml", autoReload = true)
    lateinit var config: ConfigFile
        private set
    lateinit var redisConfig: RedisConfig
        private set
    lateinit var sqlConfig: ConfigSql
        private set
    @ConfigNode("debug", bind = "settings.yml")
    var deBug: Boolean = false
        private set
    @ConfigNode("expiryTimer", bind = "settings.yml")
    var expiryTimer: String = "2d"
        private set
    @ConfigNode("useBundle", bind = "settings.yml")
    var bundle: Boolean = false
        private set
    @ConfigNode("mailDisMiss", bind = "settings.yml")
    var mailDisMiss: String = "§7剩余 §6{0} §7项未显示..."
        private set
    @ConfigNode("mailDisAppend", bind = "settings.yml")
    var mailDisAppend: String = "{0} §7* §f{1} ;"
        private set

    @Awake(LifeCycle.LOAD)
    private fun loader() {
        Metrics(16437, pluginVersion, Platform.BUKKIT)
        console().sendMessage("")
        console().sendMessage("正在加载 §3§lNeonMail§8-§9Premium  §f...  §8" + Bukkit.getVersion())
        console().sendMessage("")
        config.onReload(::reload)
        reload()
    }
    fun reload() {
        redisConfig = config.getObject("Redis", false)
        sqlConfig = config.getObject("data_storage", false)
        typeTranslate.putAll(config.getMap("typeTranslate"))
        inputCheck = mutableListOf()
        inputCheck.addAll(config.getStringList("inputCheck.local").map { Regex(it)})
        initCloud(config.getStringList("inputCheck.cloud"))
    }

    @Awake(LifeCycle.ENABLE)
    private fun register() {
        IMailNormalImpl(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()
        IMailEmptyImpl(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()
    }

    fun debug(text: String) {
        if (deBug) console().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8][§cDEBUG§8] §f$text")
    }

    fun say(msg: String) {
        console().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8] §f${msg.replace("&", "§")}")
    }

    fun checkInput(text: String): Boolean {
        return inputCheck.find { it.matches(text) } != null
    }

    fun getUseBundle(): Boolean {
        return if (MinecraftVersion.major >= 9) {
            bundle
        } else false
    }
    fun getExpiryTimer(add: Long = 0L): Long {
        return parseStringTimerToLong(expiryTimer) + add
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
        return timer * 1000
    }

    private fun initCloud(cloudUrl: List<String>) {
        submitAsync {
            val gsonBuilder = GsonBuilder()
            var connection: HttpURLConnection? = null
            try {
                cloudUrl.forEach {
                    connection = URL(it).openConnection() as HttpURLConnection
                    connection?.let { conn ->
                        conn.connectTimeout = 5000
                        BufferedReader(InputStreamReader(conn.inputStream)).use { bf ->
                            val data = gsonBuilder.create().fromJson(bf.readText(), Shield::class.java)
                            data.words.forEach { c ->
                                inputCheck.add(Regex(c))
                            }
                        }
                        conn.disconnect()
                    }
                }
            } catch (ignored: Throwable) {
            } finally {
                connection?.disconnect()
            }
        }
    }
    private data class Shield(
        val words: List<String> = mutableListOf()
    )

}