package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.runtime.CodeDSL
import org.babyfish.graphql.provider.dsl.MutationDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.meta.Transaction
import org.babyfish.graphql.provider.meta.execute
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.meta.impl.TransactionImpl
import org.babyfish.graphql.provider.runtime.registerMutationFieldImplementation
import org.babyfish.graphql.provider.runtime.userImplementationExecutionContext
import org.babyfish.graphql.provider.security.cfg.SecurityChecker
import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.util.concurrent.CompletableFuture

abstract class Mutation {

    @Autowired
    private lateinit var transactionManager: ReactiveTransactionManager

    @Autowired
    private lateinit var securityChecker: SecurityChecker

    protected val runtime = Runtime()

    private lateinit var transaction: Transaction

    private var securityPredicate: SecurityPredicate? = null

    internal fun initAfterInjected() {
        val dsl = MutationDSL()
        dsl.config()
        transaction = dsl.transaction() ?: TransactionImpl(DefaultTransactionDefinition())
        securityPredicate = dsl.predicate()
    }

    internal fun securityPredicate(): SecurityPredicate? =
        securityPredicate

    protected open fun MutationDSL.config() {}

    inner class Runtime internal constructor() {

        fun <T> mutate(block: suspend () -> T): T =
            mutateBy {
                async {
                    block()
                }
            }

        @Suppress("UNCHECKED_CAST")
        fun <T> mutateBy(block: CodeDSL<T>.() -> Unit): T {
            if (registerMutationFieldImplementation(this@Mutation)) {
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
    }
}