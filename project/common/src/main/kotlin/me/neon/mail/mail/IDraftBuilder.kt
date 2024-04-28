package me.neon.mail.mail

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonReader
import java.io.StringReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * NeonMail-Premium
 * me.neon.mail.api.mail
 *
 * @author 老廖
 * @since 2024/1/22 5:38
 */
interface IDraftBuilder {

    val sender: UUID

    val type: String

    val unique: UUID

    var title: String

    val context: MutableList<String>

    fun createNewInstance(
        sender: UUID, type: String,
        unique: UUID = UUID.randomUUID(),
        title: String = "not title",
        context: MutableList<String> = mutableListOf("not context"),
        vararg arg: Any
    ): IDraftBuilder

    fun checkGlobalModel(): Boolean

    fun senderMail()

    fun addTarget(uuid: UUID, dataType: IMailData)

    fun getTargets(): Map<UUID, IMailData>

    fun getTarget(uuid: UUID): IMailData?

    fun changeGlobalModel(): IMailData

    fun isAllowSender(): Boolean

    fun isAllowDeletion(): Boolean

    companion object {

        fun serialize(builder: Map<UUID, IMailData>): ByteArray {
            return MailRegister.getGsonBuilder().toJson(builder).toByteArray()
        }

        fun deserialize(data: ByteArray, type: Class<out IMailData>): ConcurrentHashMap<UUID, IMailData> {
            val gson = MailRegister.getGsonBuilder()
            val map = ConcurrentHashMap<UUID, IMailData>()
            val reader = JsonReader(StringReader(String(data, Charsets.UTF_8)))
            val lenient = reader.isLenient
            reader.isLenient = true
            try {
                val obj = Streams.parse(reader) as JsonObject
                obj.entrySet().forEach {
                    map[UUID.fromString(it.key)] = gson.fromJson(it.value, type)
                }
            } catch (e: StackOverflowError) {
                throw JsonParseException("Failed parsing JSON source: $reader to Json", e)
            } catch (e: OutOfMemoryError) {
                throw JsonParseException("Failed parsing JSON source: $reader to Json", e)
            } finally {
                reader.isLenient = lenient
            }
            return map
        }
    }

}