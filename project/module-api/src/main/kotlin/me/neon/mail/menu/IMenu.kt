package me.neon.mail.menu

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory


/**
 * NeonMail-Premium
 * me.neon.mail.common.menu
 *
 * @author 老廖
 * @since 2024/1/17 5:24
 */
interface IMenu {

    val player: Player

    val menuData: MenuData

    fun openMenu() {
        player.openInventory(getInventory())
    }

    fun getInventory(): Inventory

    fun <T> me.neon.mail.libs.taboolib.ui.type.Linked<T>.initMenu() {
        map(*menuData.layout)
        rows(menuData.layout.size)
        slots(menuData.getCharSlotIndex('@'))
    }
}