package me.neon.mail.libs.taboolib.ui.type


import org.bukkit.inventory.ItemStack
import me.neon.mail.libs.taboolib.ui.ClickEvent

class ActionClick : Action() {

    override fun getCursor(e: ClickEvent): ItemStack {
        return e.clicker.itemOnCursor
    }

    override fun setCursor(e: ClickEvent, item: ItemStack?) {
        e.clicker.setItemOnCursor(item)
    }

    override fun getCurrentSlot(e: ClickEvent): Int {
        return e.rawSlot
    }
}