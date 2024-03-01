package me.neon.mail.api.mail


import me.neon.mail.NeonMailLoader
import me.neon.mail.ServiceManager
import me.neon.mail.ServiceManager.getPlayerData
import me.neon.mail.ServiceManager.insertMail
import me.neon.mail.api.event.MailIconBuilderEvent
import me.neon.mail.api.menu.IDraftEdite
import me.neon.mail.api.menu.MenuIcon
import me.neon.mail.common.DataTypeNormal
import me.neon.mail.parseMailInfo
import me.neon.mail.service.packet.PlayOutMailReceivePacket
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import taboolib.module.ui.ClickEvent
import taboolib.platform.util.buildItem
import taboolib.platform.util.sendLang
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

    override var state: IMailState = IMailState.NotObtained

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
            }?: run {

            }
        } ?: sendCrossMail(target)
        if (sender != IMailRegister.console) {
            // 理论上发生者不可能不在线，就算切服仍然能够获取
            Bukkit.getPlayer(sender)?.let {
                it.getPlayerData()?.let { data ->
                    data.senderBox.add(this)
                    it.sendLang("玩家-发送邮件-送达", targetName)
                }
            }
        }
        insertMail()
    }

    override fun sendToOfflinePlayers() {
        // 全局邮件必须以控制台身份发送，不管发送者是谁，自动修正。
        // 因为只有管理员能够发送这种类型
        Bukkit.getOfflinePlayers().forEach {
            this.cloneMail(UUID.randomUUID(), IMailRegister.console, it.uniqueId, data).sendMail()
        }
    }

    override fun sendToOnlinePlayers() {
        // 全局邮件必须以控制台身份发送，不管发送者是谁，自动修正。
        // 因为只有管理员能够发送这种类型
        Bukkit.getOnlinePlayers().forEach {
            this.cloneMail(UUID.randomUUID(), IMailRegister.console, it.uniqueId, data).sendMail()
        }
    }

    private fun sendCrossMail(player: UUID) {
       PlayOutMailReceivePacket(player, this.uuid).senderPacket()
    }

    open fun parseMenuCallBack(
        icon: MenuIcon,
        player: Player,
        type: IMailData,
        builder: IDraftBuilder,
        edite: IDraftEdite
    ): Pair<ItemStack, ClickEvent.() -> Unit> {
        return type.parseCallBack(icon, player, builder, edite)
    }

    open fun parseMailIcon(player: Player, icon: MenuIcon): ItemStack {
        val item = buildItem(if (NeonMailLoader.getUseBundle()) Material.valueOf("BUNDLE") else mainIcon) {
            name = icon.name.replace("[title]", title)
            lore.addAll(parseMailInfo(icon.lore))
            customModelData = icon.model
            if (state == IMailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
        if (data is DataTypeNormal && item.type.name == "BUNDLE") {
            val type = data as DataTypeNormal
            val meta = item.itemMeta as BundleMeta
            meta.setItems(type.itemStacks)
            item.itemMeta = meta
        }
        val event = MailIconBuilderEvent(player, this, item)
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return ItemStack(Material.AIR)
        }
        return event.itemStack
    }


    fun register() {
        IMailRegister.register(this)
    }

    fun unregister() {
        IMailRegister.unregister(this)
    }

    companion object {

        val appendixCache: MutableMap<UUID, String> = mutableMapOf()

    }


}