package me.neon.mail.common

import me.neon.mail.SetTings
import me.neon.mail.api.IMailAbstract
import me.neon.mail.api.IMailDataType
import me.neon.mail.common.menu.MenuIcon
import me.neon.mail.common.menu.edit.ItemEditMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyPlayer
import taboolib.module.kether.isInt
import taboolib.module.nms.getName
import taboolib.module.ui.ClickEvent
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.inputBook
import taboolib.platform.util.nextChatInTick
import taboolib.platform.util.sendLang
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 18:55
 */
data class DataTypeNormal(
    var money: Int = 0,
    var points: Int = 0,
    var command: MutableList<String> = mutableListOf(),
    var itemStacks: CopyOnWriteArrayList<ItemStack> = CopyOnWriteArrayList()
): IMailDataType {

    override fun hasAppendix(): Boolean {
        return money != 0 || points != 0 || command.isNotEmpty() || itemStacks.isNotEmpty()
    }

    override fun getAppendixInfo(player: ProxyPlayer, pad: String): String {
        val text = IMailAbstract.appendixCache.computeIfAbsent(player.uniqueId) {
            val str: StringBuilder = StringBuilder()
            var bs = 0
            var index = 0
            if (money > 0) {
                str.append(pad).append(
                    SetTings.mailDisAppend
                    .replace("{0}", "金币")
                    .replace("{1}", (money).toString()))
                bs++
            }
            if (points > 0) {
                str.append(pad).append(
                    SetTings.mailDisAppend
                    .replace("{0}", "点券")
                    .replace("{1}", (points).toString()))
                bs++
            }
            if (command.isNotEmpty()) {
                str.append(pad).append(
                    SetTings.mailDisAppend
                    .replace("{0}", "指令包")
                    .replace("{1}", (command.size).toString()))
                bs++
            }
            if (itemStacks.isNotEmpty()) {
                for (stack in itemStacks) {
                    val meta = stack.itemMeta
                    if (meta != null && bs < 6) {
                        val manes = stack.getName((player as BukkitPlayer).player)
                        if (manes != "NO_LOCALE") {
                            str.append(pad).append(
                                SetTings.mailDisAppend
                                    .replace("{0}", manes)
                                    .replace("{1}", (stack.amount).toString())
                            )
                        } else {
                            index++
                        }
                    }
                    bs++
                }
            }
            if (index > 0 || bs >= 6) {
                if ((bs - 6) > 0) {
                    str.append(pad).append(SetTings.mailDisMiss.replace("{0}", (index + (bs - 6)).toString()))
                }
            }
            str.toString()
        }
        return text
    }

    fun parseDataUpdateCallBack(
        icon: MenuIcon,
        player: Player,
        openCall: () -> Unit
    ): Pair<ItemStack, ClickEvent.() -> Unit> {
        return when (icon.char) {
            '1' -> {
                icon.parseItems(player, "[money]" to money) to {
                    // 修改金币数量
                    player.closeInventory()
                    player.sendLang("邮件-编辑操作-金币")
                    player.nextChatInTick(400, {
                        if (it.equals("cancel", ignoreCase = true)) {
                            player.cancelNextChat(false)
                        } else if (it.isInt()) {
                            money = it.toIntOrNull() ?: 0
                            openCall.invoke()
                        } else {
                            player.sendLang("邮件-编辑操作-输入类型错误")
                        }
                    })
                }
            }
            '2' -> {
                icon.parseItems(player, "[points]" to points) to {
                    // 修改点券数量
                    player.closeInventory()
                    player.sendLang("邮件-编辑操作-点券")
                    player.nextChatInTick(400, {
                        if (it.equals("cancel", ignoreCase = true)) {
                            player.cancelNextChat(false)
                        } else if (it.isInt()) {
                            points = it.toIntOrNull() ?: 0
                            openCall.invoke()
                        } else {
                            player.sendLang("邮件-编辑操作-输入类型错误")
                        }
                    })
                }
            }
            '3' -> {
                // 非管理员不显示这个图标
                if (player.hasPermission("mail.edit.admin")) {
                    icon.parseItems(player, "[command]" to command) to {
                        // 修改指令
                        player.closeInventory()
                        player.sendLang("邮件-编辑操作-指令")
                        player.inputBook("Input the required command.", true, command) {
                            if (it.isNotEmpty()) {
                                command.clear()
                                command.addAll(it)
                            }
                            openCall.invoke()
                        }
                    }
                } else ItemStack(Material.AIR) to {}
            }
            '4' -> {
                icon.parseItems(player, "[itemStacks]" to parseItemInfo(player)) to {
                    // 修改物品
                    ItemEditMenu(player, openCall, this@DataTypeNormal).openMenu()
                }
            }
            else -> {
                icon.parseItems(player) to {
                    icon.eval(player)
                }
            }
        }
    }

    private fun parseItemInfo(player: Player): List<String> {
        val list = mutableListOf<String>()
        if (itemStacks.isNotEmpty()) {
            for (stack in itemStacks) {
                val meta = stack.itemMeta
                if (meta != null) {
                    val manes = stack.getName(player)
                    list.add(SetTings.mailDisAppend
                        .replace("{0}", manes)
                        .replace("{1}", (stack.amount).toString()))
                }
            }
        }
        return list
    }



}
