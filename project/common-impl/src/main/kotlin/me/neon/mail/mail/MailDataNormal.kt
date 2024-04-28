package me.neon.mail.mail

import me.neon.mail.Settings
import me.neon.mail.utils.syncRunner
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuIcon
import me.neon.mail.template.iTemplateType
import me.neon.mail.hook.ProviderRegister
import me.neon.mail.menu.edit.ItemEditMenu
import me.neon.mail.service.ServiceManager.updateToSql
import me.neon.mail.template.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.module.nms.getName
import taboolib.module.ui.ClickEvent
import taboolib.platform.compat.replacePlaceholder
import me.neon.mail.Settings.sendLang
import taboolib.platform.util.cancelNextChat
import taboolib.platform.util.getEmptySlot
import taboolib.platform.util.giveItem
import taboolib.platform.util.nextChatInTick
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 18:55
 */
data class MailDataNormal(
    var money: Int = 0,
    var points: Int = 0,
    var command: MutableList<String> = mutableListOf(),
    var itemStacks: CopyOnWriteArrayList<ItemStack> = CopyOnWriteArrayList()
): IMailData {

    override fun hasItemAppendix(): Boolean {
        return itemStacks.isNotEmpty()
    }

    override fun parseDataToTemplate(): List<iTemplateType<*>> {
        return mutableListOf<iTemplateType<*>>().apply {
            add(MoneyType(money.toDouble()))
            add(PointsType(points))
            add(CommandType(command.toList()))
            add(ItemStackType(itemStacks.toList()))
        }
    }

    override fun parseTemplateToData(templateType: List<iTemplateType<*>>): IMailData {
        val type = MailDataNormal()
        templateType.forEach {
            when (it) {
                is CommandType -> {
                    type.command.addAll(it.data)
                }
                is ItemStackType -> {
                    type.itemStacks.addAll(it.data)
                }
                is MoneyType -> {
                    type.money += it.data.toInt()
                }
                is PointsType -> {
                    type.points += it.data
                }
                else -> error("未知种类 -> ${it::class.java.`package`}")
            }
        }
        return type
    }

    override fun hasAppendix(): Boolean {
        return money != 0 || points != 0 || command.isNotEmpty() || itemStacks.isNotEmpty()
    }

    override fun isSuccessAppendix(player: Player): Boolean {
        if (itemStacks.isEmpty()) return true

        val air = player.getEmptySlot()
        if (air < itemStacks.size) {
            player.sendLang("玩家-没有足够背包格子", itemStacks.size-air)
            return false
        }
        return true
    }

    override fun getItemAppendix(): List<ItemStack>? {
        return itemStacks.toList()
    }

    override fun giveAppendix(player: Player): Boolean {
        if (itemStacks.isNotEmpty()) {
            player.giveItem(itemStacks)
        }
        if (money > 0) {
            ProviderRegister.money?.giveMoney(player, money.toDouble())
        }
        if (points > 0) {
            ProviderRegister.points?.add(player, points) ?: warning("找不到可用的点券实现系统...")
        }
        if (command.isNotEmpty()) {
            command.replacePlaceholder(player).forEach { out ->
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), out)
                } catch (e: Exception) {
                    warning("执行附件指令时发生异常 -> $command")
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    override fun getAllAppendixInfo(player: Player?, pad: String): String {
        val str: StringBuilder = StringBuilder()
        var bs = 0
        var index = 0
        if (money > 0) {
            str.append(pad).append(
                Settings.mailDisAppend
                    .replace("{0}", "金币")
                    .replace("{1}", (money).toString()))
            bs++
        }
        if (points > 0) {
            str.append(pad).append(
                Settings.mailDisAppend
                    .replace("{0}", "点券")
                    .replace("{1}", (points).toString()))
            bs++
        }
        if (command.isNotEmpty()) {
            if (command[0].isNotEmpty()) {
                str.append(pad).append(
                    Settings.mailDisAppend
                        .replace("{0}", "指令包")
                        .replace("{1}", (command.size).toString())
                )
                bs++
            } else {
                Settings.debug("异常的指令大小，其内容为空 ->${command[0]}<-")
                command.clear()
            }
        }
        if (itemStacks.isNotEmpty()) {
            for (stack in itemStacks) {
                if (bs < 8) {
                    val manes = stack.getName(player)
                    if (manes != "NO_LOCALE") {
                        str.append(pad).append(
                            Settings.mailDisAppend
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
                str.append(pad).append(Settings.mailDisMiss.replace("{0}", (index + (bs - 6)).toString()))
            }
        }
        return str.toString()
    }


    override fun parseCallBack(
        icon: MenuIcon,
        player: Player,
        builder: IDraftBuilder,
        edite: IDraftEdite
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
                        } else if (it.toIntOrNull() != null) {
                            val a = it.toDoubleOrNull() ?: 0.0
                            if (a <= 0) {
                                player.sendLang("邮件-编辑操作-输入类型错误")
                            } else {
                                if (edite.admin || ProviderRegister.money?.hasTakeMoney(player, a) == true) {
                                    money = a.toInt()
                                    syncRunner { edite.openMenu() }
                                    builder.updateToSql()
                                } else {
                                    player.sendLang("邮件-编辑操作-金币不足")
                                    syncRunner { edite.openMenu() }
                                }
                            }
                        } else {
                            player.sendLang("邮件-编辑操作-输入类型错误")
                        }
                    })
                }
            }
            '2' -> {
                // 如果没有点券提供者，这个图标不显示
                ProviderRegister.points?.let { api ->
                    icon.parseItems(player, "[points]" to points) to {
                        // 修改点券数量
                        player.closeInventory()
                        player.sendLang("邮件-编辑操作-点券")
                        player.nextChatInTick(400, {
                            if (it.equals("cancel", ignoreCase = true)) {
                                player.cancelNextChat(false)
                            } else if (it.toIntOrNull() != null) {
                                // 判断玩家点券是否足够
                                val a = it.toIntOrNull() ?: 0
                                if (a <= 0) {
                                    player.sendLang("邮件-编辑操作-输入类型错误")
                                } else {
                                    if (edite.admin || api.take(player, a)) {
                                        points = a
                                        syncRunner { edite.openMenu() }
                                        builder.updateToSql()
                                    } else {
                                        player.sendLang("邮件-编辑操作-点券不足")
                                        syncRunner { edite.openMenu() }
                                    }
                                }
                            } else {
                                player.sendLang("邮件-编辑操作-输入类型错误")
                            }
                        })
                    }
                } ?: (ItemStack(Material.AIR) to {})
            }
            '3' -> {
                // 非管理员不显示这个图标
                if (player.hasPermission("neonMail.command.box.admin")) {
                    icon.parseItems(player, "[command]" to command) to {
                        // 修改指令
                        player.closeInventory()
                        player.sendLang("邮件-编辑操作-指令")
                        player.nextChatInTick(800, {
                            if (it.equals("cancel", ignoreCase = true)) {
                                player.cancelNextChat(false)
                            } else {
                                if (it.isNotEmpty()) {
                                    command.clear()
                                    command.addAll(it.split(";"))
                                    builder.updateToSql()
                                }
                                edite.openMenu()
                            }
                        })
                    }
                } else ItemStack(Material.AIR) to {}
            }
            '4' -> {

                icon.parseItems(player, "[itemStacks]" to parseItemInfo(player)) to {
                    // 修改物品
                    ItemEditMenu(player, builder, itemStacks, edite).openMenu()
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
                    list.add(Settings.mailDisAppend
                        .replace("{0}", manes)
                        .replace("{1}", (stack.amount).toString())
                        // 剔除 ; 内置分割符，这里不需要
                        .replace(";", "")
                    )
                }
            }
        }
        return list
    }


}
