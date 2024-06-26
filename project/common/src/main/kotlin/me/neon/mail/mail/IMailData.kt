package me.neon.mail.mail

import me.neon.mail.template.iTemplateType
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuIcon
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent

/**
 * NeonMail-Premium
 * me.neon.mail.api
 *
 * @author 老廖
 * @since 2024/1/6 12:27
 */
interface IMailData {

    /**
     * 解析附件详细，如果有缓存优先取缓存
     *
     * @param player 需要解析的玩家
     * @param pad 前缀内容
     */
    fun getAllAppendixInfo(player: Player? = null, pad: String = ""): String

    /**
     * 是否拥有物品附件
     */
    fun hasItemAppendix(): Boolean

    /**
     * 获取物品类型的附件，如果有1的话
     */
    fun getItemAppendix(): List<ItemStack>? = null

    /**
     * 是否拥有实质性的附件
     */
    fun hasAppendix(): Boolean

    /**
     * 检查玩家是否满足附件领取条件
     */
    fun isSuccessAppendix(player: Player): Boolean

    /**
     * 为玩家领取附件
     */
    fun giveAppendix(player: Player): Boolean

    /**
     * 将模板解析为附件
     *
     * @param templateType 需要解析的模板数据
     */
    fun parseTemplateToData(templateType: List<iTemplateType<*>>): IMailData

    /**
     * 将数据类解析为模板数据
     */
    fun parseDataToTemplate(): List<iTemplateType<*>>

    /**
     * 解析界面按钮的点击回调
     * 一般用于自定义附件类型的增删改查功能
     *
     * @param icon 按钮对象
     * @param player 目标玩家
     * @param builder 草稿箱
     * @param edite 编辑菜单
     */
    fun parseCallBack(icon: MenuIcon, player: Player, builder: IDraftBuilder, edite: IDraftEdite): Pair<ItemStack, ClickEvent.() -> Unit>

}