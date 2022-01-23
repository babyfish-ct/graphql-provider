package org.babyfish.graphql.provider.server.meta

import kotlin.reflect.KClass

interface Prop {
    val name: String
    val returnType: KClass<*>
    val isReference: Boolean
    val isList: Boolean
    val isConnection: Boolean
    val isNullable: Boolean
    val isTargetNullable: Boolean
    val targetType: EntityType?
}