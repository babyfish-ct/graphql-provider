package org.babyfish.graphql.provider.dsl.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.dsl.db.TransactionDSL
import org.babyfish.graphql.provider.meta.Transaction

@GraphQLProviderDSL
open class TransactionSupporterDSL {

    private var _transaction: Transaction? = null

    fun transaction(block: (TransactionDSL.() -> Unit)? = null) {
        if (_transaction !== null) {
            throw ModelException("'transaction {...}' cannot be specified twice")
        }
        val dsl = TransactionDSL()
        if (block !== null) {
            dsl.block()
        }
        _transaction = dsl.transaction()
    }

    internal fun transaction(): Transaction? =
        _transaction
}