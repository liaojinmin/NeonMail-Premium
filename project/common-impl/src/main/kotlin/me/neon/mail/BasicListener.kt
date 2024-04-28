package me.neon.mail


import me.neon.mail.service.ServiceManager
import me.neon.mail.service.ServiceManager.waitDTO
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

/**
 * NeonMail-Premium
 * me.neon.mail.listener
 *
 * @author 老廖
 * @since 2024/1/2 22:22
 */
object BasicListener {

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        event.player.waitDTO()
    }

    @SubscribeEvent
    fun quit(event: PlayerQuitEvent) {
        ServiceManager.savePlayerData(event.player.uniqueId, true)
    }
}
