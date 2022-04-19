package org.babyfish.graphql.provider.security.cfg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.security.AsyncUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Mono

internal class AsyncUserDetailsPasswordServiceWrapper(
    private val asyncUserDetailsPasswordService: AsyncUserDetailsPasswordService<*>?
): ReactiveUserDetailsPasswordService {

    @Suppress("UNCHECKED_CAST")
    override fun updatePassword(user: UserDetails, newPassword: String): Mono<UserDetails> {
        val service = asyncUserDetailsPasswordService
            ?: error(
                "Please configure an instance whose type is '${AsyncUserDetailsPasswordService::class.qualifiedName}' for spring"
            )
        return mono(Dispatchers.Unconfined) {
            (service as AsyncUserDetailsPasswordService<UserDetails>).updatePassword(user, newPassword)
        }
    }
}