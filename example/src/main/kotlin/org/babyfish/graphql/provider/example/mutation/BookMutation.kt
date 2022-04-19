package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.dsl.MutationDSL
import org.babyfish.graphql.provider.example.mapper.input.BookDeepTreeInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookShallowTreeInputMapper
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.entities
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation() {

    // Class-level configuration DSL
    override fun MutationDSL.config() {

        // 1. Security:
        // This config section can be replaced by the annotation
        // @PostAuthorize("hasAuthority('ADMIN')"), they are similar
        security {
            authorities("ADMIN")
        }

        // 2. Transaction:
        // However, this config section can **NOT** be replaced by
        // the annotation @Transactional!
        // As far as I've tried so far, this annotation doesn't seem
        // to be valid for suspend functions
        transaction()

        // 3. As long as the class-level configuration DSL is used
        // (regardless of security{...} or transaction{...}),
        // all of the following business suspension functions need to be
        // wrapped by runtime.mutate or runtime.mutateBy
    }

    suspend fun saveBook(
        input: ImplicitInput<Book, BookInputMapper>
    ): Book = runtime.mutate {
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()
    }

    @Transactional
    suspend fun saveBooks(
        inputs: ImplicitInputs<Book, BookInputMapper>
    ): List<Book> = runtime.mutate {
        r2dbcClient.save(inputs.entities, inputs.saveOptionsBlock).entities()
    }

    @Transactional
    suspend fun saveBookShallowTree(
        input: ImplicitInput<Book, BookShallowTreeInputMapper>
    ): Book = runtime.mutate {
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()
    }

    @Transactional
    suspend fun saveBookDeepTree(
        input: ImplicitInput<Book, BookDeepTreeInputMapper>
    ): Book = runtime.mutate {
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()
    }
}