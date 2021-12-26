package org.babyfish.graphql.provider.kimmer.meta

import kotlin.reflect.KClass

interface ImmutableProp {

    val declaringType: ImmutableType

    val name: String

    val kotlinType: KClass<*>

    val isNullable: Boolean

    val isAssociation: Boolean

    val isCollection: Boolean

    val isReference: Boolean

    val targetType: ImmutableType?

    val isTargetNullable: Boolean
}