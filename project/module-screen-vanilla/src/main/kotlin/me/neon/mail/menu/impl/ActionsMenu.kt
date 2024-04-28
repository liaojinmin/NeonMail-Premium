package me.neon.mail.menu.impl

import me.neon.mail.data.IPlayerData
import me.neon.mail.utils.syncRunner
import me.neon.mail.mail.IMail
import me.neon.mail.mail.MailState
import me.neon.mail.mail.parseMailInfo
import me.neon.mail.menu.MenuLoader
import me.neon.mail.menu.MenuData
import me.neon.mail.menu.MenuType
import me.neon.mail.service.ServiceManager.deleteMail
import me.neon.mail.service.ServiceManager.updateState
import org.bukkit.entity.Player
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.replacePlaceholder
import me.neon.mail.Settings.sendLang

/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/5 23:03
 */
class ActionsMenu(
    private val player: Player,
    private val data: IPlayerData,
    private val mail: IMail<*>,
) {
    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.Actions)


    fun open() {
        //检查如果是纯文本，则将状态设置
        if (mail.state ==  MailState.Text) {
            mail.state = MailState.TextAcquired
            listOf(mail).updateState {}
        }
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
                            if (mail.state == MailState.NotObtained) {
                                mail.data.getItemAppendix()?.let { ItemPreviewMenu(player, data, mail, it).open() }
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

                            if (mail.state != MailState.NotObtained) {
                                if (data.receiveBox.removeIf { it.unique == mail.unique }) {
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
                           // if (mail.state != MailState.Text) {
                                if (mail.state == MailState.NotObtained) {
                                    if (mail.data.isSuccessAppendix(player)) {
                                        player.closeInventory()
                                        mail.state = MailState.Acquired
                                        mail.collectTimer = System.currentTimeMillis()
                                        listOf(mail).updateState {
                                            // 数据库更新成功才发放奖励
                                            syncRunner {
                                                mail.data.giveAppendix(player)
                                                player.sendLang(
                                                    "玩家-领取附件-成功",
                                                    mail.data.getAllAppendixInfo(player)
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