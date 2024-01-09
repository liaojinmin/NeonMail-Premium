package me.neon.mail.common

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.neon.mail.IMailRegister
import me.neon.mail.api.DataTypeEmpty
import me.neon.mail.api.IMail
import me.neon.mail.api.IMailDataType
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.info
import taboolib.module.lang.sendLang
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 15:15
 */
class MailDraftBuilder(
    val sender: UUID,
    val type: String,
    val uuid: UUID = UUID.randomUUID(),
    var title: String = "邮件标题",
    val context: MutableList<String> = mutableListOf(),
    // 玩家名称 、 附件
    val targets: MutableMap<UUID, IMailDataType> = mutableMapOf()
) {

    fun senderMail() {
        // 获取邮件注册类
        IMailRegister.getRegisterMail(type)?.let {
            val text = this.context.joinToString(";")
            val timer = System.currentTimeMillis()
            targets.forEach { (key, value) ->
                // 构建新的邮件信息
                val new = it.cloneMail(UUID.randomUUID(),sender, key, value)
                // 设置基本信息
                new.title = title
                new.context = text
                new.senderTimer = timer
                if (value is DataTypeEmpty) {
                    new.state = IMail.IMailState.Text
                } else {
                    new.state = IMail.IMailState.NotObtained
                }
                new.sendMail()
            }
        }
    }


    inline fun <reified T: IMail<*>> getMailSource(): T {
        return (IMailRegister.getRegisterMail(type) as? T) ?: error("找不到 $type 的邮件模型，可能未注册...")
    }


    fun isAllowSender(): Boolean {
        if (targets.isEmpty()) {
            getProxyPlayer(sender)?.sendLang("玩家-发送草稿邮件-拒绝")
            return false
        }
        return true
    }
    fun isAllowDeletion(): Boolean {
        for (pr in targets.values) {
            if (pr.hasAppendix()) {
                getProxyPlayer(sender)?.sendLang("玩家-删除草稿邮件-拒绝")
                return false
            }
        }
        return true
    }

    companion object {
        fun serialize(targets: MutableMap<UUID, IMailDataType>): ByteArray {
            info("MailEditeBuilder 序列化 start")
            targets.forEach { (t, u) ->
                info("    KEY: $t VALUE: $u")
            }
            info("MailEditeBuilder 序列化 end")
            return IMailRegister.getGsonBuilder().toJson(targets).toByteArray()
        }

        fun deserialize(data: ByteArray, type: Class<out IMailDataType>): MutableMap<UUID, IMailDataType> {
            info("MailEditeBuilder 反序列化 start")
            info("    data: ${String(data, Charsets.UTF_8)}")
            val gson = IMailRegister.getGsonBuilder()
            val map = mutableMapOf<UUID, IMailDataType>()
            val obj = JsonParser.parseString(String(data, Charsets.UTF_8)) as JsonObject
            obj.entrySet().forEach {
                map[UUID.fromString(it.key)] = gson.fromJson(it.value, type)
            }
            map.forEach { (t, u) ->
                info("    KEY: $t VALUE: $u")
            }
            info("MailEditeBuilder 反序列化 end")
            return map
        }
    }
}