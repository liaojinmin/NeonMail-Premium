package me.neon.mail.service.packet

import me.neon.mail.NeonMailLoader
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.service.packet
 *
 * @author 老廖
 * @since 2024/1/12 23:12
 */
abstract class AbstractPacket: IPacket {

    private var builder: StringBuilder = StringBuilder()

    private var start: Boolean = true

    fun writeInt(vars: Int): AbstractPacket {
        if (start) {
            start = false
            builder.append(vars)
        } else {
            builder.append(packetSplit).append(vars)
        }
        return this
    }

    fun writeString(vars: String): AbstractPacket {
        if (start) {
            start = false
            builder.append(vars)
        } else {
            builder.append(packetSplit).append(vars)
        }
        return this
    }

    fun writeUUID(vars: UUID): AbstractPacket {
        if (start) {
            start = false
            builder.append(vars)
        } else {
            builder.append(packetSplit).append(vars)
        }
        return this
    }

    // 1~server&data
    fun getMessageData(): String {
        val build = StringBuilder()
        if (builder.isEmpty())
            error("builder isEmpty")
        else {
            build.append(getPacketIndexId())
                .append(packetSplit)
                .append(NeonMailLoader.clusterId)
                .append(headerSplit)
                .append(this.builder)
        }
        return build.toString()
    }

    companion object {
        fun parseByteArrayKey(obj: String): ByteArray {
            return "mail:$obj".toByteArray(Charsets.UTF_8)
        }
        fun parseByteArrayKey(obj: UUID): ByteArray {
            return "mail:$obj".toByteArray(Charsets.UTF_8)
        }
        fun parseStringKey(obj: String): String {
            return "mail:$obj"
        }
        fun parseStringKey(obj: UUID): String {
            return "mail:$obj"
        }

        const val packetSplit: String = "~"

        const val headerSplit: String = "&"
    }


}