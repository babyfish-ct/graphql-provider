package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.*
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

    private var _securityPredicate: SecurityPredicate? = null

    override val arguments: Arguments
        get() = _userImplementation?.arguments
            ?: _filter?.arguments
            ?: Arguments.EMPTY

    override val userImplementation: UserImplementation?
        get() = _userImplementation

    override val filter: Filter?
        get() = _filter

    override val hidden: Boolean
        get() = hidden

    override val batchSize: Int?
        get() = _batchSize

    override val securityPredicate: SecurityPredicate?
        get() = _securityPredicate

    override val name: String
        get() = super.name

    override val declaringType: ModelType
        get() = super.declaringType as ModelType

    override val targetType: ModelType?
        get() = super.targetType as ModelType?

    override val targetRawClass: KClass<*>
        get() = super.targetType?.kotlinType ?: super.returnType

    internal fun setUserImplementation(
        userImplementation: UserImplementation
    ) {
        if (!isTransient) {
            error("Internal bug: '$this' is not transient property")
        }
        if (_userImplementation !== null) {
            throw ModelException(
                "The user implementation of '$this' has already been specified, don't specify again"
            )
        }
        if (_filter !== null) {
            throw ModelException(
                "Cannot set the user implementation of '$this' because it filter has been set"
            )
        }
        _userImplementation = userImplementation
    }

    internal fun setFilter(
        filter: Filter
    ) {
        if (_filter !== null) {
            error("Internal bug: filter of '$this' can only be set once")
        }
        if (_userImplementation !== null) {
            throw ModelException(
                "Cannot set the filter of '$this' because it user implementation has been set"
            )
        }
        _filter = filter
    }

    internal fun setHidden(hidden: Boolean) {
        _hidden = hidden
    }

    internal fun setBatchSize(batchSize: Int?) {
        _batchSize = batchSize
    }

    internal fun setSecurityPredicate(predicate: SecurityPredicate?) {
        _securityPredicate = predicate
    }
}