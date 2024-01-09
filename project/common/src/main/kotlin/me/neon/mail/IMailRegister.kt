package me.neon.mail

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.neon.mail.api.IMail
import me.neon.mail.api.IMailDataType
import me.neon.mail.service.sql.Exclude
import taboolib.common.platform.function.info
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
        .setExclusionStrategies(Exclude())
        .setPrettyPrinting()



    fun getRegisterMail(type: String): IMail<*>? {
        return mailCache[type]
    }

    fun getRegisterKeys(): List<String> {
        return mailCache.keys().toList()
    }

    fun deserializeMailData(data: ByteArray, type: Class<out IMailDataType>): IMailDataType {
        return gsonBuilder.create()
            .fromJson(String(data, Charsets.UTF_8), type)
    }

    fun serializeMailData(type: IMailDataType): ByteArray {
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
        mailCache[mail.mailType] = mail
        info("注册的邮件类 -> ${mail.getMailClassType()}")
        info("  索引种类名称 -> ${mail.mailType}")
        info("  数据类 -> ${mail.getDataType()}")
        gsonBuilder.registerTypeAdapter(mail.getMailClassType(), mail)
        gsonBuilder.registerTypeAdapter(mail.getDataType(), mail)
    }

    fun unregister(mail: IMail<*>) {
        mailCache.remove(mail.mailType)
    }



}