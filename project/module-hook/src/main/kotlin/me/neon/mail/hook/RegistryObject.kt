package me.neon.mail.hook

import org.bukkit.plugin.Plugin


/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 20:07
 */
class RegistryObject<T>(
    val plugin: Plugin,
    val value: T
)