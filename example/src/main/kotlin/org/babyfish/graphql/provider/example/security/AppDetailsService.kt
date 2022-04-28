package org.babyfish.graphql.provider.example.security

import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.graphql.provider.example.model.security.Role
import org.babyfish.graphql.provider.example.model.security.*
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.AsyncUserDetailsPasswordService
import org.babyfish.graphql.provider.security.AsyncUserDetailsService
import org.babyfish.kimmer.new
import org.babyfish.kimmer.newAsync
import org.babyfish.kimmer.sql.ast.eq
import org.springframework.stereotype.Component

@Component
class AppUserDetailsService(
    private val r2dbcClient: R2dbcClient
) : AsyncUserDetailsService<AppUserDetails>,
    AsyncUserDetailsPasswordService<AppUserDetails> {

    // Tell the framework how to get single user with roles.
    override suspend fun findByUsername(name: String): AppUserDetails? =
        r2dbcClient
            .query(AppUser::class) {
                where(table.email eq name)
                select(table)
            }
            .firstOrNull()
            ?.let { user ->
                AppUserDetails(
                    newAsync(AppUser::class).by (user) {
                        roles = r2dbcClient.query(Role::class) {
                            where(table.`appUsers ∩`(listOf(user.id)))
                            select(table)
                        }
                    }
                )
            }

    // Tell the framework how to update user's password.
    override suspend fun updatePassword(
        user: AppUserDetails,
        newPassword: String
    ): AppUserDetails {

        val user = (user as AppUserDetails).appUser
        r2dbcClient.update(AppUser::class) {
            set(table.password, newPassword)
            where(table.id eq user.id)
        }

        return AppUserDetails(
            new(AppUser::class).by(user) {
                password = newPassword
            }
        )
    }
}