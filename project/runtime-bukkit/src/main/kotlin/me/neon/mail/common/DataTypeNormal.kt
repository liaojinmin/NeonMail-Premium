package me.neon.mail.common

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailData
import me.neon.mail.hook.ProviderRegister
import me.neon.mail.ServiceManager.updateToSql
import me.neon.mail.api.io.syncRunner
import me.neon.mail.api.menu.IDraftEdite
import me.neon.mail.api.menu.MenuIcon
import me.neon.mail.api.template.TemplateType
import me.neon.mail.menu.edit.ItemEditMenu
import me.neon.mail.template.CommandType
import me.neon.mail.template.ItemStackType
import me.neon.mail.template.MoneyType
import me.neon.mail.template.PointsType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.module.nms.getName
import taboolib.module.ui.ClickEvent
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.*
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
): IMailData {

    override val sourceType: String = "Normal"

    override fun createNewInstance(): IMailData {
        return DataTypeNormal()
    }

    override fun parseDataToTemplate(): List<TemplateType<*>> {
        return mutableListOf<TemplateType<*>>().apply {
            add(MoneyType(money.toDouble()))
            add(PointsType(points))
            add(CommandType(command.toList()))
            add(ItemStackType(itemStacks.toList()))
        }
    }
    override fun parseTemplateToData(templateType: List<TemplateType<*>>): IMailData {
        val type = DataTypeNormal()
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

    override fun checkClaimCondition(player: Player): Boolean {
        if (itemStacks.isEmpty()) return true

        val air = player.getEmptySlot()
        if (air < itemStacks.size) {
            player.sendLang("玩家-没有足够背包格子", itemStacks.size-air)
            return false
        }
        return true
    }

    override fun giveAppendix(player: Player): Boolean {
        if (itemStacks.isNotEmpty()) {
            player.giveItem(itemStacks)
        }
        if (money > 0) {
            ProviderRegister.money?.value?.giveMoney(player, money.toDouble())
        }
        if (points > 0) {
            ProviderRegister.points?.value?.add(player, points) ?: warning("找不到可用的点券实现系统...")
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

    private fun getAppendixInfo(pad: String): String {
        val str: StringBuilder = StringBuilder()
        var bs = 0
        var index = 0
        if (money > 0) {
            str.append(pad).append(
                NeonMailLoader.mailDisAppend
                    .replace("{0}", "金币")
                    .replace("{1}", (money).toString()))
            bs++
        }
        if (points > 0) {
            str.append(pad).append(
                NeonMailLoader.mailDisAppend
                    .replace("{0}", "点券")
                    .replace("{1}", (points).toString()))
            bs++
        }
        if (command.isNotEmpty()) {
            if (command[0].isNotEmpty()) {
                str.append(pad).append(
                    NeonMailLoader.mailDisAppend
                        .replace("{0}", "指令包")
                        .replace("{1}", (command.size).toString())
                )
                bs++
            } else {
                NeonMailLoader.debug("异常的指令大小，其内容为空 ->${command[0]}<-")
                command.clear()
            }
        }
        if (itemStacks.isNotEmpty()) {
            for (stack in itemStacks) {
                val meta = stack.itemMeta
                if (meta != null && bs < 6) {
                    val manes = stack.getName()
                    if (manes != "NO_LOCALE") {
                        str.append(pad).append(
                            NeonMailLoader.mailDisAppend
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
                str.append(pad).append(NeonMailLoader.mailDisMiss.replace("{0}", (index + (bs - 6)).toString()))
            }
        }
        return str.toString()
    }

    override fun getAppendixInfo(player: Player?, pad: String, refresh: Boolean): String {
        val text: String
        if (refresh) {
            text = getAppendixInfo(pad)
            if (player != null) {
                IMailAbstract.appendixCache[player.uniqueId] = text
            }
        } else {
            text = if (player != null) {
                IMailAbstract.appendixCache.computeIfAbsent(player.uniqueId) { getAppendixInfo(pad) }
            } else {
                getAppendixInfo(pad)
            }
        }
        return text
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
                                if (edite.admin || ProviderRegister.money?.value?.hasTakeMoney(player, a) == true) {
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
                                    if (edite.admin || api.value.take(player, a)) {
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
                    ItemEditMenu(player, builder, this@DataTypeNormal, edite).openMenu()
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
                    list.add(NeonMailLoader.mailDisAppend
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
