package org.babyfish.graphql.provider.example.mapper.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.babyfish.graphql.provider.example.model.Author
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.model.BookStore
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookDeepTreeInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        // Configure "keyProps" means id is optional
        keyProps(Book::name, Book::edition)

        /*
         * Upsert scalars, associations and associated objects
         */

        allScalars()

        reference(Book::store) {
            keyProps(BookStore::name)
            allScalars()
            createAttachedObjects()
        }

        list(Book::authors) {
            keyProps(Author::firstName, Author::lastName)
            allScalars()
            createAttachedObjects()
        }
    }
}