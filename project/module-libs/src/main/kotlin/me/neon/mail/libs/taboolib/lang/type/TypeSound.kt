package me.neon.mail.libs.taboolib.lang.type

import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.playSoundResource
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


/**
 * TabooLib
 * me.neon.mail.libs.lang.gameside.TypeSound
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeSound : Type {

    lateinit var sound: String
    var volume = 1f
    var pitch = 1f
    var resource = false

    override fun init(source: Map<String, Any>) {
        sound = source["sound"].toString()
        volume = (source["volume"] ?: source["v"]).toString().toFloatOrNull() ?: 1f
        pitch = (source["pitch"] ?: source["p"]).toString().toFloatOrNull() ?: 1f
        resource = source["resource"].toString().trim().matches("^(1|true|yes)\$".toRegex())
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        if (sender is Player) {
            if (resource) {
                sender.playSoundResource(sender.location, sound, volume, pitch)
            } else {
                try {
                    println("播放音效: ${toString()}")
                    sender.playSound(sender.location, Sound.valueOf(sound.uppercase()), volume, pitch)
                } catch (ignored: IllegalArgumentException) {
                }
            }
        } else {
            sender.sendMessage(toString())
        }
    }

    override fun toString(): String {
        return "NodeSound(sound='$sound', volume=$volume, pitch=$pitch)"
    }
}