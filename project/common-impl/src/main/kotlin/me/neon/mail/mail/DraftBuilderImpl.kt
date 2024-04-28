package me.neon.mail.mail


import me.neon.mail.mail.*
import me.neon.mail.service.ServiceManager.deleteToSql
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import me.neon.mail.Settings.sendLang
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * NeonMail-Premium
 * me.neon.mail.common
 *
 * @author 老廖
 * @since 2024/1/6 15:15
 */
class DraftBuilderImpl(
    override val sender: UUID,
    override val type: String,
    override val unique: UUID = UUID.randomUUID(),
    override var title: String = "not title",
    override val context: MutableList<String> = mutableListOf("not context"),
    private var globalModel: Boolean = false,
    // 玩家名称 、 附件
    private var targets: ConcurrentHashMap<UUID, IMailData> = ConcurrentHashMap()
): IDraftBuilder {

    override fun createNewInstance(
        sender: UUID,
        type: String,
        unique: UUID,
        title: String,
        context: MutableList<String>,
        vararg arg: Any
    ): IDraftBuilder {
        return if (arg.isNotEmpty() && arg.size == 2) {
            DraftBuilderImpl(sender, type, unique, title, context).apply {
                if (arg[0] is Boolean) {
                    globalModel = arg[0] as Boolean
                }
                // 不检查类型
                if (arg[1] is Map<*, *>) {
                    (arg[1] as Map<*, *>).forEach { (any, u) ->
                        targets[any as UUID] = u as IMailData
                    }
                }
            }
        } else {
            DraftBuilderImpl(sender, type, unique, title, context)
        }
    }

    override fun checkGlobalModel(): Boolean {
        return globalModel
    }

    override fun addTarget(uuid: UUID, dataType: IMailData) {
        // 如果允许管理员发重复邮件，则取消这个判断，
        // 不然切换了全局模式，就不能再添加其它玩家
        if (!globalModel) {
            targets[uuid] = dataType
        }
    }

    override fun getTargets(): ConcurrentHashMap<UUID, IMailData> {
        return targets
    }

    override fun getTarget(uuid: UUID): IMailData? {
        return targets[uuid]
    }

    override fun changeGlobalModel(): IMailData {
        globalModel = true
        val a = MailRegister.createNewMailData(type)
        targets.clear()
        targets[MailRegister.console] = a
        return a
    }

    override fun senderMail() {
        // 获取邮件注册类
        MailRegister.getRegisterMail(type)?.let {
            // 清理草稿箱
            deleteToSql { back ->
                if (back == 1) {
                    val text = this.context.joinToString(";")
                    val timer = System.currentTimeMillis()
                    targets.forEach { (key, value) ->
                        // 修正附件类型
                        // 如果无附件信息，并且不是默认的空类，则修正
                        val new: IMail<*> = if (!value.hasAppendix() && value !is MailDataEmpty) {
                            MailEmpty(UUID.randomUUID(), sender, key)
                        } else {
                            it.cloneMail(UUID.randomUUID(), sender, key, value)
                        }
                        // 设置基本信息
                        new.title = title
                        new.context = text
                        new.senderTimer = timer
                        // 修正状态
                        if (value is MailDataEmpty) {
                            new.state = MailState.Text
                        } else {
                            new.state = MailState.NotObtained
                        }
                        if (globalModel) {
                            new.sendToOfflinePlayers()
                        } else if (key != MailRegister.console) {
                            new.sendMail()
                        } else {
                            error("系统异常，不是全局管理员模式，但发送目标是控制台...")
                        }
                    }
                }
            }
        }
    }


    override fun isAllowSender(): Boolean {
        if (targets.isEmpty()) {
            Bukkit.getPlayer(sender)?.sendLang("玩家-发送草稿邮件-拒绝")
            return false
        }
        return true
    }

    override fun isAllowDeletion(): Boolean {
        for (pr in targets.values) {
            if (pr.hasAppendix()) {
                Bukkit.getPlayer(sender)?.sendLang("玩家-删除草稿邮件-拒绝")
                return false
            }
        }
        return true
    }

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<IDraftBuilder>(DraftBuilderImpl(MailRegister.console, "err"))
        }

    }


}