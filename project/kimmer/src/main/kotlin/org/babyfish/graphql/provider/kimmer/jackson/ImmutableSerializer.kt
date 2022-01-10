package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi

class ImmutableSerializer : StdSerializer<Immutable>(Immutable::class.java) {

    override fun serialize(value: Immutable, gen: JsonGenerator, provider: SerializerProvider) {
        val spi = value as ImmutableSpi
        gen.apply {
            writeStartObject()

            writeEndObject()
        }
    }
}