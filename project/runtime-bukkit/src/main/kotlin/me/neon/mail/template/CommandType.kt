package me.neon.mail.template

import me.neon.mail.api.io.YamlDsl
import me.neon.mail.api.template.TemplateType

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:06
 */
class CommandType(
    override val data: List<String>
): TemplateType<List<String>> {


    override fun applyYaml(dls: YamlDsl) {
        dls.apply {
            "command" to data
        }
    }

    override fun createNewInstance(da: Any?): TemplateType<List<String>> {
        if (da != null) {
            return when (da) {
                is List<*> -> {
                    CommandType(da.map { it.toString() })
                }
                is String -> {
                    CommandType(listOf(da))
                }
                else -> error("")
            }
        }
        return this
    }
}