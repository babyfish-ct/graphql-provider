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

- δ: *Book.authors* is a many-to-many list.

---------------
[< Previous: Create project & Define entities](./entities.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: Configure batch size >](./batch-size.md)
