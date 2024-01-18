package me.neon.mail.hook

import org.bukkit.plugin.Plugin
import taboolib.platform.util.bukkitPlugin


/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 20:01
 */
class ProviderRegister<T> {

    private val cache = LinkedHashMap<Class<out T>, RegistryObject<T>>()
    fun <U: T> register(clazz: Class<out U>, plugin: Plugin, supplier: () -> U?): RegistryObject<U>? {
        val data: U = supplier.invoke() ?: return null
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(clazz) {
            RegistryObject(plugin, data)
        } as RegistryObject<U>
    }

    fun <U: T> get(clazz: Class<out U>): RegistryObject<U>? {
        @Suppress("UNCHECKED_CAST")
        return cache[clazz] as? RegistryObject<U>
    }

    fun <U: T> remove(clazz: Class<out U>): RegistryObject<U>? {
        @Suppress("UNCHECKED_CAST")
        return cache.remove(clazz) as? RegistryObject<U>
    }

    /**
     * 纯被动注册，首次调用时初始化
     */
    companion object {

        private val providerRegister: ProviderRegister<HookPlugin> = ProviderRegister()

        val points: RegistryObject<HookPoints>? = providerRegister.register(HookPoints::class.java, bukkitPlugin) { HookPoints().getImpl() }

        fun test(): RegistryObject<HookPoints>? {
            return providerRegister.get(HookPoints::class.java)
        }

    }


}