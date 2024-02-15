package me.neon.mail.libs.taboolib.ui

import me.neon.mail.libs.NeonLibsLoader
import me.neon.mail.libs.utils.io.asyncRunner
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import java.util.*
import java.util.stream.IntStream
import kotlin.math.cos
import kotlin.math.sin


internal class ClickListener: Listener {

    fun onDisable() {
        Bukkit.getOnlinePlayers().forEach {
            if (MenuHolder.fromInventory(it.openInventory.topInventory) != null) {
                it.closeInventory()
            }
        }
    }

    @EventHandler
    fun onOpen(e: InventoryOpenEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        // 构建回调
        Bukkit.getScheduler().runTask(NeonLibsLoader.pluginId, Runnable {
            builder.buildCallback(e.player as Player, e.inventory)
            builder.selfBuildCallback(e.player as Player, e.inventory)
        })
        // 异步构建回调
        asyncRunner {
            builder.asyncBuildCallback(e.player as Player, e.inventory)
            builder.selfAsyncBuildCallback(e.player as Player, e.inventory)
        }
    }


    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        // 锁定主手
        if (builder.handLocked && (e.rawSlot - e.inventory.size - 27 == e.whoClicked.inventory.heldItemSlot || e.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.hotbarButton == e.whoClicked.inventory.heldItemSlot)) {
            e.isCancelled = true
        }
        // 处理事件
        try {
            val event = ClickEvent(e, ClickType.CLICK, builder.getSlot(e.rawSlot), builder)
            builder.clickCallback.forEach { it(event) }
            builder.selfClickCallback(event)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        // 如果事件取消则不处理后续逻辑
        if (e.isCancelled) {
            return
        }
        // 丢弃逻辑
        if ((e.currentItem?.type != Material.AIR) && e.click == org.bukkit.event.inventory.ClickType.DROP) {
            val item = itemDrop(e.whoClicked as Player, e.currentItem)
            item.pickupDelay = 20
            item.setMetadata("internal-drop", FixedMetadataValue(NeonLibsLoader.pluginId, true))
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.currentItem?.type = Material.AIR
                e.currentItem = null
            }
        } else if (e.cursor?.type != Material.AIR && e.rawSlot == -999) {
            val item = itemDrop(e.whoClicked as Player, e.cursor)
            item.pickupDelay = 20
            item.setMetadata("internal-drop", FixedMetadataValue(NeonLibsLoader.pluginId, true))
            val event = PlayerDropItemEvent((e.whoClicked as Player), item)
            Bukkit.getPluginManager().callEvent(event)
            if (event.isCancelled) {
                event.itemDrop.remove()
            } else {
                e.view.cursor?.type = Material.AIR
                e.view.cursor = null
            }
        }
    }

    @EventHandler
    fun onDrag(e: InventoryDragEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) ?: return
        val clickEvent = ClickEvent(e, ClickType.DRAG, ' ', menu)
        menu.clickCallback.forEach { it.invoke(clickEvent) }
        menu.selfClickCallback(clickEvent)
    }


    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val menu = MenuHolder.fromInventory(e.inventory) ?: return
        // 标题更新 && 跳过关闭回调
        if (menu.isUpdateTitle && menu.skipCloseCallbackOnUpdateTitle) {
            return
        }
        menu.closeCallback.invoke(e)
        // 只触发一次
        if (menu.onceCloseCallback) {
            menu.closeCallback = {}
        }
    }


    @EventHandler
    fun onDropItem(e: PlayerDropItemEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked && !e.itemDrop.hasMetadata("internal-drop")) {
            e.isCancelled = true
        }
    }


    @EventHandler
    fun onItemHeld(e: PlayerItemHeldEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }


    @EventHandler
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        val builder = MenuHolder.fromInventory(e.player.openInventory.topInventory) ?: return
        if (builder.handLocked) {
            e.isCancelled = true
        }
    }

    fun itemDrop(player: Player, itemStack: ItemStack?, bulletSpread: Double = 0.0, radius: Double = 0.4): Item {
        val location = player.location.add(0.0, 1.5, 0.0)
        val item = player.world.dropItem(location, itemStack!!)
        val yaw = Math.toRadians((-player.location.yaw - 90.0f).toDouble())
        val pitch = Math.toRadians(-player.location.pitch.toDouble())
        val x: Double
        val y: Double
        val z: Double
        val v = cos(pitch) * cos(yaw)
        val v1 = -sin(yaw) * cos(pitch)
        if (bulletSpread > 0.0) {
            val spread = doubleArrayOf(1.0, 1.0, 1.0)
            IntStream.range(0, 3)
                .forEach { t: Int -> spread[t] = (Random().nextDouble() - Random().nextDouble()) * bulletSpread * 0.1 }
            x = v + spread[0]
            y = sin(pitch) + spread[1]
            z = v1 + spread[2]
        } else {
            x = v
            y = sin(pitch)
            z = v1
        }
        val dirVel = Vector(x, y, z)
        item.velocity = dirVel.normalize().multiply(radius)
        return item
    }
}