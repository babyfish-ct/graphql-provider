package org.babyfish.graphql.provider.runtime

import graphql.language.*
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass

fun MetaProvider.createTypeDefinitionRegistry(): TypeDefinitionRegistry =
    TypeDefinitionRegistryGenerator(
        queryType,
        mutationType,
        modelTypes,
        rootImplicitInputTypeMap,
        allImplicitInputTypes
    ).generate()

private class TypeDefinitionRegistryGenerator(
    private val queryType: QueryType,
    private val mutationType: MutationType,
    private val modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    private val rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    private val allImplicitInputTypes: List<ImplicitInputType>
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
            addScalarTypes()
            add(generateQueryType())
            add(generateMutationType())
            addAll(modelTypeMap.values.map { generateEntityType(it) })
            addAll(allImplicitInputTypes.map { generateInputType(it) })
        }

    private fun TypeDefinitionRegistry.addScalarTypes() {
        var hasUUID = false
        for (modelType in modelTypeMap.values) {
            for (prop in modelType.declaredProps.values) {
                if (prop.returnType == UUID::class) {
                    hasUUID = true
                }
            }
        }
        if (hasUUID) {
            add(ScalarTypeDefinition.newScalarTypeDefinition().name("UUID").build())
        }
    }

    private fun generateQueryType(): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name("Query")
            for (prop in queryType.props.values) {
                fieldDefinition(generateField(prop))
            }
        }.build()

    private fun generateMutationType(): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name("Mutation")
            for (prop in mutationType.props.values) {
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

    private fun generateInputType(
        implicitInputType: ImplicitInputType
    ): InputObjectTypeDefinition =
        InputObjectTypeDefinition.newInputObjectDefinition().apply {
            name(implicitInputType.name)
            inputValueDefinitions(
                implicitInputType.props.values.map { generateInputValue(it) }
            )
        }.build()

    private fun generateField(prop: GraphQLProp): FieldDefinition =
        FieldDefinition.newFieldDefinition().apply {
            name(prop.name)
            val fieldType = when {
                prop.isConnection ->
                    TODO()
                prop.isList ->
                    ListType(
                        TypeName(
                            prop.targetType!!.name
                        ).asNullable(prop.isTargetNullable)
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
                                argument.inputMapperType,
                                argument.isNullable,
                                argument.elementType,
                                argument.isElementNullable
                            )
                        )
                    }.build()
                )
            }
        }.build()

    private fun generateInputValue(prop: ImplicitInputProp): InputValueDefinition =
        InputValueDefinition.newInputValueDefinition().apply {
            name(prop.name)
            val fieldType = when {
                prop.isList && prop.targetScalarType !== null ->
                    ListType(
                        scalarType(prop.targetScalarType!!)
                            .asNullable(false)
                    ).asNullable(prop.isNullable)
                prop.isList && prop.targetImplicitType !== null ->
                    ListType(
                        TypeName(prop.targetImplicitType!!.name)
                            .asNullable(false)
                    ).asNullable(prop.isNullable)
                prop.isReference && prop.targetScalarType !== null ->
                    scalarType(prop.targetScalarType!!).asNullable(prop.isNullable)
                prop.isReference && prop.targetImplicitType !== null ->
                    TypeName(prop.targetImplicitType!!.name)
                        .asNullable(prop.isNullable)
                else ->
                    scalarType(prop.modelProp.returnType)
                        .asNullable(prop.isNullable)
            }
            type(fieldType)
        }.build()

    private fun argumentType(
        type: KClass<*>,
        inputMapperType: KClass<out InputMapper<*, *>>?,
        isNullable: Boolean,
        elementType: KClass<*>?,
        isElementNullable: Boolean
    ): Type<*> =
        when {
            !isNullable -> NonNullType(
                argumentType(
                    type,
                    inputMapperType,
                    true,
                    elementType,
                    isElementNullable
                )
            )
            elementType !== null -> ListType(
                argumentType(
                    elementType,
                    inputMapperType,
                    isElementNullable,
                    null,
                    false
                )
            )
            inputMapperType !== null ->
                TypeName(
                    rootImplicitInputTypeMap[inputMapperType]?.name
                        ?: throw ModelException(
                            "The input mapper type '${inputMapperType.qualifiedName}' " +
                                "is not manged by spring"
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
            Boolean::class -> TypeName("Boolean")
            Byte::class -> TypeName("Byte")
            Short::class -> TypeName("Short")
            Int::class -> TypeName("Int")
            Long::class -> TypeName("Long")
            Float::class -> TypeName("Float")
            Double::class -> TypeName("Double")
            BigInteger::class -> TypeName("BigInteger")
            BigDecimal::class -> TypeName("BigDecimal")
            UUID::class -> TypeName("UUID")
            else -> error("Unsupported type ${type.qualifiedName}")
        }
}

private fun Type<*>.asNullable(nullable: Boolean): Type<*> =
    if (nullable) {
        this
    } else {
        NonNullType(this)
    }