package me.neon.mail.common.menu.impl

import me.neon.mail.ServiceManager.updateDel
import me.neon.mail.ServiceManager.updateState
import me.neon.mail.api.IMailAbstract
import me.neon.mail.api.IMail
import me.neon.mail.common.PlayerData
import me.neon.mail.common.menu.MenuData
import me.neon.mail.common.menu.MenuLoader
import me.neon.mail.common.IMailDefaultImpl
import me.neon.mail.uitls.parseMailInfo
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendLang

/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/5 23:03
 */
class ActionsMenu(
    private val player: Player,
    private val data: PlayerData,
    private val mail: IMailAbstract<*>,
) {
    private val menuData: MenuData = MenuLoader.actionsMenu


    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {
        buildMenu<Basic>(menuData.title
            .replace("[title]", mail.title)
            .replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    '1','2','3' -> {
                        set(key, value.parseItems(player, mail.parseMailInfo(value.lore)))
                    }
                    '4' -> {
                        set(key, value.parseItems(player, mail.parseMailInfo(value.lore))) {
                            if (mail.containsItemsStack() && mail is IMailDefaultImpl) {
                                ItemPreviewMenu(player, data, mail, mail.data.itemStacks).open()
                            }
                        }
                    }
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            ReceiveMenu(player, data).open()
                        }
                    }
                    'D' -> {
                        set(key, value.parseItems(player)) {

                            if (mail.state == IMail.IMailState.Acquired || mail.state == IMail.IMailState.Text) {
                                if (data.receiveBox.removeIf { it.uuid == mail.uuid }) {
                                    listOf(mail).updateDel(false)
                                    player.sendLang("邮件-删除操作-成功")
                                    ReceiveMenu(player, data).open()
                                }
                            } else {
                                player.sendLang("邮件-删除操作-失败-附件存在")
                            }
                        }
                    }
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (mail.state != IMail.IMailState.Text) {
                                if (mail.state == IMail.IMailState.NotObtained) {
                                    mail.state = IMail.IMailState.Acquired
                                    mail.collectTimer = System.currentTimeMillis()
                                    listOf(mail).updateState()
                                    player.sendLang("玩家-领取附件-成功", mail.getAppendixInfo(adaptPlayer(player)))
                                    ReceiveMenu(player, data).open()
                                } else {
                                    player.sendLang("邮件-领取附件-失败")
                                }
                            }
                        }
                    }
                    else -> {
                        set(key, value.parseItems(player)) {
                            value.eval(player)
                        }
                    }
                }
            }


        }
    }


}