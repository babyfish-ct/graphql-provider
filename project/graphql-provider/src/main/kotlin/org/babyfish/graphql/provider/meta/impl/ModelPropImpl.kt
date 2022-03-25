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

    private var _userImplementation: UserImplementation? = null

    private var _filter: Filter? = null

    private var _hidden: Boolean = false

    private var _batchSize: Int? = null

    override val userImplementation: UserImplementation?
        get() = _userImplementation

    override val filter: Filter?
        get() = _filter

    override val hidden: Boolean
        get() = hidden

    override val batchSize: Int?
        get() = _batchSize

    override val name: String
        get() = super.name

    override val declaringType: ModelType
        get() = super.declaringType as ModelType

    override val targetType: ModelType?
        get() = super.targetType as ModelType?

    override val targetRawClass: KClass<*>
        get() = super.targetType?.kotlinType ?: super.returnType

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

    internal fun setFilter(filter: Filter) {
        if (_filter !== null) {
            error("Internal bug: filter of '$this' can only be set once")
        }
        _filter = filter
    }

    internal fun setHidden(hidden: Boolean) {
        _hidden = hidden
    }

    internal fun setBatchSize(batchSize: Int?) {
        _batchSize = batchSize
    }
}