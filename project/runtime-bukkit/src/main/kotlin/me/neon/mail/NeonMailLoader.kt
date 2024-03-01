package me.neon.mail

import com.google.gson.GsonBuilder
import me.neon.mail.api.io.asyncRunner
import me.neon.mail.api.io.getMap
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.common.EmptyMail
import me.neon.mail.common.NormalMail
import me.neon.mail.scheduler.SchedulerLoader
import me.neon.mail.service.channel.RedisConfig
import me.neon.mail.service.sql.ConfigSql
import me.neon.mail.template.TemplateManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.expansion.geek.ExpIryBuilder.Companion.parseStringTimerToLong
import taboolib.platform.util.bukkitPlugin
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 21:43
 */

@RuntimeDependencies(
    RuntimeDependency(
        value = "!redis.clients:jedis:4.2.2",
        test = "redis.clients.jedis.BuilderFactory",
        relocate = ["!redis.clients", "redis.clients"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!org.slf4j:slf4j-api:2.0.8",
        test = "org.slf4j.LoggerFactory",
        relocate = ["!org.slf4j", "org.slf4j"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!com.zaxxer:HikariCP:4.0.3",
        test = "com.zaxxer.hikari.HikariDataSource",
        relocate = ["!com.zaxxer.hikari", "com.zaxxer.hikari", "!org.slf4j", "org.slf4j"],
        transitive = false
    )
)
@PlatformSide(Platform.BUKKIT)
object NeonMailLoader: Plugin() {

    var typeTranslate: MutableMap<String, String> = mutableMapOf()
        private set
    var smtpTranslate: MutableMap<String, String> = mutableMapOf()
        private set
    var inputCheck: MutableList<Regex> = mutableListOf()
        private set

    private lateinit var config: YamlConfiguration

    lateinit var redisConfig: RedisConfig
    private set

    lateinit var sqlConfig: ConfigSql
    private set

    var useSmtp: Boolean = false
        private set

    private var deBug: Boolean = false

    private var expiryTimer: String = "2d"

    private var bundle: Boolean = false

    var mailDisMiss: String = "§7剩余 §6{0} §7项未显示..."
        private set

    var mailDisAppend: String = "{0} §7* §f{1} ;"
        private set


    @Awake(LifeCycle.ENABLE)
    internal fun loaderSettings() {
        val file = File(bukkitPlugin.dataFolder, "settings.yml")
        if (!file.exists()) {
            bukkitPlugin.saveResource("settings.yml", true)
        }
        config = YamlConfiguration.loadConfiguration(file)
        redisConfig = RedisConfig.loader(config.getConfigurationSection("redis") ?: throw RuntimeException())
        sqlConfig = ConfigSql.loader(config.getConfigurationSection("data_storage") ?: throw RuntimeException())

        deBug = config.getBoolean("debug")
        useSmtp = config.getBoolean("smtp.use")
        expiryTimer = config.getString("expiryTimer") ?: "2d"
        bundle = config.getBoolean("useBundle")
        mailDisMiss = config.getString("mailDisMiss") ?: "§7剩余 §6{0} §7项未显示..."
        mailDisAppend = config.getString("mailDisAppend") ?: "§f{0} §7* §f{1} ;"

        typeTranslate.putAll(config.getMap("typeTranslate"))
        config.getMap<String, String>("smtp.map").forEach { (t, u) ->
            smtpTranslate[t.replace("_", ".")] = u
        }
        inputCheck.addAll(config.getStringList("inputCheck.local").map { Regex(it)})
        initCloud(config.getStringList("inputCheck.cloud"))
    }

    fun debug(text: String) {
        if (deBug) Bukkit.getConsoleSender().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8][§cDEBUG§8] §f$text")
    }

    fun checkInput(text: String): Boolean {
        return inputCheck.find { it.matches(text) } != null
    }

    fun getUseBundle(): Boolean {
        return try {
            if (bundle) Material.valueOf("BUNDLE")
            bundle
        }catch (_: Exception) {
            false
        }
    }
    fun getExpiryTimer(add: Long = 0L): Long {
        return parseStringTimerToLong(expiryTimer) + add
    }


    override fun onLoad() {
        // Metrics(16437, pluginVersion, Platform.BUKKIT)
        Bukkit.getConsoleSender().sendMessage("")
        Bukkit.getConsoleSender().sendMessage("正在加载 §3§lNeonMail§8-§9Premium  §f...  §8" + Bukkit.getVersion())
        Bukkit.getConsoleSender().sendMessage("")

    }

    override fun onEnable() {
        NormalMail(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()
        EmptyMail(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()
    }

    private fun initCloud(cloudUrl: List<String>) {
        asyncRunner {
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

    data class Shield(val words: List<String> = mutableListOf())

}