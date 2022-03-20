package org.babyfish.graphql.provider.example.mapper.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.InputTypeDSL
import org.babyfish.graphql.provider.example.model.Book
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        optionalId()

        /*
         * Only upsert scalars
         */

        allScalars()
    }
}
