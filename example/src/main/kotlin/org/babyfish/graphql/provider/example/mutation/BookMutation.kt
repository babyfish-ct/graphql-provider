package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.ConvertFromInput
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.example.mapper.input.BookDeepTreeInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookShallowTreeInputMapper
import org.babyfish.graphql.provider.example.model.Author
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.model.BookStore
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.springframework.stereotype.Service

@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation {

    suspend fun saveBook(
        @ConvertFromInput(BookInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input) {
            keyProps(Book::name, Book::edition)
        }.totalAffectedRowCount

    suspend fun saveBookShallowTree(
        @ConvertFromInput(BookShallowTreeInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input) {
            keyProps(Book::name, Book::edition)
        }.totalAffectedRowCount

    suspend fun saveBookDeepTree(
        @ConvertFromInput(BookDeepTreeInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input) {
            keyProps(Book::name, Book::edition)
            reference(Book::store) {
                keyProps(BookStore::name)
                createAttachedObjects()
            }
            list(Book::authors) {
                keyProps(Author::firstName, Author::lastName)
                createAttachedObjects()
            }
        }.totalAffectedRowCount
}