package org.babyfish.graphql.provider.example.mapper.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.InputTypeDSL
import org.babyfish.graphql.provider.example.model.Book
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookDeepTreeInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        optionalId()

        /*
         * Upsert scalars, associations and associated objects
         */

        allScalars()

        reference(Book::store) {
            allScalars()
        }

        list(Book::authors) {
            allScalars()
        }
    }
}