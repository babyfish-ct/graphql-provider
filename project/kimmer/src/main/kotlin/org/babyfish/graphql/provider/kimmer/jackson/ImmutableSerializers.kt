package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.ser.Serializers

class ImmutableSerializers: Serializers.Base() {

    override fun findSerializer(
        config: SerializationConfig?,
        type: JavaType?,
        beanDesc: BeanDescription?
    ): JsonSerializer<*>? {
        return null
    }
}