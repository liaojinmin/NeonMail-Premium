package me.neon.mail.menu.impl

import me.neon.mail.ServiceManager.deleteMail
import me.neon.mail.ServiceManager.updateState
import me.neon.mail.api.io.syncRunner
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailState
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.menu.MenuLoader
import me.neon.mail.common.NormalMail
import me.neon.mail.api.menu.MenuData
import me.neon.mail.api.menu.MenuType
import me.neon.mail.parseMailInfo
import org.bukkit.entity.Player
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Chest
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
    private val data: PlayerDataImpl,
    private val mail: IMailAbstract<*>,
) {
    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.Actions)


    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {
        buildMenu<Chest>(menuData.title
            .replace("[title]", mail.title)
            .replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            onClick(true)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    '1','2','3' -> {
                        set(key, value.parseItems(player, mail.parseMailInfo(value.lore)))
                    }
                    '4' -> {
                        set(key, value.parseItems(player, mail.parseMailInfo(value.lore))) {
                            // 没有领取则允许打开
                            if (mail.state == IMailState.NotObtained) {
                                if (mail is NormalMail && mail.data.itemStacks.isNotEmpty()) {
                                    ItemPreviewMenu(player, data, mail, mail.data.itemStacks).open()
                                }
                            }
                        }
                    }
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            ReceiveMenu(player, data).openMenu()
                        }
                    }
                    'D' -> {
                        set(key, value.parseItems(player)) {

                            if (mail.state == IMailState.Acquired || mail.state == IMailState.Text) {
                                if (data.receiveBox.removeIf { it.uuid == mail.uuid }) {
                                    mail.deleteMail(player.uniqueId == mail.sender)
                                    player.sendLang("邮件-删除操作-成功", 1)
                                    ReceiveMenu(player, data).openMenu()
                                }
                            } else {
                                player.sendLang("邮件-删除操作-失败-附件存在")
                            }
                        }
                    }
                    'G' -> {
                        set(key, value.parseItems(player)) {
                           // if (mail.state != IMailState.Text) {
                                if (mail.state == IMailState.NotObtained) {

                                    if (mail.data.checkClaimCondition(player)) {
                                        player.closeInventory()
                                        mail.state = IMailState.Acquired
                                        mail.collectTimer = System.currentTimeMillis()
                                        listOf(mail).updateState {
                                            // 数据库更新成功才发放奖励
                                            syncRunner {
                                                mail.data.giveAppendix(player)
                                                player.sendLang(
                                                    "玩家-领取附件-成功",
                                                    mail.data.getAppendixInfo(player)
                                                )
                                                ReceiveMenu(player, data).openMenu()
                                            }
                                        }
                                    }
                                } else {
                                    player.sendLang("邮件-领取附件-失败")
                                }
                          //  }
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