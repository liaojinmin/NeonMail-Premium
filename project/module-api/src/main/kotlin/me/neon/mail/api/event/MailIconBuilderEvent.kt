package me.neon.mail.api.event

import me.neon.mail.api.mail.IMail
import org.bukkit.inventory.ItemStack
import taboolib.platform.type.BukkitProxyEvent

/**
 * NeonMail-Premium
 * me.neon.mail.event
 *
 * @author 老廖
 * @since 2024/1/5 19:42
 */
class MailIconBuilderEvent(
    mail: IMail<*>,
    itemStack: ItemStack
): BukkitProxyEvent()