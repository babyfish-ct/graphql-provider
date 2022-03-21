package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ModelPropImpl(
    declaringType: ModelTypeImpl,
    kotlinProp: KProperty1<*, *>
): EntityPropImpl(declaringType, kotlinProp), ModelProp {

    override val cache: Cache
        get() = Cache(CacheLevel.NO_CACHE)

    override val filter: Filter?
        get() = null

    override val userImplementation: UserImplementation?
        get() = null

    override val targetType: ModelType?
        get() = super.targetType as ModelType?

    override val targetRawClass: KClass<*>
        get() = super.targetType?.kotlinType ?: super.returnType
}