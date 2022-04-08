package org.babyfish.graphql.provider.security

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.springframework.security.core.context.SecurityContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

suspend fun securityContext(): SecurityContext =
    currentCoroutineContext()[SecurityContextElement]?.ctx
        ?: error("No security context")

suspend fun securityContextOrNull(): SecurityContext? =
    currentCoroutineContext()[SecurityContextElement]?.ctx

internal suspend fun <T> withSecurityContext(
    securityContext: SecurityContext,
    block: suspend () -> T
): T =
    withContext(SecurityContextElement(securityContext)) {
        block()
    }

private data class SecurityContextElement(
    val ctx: SecurityContext
) : AbstractCoroutineContextElement(SecurityContextElement) {
    companion object Key : CoroutineContext.Key<SecurityContextElement>
}