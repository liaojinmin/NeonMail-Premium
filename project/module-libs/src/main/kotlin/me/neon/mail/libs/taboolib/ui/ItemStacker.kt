package me.neon.mail.libs.taboolib.ui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

/**
 * 物品背包合并工具
 *
 * @author 坏黑
 * @since 2019-02-07 23:53
 */
abstract class ItemStacker {
    abstract fun getMaxStackSize(itemStack: ItemStack): Int

    /**
     * 从箱子里移动物品到玩家背包
     * 如果溢出则丢弃
     *
     * @param item   物品
     * @param player 玩家
     */
    fun moveItemFromChest(item: ItemStack, player: Player) {
        val result = addItemAndMerge(item, player.inventory, ArrayList())
        if (result.countOut > 0) {
            item.amount = result.countOut
            if (!addItemAndSplit(item, player.inventory, 0, true)) {
                player.world.dropItem(player.location, item)
            }
        }
    }

    /**
     * 添加并拆分，但不合并
     * 返回值为是否添加完成
     *
     *
     *
     * @param item      物品
     * @param desc      快捷栏逆向添加，用于工作台拟真，会忽略 start 参数
     * @param inventory 背包
     * @param start     起始位置
     * @return boolean
     */
    @JvmOverloads
    fun addItemAndSplit(item: ItemStack, inventory: Inventory, start: Int, desc: Boolean = false): Boolean {
        val size = if (inventory is PlayerInventory || inventory is CraftingInventory) 36 else inventory.size
        if (desc) {
            // 8 ~ 0
            for (i in 8 downTo 0) {
                if (check(item, inventory, i)) {
                    return true
                }
            }
        }
        // 9 ~ 36
        for (i in (if (desc) start + 9 else start) until size) {
            if (check(item, inventory, i)) {
                return true
            }
        }
        return false
    }

    fun addItemFromChestToPlayer(item: ItemStack, inventory: Inventory): Boolean {
        for (i in 8 downTo 0) {
            val item2 = inventory.getItem(i)
            if (item2 == null || item2.type == Material.AIR) {
                if (item.amount > getMaxStackSize(item)) {
                    val itemClone = item.clone()
                    itemClone.amount = getMaxStackSize(item)
                    inventory.setItem(i, itemClone)
                    item.amount = item.amount - getMaxStackSize(item)
                } else {
                    val itemClone = item.clone()
                    itemClone.amount = item.amount
                    inventory.setItem(i, itemClone)
                    item.amount = 0
                    return true
                }
            }
        }
        for (i in 35 downTo 9) {
            val item2 = inventory.getItem(i)
            if (item2 == null || item2.type == Material.AIR) {
                if (item.amount > getMaxStackSize(item)) {
                    val itemClone = item.clone()
                    itemClone.amount = getMaxStackSize(item)
                    inventory.setItem(i, itemClone)
                    item.amount = item.amount - getMaxStackSize(item)
                } else {
                    val itemClone = item.clone()
                    itemClone.amount = item.amount
                    inventory.setItem(i, itemClone)
                    item.amount = 0
                    return true
                }
            }
        }
        return false
    }

    /**
     * 合并物品，不新增
     *
     * @param inventory 背包
     * @param item      物品
     * @param ignore    忽略位置
     * @return [AddResult]
     */
    fun addItemAndMerge(item: ItemStack, inventory: Inventory, ignore: List<Int?>): AddResult {
        var changed = false
        var count = item.amount
        val size = if (inventory is PlayerInventory || inventory is CraftingInventory) 36 else inventory.size
        for (i in 0 until size) {
            if (ignore.contains(i)) {
                continue
            }
            val inventoryItem = inventory.getItem(i)
            if (!item.isSimilar(inventoryItem)) {
                continue
            }
            while (count > 0 && inventoryItem!!.amount < getMaxStackSize(item)) {
                changed = true
                inventoryItem.amount = inventoryItem.amount + 1
                count--
            }
            if (count == 0) {
                return AddResult(count, changed)
            }
        }
        return AddResult(count, changed)
    }

    private fun check(item: ItemStack, inventory: Inventory, i: Int): Boolean {
        val item2 = inventory.getItem(i)
        if (item2 == null || item2.type == Material.AIR) {
            // 如果物品数量过多
            if (item.amount > getMaxStackSize(item)) {
                val itemClone = item.clone()
                itemClone.amount = getMaxStackSize(item)
                inventory.setItem(i, itemClone)
                item.amount = item.amount - getMaxStackSize(item)
            } else {
                inventory.setItem(i, item.clone())
                item.amount = 0
                return true
            }
        }
        return false
    }

    class AddResult(val countOut: Int, val isChanged: Boolean) {

        override fun toString(): String {
            return "AddResult{" +
                    "countOut=" + countOut +
                    ", changed=" + isChanged +
                    '}'
        }
    }

    companion object {
        val MINECRAFT: ItemStacker = object : ItemStacker() {
            override fun getMaxStackSize(itemStack: ItemStack): Int {
                return itemStack.maxStackSize
            }
        }
    }
}

