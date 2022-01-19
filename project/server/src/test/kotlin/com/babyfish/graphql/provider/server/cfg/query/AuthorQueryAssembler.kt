package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Query
import org.babyfish.graphql.provider.server.QueryAssembler
import org.babyfish.graphql.provider.server.dsl.QueryDSL
import org.babyfish.graphql.provider.server.runtime.ilike

class AuthorQueryAssembler: QueryAssembler<Query> {

    override fun QueryDSL<Query>.assemble() {

        connection(Query::findAuthors) {

            optionalArgument("name", String::class) {
                where(table[Author::name] ilike it)
                redis {
                    dependsOn(Author::name)
                }
            }

            filter {
                orderBy(Author::name)
                redis {
                    dependsOn(Author::name)
                }
            }
        }
    }
}