# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Map entities

Next, some classes need to be created to configure the mapping between the entity interface and the database.

First create a package named *com.example.demo.mapper.entity*， create three files under it: *BookStoreMapper.kt*, *BookMapper.kt* and *AuthorMapper.kt*.

## BookMapper

BookMapper is the core in this example, so let's start with it.

```kt
package com.example.demo.mapper.entity

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component // α
class BookMapper: EntityMapper<Book, UUID>() { // β

    override fun EntityTypeDSL<Book, UUID>.config() { // γ

        reference(Book::store) // γ

        list(Book::authors) { δ
            db {
                middleTable {
                    tableName = "BOOK_AUTHOR_MAPPING"
                    joinColumnName = "BOOK_ID"
                    targetJoinColumnName = "AUTHOR_ID"
                }
            }
        }
    }
}
```

- α: *EntityMapper* must be managed by spring.

- β: *EntityMapper* must inherit *org.babyfish.graphql.provider.EntityMapper*, the first generic parameter must be specified as the entity interface, and the second generic parameter must specified as the id type of the entity.

- γ: *Book.store* is a many-to-one reference.
    There are two ways to map many-to-one associations. foreign key or middle table
    - Base on foreign key
        ```kt
        reference(Book::store) {
            db {
                foreginKey {
                    columnName = "STORE_ID",
                    onDelete = OnDeleteAction.CASCADE
                }
            }
        }
        ```
    - Base on midde table
        ```kt
        reference(Book::store) {
            db {
                middleTable {
                    columnName = "BOOK_STORE_MAPPING",
                    joinColumnName = "BOOK_ID", // for many-to-one(not many-to-many), this column must be unique
                    targetJoinColumName = "STORE_ID"
                }
            }
        }
        ```
        When all of the following conditions are met
        1. Many-to-one association is base on foreign key, not base on middle table
        2. The column name of the foreign key matches the kotlin property name. for example: kotlin property name is "word1Word2" and foreign key column name is "WORD1_WORD2_ID"
        3. No delete action
        
        All the above configuration can be omitted, as you can see in this example
        ```kt
        reference(Book::store)
        ```

- δ: *Book.authors* is a many-to-many list.
    For many-to-many associations, using an middle table is the only option
    
>   In fact, some configuration is omitted from the code above. E.g
>   
>   1. The table name of the entity in the database
>   2. Object type name in GraphQL schema
>   3. Scalar Fields
>   
>   If none of these are omitted, it should look like this
>   ```kt
>   class MyEntityMapper: EntityMapper<MyEntity, Long> {
>   }
>   ```

---------------
[< Previous: Create project & Define entities](./entities.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: Configure batch size >](./batch-size.md)
