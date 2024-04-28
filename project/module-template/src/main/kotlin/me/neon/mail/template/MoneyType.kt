package me.neon.mail.template

import me.neon.mail.utils.YamlDsl

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:01
 */
class MoneyType(
    override val data: Double
): iTemplateType<Double> {


    override fun applyYaml(dls: YamlDsl) {
        dls.apply {
            "money" to data
        }
    }

    override fun createNewInstance(da: Any?): iTemplateType<Double> {
        if (da != null) {
            return MoneyType(da.toString().toDoubleOrNull() ?: 0.0)
        }
        error("无法解析模板种类 -> MoneyType")
    }

}