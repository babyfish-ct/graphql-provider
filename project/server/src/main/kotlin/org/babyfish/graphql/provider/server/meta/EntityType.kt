package org.babyfish.graphql.provider.server.meta

import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.time.Duration

interface EntityType {

    val immutableType: ImmutableType

    val kotlinType: KClass<*>
        get() = immutableType.kotlinType

    val superTypes: List<EntityType>

    val database: Database

    val redis: Redis

    val graphql: GraphQL

    val idProp: EntityProp

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

