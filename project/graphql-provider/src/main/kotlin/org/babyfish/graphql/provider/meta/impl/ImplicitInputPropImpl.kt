package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.ImplicitInputProp
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.ModelProp
import kotlin.reflect.KClass

internal class ImplicitInputPropImpl(
    override val name: String,
    override val modelProp: ModelProp,
    override val targetScalarType: KClass<*>?,
    override val targetImplicitType: ImplicitInputType?,
    private val forceNullable: Boolean = false
): ImplicitInputProp {

    init {
        if (targetScalarType !== null && targetImplicitType !== null) {
            error("Internal bug: Both targetScalarType and targetImplicitType are not null")
        }
    }

    override val isReference: Boolean
        get() = modelProp.isReference

    override val isList: Boolean
        get() = modelProp.isList

    override val isNullable: Boolean
        get() = modelProp.isNullable || forceNullable

    override fun toString(): String =
        modelProp.toString()
}