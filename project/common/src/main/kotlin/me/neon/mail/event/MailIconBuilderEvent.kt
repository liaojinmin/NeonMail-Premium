package me.neon.mail.event

import me.neon.mail.mail.IMail
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

/**
 * NeonMail-Premium
 * me.neon.mail.event
 *
 * @author 老廖
 * @since 2024/1/5 19:42
 */
class MailIconBuilderEvent(
    player: Player,
    val mail: IMail<*>,
    val itemStack: ItemStack
): PlayerEvent(player), Cancellable {

    private var cancel = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(p0: Boolean) {
        cancel = p0
    }


    companion object {

        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }

    }

}


