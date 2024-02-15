package me.neon.mail.libs.taboolib.lang.type

import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.asList
import me.neon.mail.libs.utils.replaceWithOrder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender


/**
 * TabooLib
 * me.neon.mail.libs.lang.type.TypeCommand
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeCommand : Type {

    var command: List<String>? = null

    override fun init(source: Map<String, Any>) {
        command = source["command"]?.asList()
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        command?.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it.replace("@p", sender.name).translate(sender, *args).replaceWithOrder(*args)) }
    }

    override fun toString(): String {
        return "TypeCommand(command='$command')"
    }
}