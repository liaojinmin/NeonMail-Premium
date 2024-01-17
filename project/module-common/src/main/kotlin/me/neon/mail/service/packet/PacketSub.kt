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
abstract class PacketSub: IPacket {

    private var builder: StringBuilder = StringBuilder()

    private var start: Boolean = true

    fun writeInt(vars: Int): PacketSub {
        if (start) {
            start = false
            builder.append(vars)
        } else builder.append(DataPacket.packetSplit).append(vars)
        return this
    }

    fun writeString(vars: String): PacketSub {
        if (start) {
            start = false
            builder.append(vars)
        } else builder.append(DataPacket.packetSplit).append(vars)
        return this
    }

    fun writeUUID(vars: UUID): PacketSub {
        if (start) {
            start = false
            builder.append(vars)
        } else builder.append(DataPacket.packetSplit).append(vars)
        return this
    }

    // 1~server&data
    fun getMessageData(): String {
        val build = StringBuilder()
        if (data == null) {
            if (builder.isEmpty())
                error("builder isEmpty")
            else {
                build.append(getPacketIndexId())
                    .append(DataPacket.packetSplit)
                    .append(NeonMailLoader.clusterId)
                    .append(DataPacket.headerSplit)
                    .append(this.builder)
            }
        } else {
            if (builder.isEmpty()) {
                build.append(getPacketIndexId())
                    .append(DataPacket.packetSplit)
                    .append(NeonMailLoader.clusterId)
                    .append(DataPacket.headerSplit)
                    .append(data!!.key.toString(Charsets.UTF_8))
            } else {
                build.append(getPacketIndexId())
                    .append(DataPacket.packetSplit)
                    .append(NeonMailLoader.clusterId)
                    .append(DataPacket.headerSplit)
                    .append(data!!.key.toString(Charsets.UTF_8))
                    .append(builder.toString())
            }
        }
        return build.toString()
    }


}