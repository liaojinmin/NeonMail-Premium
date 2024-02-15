package me.neon.mail.libs

import me.neon.mail.libs.taboolib.chat.ChatListener
import me.neon.mail.libs.taboolib.command.CommandLoader
import me.neon.mail.libs.taboolib.command.SimpleCommandRegister
import me.neon.mail.libs.taboolib.lang.LangLoader
import me.neon.mail.libs.taboolib.ui.ClickListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File

/**
 * NeonMail
 * me.neon.mail.libs
 *
 * @author 老廖
 * @since 2024/2/14 18:50
 */
interface NeonLibsLoader {

    companion object {

        private lateinit var uiListener: ClickListener
        private lateinit var chatListener: ChatListener

        lateinit var loader: NeonLibsLoader
            private set

        lateinit var pluginId: Plugin
            private set

    }


    fun getPluginFile(): File

    fun loaderLibs(plugin: Plugin) {
        pluginId = plugin
        loader = this

        // ui
        uiListener = ClickListener()
        Bukkit.getPluginManager().registerEvents(uiListener, plugin)

        // lang&chat
        LangLoader.onLoader()
        chatListener = ChatListener()
        Bukkit.getPluginManager().registerEvents(chatListener, plugin)

    }

    fun registerCommand(clazz: Class<*>) {
        val simpleCommandRegister = SimpleCommandRegister()
        simpleCommandRegister.visit(clazz)
    }

    fun unLoaderLibs() {
        uiListener.onDisable()

        // command
        CommandLoader.unregisterCommands()
    }


}