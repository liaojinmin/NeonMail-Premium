package me.neon.mail.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.menu.MenuIcon
import me.neon.mail.utils.deserializeItemStacks
import me.neon.mail.utils.serializeItemStacks
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.compat.depositBalance
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.getEmptySlot
import taboolib.platform.util.giveItem
import taboolib.platform.util.sendLang
import java.lang.reflect.Type
import java.util.*


/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/2 17:43
 */
class IMailDefaultImpl(
    override val uuid: UUID = UUID.randomUUID(),
    override val sender: UUID,
    override val target: UUID,
    override var data: DataTypeNormal = DataTypeNormal()
): IMailAbstract<DataTypeNormal>() {


    override val permission: String = "mail.extend.normal"
    override val mailType: String = "混合邮件"
    override val mainIcon: XMaterial = XMaterial.DIAMOND
    override val plugin: String = pluginId


    override fun checkClaimCondition(player: ProxyPlayer): Boolean {
        if (data.itemStacks.isEmpty()) return true
        val bukkitPlayer = (player as BukkitPlayer).player
        val air = bukkitPlayer.getEmptySlot()
        if (air < data.itemStacks.size) {
            bukkitPlayer.sendLang("玩家-没有足够背包格子", data.itemStacks.size-air)
            return false
        }
        return true
    }

    override fun giveAppendix(player: ProxyPlayer): Boolean {
        val bukkitPlayer = (player as BukkitPlayer).player
        if (data.itemStacks.isNotEmpty()) {
            bukkitPlayer.giveItem(data.itemStacks)
        }
        if (data.money > 0) {
            bukkitPlayer.depositBalance(data.money.toDouble())
        }
        if (data.points > 0) {
            TODO()
        }
        if (data.command.isNotEmpty()) {
            data.command.replacePlaceholder(bukkitPlayer).forEach { out ->
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), out)
                } catch (e: Exception) {
                    warning("执行附件指令时发生异常 -> ${data.command}")
                    e.printStackTrace()
                }
            }

        }
        return true
    }


    override fun parseMailIcon(icon: MenuIcon): ItemStack {
        val item = super.parseMailIcon(icon)
        if (MinecraftVersion.major >= 9) {
            val itemMeta = item.itemMeta
            try {
                if (itemMeta is org.bukkit.inventory.meta.BundleMeta) {
                    itemMeta.setItems(data.itemStacks.toList())
                }
            } catch (_: Exception) { }
        }
        return item
    }


    override fun getMailClassType(): Class<out IMailDefaultImpl> {
        return this::class.java
    }

    override fun getDataType(): Class<out DataTypeNormal> {
        return DataTypeNormal::class.java
    }

    override fun createData(): DataTypeNormal {
        return DataTypeNormal()
    }

    override fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailDataType): IMail<DataTypeNormal> {
        return IMailDefaultImpl(uuid, sender, target, data as DataTypeNormal)
    }

    override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): DataTypeNormal {
        val obj =  p0.asJsonObject
        val money = obj.get("money").asInt
        val points = obj.get("points").asInt
        val command = obj.get("command").asString.split(",").toMutableList()
        val itemStacks = obj.get("itemStacks").asString.deserializeItemStacks()
        return DataTypeNormal(money, points, command, itemStacks)
    }

    override fun serialize(p0: DataTypeNormal, p1: Type, p2: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("money", p0.money)
        obj.addProperty("points", p0.points)
        obj.addProperty("command", p0.command.joinToString(",", "", ""))
        obj.addProperty("itemStacks", p0.itemStacks.serializeItemStacks())
        return obj
    }




}