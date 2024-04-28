package me.neon.mail.menu.edit

import me.neon.mail.Settings
import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.MailRegister
import me.neon.mail.data.IPlayerData
import me.neon.mail.utils.syncRunner
import me.neon.mail.mail.MailDataEmpty
import me.neon.mail.mail.IMailData
import me.neon.mail.menu.*
import me.neon.mail.service.ServiceManager.deleteToSql
import me.neon.mail.service.ServiceManager.updateToSql
import me.neon.mail.template.TemplateManager.saveToFile
import me.neon.mail.template.TemplatePack

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.PageableChest
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
    private val data: IPlayerData,
    private val mail: IDraftBuilder,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.DraftMailEdite)

    override fun getInventory(): Inventory {
        return buildMenu<PageableChest<Pair<UUID?, IMailData>>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements {
                val list = mutableListOf<Pair<UUID?, IMailData>>()
                mail.getTargets().forEach { (t, u) ->
                    list.add(t to u)
                }
                if (!mail.checkGlobalModel()) {
                    list.add(null to MailDataEmpty())
                }
                list
            }

            onGenerate { _, element, _, _ ->
                var icon = menuData.getCharMenuIcon('@')
                var parseItem = true
                if (element.first == null && element.second is MailDataEmpty) {
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
                                element.second.getAllAppendixInfo(player, str.toString()).split(";")
                            )
                        } else {
                            if (element.first == MailRegister.console) {
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
                Settings.debug("onclick DraftMailEditeMenu")
                // 如果已经是全体模式，拒绝再添加

                if (!mail.checkGlobalModel() && element.first == null) {
                    Settings.debug("onclick DraftMailEditeMenu -> PlayerListMenu")
                    PlayerListMenu(player, data, mail, this@DraftMailEditeMenu, admin).openMenu()
                } else {
                    element.first?.let {
                        Settings.debug("onclick DraftMailEditeMenu -> EditeAppendixMenu")
                        // 操作界面
                        EditeAppendixMenu(player, data, mail, it, element.second, this@DraftMailEditeMenu, admin).openMenu()
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
                                        } else if (Settings.checkInput(it)) {
                                            player.sendLang("邮件-编辑操作-输入存在屏蔽词")
                                        } else {
                                            mail.title = it
                                            syncRunner { openMenu() }
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
                                        } else if (Settings.checkInput(it)) {
                                            player.sendLang("邮件-编辑操作-输入存在屏蔽词")
                                        } else {
                                            mail.context.clear()
                                            mail.context.addAll(it.split(";"))
                                            syncRunner { openMenu() }
                                        }
                                    }
                                }
                            }
                            player.closeInventory()
                        }
                    }
                    // 删除
                    'D' -> {
                        set(key, value.parseItems(player)) {
                            if (mail.isAllowDeletion()) {
                                player.closeInventory()
                                mail.deleteToSql {
                                    if (it == 1) {
                                        data.delDraft(mail.unique)
                                        player.sendLang("玩家-删除草稿邮件-成功")
                                    }
                                }
                            }
                        }
                    }
                    // 返回
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            DraftBoxMenu(player, data, admin).openMenu()
                        }
                    }
                    // 模板保存
                    'A' -> {
                        if (admin) {
                            set(key, value.parseItems(player)) {
                                player.closeInventory()
                                player.sendLang("管理员-草稿邮件-保存模板确认")
                                player.nextChatInTick(400, { unid ->
                                    if (unid.equals("cancel", ignoreCase = true)) {
                                        player.cancelNextChat(false)
                                    } else if (unid.isNotEmpty()) {
                                        mail.deleteToSql {
                                            if (it == 1) {
                                                data.delDraft(mail.unique)
                                                TemplatePack(unid, mail).saveToFile()
                                                player.sendLang("玩家-删除草稿邮件-成功")
                                            }
                                        }
                                    }
                                })
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
                                data.delDraft(mail.unique)
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