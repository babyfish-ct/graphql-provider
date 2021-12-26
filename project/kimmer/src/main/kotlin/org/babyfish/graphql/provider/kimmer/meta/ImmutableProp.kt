package org.babyfish.graphql.provider.kimmer.meta

import kotlin.reflect.KProperty

interface ImmutableProp {

    val declaringType: ImmutableType

    val kotlinProp: KProperty<*>

    val isNullable: Boolean

    val isAssociation: Boolean

    val isCollection: Boolean

    val isReference: Boolean

    val targetType: ImmutableType?

    val isTargetNullable: Boolean
}