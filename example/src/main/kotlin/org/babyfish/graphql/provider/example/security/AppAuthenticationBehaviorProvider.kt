package org.babyfish.graphql.provider.example.security

import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.graphql.provider.example.model.security.by
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.AuthenticationBehaviorProvider
import org.babyfish.kimmer.new
import org.springframework.stereotype.Component

@Component
class AppAuthenticationBehaviorProvider(
    private val r2dbcClient: R2dbcClient
): AuthenticationBehaviorProvider<AppUserDetails> {

    override fun recreateUser(
        userDetails: AppUserDetails,
        newPassword: String
    ): AppUserDetails =
        AppUserDetails(
            new(AppUser::class).by (userDetails.appUser) {
                password = newPassword
            }
        )

    override suspend fun insertUser(userDetails: AppUserDetails) {
        r2dbcClient.save(userDetails.appUser) {
            insertOnly()
        }
    }
}