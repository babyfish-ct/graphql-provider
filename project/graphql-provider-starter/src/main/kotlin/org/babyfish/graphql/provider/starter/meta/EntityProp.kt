package org.babyfish.graphql.provider.starter.meta

import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

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

    val userImplementation: UserImplementation?

    val filter: Filter?

    override val targetType: KClass<*>?
        get() = immutableProp.targetType?.kotlinType

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

