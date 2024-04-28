package me.neon.mail.mail


import me.neon.mail.service.ServiceManager
import me.neon.mail.service.ServiceManager.getPlayerData
import me.neon.mail.service.ServiceManager.insertMail
import me.neon.mail.service.packet.PlayOutMailReceivePacket
import org.bukkit.Bukkit
import me.neon.mail.Settings.sendLang
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 13:16
 */
abstract class IMailAbstract<T: IMailData>: IMail<T> {


    override var title: String = "未知邮件标题"

    override var context: String = "未知邮件内容"

    override var state: MailState = MailState.NotObtained

    override var senderTimer: Long = System.currentTimeMillis()

    override var collectTimer: Long = -1L

    override val permission: String = "mail.extend.normal"

    override fun sendMail() {
        var targetName = "目标"
        Bukkit.getPlayer(target)?.let {
            targetName = it.displayName
            it.getPlayerData()?.let { data ->
                data.receiveBox.add(this)
                val info = if (context.length >= 11) context.substring(0, 10) + "§8..." else context
                it.sendLang("玩家-接收邮件-送达", title, info.replace(";", ""))

                // 未解决玩家离线时的smtp、发送。
                if (data.mail.isNotEmpty()) {
                    ServiceManager.getSmtpImpl()?.sendEmail(data.mail, this)
                }
            } ?: run {
                // 取玩家数据，并获取邮件消息，判断是否发生smtp
                ServiceManager.getOffPlayerData(target).thenAccept { data ->
                    if (data.mail.isNotEmpty()) {
                        ServiceManager.getSmtpImpl()?.sendEmail(data.mail, this)
                    }
                }
            }
        } ?: sendCrossMail(target)

        if (sender != MailRegister.console) {
            // 理论上发送者不可能不在线，就算切服仍然能够获取
            Bukkit.getPlayer(sender)?.let {
                it.getPlayerData()?.let { data ->
                    data.senderBox.add(this)
                    it.sendLang("玩家-发送邮件-送达", targetName)
                }
            }
        }
        // 入库
        insertMail()
    }

    override fun sendToOfflinePlayers() {
        // 全局邮件必须以控制台身份发送，不管发送者是谁，自动修正。
        // 因为只有管理员能够发送这种类型
        Bukkit.getOfflinePlayers().forEach {
            this.cloneMail(UUID.randomUUID(), MailRegister.console, it.uniqueId, data).sendMail()
        }
    }

    override fun sendToOnlinePlayers() {
        // 全局邮件必须以控制台身份发送，不管发送者是谁，自动修正。
        // 因为只有管理员能够发送这种类型
        Bukkit.getOnlinePlayers().forEach {
            this.cloneMail(UUID.randomUUID(), MailRegister.console, it.uniqueId, data).sendMail()
        }
    }

    private fun sendCrossMail(player: UUID) {
       PlayOutMailReceivePacket(player, this.unique).senderPacket()
    }






}