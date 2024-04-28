package me.neon.mail.menu

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem


/**
 * NeonMail-Premium
 * me.neon.mail.menu
 *
 * @author 老廖
 * @since 2024/1/4 13:42
 */
data class MenuIcon(
    val char: Char,
    val mats: Material,
    val model: Int,
    val name: String,
    val lore: List<String>,
    var action: String,
    var subIcon: MenuIcon? = null
) {

    fun parseItems(player: Player): ItemStack {
        return buildItem(this.mats) {
            name = this@MenuIcon.name.replacePlaceholder(player)
            lore.addAll(this@MenuIcon.lore.replacePlaceholder(player))
            customModelData = model
            hideAll()
        }
    }

    fun parseItems(player: Player, vararg data: Pair<String, *>): ItemStack {
        fun parseData(it: String): String {
            return if (data.isNotEmpty()) {
                var text = it
                data.forEach { a ->
                    text = text.replace(a.first, a.second.toString())
                }
                text
            } else it
        }
        return buildItem(this.mats) {
            name = parseData(this@MenuIcon.name.replacePlaceholder(player))
            this@MenuIcon.lore.forEach {
                lore.add(parseData(it))
            }
            customModelData = model
            hideAll()
        }
    }
    fun parseItems(player: Player, pair: Pair<String, List<String>>): ItemStack {
        return buildItem(this.mats) {
            name = this@MenuIcon.name.replacePlaceholder(player)
            this@MenuIcon.lore.forEach {
                if (it.contains(pair.first)) {
                    lore.addAll(pair.second)
                } else {
                    lore.add(it)
                }
            }
            customModelData = model
            hideAll()
        }
    }

    fun parseItems(player: Player, lores: List<String>): ItemStack {
        return buildItem(this.mats) {
            name = this@MenuIcon.name.replacePlaceholder(player)
            lore.addAll(lores.replacePlaceholder(player))
            customModelData = model
            hideAll()
        }
    }

    fun eval(player: Player) {

    }




}
