package me.neon.mail.template

import me.neon.mail.api.io.YamlDsl
import me.neon.mail.api.template.TemplateType

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:03
 */
class PointsType(
    override val data: Int
): TemplateType<Int> {

    override fun applyYaml(dls: YamlDsl) {
        dls.apply {
            "points" to data
        }
    }

    override fun createNewInstance(da: Any?): TemplateType<Int> {
        if (da != null) {
            return PointsType(da.toString().toIntOrNull() ?: 0)
        }
        error("无法解析模板种类 -> PointsType")
    }

}