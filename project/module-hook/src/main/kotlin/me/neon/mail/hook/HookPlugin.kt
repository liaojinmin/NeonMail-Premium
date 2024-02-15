package me.neon.mail.hook

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 19:46
 */
abstract class HookPlugin {

    abstract fun getImpl(): HookPlugin?

    fun checkHook(plugin: String): Plugin? {
        return Bukkit.getPluginManager().getPlugin(plugin)?.also {
            Bukkit.getConsoleSender().sendMessage("§8[§bNeon§9Mail§8-§ePremium§8] §7软依赖 §f$plugin §7已兼容...")
        }
    }

}