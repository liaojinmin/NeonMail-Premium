package me.neon.mail.common

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMailData
import me.neon.mail.api.mail.IMailState
import me.neon.mail.api.template.TemplateType
import me.neon.mail.api.menu.IDraftEdite
import me.neon.mail.api.menu.MenuIcon
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 16:27
 */
class DataTypeEmpty: IMailData {

    companion object {
        val air = ItemStack(Material.AIR)
    }

    override val sourceType: String = "Empty"

    override fun createNewInstance(): IMailData {
        return DataTypeEmpty()
    }

    override fun getAppendixInfo(player: Player?, pad: String, refresh: Boolean): String {
        return pad + ((NeonMailLoader.typeTranslate[IMailState.Text.state]) ?: IMailState.Text.state)
    }

    override fun checkClaimCondition(player: Player): Boolean {
        return false
    }

    override fun giveAppendix(player: Player): Boolean {
        return true
    }

    override fun parseTemplateToData(templateType: List<TemplateType<*>>): IMailData {
        return this
    }

    override fun parseDataToTemplate(): List<TemplateType<*>> {
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