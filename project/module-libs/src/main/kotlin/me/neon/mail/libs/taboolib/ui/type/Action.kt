package me.neon.mail.libs.taboolib.ui.type

import org.bukkit.inventory.ItemStack
import me.neon.mail.libs.taboolib.ui.ClickEvent


abstract class Action {

    abstract fun getCursor(e: ClickEvent): ItemStack?

    abstract fun setCursor(e: ClickEvent, item: ItemStack?)

    abstract fun getCurrentSlot(e: ClickEvent): Int
}