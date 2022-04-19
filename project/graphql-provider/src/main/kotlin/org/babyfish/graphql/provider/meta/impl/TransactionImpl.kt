package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.Transaction
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.lang.Error
import java.lang.RuntimeException
import kotlin.reflect.KClass

internal class TransactionImpl(
    private val definition: TransactionDefinition,
    private val rollbackFor: Collection<KClass<out Throwable>>? = null,
    private val noRollbackFor: Collection<KClass<out Throwable>>? = null
): Transaction {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R> execute(
        transactionManager: ReactiveTransactionManager,
        block: suspend () -> R
    ): R {
        val transactionalOperator = TransactionalOperator.create(transactionManager, definition)
        return transactionalOperator.executeAndAwait {
            block()
        } as R
    }

    private fun rollbackFor(ex: Throwable): Boolean =
        when {
            instanceOf(ex, rollbackFor) -> true
            instanceOf(ex, noRollbackFor) -> false
            else -> ex is RuntimeException || ex is Error
        }

    companion object {

        private fun instanceOf(
            ex: Throwable,
            types: Collection<KClass<out Throwable>>?
        ): Boolean {
            if (types !== null) {
                val exJavaType = ex::class.java
                for (type in types) {
                    if (type.java.isAssignableFrom(exJavaType)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}