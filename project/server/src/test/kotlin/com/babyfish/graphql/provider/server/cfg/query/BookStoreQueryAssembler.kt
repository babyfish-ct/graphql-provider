package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.BookStore
import com.babyfish.graphql.provider.server.cfg.Query
import org.babyfish.graphql.provider.server.QueryAssembler
import org.babyfish.graphql.provider.server.dsl.QueryDSL
import org.babyfish.graphql.provider.server.runtime.ilike
import org.springframework.stereotype.Component

@Component
class BookStoreQueryAssembler: QueryAssembler<Query> {

    override fun QueryDSL<Query>.assemble() {

        connection(Query::findBookStores) {

            optionalArgument("name", String::class) {
                where(table[BookStore::name] ilike it)
                redis {
                    dependsOn(BookStore::name)
                }
            }

            filter {
                orderBy(BookStore::name)
                redis {
                    dependsOn(BookStore::name)
                }
            }
        }
    }
}