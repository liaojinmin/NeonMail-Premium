package me.neon.mail.api.mail

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
object IMailRegister {

    private val mailCache: ConcurrentHashMap<String, IMail<*>> = ConcurrentHashMap()

    val console: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    private var gsonBuilder: GsonBuilder = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.getAnnotation(Expose::class.java) != null
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return clazz.getAnnotation(Expose::class.java) != null
            }
        }).setPrettyPrinting()

    fun getRegisterMail(type: String): IMail<*>? {
        return mailCache[type]
    }

    fun getRegisterMails(): MutableCollection<IMail<*>> {
        return mailCache.values
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

    fun serializeMailData(mail: IMail<*>): ByteArray {
        return gsonBuilder
                .create()
                .toJson(mail.data).toByteArray(Charsets.UTF_8)
    }

    fun getGsonBuilder(): Gson {
        return gsonBuilder.create()
    }

    fun register(mail: IMail<*>) {
        mailCache[mail.data.sourceType] = mail
        Bukkit.getConsoleSender().sendMessage("""§8[§bNeon§9Mail§8-§ePremium§8][§6注册§8]
            |    §a注册的数据类 -> §7${mail.getDataClassType()}
            |    §a索引种类名称 -> §7${mail.data.sourceType}
        """.trimMargin())
        gsonBuilder.registerTypeAdapter(mail.getDataClassType(), mail)
    }

    fun unregister(mail: IMail<*>) {
        mailCache.remove(mail.data.sourceType)
    }



}