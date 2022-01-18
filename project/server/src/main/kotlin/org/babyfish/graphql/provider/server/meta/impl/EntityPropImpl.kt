package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class EntityPropImpl(
    override val declaringType: EntityType,
    override val category: EntityProp.Category,
    kotlinProp: KProperty1<*, *>,
    mappedBy: KProperty1<*, *>? = null
): EntityProp {

    private val mappedBy: String? = mappedBy?.name

    override val immutableProp: ImmutableProp =
        declaringType.immutableType.props[kotlinProp.name]
            ?: throw IllegalArgumentException("No prop '${kotlinProp.name}' of type '${declaringType.kotlinType.qualifiedName}'")

    override var targetType: EntityType? = null

    override var oppositeProp: EntityProp? = null

    override var column: ColumnImpl? = null

    override var middleTable: EntityProp.MiddleTable? = null

    var redisDependencyMap: MutableMap<String, RedisDependencyImpl>? = null

    override val redisDependencies: Collection<RedisDependency>?
        get() = (redisDependencyMap as Map<String, RedisDependency>?)?.values

    fun resolve(provider: EntityTypeProvider) {
        val tgtKtType = immutableProp.targetType?.kotlinType
        if (tgtKtType !== null) {
            val tgtType = provider.tryGet(tgtKtType)
                ?: error("Failed to resolve $kotlinProp")
            targetType = tgtType
            if (mappedBy !== null) {
                val opposite = tgtType.props[mappedBy] as EntityPropImpl? ?: error("Internal bug")
                oppositeProp = opposite
                opposite.oppositeProp = this
            }
        }
    }

    init {
        if (category == EntityProp.Category.ID || category == EntityProp.Category.SCALAR) {
            column = ColumnImpl()
        }
        val classifier = kotlinProp.returnType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("The property '${kotlinProp}' must return class")
        if (immutableProp.isAssociation != this.isAssociation) {
            throw IllegalArgumentException("The property '${kotlinProp}' returns '${classifier.qualifiedName}' but its category is ${category}, this is not allowed")
        }
        when (category) {
            EntityProp.Category.REFERENCE, EntityProp.Category.MAPPED_REFERENCE ->
                if (!immutableProp.isReference) {
                    throw IllegalArgumentException("The property '${kotlinProp}' must returns another ${Immutable::class.qualifiedName} when category is $category")
                }
            EntityProp.Category.LIST, EntityProp.Category.MAPPED_LIST ->
                if (!immutableProp.isList) {
                    throw IllegalArgumentException("The property '${kotlinProp}' must returns another ${List::class.qualifiedName} when category is $category")
                }
            EntityProp.Category.CONNECTION, EntityProp.Category.MAPPED_CONNECTION ->
                if (!immutableProp.isConnection) {
                    throw IllegalArgumentException("The property '${kotlinProp}' must returns another ${Connection::class.qualifiedName} when category is $category")
                }
            else ->
                if (immutableProp.isAssociation) {
                    throw IllegalArgumentException("The property '${kotlinProp}' cannot returns connection, list or another immutable type when category is $category")
                }
        }
    }

    override fun toString(): String =
        kotlinProp.toString()

    inner class ColumnImpl: EntityProp.Column {

        var userName: String? = null

        override var nullable = false

        override var length: Int? = null

        override var precision: Int? = null

        override var scale: Int? = null

        override var onDelete: OnDeleteAction = OnDeleteAction.NONE

        override val name: String
            get() = userName ?: defaultValue

        private val defaultValue: String by lazy {
            databaseIdentifier(kotlinProp.name)
        }
    }

    inner class MiddleTableImpl: EntityProp.MiddleTable {

        var userTableName: String? = null

        var userJoinColumnName: String? = null

        var userTargetJoinColumnName: String? = null

        override val tableName: String
            get() = userTableName ?: defaultTableName

        override val joinColumnName: String
            get() = userJoinColumnName ?: defaultJoinColumnName

        override val targetJoinColumnName: String
            get() = userTargetJoinColumnName ?: defaultTargetJoinColumnName

        private val defaultTableName by lazy {
            val tgt = targetType ?: error("Cannot get the default table name because target is not resolved")
            "${declaringType.database.tableName}_${tgt.database.tableName}_MAPPING"
        }

        private val defaultJoinColumnName by lazy {
            databaseIdentifier(declaringType.kotlinType.simpleName!!) + "_ID"
        }

        private val defaultTargetJoinColumnName by lazy {
            val tgt = targetType ?: error("Cannot get the default table name because target is not resolved")
            databaseIdentifier(tgt.kotlinType.simpleName!!) + "_ID"
        }
    }

    class RedisDependencyImpl internal constructor(
        override val category: RedisDependency.Category,
        private val kotlinProp: KProperty1<*, *>
    ): RedisDependency {

        override val dependencies: Collection<RedisDependency>
            get() = dependencyMap.values

        override val prop: EntityProp
            get() = TODO("Not yet implemented")

        var dependencyMap = mutableMapOf<String, RedisDependencyImpl>()
    }
}