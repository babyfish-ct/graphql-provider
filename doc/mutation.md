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

Entity objects such as *Book*, *BookStore* and *Author* are kimmer objects, kimmer objects support dynamics *(see https://github.com/babyfish-ct/kimmer/blob/main/doc/kimmer-core/dynamic.md to know more)*. Whether it is a partial object, a complete object, a shallow object tree, or a deep object tree, it can be expressed as an entity object (*Book* here). This is why all three Input objects can be automatically mapped to *Book* objects.

Whether an entity object is a partial object, a complete object, a shallow object tree, or a deep object tree, the *R2dbcClient.save()* function allows developers to save it in one sentence, this is why the implementation of mutation is so simple.

## 2. Decide the return type of mutation

For simple demonstration, the above code makes mutation return an integer. This is an overly simplistic extreme, now let's look at the other extreme and see what *R2dbcClient.save()* actually returns

Let's modify the *saveBookDeepTree* function, let it return the original result returned by the underlying kimmer-sql.

```kt
suspend fun saveBookDeepTree(
        input: ImplicitInput<Book, BookDeepTreeInputMapper>
    ): org.babyfish.kimmer.sql.EntityMutationResult =
        r2dbcClient.save(input.entity, input.saveOptionsBlock)
```

Start the app, access http://localhost:8080/graphiql, execute

```
mutation {
  saveBookDeepTree(input: {
    name: "NewBook",
    price: 80,
    store: {
      name: "New Store"
    }
    authors: [
      { 
        firstName: "NewFirstName1",
        lastName: "NewLastName1",
        gender: MALE,
      },
      { 
        firstName: "NewFirstName2",
        lastName: "NewLastName2",
        gender: FEMALE
      }
    ]
  }) {
    totalAffectedRowCount
    type
    affectedRowCount
    row
    associations {
      associationName
      totalAffectedRowCount
      middleTableInsertedRowCount
      middleTableDeletedRowCount
      targets {
        totalAffectedRowCount
        type
        affectedRowCount
        row
        middleTableChanged
      }
      detachedTargets {
        totalAffectedRowCount
      }
    }
  }
}
```
You will get a response message like this
```
{
  "data": {
    "saveBookDeepTree": {
      "totalAffectedRowCount": 6, // α
      "type": "INSERT", // β
      "affectedRowCount": 1, // β
      "row": "{\"authors\":[{\"firstName\":\"NewFirstName1\",\"gender\":\"MALE\",\"lastName\":\"NewLastName1\",\"id\":\"79939500-3f1f-4171-94ab-90e9c8cf0709\"},{\"firstName\":\"NewFirstName2\",\"gender\":\"FEMALE\",\"lastName\":\"NewLastName2\",\"id\":\"143cb40b-7afe-410c-9b12-247b90579dd1\"}],\"name\":\"NewBook\",\"price\":80,\"store\":{\"name\":\"New Store\",\"website\":null,\"id\":\"0326d933-7978-4d65-a21a-efc274b69c11\"},\"id\":\"5a794a1d-73aa-4b79-8ffb-3eeca5393eca\"}", // γ
      "associations": [ 
        {
          "associationName": "store", // δ
          "totalAffectedRowCount": 1,
          "middleTableInsertedRowCount": 0,
          "middleTableDeletedRowCount": 0,
          "targets": [ // ε
            {
              "totalAffectedRowCount": 1, 
              "type": "INSERT", // ζ
              "affectedRowCount": 1, // ζ
              "row": "{\"name\":\"New Store\",\"website\":null,\"id\":\"0326d933-7978-4d65-a21a-efc274b69c11\"}", // η
              "middleTableChanged": false
            }
          ],
          "detachedTargets": [] // θ
        },
        {
          "associationName": "authors", // ι
          "totalAffectedRowCount": 4, // κ
          "middleTableInsertedRowCount": 2, // λ
          "middleTableDeletedRowCount": 0,
          "targets": [ // μ
            {
              "totalAffectedRowCount": 2,
              "type": "INSERT",  // ν
              "affectedRowCount": 1, ν
              "row": "{\"firstName\":\"NewFirstName1\",\"gender\":\"MALE\",\"lastName\":\"NewLastName1\",\"id\":\"79939500-3f1f-4171-94ab-90e9c8cf0709\"}", // ξ
              "middleTableChanged": true // ο
            },
            {
              "totalAffectedRowCount": 2,
              "type": "INSERT", // π
              "affectedRowCount": 1, // π
              "row": "{\"firstName\":\"NewFirstName2\",\"gender\":\"FEMALE\",\"lastName\":\"NewLastName2\",\"id\":\"143cb40b-7afe-410c-9b12-247b90579dd1\"}", // ρ
              "middleTableChanged": true // σ
            }
          ],
          "detachedTargets": [] // τ
        }
      ]
    }
  }
}
```

- α: Total affect row count is 6, 1 *(BOOK)* + 1 *(BOOK_STORE)* + 2 *(AUTHOR)* + 2 *(BOOK_AUTHOR_MAPPING)*
- β: Root object is inserted, affected row count is 1
- γ: The root object after mutation, note that all object ids are automatically assigned
- δ: The mutation result about the association *Book.store*
- ε: One object is retained by the association *Book.store* *(inserted, updated or not changed)*
- ζ: The associated *BookStore* is inserted, affected row count is 1
- η: The associated object of *Book.store* after mutation, note that its id is automatically assigned
- θ: No associated object of *Book.store* is detached after mutation
- ι: The mutation result about the association *Book.authors*
- κ: The association *Book.authors* affect 4 rows: 2 *(AUTHOR)* + 2 *(BOOK_AUTHOR_MAPPING)*
- λ: The data of middle table *BOOK_AUTHOR_MAPPING* of many-to-many association is modified, affected row count is 2
- μ: Two objects are retained by the association *Book.authors* *(inserted, updated or not changed)*
- ν: The first associated object of *Book.authors* is inserted, affected count is 1
- ξ: The first associated object of *Book.authors* after mutation, note that its id is automatically assigned
- ο: In order to save the first associated object of *Book.authors*, the middle table has been changed
- π: The second associated object of *Book.authors* is inserted, affected count is 1
- ρ: The second associated object of *Book.authors* after mutation, note that its id is automatically assigned
- σ: In order to save the second associated object of *Book.authors*, the middle table has been changed
- τ: No associated object of *Book.author* is detached after mutation

Although the information returned by the underlying kimmer-sql is very rich, it is unnecessary to return all this information to the client in the actual project.

In a real project, you should make the complexity of returning information somewhere between these two extremes. Typically, this should be the saved entity object. You should modify the code to look like this

```kt
@Service
class BookMutation(
    private val r2dbcClient: R2dbcClient
) : Mutation {

    @Transactional
    suspend fun saveBook(
        input: ImplicitInput<Book, BookInputMapper>
    ): Book =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()

    @Transactional
    suspend fun saveBooks(
        inputs: ImplicitInputs<Book, BookInputMapper>
    ): List<Book> =
        r2dbcClient.save(inputs.entities, inputs.saveOptionsBlock).entities()

    @Transactional
    suspend fun saveBookShallowTree(
        input: ImplicitInput<Book, BookShallowTreeInputMapper>
    ): Book =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()

    @Transactional
    suspend fun saveBookDeepTree(
        input: ImplicitInput<Book, BookDeepTreeInputMapper>
    ): Book =
        r2dbcClient.save(input.entity, input.saveOptionsBlock).entity()
}
```

Start the app, access http://localhost:8080/graphiql, execute
```
mutation {
  saveBookDeepTree(input: {
    name: "NewBook",
    price: 80,
    store: {
      name: "New Store"
    }
    authors: [
      { 
        firstName: "NewFirstName1",
        lastName: "NewLastName1",
        gender: MALE,
      },
      { 
        firstName: "NewFirstName2",
        lastName: "NewLastName2",
        gender: FEMALE
      }
    ]
  }) {
    id
    store {
      id
    }
    authors {
      id
    }
  }
}
```
You will get a response message like this
```
{
  "data": {
    "saveBookDeepTree": {
      "id": "7cd13e03-3bea-457a-81af-ece67f21b8e9",
      "store": {
        "id": "49028b2b-e08d-4e81-bd8c-af826e77392f"
      },
      "authors": [
        {
          "id": "d314dad5-d4ee-48cf-afba-e54e6e9c180e"
        },
        {
          "id": "da1d8c34-e4ab-4a53-8ff4-1b4028de0f26"
        }
      ]
    }
  }
}
```
The client can easily access the id assigned to each object after the mutation is executed

> In fact, many GraphQL-related web front-end technologies (eg: [Apollo client](https://github.com/apollographql/apollo-client), [Relay](https://github.com/facebook/relay), [graphql-state](https://github.com/babyfish-ct/graphql-state)) will require you to design mutation return values in this way


## 3. Add transaction
