# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Execute mutation

## 1. Basic usage

In the [previous article](./input-mapper.md), we discussed InputMapper, with the help of InputMapper, the implementation of mutation will be very simple.

Create a new package "com.example.demo.mutation", create a new class under it

```kt
package org.babyfish.graphql.provider.example.mutation

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.example.mapper.input.BookDeepTreeInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookInputMapper
import org.babyfish.graphql.provider.example.mapper.input.BookShallowTreeInputMapper
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.springframework.stereotype.Service

@Service // α
class BookMutation(
    private val r2dbcClient: R2dbcClient // β
) : Mutation {

    suspend fun saveBook(
        input: ImplicitInput<Book, BookInputMapper> // γ
    ): Int =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).totalAffectedRowCount

    suspend fun saveBooks(
        inputs: ImplicitInputs<Book, BookInputMapper> // δ
    ): List<Int> =
        r2dbcClient.save(inputs.entities, inputs.saveOptionsBlock).map { it.totalAffectedRowCount }

    suspend fun saveBookShallowTree(
        input: ImplicitInput<Book, BookShallowTreeInputMapper>
    ): Int =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).totalAffectedRowCount

    suspend fun saveBookDeepTree(
        input: ImplicitInput<Book, BookDeepTreeInputMapper>
    ): Int =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).totalAffectedRowCount
}
```

- α: 

    This object must be managed by spring

- β: 

    Inject *org.babyfish.graphql.provider.runtime.R2dbcClient*

- γ: 

    - *org.babyfish.graphql.provider.ImplicitInput* means an input object whose type is created by input mapper
    
    - *org.babyfish.graphql.provider.ImplicitInput.entity* can map input object to entity object.
    

- δ: 

    - *org.babyfish.graphql.provider.ImplicitInputs* means an list, each element is an input object whose type is created by input mapper
    
    - *org.babyfish.graphql.provider.ImplicitInputs.entities* can map input objects to entity objects.

This code shows that whether *BookInput*, *BookShallowInput* or *BookDeepTreeInput* in GraphQL Schema can be automatically mapped to *Book* by graphql-provider.

This is what the [previous article](./input-mapper.md) said, graphql-provider does not require developers to face two similar but different objects *(Entity and Input)*, and does not require developers to write unconstructive code and convert input to entity.

## 2. Decide the return type of mutation

## 3. Add transaction
