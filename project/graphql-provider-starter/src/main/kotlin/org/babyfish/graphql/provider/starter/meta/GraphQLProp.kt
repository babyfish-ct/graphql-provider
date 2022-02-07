package org.babyfish.graphql.provider.starter.meta

import kotlin.reflect.KClass

interface GraphQLProp {
    val name: String
    val returnType: KClass<*>
    val isReference: Boolean
    val isList: Boolean
    val isConnection: Boolean
    val isNullable: Boolean
    val isTargetNullable: Boolean
    val targetType: EntityType?
    val arguments: List<Argument>
}