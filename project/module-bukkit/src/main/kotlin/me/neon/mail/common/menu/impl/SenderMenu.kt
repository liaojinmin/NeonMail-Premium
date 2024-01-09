package me.neon.mail.common.menu.impl

import me.neon.mail.ServiceManager.updateDel
import me.neon.mail.api.IMail
import me.neon.mail.common.PlayerData
import me.neon.mail.api.IMailAbstract
import me.neon.mail.common.menu.*
import org.bukkit.entity.Player
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.nextChatInTick
import taboolib.platform.util.sendLang

/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/4 13:55
 */
class SenderMenu(
    private val player: Player,
    private val data: PlayerData
) {
    private val menuData: MenuData = MenuLoader.senderMenu


    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {

        buildMenu<Linked<IMail<*>>>(menuData.title.replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { data.senderBox }

            onGenerate { _, element, _, _ ->
                (element as IMailAbstract<*>).parseMailIcon(menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->
                if (data.senderBox.removeIf { it.uuid == element.uuid }) {
                    listOf(element).updateDel(true)
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
                                        // 修改状态
                                        list.forEach { mail -> mail.senderDel = 1 }
                                        // 更新
                                        list.updateDel(true)
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