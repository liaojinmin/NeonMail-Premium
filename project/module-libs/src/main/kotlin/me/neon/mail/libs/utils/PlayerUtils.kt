package me.neon.mail.libs.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * NeonMail
 * me.neon.mail.libs.utils
 *
 * @author 老廖
 * @since 2024/2/15 20:03
 */

fun Player.playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
    if (volume == -1f && pitch == -1f) {
        this.stopSound(sound)
        return
    }
    this.playSound(location, Sound.valueOf(sound.uppercase()), volume, pitch)
}
fun HumanEntity.getEmptySlot(hasEquipment: Boolean = false, isItemAmount: Boolean = false): Int {
    var air = 0
    for (itemStack in inventory.contents) {
        if (itemStack == null || itemStack.type == Material.AIR) { air++ }
    }
    if (hasEquipment) {
        if (inventory.itemInOffHand.type == Material.AIR) air--
        if (inventory.helmet == null) air--
        if (inventory.chestplate == null) air--
        if (inventory.leggings == null) air--
        if (inventory.boots == null) air--
    }
    return if (isItemAmount) air * 64 else air
}

fun HumanEntity.giveItem(itemStack: List<ItemStack>) {
    itemStack.forEach { giveItem(it) }
}

fun HumanEntity.giveItem(itemStack: ItemStack?, repeat: Int = 1) {
    if (itemStack != null && itemStack.type != Material.AIR) {
        // CraftInventory.addItem 的执行过程中, 实质上有可能修改ItemStack的amount, 如果不注意这一点, 则会吞物品而不自知
        val preAmount = itemStack.amount
        repeat(repeat) {
            inventory.addItem(itemStack).values.forEach { world.dropItem(location, it) }
            itemStack.amount = preAmount
        }
    }
}