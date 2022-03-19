package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.ConvertFromInput
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.example.mapper.BookDeepTreeInputMapper
import org.babyfish.graphql.provider.example.mapper.BookInputMapper
import org.babyfish.graphql.provider.example.mapper.BookShallowTreeInputMapper
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.RootMutationResult
import org.springframework.stereotype.Service

@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation {

    suspend fun saveBook(
        @ConvertFromInput(BookInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input).totalAffectedRowCount

    suspend fun saveBookShallowTree(
        @ConvertFromInput(BookShallowTreeInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input).totalAffectedRowCount

    suspend fun saveBookDeepTree(
        @ConvertFromInput(BookDeepTreeInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input).totalAffectedRowCount
}