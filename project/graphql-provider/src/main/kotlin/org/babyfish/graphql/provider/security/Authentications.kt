package org.babyfish.graphql.provider.security

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder

suspend fun authentication(): Authentication =
    authenticationOrNull() ?: error("No authentication")

suspend fun authenticationOrNull(): Authentication? =
    ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()?.authentication