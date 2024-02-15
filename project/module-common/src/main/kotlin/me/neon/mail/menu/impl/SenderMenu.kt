package me.neon.mail.menu.impl

import me.neon.mail.ServiceManager.deleteMails
import me.neon.mail.api.mail.IMail
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.libs.taboolib.chat.cancelNextChat
import me.neon.mail.libs.taboolib.chat.nextChatInTick
import me.neon.mail.libs.taboolib.lang.sendLang
import me.neon.mail.libs.taboolib.ui.buildMenu
import me.neon.mail.menu.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import me.neon.mail.libs.taboolib.ui.type.Linked
import me.neon.mail.libs.utils.replacePlaceholder


/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/4 13:55
 */
class SenderMenu(
    override val player: Player,
    private val data: PlayerDataImpl
): IMenu {
    override val menuData: MenuData = MenuLoader.getMenuData(MenuType.Sender)

    override fun getInventory(): Inventory {

        return buildMenu<Linked<IMail<*>>>(menuData.title.replacePlaceholder(player)) {
            initMenu()
            elements { data.senderBox }
            onGenerate { _, element, _, _ ->
                (element as IMailAbstract<*>).parseMailIcon(player, menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->
                if (data.senderBox.removeIf { it.uuid == element.uuid }) {
                    listOf(element).deleteMails(true)
                    player.sendLang("邮件-删除操作-成功", 1)
                    player.openInventory(build())
                }
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'D' -> {
                        set(key, value.parseItems(player)) {
                            if (data.senderBox.size > 0) {
                                player.closeInventory()
                                player.sendLang("邮件-删除操作-确认")
                                player.nextChatInTick(400, {
                                    if (it.equals("cancel", ignoreCase = true)) {
                                        player.cancelNextChat(false)
                                    } else if (it == "确认" || it == "ok") {
                                        player.sendLang("邮件-删除操作-成功", data.senderBox.size)
                                        // 复制
                                        val list = data.senderBox.toList()
                                        // 更新
                                        list.deleteMails(true)
                                        // 清理
                                        data.senderBox.clear()
                                    }
                                })
                            }
                        }
                    }
                    '>' -> setupNext(player, value, key)
                    '<' -> setupPrev(player, value, key)
                    else -> setupDefaultAction(player, value, key)
                }
            }
        }
    }
}