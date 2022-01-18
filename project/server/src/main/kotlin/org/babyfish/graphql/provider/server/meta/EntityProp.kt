package org.babyfish.graphql.provider.server.meta

import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KProperty1

interface EntityProp {

    val declaringType: EntityType

    val category: Category

    val isAssociation: Boolean
        get() = immutableProp.isAssociation

    val isMapped: Boolean
        get() = category == Category.MAPPED_REFERENCE ||
            category == Category.MAPPED_LIST ||
            category == Category.MAPPED_CONNECTION

    val immutableProp: ImmutableProp

    val kotlinProp: KProperty1<*, *>
        get() = immutableProp.kotlinProp

    val name: String
        get() = kotlinProp.name

    val targetType: EntityType?

    val oppositeProp: EntityProp?

    val column: Column?

    val middleTable: MiddleTable?

    val redisDependencies: Collection<RedisDependency>?

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
        val scale: Int?
        val onDelete: OnDeleteAction
    }

    interface MiddleTable {
        val tableName: String
        val joinColumnName: String
        val targetJoinColumnName: String
    }
}

