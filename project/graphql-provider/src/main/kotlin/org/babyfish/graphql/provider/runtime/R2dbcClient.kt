package org.babyfish.graphql.provider.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class R2dbcClient(
    val sqlClient: SqlClient,
    val databaseClient: DatabaseClient
) {
    suspend fun <E: Entity<ID>, ID: Comparable<ID>, R> execute(
        entityType: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): List<R> {
        val query = sqlClient.createQuery(entityType, block)
        return databaseClient.inConnection {
            mono(Dispatchers.Unconfined) {
                query.execute(it)
            }
        }.awaitSingle()
    }
}