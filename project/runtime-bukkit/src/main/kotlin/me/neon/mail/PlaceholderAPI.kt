package me.neon.mail

import me.neon.mail.ServiceManager.getPlayerData
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

class PlaceholderAPI : PlaceholderExpansion {

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