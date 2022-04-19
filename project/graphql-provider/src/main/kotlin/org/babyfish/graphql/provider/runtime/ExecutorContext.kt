package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono
import java.lang.UnsupportedOperationException
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class ExecutorContext(
    val prop: GraphQLProp?,
    val env: DataFetchingEnvironment?,
    val authentication: Authentication?
)

internal suspend fun executorContext(): ExecutorContext =
    currentCoroutineContext()[ExecutorContextElement]?.ctx
        ?: throw UnsupportedOperationException(
            "These functions cannot be called by app directly: " +
                "Query.Runtime.queryConnection, " +
                "Query.Runtime.queryList, " +
                "Query.Runtime.queryReference, " +
                "Query.Runtime.query, " +
                "Query.Runtime.queryBy, " +
                "Mutation.Runtime.mutate, " +
                "Mutation.Runtime.mutateBy"
        )

internal suspend fun executorContextOrNull(): ExecutorContext? =
    currentCoroutineContext()[ExecutorContextElement]?.ctx

internal suspend fun <T> withExecutorContext(
    ctx: ExecutorContext,
    block: suspend () -> T
): T =
    withContext(ExecutorContextElement(ctx)) {
        block()
    }

fun <T> graphqlMono(
    ctx: ExecutorContext?,
    block: suspend () -> T
): Mono<T> =
    mono(Dispatchers.Unconfined) {
        if (ctx === null) {
            block()
        } else {
            withExecutorContext(ctx) {
                block()
            }
        }
    }.contextWrite(
        ReactiveSecurityContextHolder.withAuthentication(ctx?.authentication)
    )

fun <T> graphqlMono(
    prop: GraphQLProp?,
    env: DataFetchingEnvironment?,
    authentication: Authentication?,
    block: suspend () -> T
): Mono<T> = graphqlMono(
    ExecutorContext(prop, env, authentication),
    block
)

private data class ExecutorContextElement(
    val ctx: ExecutorContext
) : AbstractCoroutineContextElement(ExecutorContextElement) {
    companion object Key : CoroutineContext.Key<ExecutorContextElement>
}
