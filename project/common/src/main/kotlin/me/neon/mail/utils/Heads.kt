package me.neon.mail.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submitAsync
import taboolib.library.xseries.XMaterial
import java.net.URL
import java.util.*

/**
 * @author Arasple
 * @date 2021/1/27 14:05
 */
object Heads {

    private val MOJANG_API = arrayOf(
        "https://api.mojang.com/users/profiles/minecraft/",
        "https://sessionserver.mojang.com/session/minecraft/profile/"
    )

    private val DEFAULT_HEAD = XMaterial.PLAYER_HEAD.parseItem()!!
    private val CACHED_PLAYER_TEXTURE = mutableMapOf<String, String?>()
    private val CACHED_SKULLS = mutableMapOf<String, ItemStack>()

    fun getHead(id: String): ItemStack {
        return if (id.length > 20) getCustomTextureHead(id)
        else getPlayerHead(id)
    }

    private fun getPlayerHead(name: String): ItemStack {
        return if (CACHED_SKULLS.containsKey(name)) {
            CACHED_SKULLS[name] ?: DEFAULT_HEAD
        } else {
            CACHED_SKULLS[name] = DEFAULT_HEAD.clone().also { item ->
                playerTexture(name) { modifyTexture(it, item) }
            }
            CACHED_SKULLS[name] ?: DEFAULT_HEAD
        }
    }

    private fun getCustomTextureHead(texture: String): ItemStack {
        return CACHED_SKULLS.computeIfAbsent(texture) {
            modifyTexture(texture, DEFAULT_HEAD.clone())
        }
    }


    /**
     * PRIVATE UTILS
     */
    @Suppress("DEPRECATION")
    private fun playerTexture(name: String, block: (String) -> Unit) {
        submitAsync {
            val profile = JsonParser().parse(fromURL("${MOJANG_API[0]}$name")) as? JsonObject
            if (profile == null) {
                console().sendMessage("§7[§3Texture§7] Texture player $name not found.")
                return@submitAsync
            }
            val uuid = profile["id"].asString
            (JsonParser().parse(fromURL("${MOJANG_API[1]}$uuid")) as JsonObject).getAsJsonArray("properties").forEach {
                if ("textures" == it.asJsonObject["name"].asString) {
                    CACHED_PLAYER_TEXTURE[name] = it.asJsonObject["value"].asString.also(block)
                }
            }
        }
    }

    private fun modifyTexture(input: String, itemStack: ItemStack): ItemStack {
        val meta = itemStack.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null)
        val field = meta.javaClass.getDeclaredField("profile")
        val texture = if (input.length in 60..100) encodeTexture(input) else input

        profile.properties.put("textures", Property("textures", texture, "TrMenu_TexturedSkull"))
        field.isAccessible = true
        field[meta] = profile
        itemStack.itemMeta = meta
        return itemStack
    }

    private fun encodeTexture(input: String): String {
        val encoder = Base64.getEncoder()
        return encoder.encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/$input\"}}}".toByteArray())
    }

    private fun fromURL(url: String): String {
        return try {
            String(URL(url).openStream().readBytes())
        } catch (t: Throwable) {
            ""
        }
    }

}