package com.babyfish.graphql.provider.mutation

import com.babyfish.graphql.provider.mapper.input.BookInputMapper
import com.babyfish.graphql.provider.model.Book
import org.babyfish.graphql.provider.ConvertFromInput
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.springframework.stereotype.Service

@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation {

    suspend fun saveBook(
        @ConvertFromInput(BookInputMapper::class) input: Book
    ): Int =
        r2dbcClient.save(input).totalAffectedRowCount
}