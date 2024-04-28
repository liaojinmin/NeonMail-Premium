package me.neon.mail.service.packet

import me.neon.mail.service.ServiceManager


/**
 * NeonMail-Premium
 * me.neon.mail.service.packet
 *
 * @author 老廖
 * @since 2024/1/12 23:11
 */
interface IPacket {

    fun getPacketIndexId(): Int

    fun senderPacket() {
        ServiceManager.channel.sendPacket(this)
    }
}