package org.babyfish.graphql.provider.starter.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.time.Duration

interface EntityProp: GraphQLProp {

    val declaringType: EntityType

    val immutableProp: ImmutableProp

    val kotlinProp: KProperty1<*, *>
        get() = immutableProp.kotlinProp

    override val name: String
        get() = kotlinProp.name

    val oppositeProp: EntityProp?

    val isId: Boolean

    val mappedBy: EntityProp?

    val column: Column?

    val middleTable: MiddleTable?

    val redis: Redis

    val filter: Filter?

    override val targetType: KClass<out Immutable>?
        get() = targetEntityType?.kotlinType

    override val arguments: List<Argument>
        get() = filter?.arguments ?: emptyList()

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

