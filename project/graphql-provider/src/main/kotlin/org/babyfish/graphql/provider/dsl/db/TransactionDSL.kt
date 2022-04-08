package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.Transaction
import org.babyfish.graphql.provider.meta.impl.TransactionImpl
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import kotlin.reflect.KClass

@GraphQLProviderDSL
class TransactionDSL internal constructor() {

    private val definition = DefaultTransactionDefinition()

    private val rollbackFor = mutableSetOf<KClass<out Throwable>>()

    private val noRollbackFor = mutableSetOf<KClass<out Throwable>>()

    var propagation: Propagation
        get() = Propagation
            .values()
            .first { it.value() == definition.propagationBehavior }
        set(propagation) {
            definition.propagationBehavior = propagation.value()
        }

    var isolation: Isolation
        get() = Isolation
            .values()
            .first { it.value() == definition.isolationLevel }
        set(isolation) {
            definition.isolationLevel = isolation.value()
        }

    var isReadOnly: Boolean
        get() = definition.isReadOnly
        set(readOnly) { definition.isReadOnly = readOnly }

    fun rollback(block: RollbackDSL.() -> Unit) {
        RollbackDSL().block()
    }

    inner class RollbackDSL internal constructor() {

        operator fun KClass<out Throwable>.unaryPlus() {
            rollbackFor += this
            noRollbackFor -= this
        }

        operator fun KClass<out Throwable>.unaryMinus() {
            rollbackFor -= this
            noRollbackFor += this
        }
    }

    internal fun transaction(): Transaction =
        TransactionImpl(
            definition,
            rollbackFor.takeIf { it.isNotEmpty() },
            noRollbackFor.takeIf { it.isNotEmpty() }
        )
}