package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.dsl.runtime.CodeDSL
import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.meta.execute
import org.babyfish.graphql.provider.security.SecurityChecker
import org.babyfish.graphql.provider.security.authenticationOrNull
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.produceConnectionAsync
import org.babyfish.kimmer.graphql.produceEdgeDraftAsync
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.count
import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.ast.value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Component
class Executor(
    private val r2dbcClient: R2dbcClient,
    private val transactionManager: ReactiveTransactionManager,
    private val securityChecker: SecurityChecker
) {

    @Suppress("UNCHECKED_CAST")
    suspend fun <E: Entity<ID>, ID: Comparable<ID>> queryConnection(
        block: FilterDSL<E, ID>.(Authentication?) -> Unit
    ): Connection<E> =
        r2dbcClient.execute {
            val (prop, env, authentication) = executorContext()
            prop ?: error("Internal bug: Connection fetch requires GraphQLProp")
            env ?: error("Internal bug: Connection fetch requires DataFetchingEnvironment")
            val nodeType = prop.targetType!!.kotlinType as KClass<E>
            var securityPredicate: SecurityPredicate? = null
            val query = r2dbcClient.sqlClient.createQuery(nodeType) {
                if (prop is ModelProp) {
                    val sourceId = env.getSource<Entity<*>>().id
                    val sourceTable = table
                        .`←joinConnection`(
                            prop.kotlinProp as KProperty1<E, Connection<E>>
                        )
                    where { sourceTable.id eq value(sourceId) as Expression<ID> }
                }
                val dsl = FilterDSL(this)
                dsl.block(authentication)
                securityPredicate = dsl.predicate()
                select(table)
            }
            securityChecker.check(authenticationOrNull(), securityPredicate)
            val countOnce = AsyncOnce {
                query
                    .reselect {
                        select(table.id.count())
                    }.withoutSortingAndPaging()
                    .execute(it)
                    .first()
                    .toInt()
            }
            val (limit, offset) = env.limit(countOnce)
            val nodes = if (limit > 0) {
                query.limit(limit, offset).execute(it)
            } else {
                emptyList()
            }
            produceConnectionAsync(nodeType) {
                totalCount = countOnce.get()
                edges = nodes.mapIndexed { index, node ->
                    produceEdgeDraftAsync(nodeType) {
                        this.node = node
                        cursor = indexToCursor(offset + index)
                    }
                }
                pageInfo().apply {
                    hasPreviousPage = offset > 0
                    hasNextPage = offset + limit < countOnce.get()
                    startCursor = indexToCursor(offset)
                    endCursor = indexToCursor(offset + limit - 1)
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    suspend fun <E: Entity<ID>, ID: Comparable<ID>> queryList(
        block: FilterDSL<E, ID>.(Authentication?) -> Unit
    ): List<E> =
        r2dbcClient.execute {
            val (prop, _, authentication) = executorContext()
            prop ?: error("Internal bug: List/Reference fetch requires DataFetchingEnvironment")
            val targetType = prop.targetType!!.kotlinType as KClass<E>
            var securityPredicate: SecurityPredicate? = null
            val query =
                r2dbcClient.sqlClient.createQuery(targetType) {
                    val dsl = FilterDSL(this)
                    dsl.block(authentication)
                    securityPredicate = dsl.predicate()
                    select(table)
                }
            securityChecker.check(authenticationOrNull(), securityPredicate)
            query.execute(it)
        }

    @Suppress("UNCHECKED_CAST")
    suspend fun <E: Entity<ID>, ID: Comparable<ID>> queryReference(
        block: FilterDSL<E, ID>.(Authentication?) -> Unit
    ): E? =
        queryList(block).firstOrNull()

    suspend fun <R> execute(
        predicate: SecurityPredicate?,
        transaction: Transaction?,
        block: CodeDSL<R>.() -> Unit
    ): R {
        val dsl = CodeDSL<R>()
        dsl.block()
        securityChecker.check(authenticationOrNull(), predicate, dsl.predicate())
        return transactionManager.execute(dsl.transaction() ?: transaction) {
            dsl.async()()
        }
    }
}