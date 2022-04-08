package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.dsl.QueryDSL
import org.babyfish.graphql.provider.dsl.runtime.CodeDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.meta.Transaction
import org.babyfish.graphql.provider.meta.execute
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.registerQueryFieldFilter
import org.babyfish.graphql.provider.runtime.registerQueryFieldImplementation
import org.babyfish.graphql.provider.runtime.userImplementationExecutionContext
import org.babyfish.graphql.provider.security.cfg.SecurityChecker
import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.ReactiveTransactionManager
import java.util.concurrent.CompletableFuture

abstract class Query {

    @Autowired
    private lateinit var transactionManager: ReactiveTransactionManager

    @Autowired
    private lateinit var securityChecker: SecurityChecker

    protected val runtime: Runtime = Runtime()

    private var transaction: Transaction? = null

    private var securityPredicate: SecurityPredicate? = null

    internal fun initAfterInjected() {
        val dsl = QueryDSL()
        dsl.config()
        transaction = dsl.transaction()
        securityPredicate = dsl.predicate()
    }

    protected open fun QueryDSL.config() {}

    inner class Runtime internal constructor() {

        fun <N : Entity<NID>, NID : Comparable<NID>> queryConnection(
            block: FilterDSL<N, NID>.() -> Unit
        ): Connection<N> {
            queryByFilter(block)
        }

        fun <E : Entity<EID>, EID : Comparable<EID>> queryList(
            block: FilterDSL<E, EID>.() -> Unit
        ): List<E> {
            queryByFilter(block)
        }

        fun <R : Entity<RID>, RID : Comparable<RID>> queryReference(
            block: FilterDSL<R, RID>.() -> Unit
        ): R {
            queryByFilter(block)
        }

        fun <T> query(block: suspend () -> T): T =
            queryBy {
                async {
                    block()
                }
            }

        @Suppress("UNCHECKED_CAST")
        fun <T> queryBy(block: CodeDSL<T>.() -> Unit): T {
            if (registerQueryFieldImplementation(this@Query)) {
                throw NoReturnValue()
            }
            val dsl = CodeDSL<T>()
            dsl.block()
            val ctx = userImplementationExecutionContext
            securityChecker.check(ctx.securityContext, dsl.predicate(), securityPredicate)
            ctx.result = executeWithSecurityContext(ctx.securityContext) {
                transactionManager.execute(dsl.transaction() ?: transaction) {
                    dsl.async()()
                }
            }.toFuture() as CompletableFuture<Any?>
            throw NoReturnValue()
        }

        private fun <E : Entity<ID>, ID : Comparable<ID>> queryByFilter(
            block: FilterDSL<E, ID>.() -> Unit
        ): Nothing {
            if (registerQueryFieldFilter(this@Query)) {
                throw NoReturnValue()
            }
            FilterDSL<E, ID>(filterExecutionContext).block()
            throw NoReturnValue()
        }
    }
}
