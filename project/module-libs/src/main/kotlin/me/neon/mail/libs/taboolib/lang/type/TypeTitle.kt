package me.neon.mail.libs.taboolib.lang.type


import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.replaceWithOrder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * TabooLib
 * me.neon.mail.libs.lang.gameside.TypeTitle
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeTitle : Type {

    var title: String? = null
    var subtitle: String? = null
    var fadein = 0
    var stay = 20
    var fadeout = 0

    override fun init(source: Map<String, Any>) {
        title = source["title"].toString()
        subtitle = source["subtitle"].toString()
        fadein = source["fadein"].toString().toIntOrNull() ?: 0
        stay = source["stay"].toString().toIntOrNull() ?: 20
        fadeout = source["fadeout"].toString().toIntOrNull() ?: 0
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        if (sender is Player) {
            val title = title?.translate(sender, *args)?.replaceWithOrder(*args) ?: ""
            val subtitle = subtitle?.translate(sender, *args)?.replaceWithOrder(*args) ?: ""
            sender.sendTitle(title, subtitle, fadein, stay, fadeout)
        }
    }

    override fun toString(): String {
        return "NodeTitle(title=$title, subtitle=$subtitle, fadein=$fadein, stay=$stay, fadeout=$fadeout)"
    }
}