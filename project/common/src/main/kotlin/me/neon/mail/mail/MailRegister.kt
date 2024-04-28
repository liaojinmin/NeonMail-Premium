package me.neon.mail.mail

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 14:38
 */
object MailRegister {

    private val mailCache: ConcurrentHashMap<String, IMail<*>> = ConcurrentHashMap()

    private val infoParseCache: ConcurrentHashMap<String, (IMail<*>) -> String> = ConcurrentHashMap()

    val console: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    private val gsonBuilder: GsonBuilder = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(Expose::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(Expose::class.java) != null
            }
        }).setPrettyPrinting()

    fun getRegisterMail(data: IMailData): IMail<*>? {
        return mailCache[data.parseDataName()]
    }

    fun getRegisterMail(type: String): IMail<*>? {
        return mailCache[type]
    }

    fun getRegisterMails(): MutableCollection<IMail<*>> {
        return mailCache.values
    }

    fun createNewMailData(type: String): IMailData {
        val so = mailCache[type] ?: error("找不到 $type 的邮件模型，可能未注册...")
        return so.data::class.java.getDeclaredConstructor().newInstance()
    }

    /**
     * 对单个附件内容解析
     * @param node 注册的节点
     * @param mail 需要解析的邮件对象
     */
    fun getAppendixInfoParse(node: String, mail: IMail<*>): String {
        return infoParseCache[node]?.invoke(mail) ?: ""
    }

    /**
     * 添加单个附件的内容解析
     * @param node 识别节点
     * @param func 解析方法
     */
    fun addAppendixInfoParse(node: String, func: (IMail<*>) -> String) {
        infoParseCache[node] = func
    }


    fun deserializeMailData(data: ByteArray, type: Class<out IMailData>): IMailData {
        return gsonBuilder.create()
            .fromJson(String(data, Charsets.UTF_8), type)
    }

    fun serializeMailData(type: IMailData): ByteArray {
        return gsonBuilder
            .create()
            .toJson(type).toByteArray(Charsets.UTF_8)
    }

    fun getGsonBuilder(): Gson {
        return gsonBuilder.create()
    }

    fun register(mail: IMail<*>) {
        mailCache[mail.parseDataName()] = mail
        Bukkit.getConsoleSender().sendMessage("""§8[§bNeon§9Mail§8-§ePremium§8][§6注册§8]
            |    §a注册数据类§8(§a邮件名称§8) §7-> §f${mail.parseDataName()}
        """.trimMargin())
        gsonBuilder.registerTypeAdapter(mail.data::class.java, mail)
    }

    fun unregister(mail: IMail<*>) {
        mailCache.remove(mail.parseDataName())
    }




}