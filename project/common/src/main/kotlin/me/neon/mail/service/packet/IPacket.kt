package me.neon.mail.service.packet

import me.neon.mail.ServiceManager

/**
 * GeekWorldField
 * me.geek.world.service.packet
 *
 * @author 老廖
 * @since 2023/12/12 21:55
 */
interface IPacket {

    val data: DataPacket?

    fun getTargetChannel(): String


    fun getPacketIndexId(): Int

    fun sender() {
        ServiceManager.channel.sendPacket(this)
    }
}