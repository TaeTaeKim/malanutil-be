package taeyun.malanalter.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class CommaDelimitedStringToListDeserializer : StdDeserializer<List<String>>(List::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<String> {
        // Read the JSON value as a single String
        val commaSeparatedString = p.readValueAs(String::class.java)

        // Split the string by comma, remove leading/trailing whitespace, and filter out empty items
        return commaSeparatedString
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}