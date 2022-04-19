package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.*
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.MutationPropImpl
import org.babyfish.graphql.provider.meta.impl.MutationTypeImpl
import org.babyfish.graphql.provider.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.meta.impl.QueryTypeImpl
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationResult
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.EntityMutationResult
import org.springframework.aop.support.AopUtils
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

fun createMetaProvider(
    queries: List<Query>,
    mutations: List<Mutation>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    allImplicitInputTypes: List<ImplicitInputType>
): MetaProvider {
    val queryType = QueryTypeImpl()
    val mutationType = MutationTypeImpl()
    for (query in queries) {
        for (function in query::class.declaredFunctions) {
            if (function.visibility == KVisibility.PUBLIC && function.name != "config") {
                queryType.props[function.name]?.let { conflict ->
                    throw ModelException("Conflict query functions: '${conflict.function}' and '$function'")
                }
                if (!function.isSuspend) {
                    throw ModelException("Query function '$function' must be suspend")
                }
                queryType.props[function.name] = QueryPropImpl(
                    function,
                    modelTypeMap
                )
            }
        }
    }
    for (mutation in mutations) {
        for (function in AopUtils.getTargetClass(mutation).kotlin.declaredFunctions) {
            if (function.visibility == KVisibility.PUBLIC && function.name != "config") {
                mutationType.props[function.name]?.let { conflict ->
                    throw ModelException("Conflict mutation functions: '${conflict.function}' and '$function'")
                }
                if (!function.isSuspend) {
                    throw ModelException("Mutation function '$function' must be suspend")
                }
                mutationType.props[function.name] = MutationPropImpl(
                    function,
                    modelTypeMap
                )
            }
        }
    }
    val connectionNodeTypes = mutableSetOf<ModelType>()
    val scalarKotlinTypes = mutableSetOf<KClass<*>>()
    for (prop in queryType.props.values) {
        if (prop.isConnection) {
            connectionNodeTypes += prop.targetType!!
        }
        collectScalarTypes(prop, scalarKotlinTypes)
    }
    for (prop in mutationType.props.values) {
        if (prop.isConnection) {
            connectionNodeTypes += prop.targetType!!
        }
        collectScalarTypes(prop, scalarKotlinTypes)
    }
    for (modelType in modelTypeMap.values) {
        for (prop in modelType.declaredProps.values) {
            if (prop.isConnection) {
                connectionNodeTypes += prop.targetType!!
            }
            collectScalarTypes(prop, scalarKotlinTypes)
        }
    }
    return MetaProvider(
        queryType,
        mutationType,
        modelTypeMap,
        rootImplicitInputTypeMap,
        allImplicitInputTypes,
        connectionNodeTypes,
        scalarKotlinTypes
    )
}

private fun collectScalarTypes(prop: GraphQLProp, scalarKotlinTypes: MutableSet<KClass<*>>) {
    if (prop.isReference || prop.isList || prop.isConnection) {
        return
    }
    if (prop.targetRawClass == EntityMutationResult::class ||
        prop.targetRawClass == JwtAuthenticationResult::class) {
        return
    }
    scalarKotlinTypes += prop.returnType
    for (argument in prop.arguments) {
        if (argument.inputMapperType === null) {
            val type = argument.elementType ?: argument.type
            if (!Immutable::class.java.isAssignableFrom(type.java)) {
                scalarKotlinTypes += type
            }
        }
    }
}
