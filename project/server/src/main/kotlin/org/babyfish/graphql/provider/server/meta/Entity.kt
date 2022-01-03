package org.babyfish.graphql.provider.server.meta

import org.babyfish.graphql.provider.server.Connection
import kotlin.reflect.KClass

interface Entity {

    val kotlinType: KClass<*>

    val database: Database

    val redis: Redis

    val graphql: GraphQL

    val props: Map<String, EntityProp>

    interface Database {
        val tableName: String
    }

    interface Redis {
        val enabled: Boolean
    }

    interface GraphQL {
        val defaultBatchSize: Int?
        val defaultCollectionBatchSize: Int?
    }
}

internal class EntityImpl(
    override val kotlinType: KClass<*>
): Entity {

    init {
        if (Connection::class.java.isAssignableFrom(kotlinType.java)) {
            throw IllegalArgumentException("Type '${kotlinType.qualifiedName}' cannot be considered as entity because it implements '${Connection::class.qualifiedName}'")
        }
    }

    override val database = DatabaseImpl()

    override val redis = RedisImpl()

    override val graphql = GraphQLImpl()

    override val props: Map<String, EntityProp>
        get() = TODO("Not yet implemented")

    inner class DatabaseImpl: Entity.Database {

        var userTableName: String? = null

        override val tableName: String
            get() = userTableName ?: defaultTableName

        private val defaultTableName by lazy {
            databaseIdentifier(kotlinType.simpleName!!)
        }
    }

    class RedisImpl: Entity.Redis {
        override var enabled: Boolean = true
    }

    class GraphQLImpl: Entity.GraphQL {
        override var defaultBatchSize: Int? = null
        override var defaultCollectionBatchSize: Int? = null
    }
}