package me.neon.mail.libs.taboolib.chat

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * NeonMail
 * me.neon.mail.libs.chat
 *
 * @author 老廖
 * @since 2024/2/14 19:22
 */
internal class ChatListener: Listener {

    @EventHandler
    fun e(e: PlayerQuitEvent) {
        ChatLoader.inputs.remove(e.player.name)
    }

    @EventHandler
    fun e(e: AsyncPlayerChatEvent) {
        if (ChatLoader.inputs.containsKey(e.player.name)) {
            ChatLoader.inputs.remove(e.player.name)?.invoke(e.message)
            e.isCancelled = true
        }
    }
}