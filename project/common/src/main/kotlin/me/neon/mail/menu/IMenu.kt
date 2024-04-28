package me.neon.mail.menu

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.type.PageableChest


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

    fun <T> PageableChest<T>.initMenu() {
        map(*menuData.layout)
        rows(menuData.layout.size)
        slots(menuData.getCharSlotIndex('@'))
    }
}