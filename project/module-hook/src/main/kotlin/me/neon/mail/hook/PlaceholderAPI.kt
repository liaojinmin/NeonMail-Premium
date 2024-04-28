package me.neon.mail.hook

import me.neon.mail.service.ServiceManager.getPlayerData
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PlaceholderAPI : PlaceholderExpansion {

    override val identifier: String = "nmp"

    override val autoReload: Boolean
        get() = true

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (args == "mail") {
            return player?.getPlayerData()?.mail ?: "OFF"
        }
        return "null"
    }

}