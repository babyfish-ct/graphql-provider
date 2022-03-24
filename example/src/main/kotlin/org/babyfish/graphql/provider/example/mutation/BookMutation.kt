package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.example.mapper.input.BookDeepTreeInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookShallowTreeInputMapper
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.EntityMutationResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation {

    @Transactional
    suspend fun saveBook(
        input: ImplicitInput<Book, BookInputMapper>
    ): EntityMutationResult =
        r2dbcClient.save(input.entity, input.saveOptionsBlock)

    @Transactional
    suspend fun saveBooks(
        inputs: ImplicitInputs<Book, BookInputMapper>
    ): List<EntityMutationResult> =
        r2dbcClient.save(inputs.entities, inputs.saveOptionsBlock)

    @Transactional
    suspend fun saveBookShallowTree(
        input: ImplicitInput<Book, BookShallowTreeInputMapper>
    ): EntityMutationResult =
        r2dbcClient.save(input.entity, input.saveOptionsBlock)

    @Transactional
    suspend fun saveBookDeepTree(
        input: ImplicitInput<Book, BookDeepTreeInputMapper>
    ): Book =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()
}