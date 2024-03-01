package me.neon.mail.api.template

import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMailRegister
import org.bukkit.OfflinePlayer
import taboolib.common.platform.function.warning
import java.util.*


/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:00
 */
data class TemplatePack(
    val uniqueId: String,
    val type: String,
    var title: String,
    var context: List<String>,
    var appendix: MutableList<TemplateType<*>> = mutableListOf(),
) {


    constructor(uniqueId: String, iDraftBuilder: IDraftBuilder): this(
        uniqueId,
        iDraftBuilder.type, iDraftBuilder.title, iDraftBuilder.context
    ) {
        iDraftBuilder.getTargets().forEach { (_, v) ->
            appendix.addAll(v.parseDataToTemplate())
        }
    }

    fun sendToAllOnLinePlayer() {
        IMailRegister.getRegisterMail(type)?.let { iMail ->
            iMail.cloneMail(
                IMailRegister.console,
                IMailRegister.console,
                IMailRegister.console,
                iMail.data.parseTemplateToData(appendix)
            ).apply {
                this.title = this@TemplatePack.title
                this.context = this@TemplatePack.context.joinToString(";")
                this.senderTimer = System.currentTimeMillis()
            }.sendToOnlinePlayers()
        } ?: warning("找不到 $type 的邮件类型...")
    }
    fun sendToAllOffLinePlayer() {
        IMailRegister.getRegisterMail(type)?.let { iMail ->
            iMail.cloneMail(
                IMailRegister.console,
                IMailRegister.console,
                IMailRegister.console,
                iMail.data.parseTemplateToData(appendix)
            ).apply {
                this.title = this@TemplatePack.title
                this.context = this@TemplatePack.context.joinToString(";")
                this.senderTimer = System.currentTimeMillis()
            }.sendToOfflinePlayers()
        } ?: warning("找不到 $type 的邮件类型...")
    }

    fun sendToPlayer(player: OfflinePlayer) {
        IMailRegister.getRegisterMail(type)?.let { iMail ->
            iMail.cloneMail(
                UUID.randomUUID(),
                IMailRegister.console,
                player.uniqueId,
                iMail.data.parseTemplateToData(appendix)
            ).apply {
                this.title = this@TemplatePack.title
                this.context = this@TemplatePack.context.joinToString(";")
                this.senderTimer = System.currentTimeMillis()
            }.sendMail()
        } ?: warning("找不到 ${type} 的邮件类型...")
    }

}
