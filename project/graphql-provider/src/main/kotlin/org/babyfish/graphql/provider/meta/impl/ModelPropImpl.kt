package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ModelPropImpl(
    declaringType: ModelTypeImpl,
    kotlinProp: KProperty1<*, *>
): EntityPropImpl(declaringType, kotlinProp), ModelProp {

    private var _filter: Filter? = null

    private var _userImplementation: UserImplementation? = null

    override val cache: Cache
        get() = Cache(CacheLevel.NO_CACHE)

    override val filter: Filter?
        get() = _filter

    override val userImplementation: UserImplementation?
        get() = _userImplementation

    override val targetType: ModelType?
        get() = super.targetType as ModelType?

    override val targetRawClass: KClass<*>
        get() = super.targetType?.kotlinType ?: super.returnType

    internal fun setFilter(filter: Filter) {
        if (_filter !== null) {
            error("Internal bug: filter of '$this' can only be set once")
        }
        _filter = filter
    }

    internal fun setUserImplementation(userImplementation: UserImplementation) {
        if (!isTransient) {
            error("Internal bug: '$this' is not transient property")
        }
        if (_userImplementation !== null) {
            throw ModelException(
                "The user implementation of '$this' has already been specified, don't specify again"
            )
        }
        _userImplementation = userImplementation
    }
}