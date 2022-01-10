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
        if (type === null) {
            throw JsonMappingException(jp, "Cannot deserialize the object whose type is '${rawClass.name}'")
        }
        val node: JsonNode = jp.codec.readTree(jp)
        return new(type.draftInfo.syncType.kotlin) {
            for (prop in type.props.values) {
                if (node.has(prop.name)) {
                    val value = if (prop.isList) {
                        ctx.readTreeAsValue(
                            node[prop.name],
                            CollectionType.construct(
                                List::class.java,
                                null,
                                null,
                                null,
                                SimpleType.constructUnsafe(prop.targetType!!.kotlinType.java)
                            )
                        )
                    } else {
                        ctx.readTreeAsValue(node[prop.name], prop.returnType.java)
                    }
                    Draft.set(this, prop, value)
                }
            }
        }
    }
}