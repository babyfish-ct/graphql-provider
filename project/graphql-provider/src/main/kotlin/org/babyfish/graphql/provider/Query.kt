package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.dsl.QueryDSL
import org.babyfish.graphql.provider.dsl.runtime.CodeDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.meta.Transaction
import org.babyfish.graphql.provider.runtime.*
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

abstract class Query {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val executor: Executor by lazy { // Resolve circular reference problem
        applicationContext.getBean(Executor::class.java)
    }

    protected open val runtime: Runtime = Runtime()

    private var transaction: Transaction? = null

    private var securityPredicate: SecurityPredicate? = null

    init {
        val dsl = QueryDSL()
        dsl.config()
        transaction = dsl.transaction()
        securityPredicate = dsl.predicate()
    }

    internal fun securityPredicate(): SecurityPredicate? =
        securityPredicate

    protected open fun QueryDSL.config() {}

    inner class Runtime internal constructor() {

        suspend fun <N : Entity<NID>, NID : Comparable<NID>> queryConnection(
            block: FilterDSL<N, NID>.() -> Unit
        ): Connection<N> =
            executor.queryConnection(block)

        suspend fun <E : Entity<EID>, EID : Comparable<EID>> queryList(
            block: FilterDSL<E, EID>.() -> Unit
        ): List<E> =
            executor.queryList(block)

        suspend fun <R : Entity<RID>, RID : Comparable<RID>> queryReference(
            block: FilterDSL<R, RID>.() -> Unit
        ): R? =
            executor.queryReference(block)

        suspend fun <T> query(block: suspend () -> T): T =
            queryBy {
                async {
                    block()
                }
            }

        @Suppress("UNCHECKED_CAST")
        suspend fun <T> queryBy(block: CodeDSL<T>.() -> Unit): T =
            executor.execute(securityPredicate, transaction, block)
    }
}
