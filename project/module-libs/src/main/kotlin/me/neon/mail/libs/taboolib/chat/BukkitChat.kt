

package me.neon.mail.libs.taboolib.chat

import me.neon.mail.libs.NeonLibsLoader
import org.bukkit.Bukkit
import org.bukkit.entity.Player

    /**
     * 捕获玩家输入的消息
     */
    fun Player.nextChat(function: (message: String) -> Unit) {
        me.neon.mail.libs.taboolib.chat.ChatLoader.inputs[name] = function
    }

    /**
     * 捕获玩家输入的消息
     */
    fun Player.nextChat(function: (message: String) -> Unit, reuse: (player: Player) -> Unit = {}) {
        if (me.neon.mail.libs.taboolib.chat.ChatLoader.inputs.containsKey(name)) {
            reuse(this)
        } else {
            me.neon.mail.libs.taboolib.chat.ChatLoader.inputs[name] = function
        }
    }

    /**
     * 捕获玩家输入的消息（在一定时间内）
     */
    fun Player.nextChatInTick(tick: Long, func: (message: String) -> Unit, timeout: (player: Player) -> Unit = {}, reuse: (player: Player) -> Unit = {}) {
        if (me.neon.mail.libs.taboolib.chat.ChatLoader.inputs.containsKey(name)) {
            reuse(this)
        } else {
            me.neon.mail.libs.taboolib.chat.ChatLoader.inputs[name] = func

            Bukkit.getScheduler().runTaskLater(NeonLibsLoader.pluginId, Runnable {
                if (me.neon.mail.libs.taboolib.chat.ChatLoader.inputs.containsKey(name)) {
                    timeout(this@nextChatInTick)
                    me.neon.mail.libs.taboolib.chat.ChatLoader.inputs.remove(name)
                }
            }, tick)
        }
    }

    fun Player.cancelNextChat(execute: Boolean = true) {
        val listener = me.neon.mail.libs.taboolib.chat.ChatLoader.inputs.remove(name)
        if (listener != null && execute) {
            listener("")
        }
    }





