package org.babyfish.graphql.provider.starter.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.time.Duration

interface EntityType {

    val name: String

    val immutableType: ImmutableType

    val kotlinType: KClass<out Immutable>
        get() = immutableType.kotlinType

    val superTypes: List<EntityType>

    val derivedTypes: List<EntityType>

    val database: Database

    val redis: Redis

    val graphql: GraphQL

    val idProp: EntityProp

    val isMapped: Boolean

    val declaredProps: Map<String, EntityProp>

    val props: Map<String, EntityProp>

    interface Database {
        val tableName: String
    }

    interface Redis {
        val enabled: Boolean
        val timeout: Duration?
        val nullTimeout: Duration?
    }

    interface GraphQL {
        val defaultBatchSize: Int?
        val defaultCollectionBatchSize: Int?
    }
}

