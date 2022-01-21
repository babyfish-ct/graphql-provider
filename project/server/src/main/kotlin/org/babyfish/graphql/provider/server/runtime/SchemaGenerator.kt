package org.babyfish.graphql.provider.server.runtime

import graphql.Scalars
import graphql.schema.*
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.EntityType
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions

internal class SchemaGenerator(
    private val queryServices: Collection<QueryService>,
    private val entityTypes: Collection<EntityType>
) {
    fun generate(): GraphQLSchema =
        GraphQLSchema.newSchema().apply {
            query(generateQueryType())
            for (entityType in entityTypes) {
                additionalType(generateEntityType(entityType))
            }
        }.build()

    private fun generateQueryType(): GraphQLObjectType =
        GraphQLObjectType.newObject().apply {
            name("Query")
            var hasFunction = false
            for (queryService in queryServices) {
                for (function in queryService::class.functions) {
                    if (function.visibility == KVisibility.PUBLIC) {
                        field(generateQueryField(function))
                        hasFunction = true
                    }
                }
            }
            if (!hasFunction) {
                error("No query functions")
            }
        }.build()

    private fun generateQueryField(function: KFunction<*>): GraphQLFieldDefinition {
        if (function.returnType.classifier == Unit::class) {
            error("Query function cannot return Unit")
        }
        return GraphQLFieldDefinition.newFieldDefinition().apply {
            name(function.name)
            type(Scalars.GraphQLString)
        }.build()
    }

    private fun generateEntityType(entityType: EntityType): GraphQLType =
        if (entityType.derivedTypes.isEmpty()) {
            generateObjectType(entityType)
        } else if (entityType.props.isEmpty()) {
            generateUnionType(entityType)
        } else {
            generateInterfaceType(entityType)
        }

    private fun generateUnionType(entityType: EntityType): GraphQLUnionType =
        GraphQLUnionType.newUnionType().apply {
            name(entityType.name)
            for (possibleType in entityType.derivedTypes) {
                possibleType(GraphQLTypeReference(possibleType.name))
            }
        }.build()

    private fun generateObjectType(entityType: EntityType): GraphQLObjectType =
        GraphQLObjectType.newObject().apply {
            name(entityType.name)
            for (superType in entityType.superTypes) {
                if (superType.props.isNotEmpty()) {
                    withInterface(GraphQLTypeReference(superType.name))
                }
            }
            for (prop in entityType.declaredProps.values) {
                field(generateEntityField(prop))
            }
        }.build()

    private fun generateInterfaceType(entityType: EntityType): GraphQLInterfaceType =
        GraphQLInterfaceType.newInterface().apply {
            name(entityType.name)
            for (superType in entityType.superTypes) {
                if (superType.props.isNotEmpty()) {
                    withInterface(GraphQLTypeReference(superType.name))
                }
            }
            for (prop in entityType.declaredProps.values) {
                field(generateEntityField(prop))
            }
        }.build()

    private fun generateEntityField(prop: EntityProp): GraphQLFieldDefinition =
        GraphQLFieldDefinition.newFieldDefinition().apply {
            name(prop.name)
            val immutableProp = prop.immutableProp
            val fieldType = when {
                immutableProp.isConnection ->
                    TODO()
                immutableProp.isList ->
                    GraphQLList(
                        GraphQLTypeReference(immutableProp.targetType!!.simpleName)
                            .asNullable(immutableProp.isTargetNullable)
                    ).asNullable(immutableProp.isNullable)
                immutableProp.isReference ->
                    GraphQLTypeReference(
                        immutableProp.targetType!!.simpleName
                    ).asNullable(immutableProp.isTargetNullable)
                else ->
                    scalarType(immutableProp.returnType)
                        .asNullable(immutableProp.isTargetNullable)
            }
            type(fieldType)
        }.build()

    private fun scalarType(type: KClass<*>): GraphQLScalarType =
        when (type) {
            String::class -> Scalars.GraphQLString
            Int::class -> Scalars.GraphQLInt
            Float::class -> Scalars.GraphQLFloat
            Boolean::class -> Scalars.GraphQLBoolean
            BigDecimal::class -> Scalars.GraphQLFloat
            else -> error("Illegal type $type")
        }
}

private fun GraphQLOutputType.asNullable(nullable: Boolean): GraphQLOutputType =
    if (nullable) {
        this
    } else {
        GraphQLNonNull(this)
    }