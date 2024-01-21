package me.neon.mail.menu.edit

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.ServiceManager.deleteToSql
import me.neon.mail.ServiceManager.updateToSql
import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.common.DataTypeEmpty
import me.neon.mail.menu.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.*
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.menu.edit
 *
 * @author 老廖
 * @since 2024/1/6 16:16
 */
class DraftMailEditeMenu(
    override val player: Player,
    private val data: PlayerDataImpl,
    private val mail: IDraftBuilder,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData: MenuData = MenuLoader.draftMailEditeMenu


    override fun getInventory(): Inventory {
        return buildMenu<Linked<Pair<UUID?, IMailDataType>>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements {
                val list = mutableListOf<Pair<UUID?, IMailDataType>>()
                mail.getTargets().forEach { (t, u) ->
                    list.add(t to u)
                }
                list.add(null to DataTypeEmpty())
                list
            }

            onGenerate { _, element, _, _ ->
                var icon = menuData.getCharMenuIcon('@')
                var parseItem = true
                if (element.first == null && element.second is DataTypeEmpty) {
                    icon = icon.subIcon ?: error("找不到子图标")
                    parseItem = false
                }
                val itemBuilder = ItemBuilder(icon.mats)
                itemBuilder.name = icon.name.replacePlaceholder(player)
                itemBuilder.customModelData = icon.model
                if (parseItem) {
                    icon.lore.forEach {
                        if (it.contains("[info]")) {
                            // 取空格
                            val str = StringBuilder()
                            for (c in it) {
                                if (c != ' ') break
                                str.append(" ")
                            }
                            itemBuilder.lore.addAll(
                                element.second.getAppendixInfo(adaptPlayer(player), str.toString(), true).split(";")
                            )
                        } else {
                            if (element.first == IMailRegister.console) {
                                itemBuilder.lore.add(it.replace("[player]", "all"))
                            } else {
                                itemBuilder.lore.add(
                                    it.replace(
                                        "[player]",
                                        Bukkit.getOfflinePlayer(element.first!!).name ?: element.first.toString()
                                    )
                                )
                            }
                        }
                    }
                }
                itemBuilder.build()
            }

            onClick { _, element ->
                // 如果已经是全体模式，拒绝再添加
                if (!mail.checkGlobalModel() && element.first == IMailRegister.console) {
                    PlayerListMenu(player, data, mail, this@DraftMailEditeMenu, admin).openMenu()
                } else {
                    element.first?.let {
                        // 操作界面
                        MailAppEditeMenu(player, data, mail, it, element.second, this@DraftMailEditeMenu, admin).openMenu()
                    }
                }
            }


            menuData.icon.forEach { (key, value) ->
                when (key) {
                    '1' -> {
                        set(key, value.parseItems(player, "[title]" to mail.title)) {
                            player.sendLang("邮件-编辑操作-标题")
                            player.nextChat {
                                if (it.isNotEmpty()) {
                                    if (it.equals("cancel", ignoreCase = true)) {
                                        player.cancelNextChat(false)
                                    } else {
                                        if (it.length >= 120) {
                                            player.sendLang("邮件-编辑操作-输入长度超出", 120, it.length)
                                        } else if (NeonMailLoader.checkInput(it)) {
                                            player.sendLang("邮件-编辑操作-输入存在屏蔽词")
                                        } else {
                                            mail.title = it
                                            submit { openMenu() }
                                        }
                                    }
                                }
                            }
                            player.closeInventory()
                        }
                    }
                    '2' -> {
                        set(key, value.parseItems(player, "[text]" to mail.context)) {
                            player.sendLang("邮件-编辑操作-文本")
                            player.nextChat {
                                if (it.isNotEmpty()) {
                                    if (it.equals("cancel", ignoreCase = true)) {
                                        player.cancelNextChat(false)
                                    } else {
                                        if (it.length >= 255) {
                                            player.sendLang("邮件-编辑操作-输入长度超出",255, it.length)
                                        } else if (NeonMailLoader.checkInput(it)) {
                                            player.sendLang("邮件-编辑操作-输入存在屏蔽词")
                                        } else {
                                            mail.context.clear()
                                            mail.context.addAll(it.split(";"))
                                            submit { openMenu() }
                                        }
                                    }
                                }
                            }
                            player.closeInventory()
                        }
                    }
                    // 删除
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            if (mail.isAllowDeletion()) {
                                player.closeInventory()
                                mail.deleteToSql {
                                    if (it == 1) {
                                        data.delDraft(mail.uuid)
                                        player.sendLang("玩家-删除草稿邮件-成功")
                                    }
                                }
                            }
                        }
                    }
                    // 保存
                    'S' -> {
                        set(key, value.parseItems(player)) {
                            if (!MenuLoader.isClickCD(player.uniqueId)) {
                                MenuLoader.addClickCD(player.uniqueId, 10)
                                mail.updateToSql()
                                player.sendLang("玩家-草稿邮件-保存", mail.title)
                            }
                        }
                    }
                    // 发送
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (mail.isAllowSender()) {
                                player.closeInventory()
                                mail.senderMail()
                                data.delDraft(mail.uuid)
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