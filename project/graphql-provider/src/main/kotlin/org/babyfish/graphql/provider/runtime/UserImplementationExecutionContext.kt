package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.springframework.security.core.Authentication
import java.lang.UnsupportedOperationException
import java.util.concurrent.CompletableFuture

class UserImplementationExecutionContext(
    val prop: GraphQLProp,
    val env: DataFetchingEnvironment,
    val argumentsConverter: ArgumentsConverter,
    val authentication: Authentication?
) {
    internal var result: CompletableFuture<Any?>? = null
}

internal fun userImplementationExecutionContext(): UserImplementationExecutionContext =
    userImplementationContextLocal.get()
        ?: throw UnsupportedOperationException(
            "These functions cannot be called by app directly: " +
                "EntityMapper.Runtime.implement, " +
                "EntityMapper.Runtime.implementBy, " +
                "EntityMapper.Runtime.batchImplement, " +
                "EntityMapper.Runtime.batchImplementBy"
        )

internal fun withUserImplementationExecutionContext(
    ctx: UserImplementationExecutionContext,
    block: () -> Unit
) {
    userImplementationContextLocal.set(ctx)
    try {
        block()
    } finally {
        userImplementationContextLocal.remove()
    }
}

private val userImplementationContextLocal =
    ThreadLocal<UserImplementationExecutionContext>()