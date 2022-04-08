package org.babyfish.graphql.provider.meta

import kotlin.reflect.KClass

interface GraphQLProp {
    val name: String
    val returnType: KClass<*>
    val isReference: Boolean
    val isConnection: Boolean
    val isNullable: Boolean
    val isList: Boolean
    val isTargetNullable: Boolean
    val targetRawClass: KClass<*>
    val targetType: ModelType?
    val arguments: List<Argument>
    val securityPredicate: SecurityPredicate?
    val userImplementation: UserImplementation?
}