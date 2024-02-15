package me.neon.mail.libs.taboolib.command

import me.neon.mail.libs.NeonLibsLoader
import net.kyori.adventure.text.Component
import me.neon.mail.libs.taboolib.command.component.CommandBase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TranslatableComponent
import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimplePluginManager
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import java.lang.reflect.Constructor

/**
 * NeonMail
 * me.neon.mail.libs.command
 *
 * @author 老廖
 * @since 2024/2/14 23:25
 */
object CommandLoader {

    private var isSupportedUnknownCommand = false

    private val registeredCommands = ArrayList<CommandStructure>()

    private val commandMap: SimpleCommandMap by lazy {
        val field = SimplePluginManager::class.java.getDeclaredField("commandMap")
        field.isAccessible = true
        field.get(Bukkit.getPluginManager()) as SimpleCommandMap
    }

    private val knownCommands by lazy {
        commandMap.getProperty<MutableMap<String, Command>>("knownCommands")!!
    }

    private val constructor: Constructor<PluginCommand> by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java).also {
            it.isAccessible = true
        }
    }

    fun registerCommand(
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
        commandBuilder: CommandBase.() -> Unit,
    ) {
        val pluginCommand = constructor.newInstance(command.name, NeonLibsLoader.pluginId)
        pluginCommand.setExecutor { sender, _, label, args ->
            executor.execute(sender, command, label, args)
        }
        pluginCommand.setTabCompleter { sender, _, label, args ->
            completer.execute(sender, command, label, args) ?: emptyList()
        }
        val permission = command.permission.ifEmpty { "${NeonLibsLoader.pluginId.name.lowercase()}.command.${command.name}.use" }
        // 修改属性
        pluginCommand.setProperty("description", command.description.ifEmpty { command.name })
        pluginCommand.setProperty("usageMessage", command.usage)
        pluginCommand.setProperty("aliases", command.aliases)
        pluginCommand.setProperty("activeAliases", command.aliases)
        pluginCommand.setProperty("permission", permission)

        val permissionMessage = command.permissionMessage.ifEmpty { "§c你没有权限使用..." }
        try {
            pluginCommand.setProperty("permissionMessage", permissionMessage)
        } catch (ex: Exception) {
            pluginCommand.setProperty("permissionMessage", Component.text(permission))
        }

        // 注册权限
        fun registerPermission(permission: String, default: PermissionDefault) {
            if (Bukkit.getPluginManager().getPermission(permission) == null) {
                try {
                    val p = Permission(permission, org.bukkit.permissions.PermissionDefault.values()[default.ordinal])
                    Bukkit.getPluginManager().addPermission(p)
                    Bukkit.getPluginManager().recalculatePermissionDefaults(p)
                    p.recalculatePermissibles()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
        registerPermission(permission, command.permissionDefault)

        command.permissionChildren.forEach {
            registerPermission(it.key, it.value)
        }
        // 注册命令
        val map = commandMap.knownCommands
        map.remove(command.name)
        map["${NeonLibsLoader.pluginId.name.lowercase()}:${pluginCommand.name}"] = pluginCommand
        map[pluginCommand.name] = pluginCommand
        pluginCommand.aliases.forEach {
            map[it] = pluginCommand
        }
        pluginCommand.register(commandMap)

        kotlin.runCatching {
            if (pluginCommand.getProperty<Any>("timings") == null) {
                val timingsManager = Class.forName("co.aikar.timings.TimingsManager")
                pluginCommand.setProperty("timings", timingsManager.invokeMethod("getCommandTiming", NeonLibsLoader.pluginId.name, pluginCommand))
            }
        }

    }
    fun unregisterCommand(command: String) {
        knownCommands.remove(command)
        sync()
    }

    fun unregisterCommands() {
        registeredCommands.forEach {
            unregisterCommand(it.name)
            it.aliases.forEach { a -> unregisterCommand(a) }
        }
        sync()
        registeredCommands.clear()
    }
    fun unknownCommand(sender: CommandSender, command: String, state: Int) {
        when (state) {
            1 -> sender.spigot().sendMessage(TranslatableComponent("command.unknown.command").also {
                it.color = ChatColor.RED
            })
            2 -> sender.spigot().sendMessage(TranslatableComponent("command.unknown.argument").also {
                it.color = ChatColor.RED
            })
            else -> return
        }
        val components = ArrayList<BaseComponent>()
        components += TextComponent(command).also {
            it.color = ChatColor.GRAY
        }
        components += TranslatableComponent("command.context.here").also {
            it.color = ChatColor.RED
            it.isItalic = true
        }
        sender.spigot().sendMessage(*components.toTypedArray())
    }

    private fun sync() {
        // 1.13 sync commands
        kotlin.runCatching {
            Bukkit.getServer().invokeMethod<Void>("syncCommands")
            Bukkit.getOnlinePlayers().forEach { it.updateCommands() }
            isSupportedUnknownCommand = true
        }
    }

}