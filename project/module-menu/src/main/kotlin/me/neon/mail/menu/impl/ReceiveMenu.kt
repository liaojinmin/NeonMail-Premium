package me.neon.mail.menu.impl

import me.neon.mail.service.ServiceManager.deleteMails
import me.neon.mail.service.ServiceManager.updateState
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailState
import me.neon.mail.common.PlayerData
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.menu.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
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
class ReceiveMenu(
    override val player: Player,
    private val data: PlayerData
): IMenu {
    override val menuData: MenuData = MenuLoader.receiveMenu

    override fun getInventory(): Inventory {

        return buildMenu<Linked<IMail<*>>>(menuData.title.replacePlaceholder(player)) {
            initMenu()
            elements { data.receiveBox }

            onGenerate { _, element, _, _ ->
                (element as IMailAbstract<*>).parseMailIcon(menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->
                //if (element.state != IMail.IMailState.Text && element.state != IMail.IMailState.Acquired
                  //  && element.data !is DataTypeEmpty)
               // {
                    // 打开操作界面
                    // 理论上，应该拒绝上面状态的邮件打开操作界面的，但是考虑到只用左键可能无法操作删除
                    ActionsMenu(player, data, element as IMailAbstract<*>).open()
              //  }
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    // 领取全部
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val pl = adaptPlayer(player)
                                val list = data.receiveBox.filter {
                                    it.state == IMailState.NotObtained && it.checkClaimCondition(pl)
                                }
                                if (list.isNotEmpty()) {
                                    val timer = System.currentTimeMillis()
                                    // 为了安全，不得不两次遍历
                                    // 修改状态 -> 更新数据库 -> 发放附件
                                    // 必须确保数据库更新成功
                                    list.forEach {
                                        it.state = IMailState.Acquired
                                        it.collectTimer = timer
                                    }
                                    player.closeInventory()
                                    list.updateState {
                                        // 转同步
                                        submit {
                                            // 发送附件
                                            list.forEach {
                                                if (it.giveAppendix(adaptPlayer(player))) {
                                                    player.sendLang("玩家-领取附件-成功", it.data.getAppendixInfo(pl))
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
                      //  TODO("功能未实现")
                    }
                    // 删除已读
                    'D' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val list = data.receiveBox.filter { it.state == IMailState.Acquired }
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
                                            data.receiveBox.removeIf { a -> a.state == IMailState.Acquired }
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