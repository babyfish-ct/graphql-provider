package com.babyfish.graphql.provider.mapper.input

import com.babyfish.graphql.provider.model.Book
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookDeepTreeInputMapper : InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {
        allScalars()
        reference(Book::store) {
            allScalars()
        }
        list(Book::authors) {
            allScalars()
        }
    }
}