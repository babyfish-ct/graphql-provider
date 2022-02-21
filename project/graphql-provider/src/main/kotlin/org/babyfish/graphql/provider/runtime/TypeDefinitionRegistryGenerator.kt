package org.babyfish.graphql.provider.runtime

import graphql.language.*
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.babyfish.graphql.provider.meta.QueryType
import org.babyfish.kimmer.Immutable
import java.math.BigDecimal
import kotlin.reflect.KClass

internal class TypeDefinitionRegistryGenerator(
    private val queryType: QueryType,
    private val modelTypes: Collection<ModelType>
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
            addAll(modelTypes.map { generateEntityType(it) })
        }

    private fun generateQueryType(): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name("Query")
            for (prop in queryType.props.values) {
                fieldDefinition(generateField(prop))
            }
        }.build()

    private fun generateEntityType(modelType: ModelType): TypeDefinition<*> =
        if (modelType.derivedTypes.isEmpty()) {
            generateObjectType(modelType)
        } else if (modelType.props.isEmpty()) {
            generateUnionType(modelType)
        } else {
            generateInterfaceType(modelType)
        }

    private fun generateUnionType(modelType: ModelType): UnionTypeDefinition =
        UnionTypeDefinition.newUnionTypeDefinition().apply {
            name(modelType.name)
            memberTypes(modelType.derivedTypes.map { TypeName(it.name) })
        }.build()

    private fun generateObjectType(modelType: ModelType): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name(modelType.name)
            modelType.superType?.let { implementz(TypeName(it.name)) }
            // TODO: Bad API of kimmer-sql
            fieldDefinitions(modelType.declaredProps.values.map { generateField(it as GraphQLProp) })
        }.build()

    private fun generateInterfaceType(modelType: ModelType): InterfaceTypeDefinition =
        InterfaceTypeDefinition.newInterfaceTypeDefinition().apply {
            name(modelType.name)
            modelType.superType?.let { TypeName(it.name) }
            // TODO: Bad API of kimmer-sql
            definitions(modelType.declaredProps.values.map { generateField(it as GraphQLProp) })
        }.build()

    private fun generateField(prop: GraphQLProp): FieldDefinition =
        FieldDefinition.newFieldDefinition().apply {
            name(prop.name)
            val fieldType = when {
                prop.isConnection ->
                    TODO()
                prop.isList ->
                    ListType(
                        TypeName(prop.targetType!!.name)
                            .asNullable(prop.isTargetNullable)
                    ).asNullable(prop.isNullable)
                prop.isReference ->
                    TypeName(
                        prop.targetType!!.name
                    ).asNullable(prop.isTargetNullable)
                else ->
                    scalarType(prop.returnType)
                        .asNullable(prop.isTargetNullable)
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