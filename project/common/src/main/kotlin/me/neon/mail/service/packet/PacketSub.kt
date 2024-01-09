package me.neon.mail.service.packet

import me.neon.mail.SetTings
import java.util.UUID

/**
 * GeekWorldField
 * me.geek.world.service.packet
 *
 * @author 老廖
 * @since 2023/12/13 12:58
 */
abstract class PacketSub: IPacket {

    private var builder: StringBuilder = java.lang.StringBuilder()

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
        return if (data == null) {
            if (builder.isEmpty())
                error("builder isEmpty")
            else
                "${getPacketIndexId()}" + DataPacket.packetSplit + SetTings.clusterId + DataPacket.headerSplit + builder.toString()
        } else {
            if (builder.isEmpty())
                "${getPacketIndexId()}" + DataPacket.packetSplit + SetTings.clusterId + DataPacket.headerSplit + data!!.key.toString(Charsets.UTF_8)
            else
                "${getPacketIndexId()}" + DataPacket.packetSplit + SetTings.clusterId + DataPacket.headerSplit + data!!.key.toString(Charsets.UTF_8) + builder.toString()
        }
    }


}