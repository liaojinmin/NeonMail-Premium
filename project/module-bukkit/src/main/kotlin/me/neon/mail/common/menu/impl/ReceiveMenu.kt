package me.neon.mail.common.menu.impl

import me.neon.mail.ServiceManager.updateDel
import me.neon.mail.ServiceManager.updateState
import me.neon.mail.api.IMail
import me.neon.mail.common.PlayerData
import me.neon.mail.api.IMailAbstract
import me.neon.mail.common.menu.*
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
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
    private val player: Player,
    private val data: PlayerData
) {
    private val menuData: MenuData = MenuLoader.receiveMenu


    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {

        buildMenu<Linked<IMail<*>>>(menuData.title.replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { data.receiveBox }

            onGenerate { _, element, _, _ ->
                (element as IMailAbstract<*>).parseMailIcon(menuData.getCharMenuIcon('@'))
            }

            onClick(true)

            onClick { _, element ->
               // player.closeInventory()
                // 打开操作界面
                ActionsMenu(player, data, element as IMailAbstract<*>).open()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    // 领取全部
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val list = data.receiveBox.filter { it.state == IMail.IMailState.NotObtained }
                                if (list.isNotEmpty()) {
                                    val timer = System.currentTimeMillis()
                                    // 领取附件
                                    var update = false
                                    list.forEach {
                                        if (it.giveAppendix(adaptPlayer(player))) {
                                            update = true
                                            it.state = IMail.IMailState.Acquired
                                            it.collectTimer = timer
                                            (it as IMailAbstract).let { abs ->
                                                player.sendLang("玩家-领取附件-成功", abs.getAppendixInfo(adaptPlayer(player)))
                                                if (currentItem != null) {
                                                    currentItem = abs.parseMailIcon(value)
                                                }
                                            }
                                        }
                                    }
                                    if (update) list.updateState()
                                } else {
                                    player.sendLang("玩家-领取全部附件-失败")
                                }
                            }
                        }
                    }
                    // 绑定邮箱
                    'B' -> {
                        TODO("功能未实现")
                    }
                    // 删除已读
                    'D' -> {
                        set(key, value.parseItems(player)) {
                            if (data.receiveBox.size > 0) {
                                val list = data.receiveBox.filter { it.state == IMail.IMailState.Acquired }
                                if (list.isNotEmpty()) {
                                    player.closeInventory()
                                    player.sendLang("邮件-删除操作-确认")
                                    player.nextChatInTick(400, {
                                        if (it.equals("cancel", ignoreCase = true)) {
                                            player.cancelNextChat(false)
                                        } else if (it == "确认" || it == "ok") {
                                            player.sendLang("邮件-删除操作-成功", list.size)
                                            // 更新
                                            list.updateDel(false)
                                            // 清理
                                            data.receiveBox.removeIf {  it.state == IMail.IMailState.Acquired }
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