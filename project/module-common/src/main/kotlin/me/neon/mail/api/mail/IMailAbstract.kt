package me.neon.mail.api.mail


import me.neon.mail.NeonMailLoader
import me.neon.mail.ServiceManager.getPlayerData
import me.neon.mail.ServiceManager.insertMail
import me.neon.mail.common.DataTypeNormal
import me.neon.mail.common.MailNormalImpl
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuIcon
import me.neon.mail.ServiceManager
import me.neon.mail.api.event.MailIconBuilderEvent
import me.neon.mail.service.packet.PlayOutMailReceivePacket
import me.neon.mail.utils.parseMailInfo
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.module.lang.sendLang
import taboolib.module.ui.ClickEvent
import taboolib.platform.util.*
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 13:16
 */
abstract class IMailAbstract<T: IMailDataType>: IMail<T> {

    override var title: String = "未知邮件标题"

    override var context: String = "未知邮件内容"

    override var state: IMailState = IMailState.NotObtained

    override var senderTimer: Long = -1L

    override var collectTimer: Long = -1L

    override fun sendMail() {
        var targetName = "目标"
        getProxyPlayer(target)?.let {
            targetName = it.displayName!!
            it.getPlayerData()?.let { data ->
                data.receiveBox.add(this)
                val info = if (context.length >= 11) context.substring(0, 10) + "§8..." else context
                it.sendLang("玩家-接收邮件-送达", title, info)

                // 未解决玩家离线时的smtp、发送。
                if (data.mail.isNotEmpty()) {
                    ServiceManager.getSmtpImpl()?.sendEmail(data.mail, this)
                }
            }
        } ?: sendCrossMail(target)
        if (sender != IMailRegister.console) {
            // 理论上发生者不可能不在线，就算切服仍然能够获取
            getProxyPlayer(sender)?.let {
                it.getPlayerData()?.let { data ->
                    data.senderBox.add(this)
                    it.sendLang("玩家-发送邮件-送达", targetName)
                }
            }
        }
        insertMail()
    }

    override fun sendGlobalMail() {
        // 全局邮件必须以控制台身份发送，不管发送者是谁，自动修正。
        // 因为只有管理员能够发送这种类型
        Bukkit.getOfflinePlayers().forEach {
            this.cloneMail(UUID.randomUUID(), IMailRegister.console, it.uniqueId, data).sendMail()
        }
    }

    private fun sendCrossMail(player: UUID) {
       PlayOutMailReceivePacket(player, this.uuid).senderPacket()
    }



    open fun parseDataUpdateCallBack(
        icon: MenuIcon,
        player: Player,
        type: IMailDataType,
        builder: IDraftBuilder,
        edite: IDraftEdite
    ): Pair<ItemStack, ClickEvent.() -> Unit> {
        if (type !is DataTypeNormal) {
            warning("请不要使用默认方法解析非默认附件种类 ${data::class.java} eq ${DataTypeNormal::class.java}")
            return ItemStack(Material.AIR) to {}
        }
        if (this is MailNormalImpl) {
            return type.parseDataUpdateCallBack(icon, player, builder, edite)
        } else {
            TODO("自定义附件种类请实现 -> parseDataUpdateCallBack() 方法,并定位到相应的按钮解析")
        }
    }

    open fun parseMailIcon(icon: MenuIcon): ItemStack {
        val item = buildItem(if (NeonMailLoader.getUseBundle()) XMaterial.BUNDLE else mainIcon) {
            name = icon.name.replace("[title]", title)
            lore.addAll(parseMailInfo(icon.lore))
            customModelData = icon.model
            if (state == IMailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
 
        val event = MailIconBuilderEvent(this, item)
        if (!event.callEvent()) {
            return ItemStack(Material.AIR)
        }
        return event.itemStack
    }


    companion object {

        val appendixCache: MutableMap<UUID, String> = mutableMapOf()


    }


}