package me.neon.mail.common.menu.edit

import me.neon.mail.IMailRegister
import me.neon.mail.ServiceManager.deleteToSql
import me.neon.mail.ServiceManager.updateToSql
import me.neon.mail.SetTings
import me.neon.mail.api.IMailDataType
import me.neon.mail.common.PlayerData
import me.neon.mail.api.DataTypeEmpty
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.common.menu.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.inputBook
import taboolib.platform.util.sendLang
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
    private val data: PlayerData,
    private val mail: MailDraftBuilder
): DraftEdite {

    private val menuData: MenuData = MenuLoader.draftMailEditeMenu


    override fun getInventory(): Inventory {
        return buildMenu<Linked<Pair<UUID, IMailDataType>>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements {
                val list = mutableListOf<Pair<UUID, IMailDataType>>()
                mail.targets.forEach { (t, u) ->
                    list.add(t to u)
                }
                list.add(IMailRegister.console to DataTypeEmpty())
                list
            }

            onGenerate { _, element, _, _ ->
                var icon = menuData.getCharMenuIcon('@')
                if (element.first == IMailRegister.console) {
                    icon = icon.subIcon ?: error("找不到子图标")
                }
                val itemBuilder = ItemBuilder(icon.mats)
                itemBuilder.name = icon.name.replacePlaceholder(player)
                itemBuilder.customModelData = icon.model
                if (element.first != IMailRegister.console) {
                    icon.lore.forEach {
                        if (it.contains("[info]")) {
                            // 取空格
                            val str = StringBuilder()
                            for (c in it) {
                                if (c != ' ') break
                                str.append(" ")
                            }
                            itemBuilder.lore.addAll(
                                element.second.getAppendixInfo(adaptPlayer(player), str.toString()).split(";")
                            )
                        } else {
                            itemBuilder.lore.add(it.replace("[player]", Bukkit.getOfflinePlayer(element.first).name ?: element.first.toString()))
                        }
                    }
                }
                itemBuilder.build()
            }

            onClick { _, element ->
                if (element.first == IMailRegister.console) {
                    PlayerListMenu(player, data, mail, this@DraftMailEditeMenu).openMenu()
                } else {
                    // 操作界面
                    // TODO("暂不考虑已编辑的附件删除")
                    MailAppEditeMenu(player, data, mail, element.first, element.second, this@DraftMailEditeMenu).openMenu()
                }
            }


            menuData.icon.forEach { (key, value) ->
                when (key) {
                    '1' -> {
                        set(key, value.parseItems(player, "[title]" to mail.title)) {
                            player.closeInventory()
                            player.sendLang("邮件-编辑操作-标题")
                            player.inputBook("Enter Email Subject", true, listOf(mail.title)) {
                                if (it.isNotEmpty()) {
                                    mail.title = it[0]
                                }
                                openMenu()
                            }
                        }
                    }
                    '2' -> {
                        set(key, value.parseItems(player, "[text]" to mail.context)) {
                            player.closeInventory()
                            player.sendLang("邮件-编辑操作-文本")
                            player.inputBook("Enter Email Text", true, mail.context) {
                                if (it.isNotEmpty()) {
                                    mail.context.clear()
                                    mail.context.addAll(it)
                                }
                                openMenu()
                            }
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
                                player.sendLang("玩家-草稿邮件-保存")
                            }
                        }
                    }
                    // 发送
                    'G' -> {
                        set(key, value.parseItems(player)) {
                            if (mail.isAllowSender()) {
                                player.closeInventory()
                                mail.senderMail()
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