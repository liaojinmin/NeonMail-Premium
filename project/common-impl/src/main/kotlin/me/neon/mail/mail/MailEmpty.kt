package me.neon.mail.mail

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import me.neon.mail.Settings
import org.bukkit.Material
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.pluginId
import java.lang.reflect.Type
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail.common
 *
 * @author 老廖
 * @since 2024/1/17 5:48
 */
class MailEmpty(
    override val unique: UUID = UUID.randomUUID(),
    override val sender: UUID,
    override val target: UUID,
    override var data: MailDataEmpty = MailDataEmpty()
): IMailAbstract<MailDataEmpty>() {


    override val mainIcon: Material = Material.BOOK

    override val translateType: String = Settings.typeTranslate["纯文本"] ?: "纯文本"

    override val plugin: String = pluginId


    override fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailData): IMail<MailDataEmpty> {
        return MailEmpty(uuid, sender, target, if (data is MailDataEmpty) data else MailDataEmpty())
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): MailDataEmpty {
        return MailDataEmpty()
    }

    override fun serialize(p0: MailDataEmpty?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        return JsonObject()
    }

    companion object {

        @Awake(LifeCycle.ENABLE)
        private fun onEnable() = MailRegister.register(
            MailEmpty(
                MailRegister.console,
                MailRegister.console,
                MailRegister.console
            )
        )
    }


}