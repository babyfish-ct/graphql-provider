# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/User implementation fields

In actual projects, in addition to the fields that can be automatically mapped as discussed earlier, entity objects should also have user-defined fields whose functions are determined by the business and should also be implemented by users.

In this article we demonstrate two user-implemented fields

1. *Author.fullName*

This is a simple user implementation field that requires neither access the database nor batch load using the DataLoader

2. *BookStore.avgPrice*

This is a complex user implementation field that requires not only access to the database, but also batch loading using DataLoader

## 1. Author.fullName

1. Add new kotlin property in entity interface

    ```kt
    interface Author {

        ...other properties...

        val fullName: String
    }
    ```

2. Change *AuthorMapper*

    ```kt
    @Component
    class AuthorMapper: EntityMapper<Author, UUID> {

        override fun EntityTypeDSL<Author, UUID>.config() {

            ...other configuration...

            userImplementation(Author::fullName) // α
        }

        fun fullName(separator: String?) = // β
            runtime.implementation(Author::fullName) { // γ
                "${it.firstName}${separator ?: " "}${it.lastName}" // δ
            }
    }
    ```

    - α

        Map *Author.fullName* as user implementation field

    - β

        Provides a public function.

        - In theory, the function name is arbitrary, but for readability it is recommended to keep the same as the name of *Author::fullName* at γ

        - The arguments of this function will be converted to the arguments of the *Author.fullName* field in the GraphQL Schema

        - This function has no return type

    - γ

        - *runtime* is a protected property provided by the superclass *org.babyfish.graphql.provider.EntityMapper*

        - *Auhor::fullName* represents the field for which you want to implement by your code


    - δ

        *it* is an implicit variable provided by the lambda expression of the *runtime.implement function*, representing the current *Author* object
    
Start the app, access http://localhost:8080/graphiql and execute
```
query {
  findBooks {
    name
    store {
      name
    }
    authors {
      fullName
    }
  }
}
```
The response is
```
{
  "data": {
    "findBooks": [
      {
        "name": "Effective TypeScript",
        "store": {
          "name": "O'REILLY"
        },
        "authors": [
          {
            "fullName": "Dan Vanderkam"
          }
        ]
      },
      {
        "name": "GraphQL in Action",
        "store": {
          "name": "MANNING"
        },
        "authors": [
          {
            "fullName": "Samer Buna"
          }
        ]
      },
      {
        "name": "Learning GraphQL",
        "store": {
          "name": "O'REILLY"
        },
        "authors": [
          {
            "fullName": "Eve Procello"
          },
          {
            "fullName": "Alex Banks"
          }
        ]
      },
      {
        "name": "Programming TypeScript",
        "store": {
          "name": "O'REILLY"
        },
        "authors": [
          {
            "fullName": "Boris Cherny"
          }
        ]
      }
    ]
  }
}
```

## 2. BookStore.avgPrice

1. Add new kotlin property in entity interface

    ```kt
    interface BookStore {

        ...other properties...

        val avgPrice: BigDecimal
    }
    ```

2. Add your repository

    Create new package *com.example.demo.dal*, add *BookRepository* under it
    
    ```kt
    package com.example.demo.dal
    
    import com.example.demo.model.Book
    import com.example.demo.model.price
    import com.example.demo.model.store
    import org.babyfish.graphql.provider.runtime.R2dbcClient
    import org.babyfish.kimmer.sql.ast.avg
    import org.babyfish.kimmer.sql.ast.valueIn
    import org.springframework.stereotype.Repository
    import java.math.BigDecimal
    import java.util.*

    @Repository // α
    class BookRepository(
        private val r2dbcClient: R2dbcClient // β
    ) {
        suspend fun findAvgPriceGroupByStoreIds(
            storeIds: Collection<UUID> // γ
        ): Map<UUID, BigDecimal> = // δ
            r2dbcClient.query(Book::class) {
                select {
                    where(table.store.id valueIn storeIds)
                    groupBy(table.store.id)
                    table.store.id then
                        table.price.avg().asNonNull()
                }
            }.associateBy({it.first}) { // ε
                it.second
            }
    }
    ```
    
    - α
    
        The current object needs to be managed by spring
        
    - β

        Inject *org.babyfish.graphql.provider.runtime.R2dbcClient*
        
        *org.babyfish.graphql.provider.runtime.R2dbcClient* is a wrapper for [*SqlClient*](https://github.com/babyfish-ct/kimmer/blob/main/project/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/SqlClient.kt) of kimmer-sql. It combines kimmer-sql with spring data r2dbc, allowing kimmer-sql to enjoy the connection management and transaction management of spring data r2dbc.
        
    - γ

        This query function accepts many *BookStore* ids
        
    - δ

        For each bookstore id specified by the caller, return the average price of all books in the bookstore.
        
    - ε
        
        The query result of strongly typed SQL contains two columns

        - First column: group key, bookstore id
        - The second column: the average price of all books in the current group

        The *associateBy* function here uses the first column of the query result as the key and the second column of the query result as the value, and returns a Map

3. Change BookStoreMapper

    ```kt
    @Component
    class BookStoreMapper: EntityMapper<BookStore, UUID>() {

        override fun EntityTypeDSL<BookStore, UUID>.config() {

            ... other configuration ...

            userImplementation(BookStore::avgPrice) // α
        }

        fun avgPrice() = // β
            runtime.batchImplementation(BookStore::avgPrice) { // γ
                spring(BookRepository::class) // δ
                    .findAvgPriceGroupByStoreIds(it) // ε
            }
    }
    ```
    
    - α

        Map *BookStore.avgPrice* as user implementation field

    - β

        Provides a public function.

        - In theory, the function name is arbitrary, but for readability it is recommended to keep the same as the name of *BookStore::avgPrice* at γ

        - This function has no return type

    - γ

        - *runtime* is a protected property provided by the superclass *org.babyfish.graphql.provider.EntityMapper*

        - *BookStore::avgPrice* represents the field for which you want to implement by your code

        - Here, the function we call is not *runtime.implementation*, but *runtime.batchImplementation*. This means that *BookStore.avgPrice* can be optimized by DataLoader

    - δ
    
        *spring()* is a protected function provided by the superclass *org.babyfish.graphql.provider.EntityMapper*
    
        In Spring dependencies, the dependency chain is as follows:
       
        `BookRepository ➤ R2dbcClient ➤ SqlClient ➤ All Entity Mappers`
        
        If we directly inject *BookRepository* into *BookStoreMapper* here, spring will throw an exception because of the circular dependencies problem.
        
        The solution provided by Spring for this scenario is @*org.springframework.beans.factory.annotation.Lookup*.
        
        To simplify this problem, *org.babyfish.graphql.provider.EntityMapper* provides the *spring()* function, which can help us get external dependencies that cannot be injected directly.
        
    - ε
    
        *it* is an implicit variable provided by the lambda expression of the *runtime.batchImplementation* function, it represents the collection formed by the ids of all *BookStores* in a DataLoader batch processing.
        
        The length of this collection is controlled by the *batchSize* configuration of the *BookStore.avgPrice*, We have already discussed the *bachSize* configuration in [Configure batch size](./batch-size.md), so I won't repeat it here.
        
Start app, access http://localhost:8080/graphiql and execute
```
query {
  findBooks {
    name
    store {
      name
      avgPrice
    }
  }
}
```
The response is
```
{
  "data": {
    "findBooks": [
      {
        "name": "Effective TypeScript",
        "store": {
          "name": "O'REILLY",
          "avgPrice": 56.833333333333336
        }
      },
      {
        "name": "GraphQL in Action",
        "store": {
          "name": "MANNING",
          "avgPrice": 80
        }
      },
      {
        "name": "Learning GraphQL",
        "store": {
          "name": "O'REILLY",
          "avgPrice": 56.833333333333336
        }
      },
      {
        "name": "Programming TypeScript",
        "store": {
          "name": "O'REILLY",
          "avgPrice": 56.833333333333336
        }
      }
    ]
  }
}
```
        
-----------------

[< Previous: Add arguments to association](./association-arguments.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: Pagination query >](./pagination.md)
