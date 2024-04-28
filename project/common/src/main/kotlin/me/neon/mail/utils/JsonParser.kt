package me.neon.mail.utils

import com.google.gson.JsonElement
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * NeonMail
 * me.neon.mail.utils
 *
 * @author 老廖
 * @since 2024/3/12 12:10
 */
object JsonParser {

    fun parseString(json: String): JsonElement {
        return parseReader(StringReader(json))
    }

    /**
     * Parses the specified JSON string into a parse tree
     *
     * @param reader JSON text
     * @return a parse tree of [JsonElement]s corresponding to the specified JSON
     * @throws JsonParseException if the specified text is not valid JSON
     */
    fun parseReader(reader: Reader): JsonElement {
        return try {
            val jsonReader = JsonReader(reader)
            val element = parseReader(jsonReader)
            if (!element.isJsonNull && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonSyntaxException("Did not consume the entire document.")
            }
            element
        } catch (e: MalformedJsonException) {
            throw JsonSyntaxException(e)
        } catch (e: IOException) {
            throw JsonIOException(e)
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    /**
     * Returns the next value from the JSON stream as a parse tree.
     *
     * @throws JsonParseException if there is an IOException or if the specified
     * text is not valid JSON
     */
    fun parseReader(reader: JsonReader): JsonElement {
        val lenient = reader.isLenient
        reader.isLenient = true
        return try {
            Streams.parse(reader)
        } catch (e: StackOverflowError) {
            throw JsonParseException("Failed parsing JSON source: $reader to Json", e)
        } catch (e: OutOfMemoryError) {
            throw JsonParseException("Failed parsing JSON source: $reader to Json", e)
        } finally {
            reader.isLenient = lenient
        }
    }
}