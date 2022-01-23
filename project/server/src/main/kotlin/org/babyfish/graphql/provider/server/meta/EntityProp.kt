package org.babyfish.graphql.provider.server.meta

import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KProperty1
import kotlin.time.Duration

interface EntityProp: Prop {

    val declaringType: EntityType

    val immutableProp: ImmutableProp

    val kotlinProp: KProperty1<*, *>
        get() = immutableProp.kotlinProp

    override val name: String
        get() = kotlinProp.name

    val oppositeProp: EntityProp?

    val isId: Boolean

    val column: Column?

    val middleTable: MiddleTable?

    val redis: Redis

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

    interface Redis {
        val enabled: Boolean
        val timeout: Duration?
        val nullTimeout: Duration?
    }
}

