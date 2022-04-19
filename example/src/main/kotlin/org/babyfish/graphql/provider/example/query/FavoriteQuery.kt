package org.babyfish.graphql.provider.example.query

import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.dsl.QueryDSL
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.model.`fans ∩`
import org.babyfish.graphql.provider.example.security.AppUserDetails
import org.babyfish.graphql.provider.security.authentication
import org.springframework.stereotype.Service

@Service
class FavoriteQuery : Query() {

    override fun QueryDSL.config() {
        security {
            not {
                anonymous()
            }
        }
    }

    suspend fun myFavoriteBooks(): List<Book> {
        val me = (authentication().principal as AppUserDetails).appUser
        return runtime.queryList {
            db {
                where(table.`fans ∩`(listOf(me.id)))
            }
        }
    }
}