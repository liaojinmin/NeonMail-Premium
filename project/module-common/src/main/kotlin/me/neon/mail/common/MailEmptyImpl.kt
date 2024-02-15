package me.neon.mail.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailDataType
import org.bukkit.Material
import org.bukkit.entity.Player
import java.lang.reflect.Type
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail.common
 *
 * @author 老廖
 * @since 2024/1/17 5:48
 */
class MailEmptyImpl(
    override val uuid: UUID = UUID.randomUUID(),
    override val sender: UUID,
    override val target: UUID,
    override var data: DataTypeEmpty = DataTypeEmpty()
): IMailAbstract<DataTypeEmpty>() {

    override val permission: String = "mail.extend.text"
    override val mainIcon: Material = Material.BOOK
    override val mailType: String = NeonMailLoader.typeTranslate["纯文本"] ?: "纯文本"
    override val plugin: String = NeonMailLoader.plugin.name

    override fun giveAppendix(player: Player): Boolean {
        return true
    }

    override fun getMailClassType(): Class<out IMail<DataTypeEmpty>> {
        return this::class.java
    }

    override fun getDataClassType(): Class<out DataTypeEmpty> {
        return DataTypeEmpty::class.java
    }

    override fun createData(): DataTypeEmpty {
        return DataTypeEmpty()
    }

    override fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailDataType): IMail<DataTypeEmpty> {
        return MailEmptyImpl(uuid, sender, target, if (data is DataTypeEmpty) data else DataTypeEmpty())
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): DataTypeEmpty {
        return createData()
    }

    override fun serialize(p0: DataTypeEmpty?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        return JsonObject()
    }

    override fun checkClaimCondition(player: Player): Boolean {
        return false
    }

}