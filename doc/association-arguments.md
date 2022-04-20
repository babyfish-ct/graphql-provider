# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Add arguments to associations

GraphQL has a nice feature that not only queries can have arguments, but associations between entities can also have arguments.

Graphql-provider allows you to add arguments to collection fields, i.e. fields mapped by *list*, *connection*, *mappedList* and *mappedConnection*.

Here, we demonstrate how to add arguments to the association *Book.authors*.

```kt
package com.example.demo.mapper.entity

import com.example.demo.model.Book
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import com.example.demo.model.firstName
import com.example.demo.model.lastName
import org.babyfish.kimmer.sql.ast.ilike
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookMapper: EntityMapper<Book, UUID>() {

    override fun EntityTypeDSL<Book, UUID>.config() {

        ... The static mapping configuration we discussed ...
    }

    fun authors(firstName: String?, lastName: String?) = // α
        runtime.filterList(Book::authors) { // β
            db {
                firstName?.let {
                    where { table.firstName ilike it } // γ
                }
                lastName?.let {
                    where { table.lastName ilike it }
                }
            }
        }
}
```

- α

    Provides a public function. 
    
    - In theory, the function name is arbitrary, but for readability it is recommended to keep the same as the name of *Book::authors* at β
    
    - The arguments of this function will be converted to the arguments of the *Book.authors* association in the GraphQL Schema
    
    - This function has no return type
    
- β

    - *runtime* is a protected property provided by the superclass *org.babyfish.graphql.provider.EntityMapper*
    
    - *Book::authors* represents the association property for which you want to add arguments

- γ

    *table* represents the generic parameter of the association property type. Here, the generic parameter of type *List&lt;Author&gt;* is *Author*, so *table* represents *AUTHOR*
    
    
Start app, access http://localhost:8080/graphiql, and execute
```
query {
  books {
    name
    store {
      name
    }
    authors(firstName: "Alex") {
      firstName
      lastName
    }
  }
}
```
The response is
```
{
  "data": {
    "books": [
      {
        "name": "Effective TypeScript",
        "store": {
          "name": "O'REILLY"
        },
        "authors": []
      },
      {
        "name": "GraphQL in Action",
        "store": {
          "name": "MANNING"
        },
        "authors": []
      },
      {
        "name": "Learning GraphQL",
        "store": {
          "name": "O'REILLY"
        },
        "authors": [
          {
            "firstName": "Alex",
            "lastName": "Banks"
          }
        ]
      },
      {
        "name": "Programming TypeScript",
        "store": {
          "name": "O'REILLY"
        },
        "authors": []
      }
    ]
  }
}
```
    
------------
    
[< Previous: Add arguments into query](./query-arguments.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: User implementation fields >](./user-implementation.md)
