package org.babyfish.graphql.provider.security.cfg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.security.AsyncUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Mono

internal open class AsyncUserDetailsServiceWrapper(
    private val asyncUserDetailsService: AsyncUserDetailsService<*>?
): ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        val service = asyncUserDetailsService
            ?: error(
                "Please configure an instance whose type is '${AsyncUserDetailsService::class.qualifiedName}' for spring"
            )
        return mono(Dispatchers.Unconfined) {
            service.findByUsername(username)
        }
    }
}