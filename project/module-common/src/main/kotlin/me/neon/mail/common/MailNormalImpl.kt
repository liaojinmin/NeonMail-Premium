package me.neon.mail.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.hook.ProviderRegister
import me.neon.mail.libs.taboolib.lang.sendLang
import me.neon.mail.libs.utils.*
import me.neon.mail.menu.MenuIcon
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type
import java.util.*


/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/2 17:43
 */
class MailNormalImpl(
    override val uuid: UUID = UUID.randomUUID(),
    override val sender: UUID,
    override val target: UUID,
    override var data: DataTypeNormal = DataTypeNormal()
): IMailAbstract<DataTypeNormal>() {


    override val permission: String = "mail.extend.normal"
    override val mailType: String = NeonMailLoader.typeTranslate["混合邮件"] ?: "混合邮件"
    override val mainIcon: Material = Material.DIAMOND
    override val plugin: String = NeonMailLoader.plugin.name


    override fun checkClaimCondition(player: Player): Boolean {
        if (data.itemStacks.isEmpty()) return true

        val air = player.getEmptySlot()
        if (air < data.itemStacks.size) {
            player.sendLang("玩家-没有足够背包格子", data.itemStacks.size-air)
            return false
        }
        return true
    }

    override fun giveAppendix(player: Player): Boolean {
        if (data.itemStacks.isNotEmpty()) {
            player.giveItem(data.itemStacks)
        }
        if (data.money > 0) {
            ProviderRegister.money?.value?.giveMoney(player, data.money.toDouble())
        }
        if (data.points > 0) {
            ProviderRegister.points?.value?.add(player, data.points) ?: NeonMailLoader.plugin.logger.warning("找不到可用的点券实现系统...")
        }
        if (data.command.isNotEmpty()) {
            data.command.replacePlaceholder(player).forEach { out ->
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), out)
                } catch (e: Exception) {
                    NeonMailLoader.plugin.logger.warning("执行附件指令时发生异常 -> ${data.command}")
                    e.printStackTrace()
                }
            }
        }
        return true
    }


    override fun parseMailIcon(player: Player, icon: MenuIcon): ItemStack {
        val item = super.parseMailIcon(player, icon)
        if (NeonMailLoader.getUseBundle()) {
            val itemMeta = item.itemMeta
            try {
                if (itemMeta is org.bukkit.inventory.meta.BundleMeta) {
                    itemMeta.setItems(data.itemStacks.toList())
                }
            } catch (_: Exception) { }
        }
        return item
    }


    override fun getMailClassType(): Class<out MailNormalImpl> {
        return this::class.java
    }

    override fun getDataClassType(): Class<out DataTypeNormal> {
        return DataTypeNormal::class.java
    }

    override fun createData(): DataTypeNormal {
        return DataTypeNormal()
    }

    override fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailDataType): IMail<DataTypeNormal> {
        val obj = MailNormalImpl(uuid, sender, target, data as DataTypeNormal)
        obj.title = this.title
        obj.context = this.context
        obj.state = this.state
        obj.senderTimer = this.senderTimer
        obj.collectTimer = this.collectTimer
        return obj
    }

    override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): DataTypeNormal {
        val obj =  p0.asJsonObject
        val money = obj.get("money").asInt
        val points = obj.get("points").asInt
        val cmd = obj.get("command").asString
        val command = if (cmd.isNotEmpty()) cmd.split(",").toMutableList() else mutableListOf()
        val itemStacks = obj.get("itemStacks").asString.deserializeItemStacks()
        return DataTypeNormal(money, points, command, itemStacks)
    }

    override fun serialize(p0: DataTypeNormal, p1: Type, p2: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("money", p0.money)
        obj.addProperty("points", p0.points)
        obj.addProperty("command", if (p0.command.isEmpty()) "" else p0.command.joinToString(",", "", ""))
        obj.addProperty("itemStacks", p0.itemStacks.serializeItemStacks())
        return obj
    }




}