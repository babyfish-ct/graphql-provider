package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers
import org.babyfish.graphql.provider.kimmer.Immutable

class ImmutableDeserializers: Deserializers.Base() {

    override fun findBeanDeserializer(
        type: JavaType,
        config: DeserializationConfig,
        beanDesc: BeanDescription
    ): JsonDeserializer<*>? =
        type
            .takeIf { Immutable::class.java.isAssignableFrom(type!!.rawClass) }
            ?.let {
                ImmutableDeserializer(it.rawClass as Class<out Immutable>)
            }
}