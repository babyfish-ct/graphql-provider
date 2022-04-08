package org.babyfish.graphql.provider.meta

import org.springframework.transaction.ReactiveTransactionManager

interface Transaction {

    suspend fun <R> execute(
        transactionManager: ReactiveTransactionManager,
        block: suspend () -> R
    ): R
}

internal suspend fun <R> ReactiveTransactionManager.execute(
    transaction: Transaction?,
    block: suspend () -> R
): R =
    if (transaction === null) {
        block()
    } else {
        transaction.execute(this, block)
    }