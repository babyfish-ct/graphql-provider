package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.runtime.CodeDSL
import org.babyfish.graphql.provider.dsl.MutationDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.meta.Transaction
import org.babyfish.graphql.provider.runtime.Executor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

abstract class Mutation {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val executor: Executor by lazy { // Resolve circular reference problem
        applicationContext.getBean(Executor::class.java)
    }

    protected open val runtime = Runtime()

    private var transaction: Transaction? = null

    private var securityPredicate: SecurityPredicate? = null

    init {
        val dsl = MutationDSL()
        dsl.config()
        transaction = dsl.transaction()
        securityPredicate = dsl.predicate()
    }

    internal fun securityPredicate(): SecurityPredicate? =
        securityPredicate

    protected open fun MutationDSL.config() {}

    inner class Runtime internal constructor() {

        suspend fun <T> mutate(block: suspend () -> T): T =
            mutateBy {
                async {
                    block()
                }
            }

        @Suppress("UNCHECKED_CAST")
        suspend fun <T> mutateBy(block: CodeDSL<T>.() -> Unit): T =
            executor.execute(securityPredicate, transaction, block)
    }
}