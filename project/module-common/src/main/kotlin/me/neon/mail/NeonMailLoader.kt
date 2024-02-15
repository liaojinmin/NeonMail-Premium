package me.neon.mail

import com.google.gson.GsonBuilder
import me.neon.mail.common.MailNormalImpl
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.cmd.CmdCore
import me.neon.mail.common.MailEmptyImpl
import me.neon.mail.common.listener.BasicListener
import me.neon.mail.libs.NeonLibsLoader
import me.neon.mail.libs.utils.io.getMap
import me.neon.mail.libs.utils.io.asyncRunner
import me.neon.mail.menu.MenuLoader
import me.neon.mail.service.channel.RedisConfig
import me.neon.mail.service.sql.ConfigSql
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
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


class NeonMailLoader: JavaPlugin(), NeonLibsLoader {

    companion object {

        private val expiryRegex: Regex = Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?")
        var typeTranslate: MutableMap<String, String> = mutableMapOf()
            private set
        var smtpTranslate: MutableMap<String, String> = mutableMapOf()
            private set
        var inputCheck: MutableList<Regex> = mutableListOf()
            private set

        internal lateinit var plugin: NeonMailLoader

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


        fun loaderSettings() {
            val file = File(plugin.dataFolder, "settings.yml")
            if (!file.exists()) {
                plugin.saveResource("settings.yml", true)
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
            smtpTranslate.putAll(config.getMap("smtp.map"))
            inputCheck.addAll(config.getStringList("inputCheck.local").map { Regex(it)})
            plugin.initCloud(config.getStringList("inputCheck.cloud"))
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
            return plugin.parseStringTimerToLong(expiryTimer) + add
        }
    }

    override fun onLoad() {
        plugin = this

        // Metrics(16437, pluginVersion, Platform.BUKKIT)
        Bukkit.getConsoleSender().sendMessage("")
        Bukkit.getConsoleSender().sendMessage("正在加载 §3§lNeonMail§8-§9Premium  §f...  §8" + Bukkit.getVersion())
        Bukkit.getConsoleSender().sendMessage("")

    }

    override fun onEnable() {
        MailNormalImpl(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()
        MailEmptyImpl(IMailRegister.console, IMailRegister.console, IMailRegister.console)
            .register()

        // 初始化库
        loaderLibs(this)

        // 设置
        loaderSettings()

        // 菜单
        MenuLoader.loader()

        // 服务
        ServiceManager.initService()

        // 事件
        Bukkit.getPluginManager().registerEvents(BasicListener(), this)

        // 指令
        registerCommand(CmdCore::class.java)
    }

    override fun onDisable() {
        // 卸载库
        unLoaderLibs()

        // 服务
        ServiceManager.closeService()
    }

    override fun getPluginFile(): File {
        return this.file
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
    private data class Shield(val words: List<String> = mutableListOf())

}