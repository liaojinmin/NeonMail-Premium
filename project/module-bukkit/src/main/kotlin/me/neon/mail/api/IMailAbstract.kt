package me.neon.mail.api


import me.neon.mail.IMailRegister
import me.neon.mail.ServiceManager.getPlayerData
import me.neon.mail.ServiceManager.insert
import me.neon.mail.SetTings
import me.neon.mail.api.event.MailIconBuilderEvent
import me.neon.mail.common.IMailDefaultImpl
import me.neon.mail.common.DataTypeNormal
import me.neon.mail.common.menu.MenuIcon
import me.neon.mail.uitls.parseMailInfo
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

    override var state: IMail.IMailState = IMail.IMailState.NotObtained

    override var senderTimer: Long = -1L

    override var collectTimer: Long = -1L

    override var senderDel: Int = 0

    override var targetDel: Int = 0

    abstract fun containsItemsStack(): Boolean

    override fun sendMail() {
        var targetName = "目标"
        getProxyPlayer(target)?.let {
            targetName = it.displayName!!
            it.getPlayerData()?.let { data ->
                data.receiveBox.add(this)
                val info = if (context.length >= 11) context.substring(0, 10) + "§8..." else context
                it.sendLang("玩家-接收邮件-送达", title, info)
            }
        } ?: sendCrossMail()
        if (sender != IMailRegister.console) {
            getProxyPlayer(sender)?.let {
                it.getPlayerData()?.let { data ->
                    data.senderBox.add(this)
                    it.sendLang("玩家-发送邮件-送达", targetName)
                }
            }
        }
        insert()
    }

    internal fun sendCrossMail() {
       // TODO("Not yet implemented")
    }

    internal fun sendGlobalMail() {
      //  TODO("Not yet implemented")
    }

    open fun parseDataUpdateCallBack(
        icon: MenuIcon,
        player: Player,
        type: IMailDataType,
        openCall: () -> Unit
    ): Pair<ItemStack, ClickEvent.() -> Unit> {
        if (type !is DataTypeNormal) {
            warning("请不要使用默认方法解析非默认附件种类 ${data::class.java} eq ${DataTypeNormal::class.java}")
            return ItemStack(Material.AIR) to {}
        }
        if (this is IMailDefaultImpl) {
            return type.parseDataUpdateCallBack(icon, player, openCall)
        } else TODO("自定义附件种类请实现 -> parseDataUpdateCallBack() 方法")
    }

    open fun parseMailIcon(icon: MenuIcon): ItemStack {
        val item = buildItem(if (SetTings.getUseBundle()) XMaterial.BUNDLE else mainIcon) {
            name = icon.name.replace("[title]", title)
            lore.addAll(parseMailInfo(icon.lore))
            customModelData = icon.model
            if (state == IMail.IMailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
        val event = MailIconBuilderEvent(this, item)
        if (!event.callEvent()) {
            return ItemStack(Material.AIR)
        }
        return item
    }

    companion object {

        val appendixCache: MutableMap<UUID, String> = mutableMapOf()



    }


}