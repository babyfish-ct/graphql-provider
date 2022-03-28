package org.babyfish.graphql.provider.example.mapper.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.babyfish.graphql.provider.example.model.Book
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookShallowTreeInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        // Configure "keyProps" means id is optional
        keyProps(Book::name)

        /*
         * Upsert scalars and associations(exclude associated objects)
         */

        allScalars()

        referenceId(Book::store)

        listIds(Book::authors)
    }
}
/*
 * input BookShallowTreeInput {
 *     id: UUID
 *     name: String!
 *     price: BigDecimal!
 *     storeId: UUID
 *     authorIds: [UUID!]!
 * }
 */