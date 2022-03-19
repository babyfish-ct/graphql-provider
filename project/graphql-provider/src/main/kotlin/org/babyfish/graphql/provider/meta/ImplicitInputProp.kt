package org.babyfish.graphql.provider.meta

import kotlin.reflect.KClass

interface ImplicitInputProp {
    val name: String
    val isNullable: Boolean
    val isReference: Boolean
    val isList: Boolean
    val targetScalarType: KClass<*>?
    val targetImplicitType: ImplicitInputType?
    val modelProp: ModelProp
}