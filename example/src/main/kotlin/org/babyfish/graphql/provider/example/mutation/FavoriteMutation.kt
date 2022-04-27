package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.security.AppUserDetails
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.currentUserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class FavoriteMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation() {

    suspend fun like(bookId: UUID): Boolean =
        runtime.mutate {
            val me = currentUserDetails<AppUserDetails>().appUser
            r2dbcClient.execute {
                associations.byList(Book::fans).saveCommand(
                    bookId,
                    me.id
                ).execute(it).totalAffectedRowCount != 0
            }
        }

    suspend fun dislike(bookId: UUID): Boolean =
        runtime.mutate {
            val me = currentUserDetails<AppUserDetails>().appUser
            r2dbcClient.execute {
                associations.byList(Book::fans).deleteCommand(
                    bookId,
                    me.id
                ).execute(it).totalAffectedRowCount != 0
            }
        }
}