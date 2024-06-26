package me.neon.mail.menu.impl

import me.neon.mail.data.IPlayerData
import me.neon.mail.mail.IMail
import me.neon.mail.menu.*
import me.neon.mail.service.ServiceManager.deleteMails
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.nextChatInTick
import me.neon.mail.Settings.sendLang


/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/4 13:55
 */
class SenderMenu(
    override val player: Player,
    private val data: IPlayerData
): IMenu {
    override val menuData: MenuData = MenuLoader.getMenuData(MenuType.Sender)

    override fun getInventory(): Inventory {

        return buildMenu<PageableChest<IMail<*>>>(menuData.title.replacePlaceholder(player)) {
            initMenu()
            elements { data.senderBox }
            onGenerate { _, element, _, _ ->
                element.parseMailIcon(player, menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->
                if (data.senderBox.removeIf { it.unique == element.unique }) {
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