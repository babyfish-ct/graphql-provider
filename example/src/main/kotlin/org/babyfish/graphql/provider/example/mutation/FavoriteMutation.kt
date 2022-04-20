package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.dsl.MutationDSL
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.security.AppUserDetails
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.authentication
import org.springframework.stereotype.Service
import java.util.*

@Service
class FavoriteMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation() {

    override fun MutationDSL.config() {
        security {
            not {
                anonymous()
            }
        }
    }

    suspend fun like(bookId: UUID): Boolean =
        runtime.mutate {
            val me = (authentication().principal as AppUserDetails).appUser
            r2dbcClient.execute {
                associations.byList(Book::fans).saveCommand(
                    bookId,
                    me.id
                ).execute(it).totalAffectedRowCount != 0
            }
        }

    suspend fun unlike(bookId: UUID): Boolean =
        runtime.mutate {
            val me = (authentication().principal as AppUserDetails).appUser
            r2dbcClient.execute {
                associations.byList(Book::fans).deleteCommand(
                    bookId,
                    me.id
                ).execute(it).totalAffectedRowCount != 0
            }
        }
}