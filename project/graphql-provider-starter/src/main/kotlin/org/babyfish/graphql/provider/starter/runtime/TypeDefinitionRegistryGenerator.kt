package org.babyfish.graphql.provider.starter.runtime

import graphql.language.*
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.GraphQLProp
import org.babyfish.graphql.provider.starter.meta.QueryType
import org.babyfish.kimmer.Immutable
import java.math.BigDecimal
import kotlin.reflect.KClass

internal class TypeDefinitionRegistryGenerator(
    private val queryType: QueryType,
    private val entityTypes: Collection<EntityType>
) {
    init {
        if (queryType.props.isEmpty()) {
            throw ModelException(
                "No bean whose type is '${Query::class.qualifiedName}'," +
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
            for (prop in queryType.props.values) {
                fieldDefinition(generateField(prop))
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
            fieldDefinitions(entityType.declaredProps.values.map { generateField(it) })
        }.build()

    private fun generateInterfaceType(entityType: EntityType): InterfaceTypeDefinition =
        InterfaceTypeDefinition.newInterfaceTypeDefinition().apply {
            name(entityType.name)
            implementz(entityType.superTypes.map { TypeName(it.name) })
            definitions(entityType.declaredProps.values.map { generateField(it) })
        }.build()

    private fun generateField(prop: GraphQLProp): FieldDefinition =
        FieldDefinition.newFieldDefinition().apply {
            name(prop.name)
            val fieldType = when {
                prop.isConnection ->
                    TODO()
                prop.isList ->
                    ListType(
                        TypeName(prop.targetEntityType!!.name)
                            .asNullable(prop.isElementNullable)
                    ).asNullable(prop.isNullable)
                prop.isReference ->
                    TypeName(
                        prop.targetEntityType!!.name
                    ).asNullable(prop.isElementNullable)
                else ->
                    scalarType(prop.returnType)
                        .asNullable(prop.isElementNullable)
            }
            type(fieldType)
            for (argument in prop.arguments) {
                inputValueDefinition(
                    InputValueDefinition.newInputValueDefinition().apply {
                        name(argument.name)
                        type(
                            argumentType(
                                argument.type,
                                argument.isNullable,
                                argument.elementType,
                                argument.isElementNullable
                            )
                        )
                    }.build()
                )
            }
        }.build()

    private fun argumentType(
        type: KClass<*>,
        isNullable: Boolean,
        elementType: KClass<*>?,
        isElementNullable: Boolean
    ): Type<*> =
        when {
            !isNullable -> NonNullType(
                argumentType(
                    type,
                    true,
                    elementType,
                    isElementNullable
                )
            )
            elementType !== null -> ListType(
                argumentType(
                    elementType,
                    isElementNullable,
                    null,
                    false
                )
            )
            Immutable::class.java.isAssignableFrom(type.java) ->
                TypeName(type.simpleName!!)
            else ->
                scalarType(type)
        }

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