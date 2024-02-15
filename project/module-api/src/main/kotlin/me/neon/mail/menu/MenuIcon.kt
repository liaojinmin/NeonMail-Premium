package me.neon.mail.menu

import me.neon.mail.libs.utils.buildItem
import me.neon.mail.libs.utils.replacePlaceholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


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
        return buildItem(this.mats) {
            name = this@MenuIcon.name.replacePlaceholder(player)
            this@MenuIcon.lore.forEach {
                lore.addAll(data.map { a -> it
                    .replace(a.first, a.second.toString())
                })
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
