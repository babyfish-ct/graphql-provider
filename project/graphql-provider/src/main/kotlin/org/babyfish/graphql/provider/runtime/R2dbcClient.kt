package org.babyfish.graphql.provider.runtime

import io.r2dbc.spi.Connection
import kotlinx.coroutines.reactor.awaitSingle
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.EntityMutationResult
import org.babyfish.kimmer.sql.SaveOptionsDSL
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.MutableDelete
import org.babyfish.kimmer.sql.ast.MutableUpdate
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import kotlin.reflect.KClass

class R2dbcClient(
    internal val sqlClient: SqlClient,
    private val databaseClient: DatabaseClient
) {
    suspend fun <R> execute(
        block: suspend SqlClient.(Connection) -> R
    ): R {
        val ctx = executorContextOrNull()
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                sqlClient.block(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>, R> query(
        entityType: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): List<R> {
        val ctx = executorContextOrNull()
        val query = sqlClient.createQuery(entityType, block)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                query.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> update(
        entityType: KClass<E>,
        block: MutableUpdate<E, ID>.() -> Unit
    ): Int {
        val ctx = executorContextOrNull()
        val update = sqlClient.createUpdate(entityType, block)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                update.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> delete(
        entityType: KClass<E>,
        block: MutableDelete<E, ID>.() -> Unit
    ): Int {
        val ctx = executorContextOrNull()
        val update = sqlClient.createDelete(entityType, block)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                update.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> save(
        entity: E,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): EntityMutationResult {
        val ctx = executorContextOrNull()
        val command = sqlClient.entities.saveCommand(entity, block)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                command.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> save(
        entities: List<E>,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): List<EntityMutationResult> {
        val ctx = executorContextOrNull()
        val command = sqlClient.entities.saveCommand(entities, block)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                command.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> delete(
        entityType: KClass<E>,
        id: ID
    ): EntityMutationResult {
        val ctx = executorContextOrNull()
        val command = sqlClient.entities.deleteCommand(entityType, id)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                command.execute(it)
            }
        }.awaitSingle()
    }

    suspend fun <E: Entity<ID>, ID: Comparable<ID>> delete(
        entityType: KClass<E>,
        ids: List<ID>
    ): List<EntityMutationResult> {
        val ctx = executorContextOrNull()
        val command = sqlClient.entities.deleteCommand(entityType, ids)
        return databaseClient.inConnection {
            graphqlMono(ctx) {
                command.execute(it)
            }
        }.awaitSingle()
    }
}