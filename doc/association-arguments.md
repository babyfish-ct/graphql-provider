# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Add arguments to associations

GraphQL has a nice feature that not only queries can have parameters, but relationships between entities can also have parameters.

Graphql-provider allows you to add arguments to collection properties, i.e. properties mapped by *list*, *connection*, *mappedList* and *mappedConnection*.

Here, we demonstrate how to add parameters to the association *Book.authors*.

```kt
package org.babyfish.graphql.provider.example.mapper.entity

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.firstName
import org.babyfish.graphql.provider.example.model.lastName
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
            firstName?.let {
                db {
                    where { table.firstName ilike it }
                }
            }
            lastName?.let {
                db {
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
