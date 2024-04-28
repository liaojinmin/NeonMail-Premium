package me.neon.mail.utils


import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList


fun parseItemStackToString(isFace: Boolean = true, vararg itemStacks: ItemStack): List<String> {
    return mutableListOf<String>().apply {
        itemStacks.forEach {
            if (isFace) {
                val builder = StringBuilder()
                builder.append("material:").append(it.type).append(",")
                builder.append("amount:").append(it.amount)
                it.itemMeta?.let { meta ->
                    if (meta.hasDisplayName()) {
                        builder.append(",").append("name:").append(meta.displayName)
                    }

                    meta.lore?.let { l1 ->
                        val b = StringBuilder()
                        l1.forEach { l2 ->
                            b.append(l2).append("\\n")
                        }
                        builder.append(",").append("lore:").append(b)
                    }

                    if ((meta as Damageable).hasDamage()) {
                        builder.append(",").append("data:").append((meta as Damageable).damage)
                    }

                    try {
                        if (meta.hasCustomModelData()) {
                            builder.append(",").append("modelData:").append(meta.customModelData)
                        }
                    } catch (ignored: NoSuchMethodException) {
                    }
                }
                add(builder.toString())
            } else {
                add(it.serializeItemStacks())
            }
        }
    }
}

fun parseItemStack(input: String): ItemStack {
    if (!input.contains("material")) {
        return input.deserializeItemStack() ?: error("无法反序列化物品 源: $input")
    }
    val parts = input.split(",").associate {
        val (key, value) = it.split(":")
        key.trim() to value.trim()
    }
    val materialString = parts["material"]?.uppercase() ?: error("无法解析物品 -> $input")
    return buildItem(Material.valueOf(materialString)) {

        amount = parts["amount"]?.toIntOrNull() ?: 1

        parts["name"]?.let {
            name = it.colored()
        }

        parts["lore"]?.let {
            lore.addAll(it.split("\\n").colored())
        }

        parts["data"]?.let {
            this.damage = it.toIntOrNull() ?: 0
        }

        parts["modelData"]?.let {
            this.customModelData = it.toIntOrNull() ?: 0
        }
    }
}


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


