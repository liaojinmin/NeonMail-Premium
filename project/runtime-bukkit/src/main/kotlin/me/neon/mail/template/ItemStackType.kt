package me.neon.mail.template

import me.neon.mail.api.io.YamlDsl
import me.neon.mail.api.io.parseItemStack
import me.neon.mail.api.io.parseItemStackToString
import me.neon.mail.api.template.TemplateType
import org.bukkit.inventory.ItemStack

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:09
 */
class ItemStackType(
    override val data: List<ItemStack>
): TemplateType<List<ItemStack>> {

    override fun applyYaml(dls: YamlDsl) {
        dls.apply {
            "itemStack" to parseItemStackToString(false, *data.toTypedArray())
        }
    }

    override fun createNewInstance(da: Any?): TemplateType<List<ItemStack>> {
        if (da != null) {
            return when (da) {
                is List<*> -> {
                    ItemStackType(da.map { parseItemStack(it.toString()) })
                }
                is String -> {
                    ItemStackType(listOf(parseItemStack(da)))
                }
                else -> {
                    error("未知模板附件种类种类 -> $da")
                }
            }
        }
        return this
    }
}