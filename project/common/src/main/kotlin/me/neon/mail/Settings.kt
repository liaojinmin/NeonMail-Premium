package me.neon.mail

import com.google.gson.GsonBuilder
import me.neon.mail.utils.JsonParser
import me.neon.mail.utils.asyncRunner
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.adaptCommandSender
import taboolib.expansion.geek.ExpIryBuilder
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.module.configuration.util.getMap
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * NeonMail
 * me.neon.mail.api
 *
 * @author 老廖
 * @since 2024/3/5 6:04
 */

object Settings {

    @Config(value = "settings.yml")
    lateinit var setting: SecuredFile
        private set

    var typeTranslate: MutableMap<String, String> = mutableMapOf()
        private set

    var smtpTranslate: MutableMap<String, String> = mutableMapOf()
        private set

    var inputCheck: MutableList<Regex> = mutableListOf()
        private set

    var useSmtp: Boolean = false
        private set

    var deBug: Boolean = false
        private set

    var expiryTimer: String = "2d"
        private set

    var bundle: Boolean = false
        private set

    var mailDisMiss: String = "§7剩余 §6{0} §7项未显示..."
        private set

    var mailDisAppend: String = "{0} §7* §f{1} ;"
        private set

    var activate: Boolean = false
        private set

    fun debug(text: String) {
        if (deBug) {
            Bukkit.getConsoleSender()
                .sendMessage("§8[§bNeon§9Mail§8-§ePremium§8][§cDEBUG§8] §f$text")
        }
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

    fun activate(): Boolean {
        val obj = JsonParser.parseString(
            URL("http://api.neonstudio.cn/api/verify/active?token=${setting.getString("verifyId") ?: ""}&plugin=NeonMail").readText()
        ).asJsonObject
        return if (!obj.get("data").asBoolean) {
            Bukkit.getConsoleSender().sendMessage("""§8[§bNeon§9Mail§8-§ePremium§8][§6验证§8]
                |
                |       §c当前登录 IP 过多...
                |       
                """.trimMargin())
            false
        } else {
            activate = true
            true
        }
    }

    fun Player.sendLang(node: String, vararg vars: Any) {
        if (!activate) {
            sendMessage("§8[§bNeon§9Mail§8-§ePremium§8][§cDEBUG§8] §8此插件未经正版验证，请勿用作生产服，可能存在致命问题...")
        }
        adaptCommandSender(this).sendLang(node, *vars)
    }

    fun getExpiryTimer(add: Long = 0L): Long {
       // println("解析时间 : ${ExpIryBuilder.parseStringTimerToLong(expiryTimer) }")
        return (ExpIryBuilder.parseStringTimerToLong(expiryTimer) * 1000) + add
    }

    @Awake(LifeCycle.LOAD)
    private fun logo() {
        // Metrics(16437, pluginVersion, Platform.BUKKIT)
        Bukkit.getConsoleSender().sendMessage("")
        Bukkit.getConsoleSender().sendMessage("正在加载 §3§lNeonMail§8-§9Premium  §f...  §8" + Bukkit.getVersion())
        Bukkit.getConsoleSender().sendMessage("")
    }

    @Awake(LifeCycle.LOAD)
    fun loaderSettings() {
        deBug = setting.getBoolean("debug")
        useSmtp = setting.getBoolean("smtp.use")
        expiryTimer = setting.getString("expiryTimer") ?: "2d"
        bundle = setting.getBoolean("useBundle")
        mailDisMiss = setting.getString("mailDisMiss") ?: "§7剩余 §6{0} §7项未显示..."
        mailDisAppend = setting.getString("mailDisAppend") ?: "§f{0} §7* §f{1} ;"

        typeTranslate.putAll(setting.getMap("typeTranslate"))
        setting.getMap<String, String>("smtp.map").forEach { (t, u) ->
            smtpTranslate[t.replace("_", ".")] = u
        }
        inputCheck.addAll(setting.getStringList("inputCheck.local").map { Regex(it)})
        initCloud(setting.getStringList("inputCheck.cloud"))
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