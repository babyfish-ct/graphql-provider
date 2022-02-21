package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import kotlin.reflect.KProperty1

internal class ModelPropImpl(
    declaringType: ModelTypeImpl,
    kotlinProp: KProperty1<*, *>
): EntityPropImpl(declaringType, kotlinProp), ModelProp {

    override val cache: Cache
        get() = TODO("Not yet implemented")

    override val filter: Filter?
        get() = TODO("Not yet implemented")

    override val userImplementation: UserImplementation?
        get() = TODO("Not yet implemented")

    override val targetType: ModelType?
        get() = super.targetType as ModelType?
}