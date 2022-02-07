package org.babyfish.graphql.provider.starter.runtime

import graphql.language.*
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.QueryService
import org.babyfish.graphql.provider.starter.meta.EntityProp
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.kimmer.graphql.Connection
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

internal class TypeDefinitionRegistryGenerator(
    private val queryServices: Collection<QueryService>,
    private val entityTypes: Collection<EntityType>
) {
    init {
        if (queryServices.isEmpty()) {
            throw ModelException(
                "No bean whose type is '${QueryService::class.qualifiedName}'," +
                    "at least one needs to be defined"
            )
        }
    }

    fun generate(): TypeDefinitionRegistry =
        TypeDefinitionRegistry().apply {
            add(generateQueryType())
            addAll(entityTypes.map { generateEntityType(it) })
        }

    private fun generateQueryType(): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name("Query")
            var hasFunction = false
            for (queryService in queryServices) {
                for (function in queryService::class.declaredFunctions) {
                    if (function.visibility == KVisibility.PUBLIC) {
                        fieldDefinition(generateQueryField(function))
                        hasFunction = true
                    }
                }
            }
        }.build()

    private fun generateEntityType(entityType: EntityType): TypeDefinition<*> =
        if (entityType.derivedTypes.isEmpty()) {
            generateObjectType(entityType)
        } else if (entityType.props.isEmpty()) {
            generateUnionType(entityType)
        } else {
            generateInterfaceType(entityType)
        }

    private fun generateUnionType(entityType: EntityType): UnionTypeDefinition =
        UnionTypeDefinition.newUnionTypeDefinition().apply {
            name(entityType.name)
            memberTypes(entityType.derivedTypes.map { TypeName(it.name) })
        }.build()

    private fun generateObjectType(entityType: EntityType): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name(entityType.name)
            implementz(entityType.superTypes.map { TypeName(it.name) })
            fieldDefinitions(entityType.declaredProps.values.map { generateEntityField(it) })
        }.build()

    private fun generateInterfaceType(entityType: EntityType): InterfaceTypeDefinition =
        InterfaceTypeDefinition.newInterfaceTypeDefinition().apply {
            name(entityType.name)
            implementz(entityType.superTypes.map { TypeName(it.name) })
            definitions(entityType.declaredProps.values.map { generateEntityField(it) })
        }.build()

    private fun generateQueryField(function: KFunction<*>): FieldDefinition {
        if (function.returnType.classifier == Unit::class) {
            throw ModelException("Query function '${function}' cannot return Unit")
        }
        return FieldDefinition.newFieldDefinition().apply {
            name(function.name)
            val fieldType = (
                function.returnType.classifier as? KClass<*>
                    ?: ModelException("Query function '${function}' must return class")
            ).let {

            }
        }.build()
    }

    private fun generateEntityField(prop: EntityProp): FieldDefinition =
        FieldDefinition.newFieldDefinition().apply {
            name(prop.name)
            val immutableProp = prop.immutableProp
            val fieldType = when {
                immutableProp.isConnection ->
                    TODO()
                immutableProp.isList ->
                    ListType(
                        TypeName(immutableProp.targetType!!.simpleName)
                            .asNullable(immutableProp.isTargetNullable)
                    ).asNullable(immutableProp.isNullable)
                immutableProp.isReference ->
                    TypeName(
                        immutableProp.targetType!!.simpleName
                    ).asNullable(immutableProp.isTargetNullable)
                else ->
                    scalarType(prop.kotlinProp.returnType.classifier as KClass<*>)
                        .asNullable(immutableProp.isTargetNullable)
            }
            type(fieldType)
        }.build()

    private fun scalarType(type: KClass<*>): TypeName =
        when (type) {
            String::class -> TypeName("String")
            Int::class -> TypeName("String")
            Boolean::class -> TypeName("String")
            BigDecimal::class -> TypeName("Float")
            else -> error("Unsupported type ${type.qualifiedName}")
        }
}

private fun Type<*>.asNullable(nullable: Boolean): Type<*> =
    if (nullable) {
        this
    } else {
        NonNullType(this)
    }