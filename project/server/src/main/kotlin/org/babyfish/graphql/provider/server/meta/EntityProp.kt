package org.babyfish.graphql.provider.server.meta

import kotlin.reflect.KProperty1

interface EntityProp {

    val category: Category

    val isAssociation: Boolean
        get() = category == Category.REFERENCE ||
            category == Category.LIST ||
            category == Category.CONNECTION ||
            category == Category.MAPPED_REFERENCE ||
            category == Category.MAPPED_LIST ||
            category == Category.MAPPED_CONNECTION

    val isMapped: Boolean
        get() = category == Category.MAPPED_REFERENCE ||
            category == Category.MAPPED_LIST ||
            category == Category.MAPPED_CONNECTION

    val kotlinProp: KProperty1<*, *>

    val target: Entity?

    val opposite: EntityProp?

    val column: Column?

    val middleTable: MiddleTable?

    val redisDependency: List<RedisDependency>

    enum class Category {
        ID,
        SCALAR,
        REFERENCE,
        LIST,
        CONNECTION,
        MAPPED_REFERENCE,
        MAPPED_LIST,
        MAPPED_CONNECTION,
        COMPUTED
    }

    interface Column {
        val name: String
        val nullable: Boolean
        val length: Int?
        val precision: Int?
        val scala: Int?
        val onDelete: OnDeleteAction
    }

    interface MiddleTable {
        val tableName: String
        val joinColumn: String
        val targetJoinColumn: String
    }
}

internal class EntityPropImpl(
    override val category: EntityProp.Category,
    override val kotlinProp: KProperty1<*, *>
): EntityProp {

    override var target: Entity? = null

    override var opposite: EntityProp? = null

    override var column: EntityProp.Column? = null

    override var middleTable: EntityProp.MiddleTable? = null

    override var redisDependency = mutableListOf<RedisDependency>()

    init {
        if (category == EntityProp.Category.ID || category == EntityProp.Category.SCALAR) {
            column = ColumnImpl()
        }
    }

    inner class ColumnImpl: EntityProp.Column {

        var userName: String? = null

        override var nullable = false

        override var length: Int? = null

        override var precision: Int? = null

        override var scala: Int? = null

        override var onDelete: OnDeleteAction = OnDeleteAction.NONE

        override val name: String
            get() = userName ?: defaultValue

        private val defaultValue: String by lazy {
            databaseIdentifier(kotlinProp.name)
        }
    }
}