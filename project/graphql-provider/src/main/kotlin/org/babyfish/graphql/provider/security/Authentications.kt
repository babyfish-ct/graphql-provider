package org.babyfish.graphql.provider.security

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.rmi.server.UID

suspend fun authentication(): Authentication =
    authenticationOrNull() ?:
        error("No authentication")

suspend fun authenticationOrNull(): Authentication? =
    ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()?.authentication

@SuppressWarnings("UNCHECKED_CAST")
suspend fun <UD: UserDetails> currentUserDetails(): UD =
    authenticationOrNull()?.principal.let {
        when {
            it is UserDetails ->
                it as UD
            it === null ->
                throw AccessDeniedException("Access Denied")
            else ->
                throw AccessDeniedException("Current principal is not UserDetails")
        }
    }