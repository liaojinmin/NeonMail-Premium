package me.neon.mail.template

import me.neon.mail.api.io.YamlDsl
import me.neon.mail.api.template.TemplateType

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:01
 */
class MoneyType(
    override val data: Double
): TemplateType<Double> {


    override fun applyYaml(dls: YamlDsl) {
        dls.apply {
            "money" to data
        }
    }

    override fun createNewInstance(da: Any?): TemplateType<Double> {
        if (da != null) {
            return MoneyType(da.toString().toDoubleOrNull() ?: 0.0)
        }
        error("无法解析模板种类 -> MoneyType")
    }

}