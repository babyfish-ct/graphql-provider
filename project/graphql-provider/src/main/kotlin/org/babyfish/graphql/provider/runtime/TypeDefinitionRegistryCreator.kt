package org.babyfish.graphql.provider.runtime

import graphql.language.*
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.EntityMutationResult
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
        allImplicitInputTypes,
        connectionNodeTypes
    ).generate()

private class TypeDefinitionRegistryGenerator(
    private val queryType: QueryType,
    private val mutationType: MutationType,
    private val modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    private val rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    private val allImplicitInputTypes: List<ImplicitInputType>,
    private val connectionNodeTypes: Set<ModelType>
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
            addAll(generateMutationResultTypes())
            addAll(modelTypeMap.values.map { generateEntityType(it) })
            addAll(allImplicitInputTypes.map { generateInputType(it) })
            addAll(generateConnectionTypes())
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
                    TypeName("${prop.targetType!!.graphql.name}Connection")
                prop.isList ->
                    ListType(
                        TypeName(
                            prop.targetType?.graphql?.name
                                ?: prop
                                    .takeIf { it.targetRawClass == EntityMutationResult::class }
                                    ?.let { "_EntityMutationResult" }
                                ?: error("Intern bug Bad list element type")
                        ).asNullable(prop.isTargetNullable)
                    ).asNullable(prop.isNullable)
                prop.isReference ->
                    TypeName(
                        prop.targetType!!.graphql.name
                    ).asNullable(prop.isTargetNullable)
                prop.returnType == EntityMutationResult::class ->
                    TypeName("_EntityMutationResult")
                        .asNullable(prop.isNullable)
                else ->
                    scalarType(prop.returnType)
                        .asNullable(prop.isNullable)
            }
            type(fieldType)
            for (argument in prop.arguments) {
                if (prop.isConnection && connectionArgumentNames.contains(argument.name)) {
                    throw ModelException(
                        "Illegal prop '${prop}', it cannot have an argument named '${argument.name}' " +
                            "because it's a connection field so that that argument '${argument.name}' " +
                            "can only be generated automatically"
                    )
                }
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
            if (prop.isConnection) {
                inputValueDefinition(InputValueDefinition("first", TypeName("Int")))
                inputValueDefinition(InputValueDefinition("after", TypeName("String")))
                inputValueDefinition(InputValueDefinition("last", TypeName("Int")))
                inputValueDefinition(InputValueDefinition("before", TypeName("String")))
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
            inputMapperType !== null -> // Must before "elementType !== null"
                TypeName(
                    rootImplicitInputTypeMap[inputMapperType]?.name
                        ?: throw ModelException(
                            "The input mapper type '${inputMapperType.qualifiedName}' " +
                                "is not manged by spring"
                        )
                ).let {
                    if (type == ImplicitInputs::class) {
                        ListType(it.asNullable(false))
                    } else {
                        it
                    }
                }
            elementType !== null -> ListType(
                argumentType(
                    elementType,
                    null,
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

    private fun generateMutationResultTypes(): List<SDLDefinition<*>> {
        val usedMutationResultType = mutationType.props.values.any {
            it.targetRawClass == EntityMutationResult::class
        }
        if (!usedMutationResultType) {
            return emptyList()
        }
        val totalAffectedRowCountField =
            FieldDefinition("totalAffectedRowCount", TypeName("Int").asNullable(false))
        val entityMutationResultFields = listOf(
            totalAffectedRowCountField,
            FieldDefinition("type", TypeName("_MutationType").asNullable(false)),
            FieldDefinition("affectedRowCount", TypeName("Int").asNullable(false)),
            FieldDefinition("row", TypeName("String").asNullable(false)),
            FieldDefinition(
                "associations",
                ListType(
                    TypeName("_AssociationMutationResult").asNullable(false)
                ).asNullable(false)
            )
        )
        return listOf(
            EnumTypeDefinition.newEnumTypeDefinition().apply {
                name("_MutationType")
                enumValueDefinition(EnumValueDefinition("NONE"))
                enumValueDefinition(EnumValueDefinition("INSERT"))
                enumValueDefinition(EnumValueDefinition("UPDATE"))
                enumValueDefinition(EnumValueDefinition("DELETE"))
            }.build(),
            ObjectTypeDefinition.newObjectTypeDefinition().apply {
                name("_EntityMutationResult")
                fieldDefinitions(entityMutationResultFields)
            }.build(),
            ObjectTypeDefinition.newObjectTypeDefinition().apply {
                name("_AssociationMutationResult")
                fieldDefinition(FieldDefinition("associationName", TypeName("String").asNullable(false)))
                fieldDefinition(totalAffectedRowCountField)
                fieldDefinition(FieldDefinition("middleTableAffectedRowCount", TypeName("Int").asNullable(false)))
                fieldDefinition(FieldDefinition("middleTableInsertedRowCount", TypeName("Int").asNullable(false)))
                fieldDefinition(FieldDefinition("middleTableDeletedRowCount", TypeName("Int").asNullable(false)))
                fieldDefinition(
                    FieldDefinition(
                        "targets",
                        ListType(
                            TypeName("_AssociatedTargetMutationResult").asNullable(false)
                        ).asNullable(false)
                    )
                )
                fieldDefinition(
                    FieldDefinition(
                        "detachedTargets",
                        ListType(
                            TypeName("_AssociatedTargetMutationResult").asNullable(false)
                        ).asNullable(false)
                    )
                )
            }.build(),
            ObjectTypeDefinition.newObjectTypeDefinition().apply {
                name("_AssociatedTargetMutationResult")
                fieldDefinitions(entityMutationResultFields)
                fieldDefinition(FieldDefinition("middleTableChanged", TypeName("Boolean").asNullable(false)))
            }.build()
        )
    }

    private fun generateConnectionTypes(): List<SDLDefinition<*>> {
        if (connectionNodeTypes.isEmpty()) {
            return emptyList()
        }
        val definitions = mutableListOf<SDLDefinition<*>>()
        definitions += ObjectTypeDefinition.newObjectTypeDefinition().apply {
            name("PageInfo")
            fieldDefinition(FieldDefinition("hasNextPage", TypeName("Boolean").asNullable(false)))
            fieldDefinition(FieldDefinition("hasPreviousPage", TypeName("Boolean").asNullable(false)))
            fieldDefinition(FieldDefinition("startCursor", TypeName("String").asNullable(false)))
            fieldDefinition(FieldDefinition("endCursor", TypeName("String").asNullable(false)))
        }.build()
        for (nodeType in connectionNodeTypes) {
            val nodeName = nodeType.graphql.name
            definitions += ObjectTypeDefinition.newObjectTypeDefinition().apply {
                name("${nodeName}Connection")
                fieldDefinition(
                    FieldDefinition(
                        "edges",
                        ListType(
                            TypeName("${nodeName}Edge").asNullable(false)
                        ).asNullable(false)
                    )
                )
                fieldDefinition(FieldDefinition("pageInfo", TypeName("PageInfo").asNullable(false)))
                fieldDefinition(FieldDefinition("totalCount", TypeName("Int").asNullable(false)))
            }.build()
            definitions += ObjectTypeDefinition.newObjectTypeDefinition().apply {
                name("${nodeName}Edge")
                fieldDefinition(FieldDefinition("node", TypeName(nodeName).asNullable(false)))
                fieldDefinition(FieldDefinition("cursor", TypeName("String").asNullable(false)))
            }.build()
        }
        return definitions
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
            BigDecimal::class -> TypeName("Int")
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

private val connectionArgumentNames = setOf("first", "after", "last", "before")