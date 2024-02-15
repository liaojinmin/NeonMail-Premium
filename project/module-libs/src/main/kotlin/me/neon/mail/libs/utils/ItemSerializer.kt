package me.neon.mail.libs.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList


/**
 * 序列化 ItemStack 为字符串
 */
fun ItemStack.serializeItemStacks(): String {
    val byteOutputStream = ByteArrayOutputStream()
    try {
        BukkitObjectOutputStream(byteOutputStream).use {
            it.writeInt(1)
            it.writeObject(serialize(this))
            return Base64Coder.encodeLines(byteOutputStream.toByteArray())
        }
    } catch (e: IOException) {
        throw IllegalArgumentException("无法序列化物品堆栈数据")
    }
}

/**
 * 序列化 ItemStack 数组 为字符串
 */
fun CopyOnWriteArrayList<ItemStack>?.serializeItemStacks(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    val byteOutputStream = ByteArrayOutputStream()
    try {
        BukkitObjectOutputStream(byteOutputStream).use {
            it.writeInt(this.size)
            for (items in this) {
                it.writeObject(serialize(items))
            }
            return Base64Coder.encodeLines(byteOutputStream.toByteArray())
        }
    } catch (e: IOException) {
        throw IllegalArgumentException("无法序列化物品堆栈数据")
    }
}

fun String.deserializeItemStack(): ItemStack? {
    ByteArrayInputStream(Base64Coder.decodeLines(this)).use {
        BukkitObjectInputStream(it).use { var2 ->
            var2.readInt()
            return deserialize(var2.readObject())
        }
    }
}

fun String.deserializeItemStacks(): CopyOnWriteArrayList<ItemStack> {
    if (this == "null" || this.isEmpty()) {
        return CopyOnWriteArrayList()
    }
    ByteArrayInputStream(Base64Coder.decodeLines(this)).use {
        BukkitObjectInputStream(it).use { var2 ->
            val contents = arrayOfNulls<ItemStack?>(var2.readInt())
            for ((index, _) in contents.withIndex()) {
                contents[index] = deserialize(var2.readObject())
            }
            return CopyOnWriteArrayList(contents.filterNotNull())
        }
    }
}



private fun deserialize(item: Any?): ItemStack? {
    return if (item != null) ItemStack.deserialize((item as Map<String, Any>)) else null
}

private fun serialize(item: ItemStack?): Map<String, Any>? {
    return item?.serialize()
}
