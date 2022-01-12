package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.SimpleType
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.new


class ImmutableDeserializer(type: Class<out Immutable>): StdDeserializer<Immutable>(type) {

    override fun deserialize(
        jp: JsonParser,
        ctx: DeserializationContext
    ): Immutable {

        val rawClass = handledType()
        val type = ImmutableType.fromAnyType(rawClass)
            ?: throw JsonMappingException(jp, "Cannot deserialize the object whose type is '${rawClass.name}'")
        val syncDraftType = type.draftInfo.syncType
            ?: throw JsonMappingException(jp, "The immutable type '${type.kotlinType.qualifiedName}' is abstract")

        val node: JsonNode = jp.codec.readTree(jp)

        return new(syncDraftType.kotlin) {
            for (prop in type.props.values) {
                if (node.has(prop.name)) {
                    val value = ctx.readTreeAsValue<Any>(node[prop.name], prop.jacksonType)
                    Draft.set(this, prop, value)
                }
            }
        }
    }
}