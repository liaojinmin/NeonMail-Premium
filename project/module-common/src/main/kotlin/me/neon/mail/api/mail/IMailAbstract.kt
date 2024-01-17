package me.neon.mail.api.mail


import me.neon.mail.NeonMailLoader
import me.neon.mail.service.ServiceManager.getPlayerData
import me.neon.mail.service.ServiceManager.insertMail
import me.neon.mail.SetTings
import me.neon.mail.common.DataTypeNormal
import me.neon.mail.common.IMailDefaultImpl
import me.neon.mail.menu.MenuIcon
import me.neon.mail.utils.parseMailInfo
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

    override var senderDel: Int = 0

    override var targetDel: Int = 0

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
        insertMail()
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
        val item = buildItem(if (NeonMailLoader.getUseBundle()) XMaterial.BUNDLE else mainIcon) {
            name = icon.name.replace("[title]", title)
            lore.addAll(parseMailInfo(icon.lore))
            customModelData = icon.model
            if (state == IMailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
        /*
        val event = MailIconBuilderEvent(this, item)
        if (!event.callEvent()) {
            return ItemStack(Material.AIR)
        }

         */
        return item
    }


    companion object {

        val appendixCache: MutableMap<UUID, String> = mutableMapOf()


    }


}