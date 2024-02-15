package me.neon.mail.hook

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 19:56
 */
class HookMoney: HookPlugin() {

    private var economy: Economy? = null

    override fun getImpl(): HookMoney? {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.let {
                this.economy = it.provider
            }
        }
        return if (economy != null) this else null
    }

    fun giveMoney(player: Player, amt: Double) {
        economy?.depositPlayer(player, amt)
    }
    fun giveMoney(player: OfflinePlayer, amt: Double) {
        economy?.depositPlayer(player, amt)
    }

    fun takeMoney(player: Player, amt: Double) {
        economy?.withdrawPlayer(player, amt)
    }

    fun hasMoney(player: Player, amt: Double): Boolean {
        return economy?.has(player, amt) ?: false
    }

    fun hasTakeMoney(player: Player, amt: Double): Boolean {
        return if (hasMoney(player, amt)) {
            takeMoney(player, amt)
            true
        } else false
    }


}