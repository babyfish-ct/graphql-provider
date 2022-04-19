package org.babyfish.graphql.provider.example.mutation.security

import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.dsl.MutationDSL
import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.graphql.provider.example.model.security.by
import org.babyfish.graphql.provider.example.security.AppUserDetails
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationResult
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService
import org.babyfish.kimmer.new
import org.springframework.stereotype.Service

@Service
class AuthenticationMutation(
    private val authenticationService: JwtAuthenticationService
): Mutation() {

    override fun MutationDSL.config() {
        transaction()
    }

    suspend fun createUser(
        email: String,
        password: String
    ): JwtAuthenticationResult = runtime.mutate {
        authenticationService.createUser(
            AppUserDetails(
                new(AppUser::class).by {
                    this.email = email
                    this.password = password
                    roles = emptyList()
                }
            )
        )
        /*
         * In this demo, createUser returns the accessToken and refreshToken immediately.
         *
         * In a real project, please
         *  1. Create a locked user
         *  2. Send email with activation link to the user.
         *  3. Unlock that user row click the user receives the email and click the activation link.
         */
    }
}