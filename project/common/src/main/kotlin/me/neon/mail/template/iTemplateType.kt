package me.neon.mail.template

import me.neon.mail.utils.YamlDsl

/**
 * NeonMail
 * me.neon.mail.template
 *
 * @author 老廖
 * @since 2024/2/18 20:00
 */
interface iTemplateType<T> {

    val data: T

    /**
     * 创建新的模板附件实例
     * @param da 可是 String、Int、List<String>....等[org.bukkit.configuration.ConfigurationSection]可储存的基本类型
     * @return [iTemplateType]
     */
    fun createNewInstance(da: Any?): iTemplateType<T>

    /**
     * 将本模板附件解析为[YamlDsl]配置字节
     * @param dls yaml实例，需要将数据写入到这里
     */
    fun applyYaml(dls: YamlDsl)

}