package me.neon.mail.common

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.hook.ProviderRegister
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuData
import me.neon.mail.menu.MenuIcon
import me.neon.mail.menu.MenuLoader
import me.neon.mail.service.ServiceManager.updateToSql
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.submit
import taboolib.module.kether.isInt
import taboolib.module.nms.getName
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.compat.withdrawBalance
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

    override fun getAppendixInfo(player: ProxyPlayer, pad: String, refresh: Boolean): String {
        NeonMailLoader.debug("getAppendixInfo ${player.displayName}")
        fun parse(): String {
            val str: StringBuilder = StringBuilder()
            var bs = 0
            var index = 0
            if (money > 0) {
                NeonMailLoader.debug("解析金币值变量 -> $money")
                str.append(pad).append(
                    NeonMailLoader.mailDisAppend
                        .replace("{0}", "金币")
                        .replace("{1}", (money).toString()))
                bs++
            }
            if (points > 0) {
                NeonMailLoader.debug("解析点券值变量 -> $points")
                str.append(pad).append(
                    NeonMailLoader.mailDisAppend
                        .replace("{0}", "点券")
                        .replace("{1}", (points).toString()))
                bs++
            }
            if (command.isNotEmpty()) {
                if (command[0].isNotEmpty()) {
                    NeonMailLoader.debug("解析指令值变量 -> $command")
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
                        val manes = stack.getName((player as BukkitPlayer).player)
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
        val text: String
        if (refresh) {
            text = parse()
            IMailAbstract.appendixCache[player.uniqueId] = text
        } else {
            text = IMailAbstract.appendixCache.computeIfAbsent(player.uniqueId) { parse() }
        }
        return text
    }

    fun parseDataUpdateCallBack(
        icon: MenuIcon,
        player: Player,
        builder: MailDraftBuilder,
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
                        } else if (it.isInt()) {
                            val a = it.toDoubleOrNull() ?: 0.0
                            if (a <= 0) {
                                player.sendLang("邮件-编辑操作-输入类型错误")
                            } else {
                                if (edite.admin || player.withdrawBalance(a).transactionSuccess()) {
                                    money = a.toInt()
                                    submit { edite.openMenu() }
                                    builder.updateToSql()
                                } else {
                                    player.sendLang("邮件-编辑操作-金币不足")
                                    submit { edite.openMenu() }
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
                            } else if (it.isInt()) {
                                // 判断玩家点券是否足够
                                val a = it.toIntOrNull() ?: 0
                                if (a <= 0) {
                                    player.sendLang("邮件-编辑操作-输入类型错误")
                                } else {
                                    if (edite.admin || api.value.take(player, a)) {
                                        points = a
                                        submit { edite.openMenu() }
                                        builder.updateToSql()
                                    } else {
                                        player.sendLang("邮件-编辑操作-点券不足")
                                        submit { edite.openMenu() }
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
                if (player.hasPermission("mail.edit.admin")) {
                    icon.parseItems(player, "[command]" to command) to {
                        // 修改指令
                        player.closeInventory()
                        player.sendLang("邮件-编辑操作-指令")
                        player.inputBook("Input the required command.", true, command) {
                            if (it.isNotEmpty()) {
                                command.clear()
                                command.addAll(it)
                                builder.updateToSql()
                            }
                            edite.openMenu()
                        }
                    }
                } else ItemStack(Material.AIR) to {}
            }
            '4' -> {
                icon.parseItems(player, "[itemStacks]" to parseItemInfo(player)) to {
                    // 修改物品
                    MailItemEditMenu(player, builder,this@DataTypeNormal, edite).openMenu()
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
                        .replace("{1}", (stack.amount).toString()))
                }
            }
        }
        return list
    }


    // TODO: 这里需要不锁定玩家点击 
    class MailItemEditMenu(
        private val player: Player,
        private val builders: MailDraftBuilder,
        private val type: IMailDataType,
        private val edite: IDraftEdite,
    ) {

        private val menuData: MenuData = MenuLoader.itemEditeMenu

        fun openMenu() {
            player.openInventory(getInventory())
        }

        private fun getInventory(): Inventory {
            return buildMenu<Linked<ItemStack>>(
                menuData.title.replacePlaceholder(player)
            ) {
                map(*menuData.layout)
                rows(menuData.layout.size)
                slots(menuData.getCharSlotIndex('@'))
                elements {
                    // TODO("标记，未验证玩家取走物品后列表是否更新")
                    if (type is DataTypeNormal) {
                        type.itemStacks
                    } else emptyList()
                }

                onGenerate { _, element, _, _ ->  element }

                onClick(false)

                menuData.icon.forEach { (key, value) ->
                    when (key) {
                        'B' -> {
                            set(key, value.parseItems(player)) {
                                isCancelled = true
                                edite.openMenu()
                                builders.updateToSql()
                            }
                        }
                        else -> {
                            if (key != '@') {
                                set(key, value.parseItems(player)) {
                                    isCancelled = true
                                    value.eval(player)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
