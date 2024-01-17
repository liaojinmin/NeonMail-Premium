package me.neon.mail.service.packet

import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.service.packet
 *
 * @author 老廖
 * @since 2024/1/12 23:11
 */
data class DataPacket(
    val key: ByteArray,
    val data: ByteArray,
    val timer: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataPacket

        if (!data.contentEquals(other.data)) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + key.contentHashCode()
        return result
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
