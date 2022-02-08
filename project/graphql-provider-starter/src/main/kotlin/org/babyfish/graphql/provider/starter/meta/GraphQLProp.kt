package org.babyfish.graphql.provider.starter.meta

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

interface GraphQLProp {
    val name: String
    val returnType: KClass<*>
    val isReference: Boolean
    val isConnection: Boolean
    val isNullable: Boolean
    val isList: Boolean
    val isElementNullable: Boolean
    val targetType: KClass<*>?
    val targetEntityType: EntityType?
    val arguments: List<Argument>
}