package me.neon.mail.menu.impl


import me.neon.mail.data.IPlayerData
import me.neon.mail.utils.syncRunner
import me.neon.mail.mail.IMail
import me.neon.mail.mail.MailState
import me.neon.mail.menu.*
import me.neon.mail.service.ServiceManager
import me.neon.mail.service.ServiceManager.deleteMails
import me.neon.mail.service.ServiceManager.updateState
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.impl.PageableChestImpl
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
class ReceiveMenu(
    override val player: Player,
    private val data: IPlayerData
): IMenu {
    override val menuData: MenuData = MenuLoader.getMenuData(MenuType.Receive)

    override fun getInventory(): Inventory {

        return buildMenu<PageableChestImpl<IMail<*>>>(menuData.title.replacePlaceholder(player)) {
            initMenu()
            elements { data.receiveBox }

            onGenerate { _, element, _, _ ->
                element.parseMailIcon(player, menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->

                //if (element.state != IMail.MailState.Text && element.state != IMail.MailState.Acquired
                  //  && element.data !is MailDataEmpty)
               // {
                    // 打开操作界面
                    // 理论上，应该拒绝上面状态的邮件打开操作界面的，但是考虑到只用左键可能无法操作删除
                    ActionsMenu(player, data, element).open()
              //  }
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    // 领取全部
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val list = data.receiveBox.filter {
                                    it.state == MailState.NotObtained && it.data.isSuccessAppendix(player)
                                }
                                if (list.isNotEmpty()) {
                                    val timer = System.currentTimeMillis()
                                    // 为了安全，不得不两次遍历
                                    // 修改状态 -> 更新数据库 -> 发放附件
                                    // 必须确保数据库更新成功
                                    list.forEach {
                                        it.state = MailState.Acquired
                                        it.collectTimer = timer
                                    }
                                    player.closeInventory()
                                    list.updateState {
                                        // 转同步
                                        syncRunner {
                                            // 发送附件
                                            list.forEach {
                                                if (it.data.giveAppendix(player)) {
                                                    player.sendLang("玩家-领取附件-成功", it.data.getAllAppendixInfo(player))
                                                }
                                            }
                                            openMenu()
                                        }
                                    }
                                } else {
                                    player.sendLang("玩家-领取全部附件-失败")
                                }
                            }
                        }
                    }
                    // 绑定邮箱
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            ServiceManager.getSmtpImpl()?.let {
                                if (data.mail.isEmpty()) {
                                    player.closeInventory()
                                    player.sendLang("玩家-邮箱绑定-输入")
                                    player.nextChatInTick(400, { msg ->
                                        if (msg.equals("cancel", ignoreCase = true)) {
                                            player.cancelNextChat(false)
                                        } else {
                                            if (ServiceManager.mainRegex.matches(msg)) {
                                                val code = ((Math.random()*9+1)*100000).toInt()
                                                ServiceManager.bindCode[player.uniqueId] = "$code;$msg"
                                                it.sendBindEmail(player, code.toString(), msg)
                                                player.sendLang("玩家-邮箱绑定-确认")
                                            } else {
                                                player.sendLang("玩家-邮箱绑定-错误")
                                            }
                                        }
                                    })
                                }
                            } ?: player.sendMessage("功能未配置，请联系管理员...")
                        }
                    }
                    // 删除已读
                    'D' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val list = data.receiveBox.filter { it.state == MailState.Acquired || it.state == MailState.TextAcquired }
                                if (list.isNotEmpty()) {
                                    player.closeInventory()
                                    player.sendLang("邮件-删除操作-确认")
                                    player.nextChatInTick(400, {
                                        if (it.equals("cancel", ignoreCase = true)) {
                                            player.cancelNextChat(false)
                                        } else if (it == "确认" || it == "ok") {
                                            player.sendLang("邮件-删除操作-成功", list.size)
                                            // 更新
                                            list.deleteMails(false)
                                            // 清理
                                            data.receiveBox.removeIf { a -> a.state == MailState.Acquired || a.state == MailState.TextAcquired }
                                        }
                                    })
                                } else {
                                    player.sendLang("邮件-删除操作-失败")
                                }
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