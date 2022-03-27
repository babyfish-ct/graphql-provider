# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Add arguments to query

You can add parameters to the query.

Since the underlying kimmer-sql itself is designed for dynamic query, here we make all parameters nullable, thus realizing dynamic query.

```kt
package com.example.demo.query

import org.babyfish.graphql.provider.Query
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.ast.valueNotIn
import com.example.demo.model.* // α

@Service
class BookQuery: Query() {
    fun findBooks(
        name: String?,
        storeName: String?,
        authorFirstName: String?,
        authorLastName: String?
    ): List<Book> =
        runtime.queryList {
            name?.let {
                db {
                    where(table.name ilike it)
                }
            }
            storeName?.let { 
                db {
                    where(table.store.name ilike it) // β
                }
            }
            if (authorFirstName !== null || authorLastName !== null) { 
                db { 
                    where { 
                        table.id valueIn subQuery(Author::class) { // γ
                            authorFirstName?.let { 
                                where(table.firstName ilike it)
                            }
                            authorLastName?.let {
                                where(table.lastName ilike it)
                            }
                            select(table.books.id) // δ
                        }
                    }
                }
            }
            db {
                orderBy(table.name)
            }
        }
}
```

- α: We used to add the *ksp* plugin to *build.gralde.kts* to automatically generate source code required for strongly typed SQL based on user-defined entity types. Now, we import the auto-generated code.

    In the above code, we have used some expressions: *table.name*, *table.store.name*, *table.firstName*, *table.lastName*, *table.books.id*. In fact, they are all extension properties defined in the automatically generated code.
