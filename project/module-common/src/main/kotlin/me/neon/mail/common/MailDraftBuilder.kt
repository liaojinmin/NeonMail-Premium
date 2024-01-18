package me.neon.mail.common

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.*
import me.neon.mail.service.ServiceManager.deleteToSql
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * NeonMail-Premium
 * me.neon.mail.common
 *
 * @author 老廖
 * @since 2024/1/6 15:15
 */
class MailDraftBuilder(
    val sender: UUID,
    val type: String,
    val uuid: UUID = UUID.randomUUID(),
    var title: String = "not title",
    val context: MutableList<String> = mutableListOf("not context"),
    private var globalModel: Boolean = false,
    // 玩家名称 、 附件
    private val targets: ConcurrentHashMap<UUID, IMailDataType> = ConcurrentHashMap()
) {


    fun checkGlobalModel(): Boolean {
        return globalModel
    }

    fun addTarget(uuid: UUID, dataType: IMailDataType) {
        // 如果允许管理员发重复邮件，则取消这个判断，
        // 不然切换了全局模式，就不能再添加其它玩家
        if (!globalModel) {
            targets[uuid] = dataType
        }
    }
    fun getTargets(): ConcurrentHashMap<UUID, IMailDataType> {
        return targets
    }

    fun changeGlobalModel(): IMailDataType  {
        globalModel = true
        val a = getMailSource<IMailAbstract<*>>().createData()
        targets.clear()
        targets[IMailRegister.console] = a
        return a
    }

    fun senderMail() {
        // 获取邮件注册类
        IMailRegister.getRegisterMail(type)?.let {
            // 清理草稿箱
            deleteToSql { back ->
                if (back == 1) {
                    val text = this.context.joinToString(";")
                    val timer = System.currentTimeMillis()
                    targets.forEach { (key, value) ->
                        // 修正附件类型
                        // 如果无附件信息，并且不是默认的空类，则修正
                        val new: IMail<*> = if (!value.hasAppendix() && value !is DataTypeEmpty) {
                            IMailEmptyImpl(UUID.randomUUID(), sender, key)
                        } else {
                            it.cloneMail(UUID.randomUUID(), sender, key, value)
                        }
                        // 设置基本信息
                        new.title = title
                        new.context = text
                        new.senderTimer = timer
                        if (value is DataTypeEmpty) {
                            new.state = IMailState.Text
                        } else {
                            new.state = IMailState.NotObtained
                        }
                        if (globalModel) {
                            new.sendGlobalMail()
                        } else if (key != IMailRegister.console) {
                            new.sendMail()
                        } else {
                            error("系统异常，不是全局管理员模式，但发送目标是控制台...")
                        }
                    }
                    NeonMailLoader.debug("MailDraftBuilder.senderMail() -> 已同步删除数据库中的草稿箱")
                } else {
                    NeonMailLoader.debug("MailDraftBuilder.senderMail() -> 删除草稿箱失败 $uuid")
                }
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
        fun serialize(builder: ConcurrentHashMap<UUID, IMailDataType>): ByteArray {
        //    NeonMailLoader.debug("MailEditeBuilder -> serialize")
         //   targets.forEach { (t, u) ->
          //      NeonMailLoader.debug("    key: $t")
          //      NeonMailLoader.debug("    value: $u")
         //   }
            return IMailRegister.getGsonBuilder().toJson(builder).toByteArray()
        }

        fun deserialize(data: ByteArray, type: Class<out IMailDataType>): ConcurrentHashMap<UUID, IMailDataType> {
         //   NeonMailLoader.debug("MailEditeBuilder -> deserialize")
         //   NeonMailLoader.debug("    data: ${String(data, Charsets.UTF_8)}")
            val gson = IMailRegister.getGsonBuilder()
            val map = ConcurrentHashMap<UUID, IMailDataType>()
            val obj = JsonParser.parseString(String(data, Charsets.UTF_8)) as JsonObject
            obj.entrySet().forEach {
                map[UUID.fromString(it.key)] = gson.fromJson(it.value, type)
            }
            return map
        }
    }
}