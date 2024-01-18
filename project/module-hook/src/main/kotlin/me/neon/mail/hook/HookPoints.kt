package me.neon.mail.hook


import org.bukkit.entity.Player

/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 19:56
 */
class HookPoints: HookPlugin() {

    private var pointsAPI: Points? = null

    override fun getImpl(): HookPoints? {
        if (checkHook("PlayerPoints") != null) {
            pointsAPI = PlayerPointsAPI()
        } else if (checkHook("GeekEconomy") != null) {
            pointsAPI = GeekEconomyAPI()
        }
        return if (pointsAPI != null) this else null
    }

    fun look(player: Player): Int {
        return pointsAPI?.look(player) ?: -1
    }

    fun add(player: Player, amount: Int): Boolean {
        return pointsAPI?.add(player, amount) ?: false
    }

    fun take(player: Player, amount: Int): Boolean {
        return pointsAPI?.take(player, amount) ?: false
    }

    fun set(player: Player, amount: Int): Boolean {
        return pointsAPI?.set(player, amount) ?: false
    }

    interface Points {
        fun look(player: Player): Int

        fun add(player: Player, amount: Int): Boolean

        fun take(player: Player, amount: Int): Boolean

        fun set(player: Player, amount: Int): Boolean
    }

    class GeekEconomyAPI: Points {
        private val api = me.geek.vault.api.DataManager
        override fun look(player: Player): Int {
            val data = api.getDataCache(player.uniqueId)
            if (data != null) {
                return data.getPlayerPoints()
            }
            return -1
        }

        override fun add(player: Player, amount: Int): Boolean {
            return api.getDataCache(player.uniqueId)?.givePlayerPoints(amount) ?: false
        }

        override fun take(player: Player, amount: Int): Boolean {
            return api.getDataCache(player.uniqueId)?.takePlayerPoints(amount) ?: false
        }

        override fun set(player: Player, amount: Int): Boolean {
            return api.getDataCache(player.uniqueId)?.setPlayerPoints(amount) ?: false
        }

    }

    class PlayerPointsAPI: Points {

        private val playerPointsAPI = org.black_ixx.playerpoints.PlayerPoints.getInstance().api
        override fun look(player: Player): Int {
            return playerPointsAPI.look(player.uniqueId)
        }

        override fun add(player: Player, amount: Int): Boolean {
            return playerPointsAPI.give(player.uniqueId, amount)
        }

        override fun take(player: Player, amount: Int): Boolean {
            return playerPointsAPI.take(player.uniqueId, amount)
        }

        override  fun set(player: Player, amount: Int): Boolean {
            return playerPointsAPI.set(player.uniqueId, amount)
        }
    }
}