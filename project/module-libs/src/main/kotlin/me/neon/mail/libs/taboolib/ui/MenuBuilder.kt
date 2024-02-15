package me.neon.mail.libs.taboolib.ui

import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack





/**
 * 构建一个菜单
 */
inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    return T::class.java.getDeclaredConstructor(String::class.java).newInstance(title).also(builder).build()
}

/**
 * 构建一个菜单并为玩家打开
 */
inline fun <reified T : Menu> HumanEntity.openMenu(title: String = "chest", builder: T.() -> Unit) {
    try {
        openMenu(buildMenu(title, builder))
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}

/**
 * 打开一个构建后的菜单
 */
fun HumanEntity.openMenu(buildMenu: Inventory) {
    openInventory(buildMenu)
}

/**
 * 获取当前点击事件下所有受影响的物品
 */
fun InventoryClickEvent.getAffectItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>()
    if (click == ClickType.NUMBER_KEY) {
        val hotbarButton = whoClicked.inventory.getItem(hotbarButton)
        if (hotbarButton != null && hotbarButton.type != Material.AIR) {
            items += hotbarButton
        }
    }
    if (currentItem?.type !=  Material.AIR) {
        items += currentItem!!
    }
    return items
}