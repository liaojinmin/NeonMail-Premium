package me.neon.mail.mail

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import me.neon.mail.Settings
import me.neon.mail.event.MailIconBuilderEvent
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuIcon
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getName
import taboolib.module.ui.ClickEvent
import taboolib.platform.util.buildItem
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 12:49
 */
interface IMail<T: IMailData>: JsonSerializer<T>, JsonDeserializer<T> {

    val unique: UUID

    val sender: UUID

    val target: UUID

    var title: String

    var context: String

    var state: MailState

    var senderTimer: Long

    var collectTimer: Long

    var data: T

    val permission: String

    val mainIcon: Material

    val translateType: String

    val plugin: String

    fun sendMail()

    /**
     * 发送到所有离线玩家
     */
    fun sendToOfflinePlayers()

    /**
     * 发送到所有在线玩家
     */
    fun sendToOnlinePlayers()

    /**
     * 克隆方法
     */
    fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailData): IMail<T>

    private fun parseIcon(player: Player, itemStack: ItemStack): ItemStack {
        if (data.hasItemAppendix() && itemStack.type.name.contains("BUNDLE")) {
            data.getItemAppendix()?.let {
                val meta = itemStack.itemMeta as org.bukkit.inventory.meta.BundleMeta
                meta.setItems(it)
                itemStack.itemMeta = meta
            }
        }
        val event = MailIconBuilderEvent(player, this, itemStack)
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return ItemStack(Material.AIR)
        }
        return event.itemStack
    }

    fun parseMailIcon(player: Player, itemStack: ItemStack): ItemStack {
        val item = buildItem(if (Settings.getUseBundle()) Material.valueOf("BUNDLE") else mainIcon) {
            name = itemStack.getName(player).replace("[title]", title)
            if (itemStack.hasItemMeta()) {
                val itemMeta = itemStack.itemMeta
                if (itemMeta.hasLore()) {
                    lore.addAll(parseMailInfo(itemMeta.lore ?: emptyList()))
                }
                runCatching {
                    customModelData = itemMeta.customModelData
                }
            }
            if (state == MailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
        return parseIcon(player, item)
    }


    fun parseMailIcon(player: Player, icon: MenuIcon): ItemStack {
        val item = buildItem(if (Settings.getUseBundle()) Material.valueOf("BUNDLE") else mainIcon) {
            name = icon.name.replace("[title]", title)
            lore.addAll(parseMailInfo(icon.lore))
            customModelData = icon.model
            if (state == MailState.NotObtained) {
                enchants[Enchantment.DAMAGE_ALL] = 1
            }
            hideAll()
        }
        return parseIcon(player, item)
    }

}