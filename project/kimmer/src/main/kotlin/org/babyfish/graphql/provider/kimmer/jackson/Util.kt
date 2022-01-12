package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.SimpleType
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp

internal val ImmutableProp.jacksonType: JavaType
    get() = if (isList) {
        CollectionType.construct(
            List::class.java,
            null,
            null,
            null,
            SimpleType.constructUnsafe(targetType!!.kotlinType.java)
        )
    } else {
        SimpleType.constructUnsafe(returnType.java)
    }