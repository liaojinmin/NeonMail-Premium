package me.neon.mail.menu.edit

import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuData
import me.neon.mail.menu.MenuType
import me.neon.mail.menu.MenuLoader
import me.neon.mail.service.ServiceManager.updateToSql
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickType
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.compat.replacePlaceholder

/**
 * NeonMail
 * me.neon.mail.menu.edit
 *
 * @author 老廖
 * @since 2024/2/18 23:00
 */
class ItemEditMenu(
    private val player: Player,
    private val builders: IDraftBuilder,
    private val itemList: MutableList<ItemStack>,
    private val edite: IDraftEdite?,
) {

    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.ItemEdite)

    fun openMenu() {
        player.openInventory(getInventory())
    }

    private fun getInventory(): Inventory {
        return buildMenu<PageableChest<ItemStack>>(
            menuData.title.replacePlaceholder(player)
        ) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { itemList }

            onGenerate { _, element, _, _ ->  element }

            menuLocked(false)

            onClick(false)

            onClick(false) {
               // info("onClick")
                if (it.clickType == ClickType.CLICK) {
                    val bukkitEvent = it.clickEvent()
                    bukkitEvent.clickedInventory?.let { a ->
                        if (bukkitEvent.isShiftClick) {
                            it.isCancelled = true
                        }
                        if (a.type != InventoryType.PLAYER) {
                            if (it.slot != '@') {
                                it.isCancelled = true
                            }
                        }
                    }
                } else {
                    if (it.slot != '@') {
                        it.isCancelled = true
                    }
                }
            }

            onClose {
                var update = false
                if (itemList.isNotEmpty()) {
                    update = true
                    itemList.clear()
                }
                menuData.getCharSlotIndex('@').forEach { index ->
                    val item = it.inventory.getItem(index)
                    if (item != null && item.type != Material.AIR) {
                        update = true
                        itemList.add(item)
                    }
                }
                if (update) builders.updateToSql()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            isCancelled = true
                            edite?.openMenu()
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