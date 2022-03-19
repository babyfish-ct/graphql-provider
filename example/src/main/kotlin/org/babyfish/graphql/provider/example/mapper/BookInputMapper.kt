package org.babyfish.graphql.provider.example.mapper

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.InputTypeDSL
import org.babyfish.graphql.provider.example.model.Book
import org.springframework.stereotype.Component
import java.util.*

// BookInput: Only upsert scalars
@Component
class BookInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        name("BookInput")

        optionalId()
        allScalars()
    }
}

// BookShallowTreeInput: Upsert scalars and associations(exclude associated objects)
@Component
class BookShallowTreeInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        name("BookShallowTreeInput")

        optionalId()
        allScalars()

        referenceId(Book::store)
        listIds(Book::authors)
    }
}

// BookDeepTreeInput: Upsert scalars, associations and associated objects
@Component
class BookDeepTreeInputMapper: InputMapper<Book, UUID> {

    override fun InputTypeDSL<Book, UUID>.config() {

        name("BookDeepTreeInput")

        optionalId()
        allScalars()

        reference(Book::store) {
            allScalars()
        }

        list(Book::authors) {
            allScalars()
        }
    }
}