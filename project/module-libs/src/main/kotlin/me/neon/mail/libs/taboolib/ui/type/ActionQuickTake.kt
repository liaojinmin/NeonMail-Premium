package me.neon.mail.libs.taboolib.ui.type

import me.neon.mail.libs.taboolib.ui.ItemStacker
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import me.neon.mail.libs.taboolib.ui.ClickEvent


class ActionQuickTake : Action() {

    override fun getCursor(e: ClickEvent): ItemStack {
        return e.clicker.itemOnCursor
    }

    override fun setCursor(e: ClickEvent, item: ItemStack?) {
        if (item != null && item.type != Material.AIR) {
            ItemStacker.MINECRAFT.moveItemFromChest(item, e.clicker)
        }
        e.clicker.setItemOnCursor(null)
    }

    override fun getCurrentSlot(e: ClickEvent): Int {
        return e.rawSlot
    }
}