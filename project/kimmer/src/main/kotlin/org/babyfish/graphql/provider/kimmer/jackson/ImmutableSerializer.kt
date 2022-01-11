package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType

class ImmutableSerializer : StdSerializer<Immutable>(Immutable::class.java) {

    override fun serialize(
        value: Immutable,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val type = ImmutableType.fromInstance(value)
        gen.apply {
            writeStartObject()
            for (prop in type.props.values) {
                if (Immutable.isLoaded(value, prop)) {
                    writeObjectField(prop.name, Immutable.get(value, prop))
                }
            }
            writeEndObject()
        }
    }
}