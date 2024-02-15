package me.neon.mail.libs.taboolib.lang.type


import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.replaceWithOrder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * TabooLib
 * me.neon.mail.libs.lang.type.TypeActionBar
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeActionBar : Type {

    lateinit var text: String

    override fun init(source: Map<String, Any>) {
        text = source["text"].toString()
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        if (sender is Player) {
            val newText = text.translate(sender, *args).replaceWithOrder(*args)
            sender.sendActionBar(newText)
        }
    }

    override fun toString(): String {
        return "NodeActionBar(text='$text')"
    }
}