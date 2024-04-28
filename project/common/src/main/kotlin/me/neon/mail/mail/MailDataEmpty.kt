package me.neon.mail.mail


import me.neon.mail.Settings
import me.neon.mail.template.iTemplateType
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuIcon
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent

/**
 * NeonMail-Premium
 * me.neon.mail.mail
 *
 * @author 老廖
 * @since 2024/1/6 16:27
 */
class MailDataEmpty: IMailData {

    companion object {
        val air = ItemStack(Material.AIR)
    }

    override fun hasItemAppendix(): Boolean {
        return false
    }

    override fun getAllAppendixInfo(player: Player?, pad: String): String {
        return pad + ((Settings.typeTranslate["无"]) ?: "Not")
    }

    override fun isSuccessAppendix(player: Player): Boolean {
        return false
    }

    override fun giveAppendix(player: Player): Boolean {
        return true
    }

    override fun parseTemplateToData(templateType: List<iTemplateType<*>>): IMailData {
        return this
    }

    override fun parseDataToTemplate(): List<iTemplateType<*>> {
        return emptyList()
    }

    override fun parseCallBack(
        icon: MenuIcon,
        player: Player,
        builder: IDraftBuilder,
        edite: IDraftEdite
    ): Pair<ItemStack, ClickEvent.() -> Unit> {
        return air to {}
    }

    override fun hasAppendix(): Boolean {
        return false
    }

}