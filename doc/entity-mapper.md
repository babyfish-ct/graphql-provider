# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Map entities

Next, some classes need to be created to configure the mapping between the entity interface and the database.

First create a package named *com.example.demo.mapper.entity*， create three files under it: *BookStoreMapper.kt*, *BookMapper.kt* and *AuthorMapper.kt*.

## 1. BookMapper

BookMapper is the core in this example, so let's start with it.

```kt
package com.example.demo.mapper.entity

import com.example.demo.model.Book
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
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
    
>   In fact, more configuration is omitted from the code above. E.g
>   
>   1. Table name in the database
>   2. Object type name in GraphQL schema
>   3. Scalar Fields
>   
>   If none of these are omitted, it should look like this
>   ```kt
>   class MyEntityMapper: EntityMapper<MyEntity, Long> {
>       db {
>           tableName = "MY_ENTITY_TABLE" // Table name in database
>           idGenerator(SequenceIdGenerator("MY_ENTITY_SEQ_ID")) // Sequence to allocate id
>       }
>       graphql {
>           name = "MyEntityObject" // Object type name in GraphQL schema
>       }
>       scalar(MyEntity::id) {
>           db {
>               column {
>                   name = "MY_ENTITY_ID"
>               }
>           }
>       }
>       scalar(MyEntity::name) {
>           db {
>               column {
>                   name = "NAME"
>                   
>                   // "length" can only be used for string type, otherwise a compilation error will occur
>                   length = 50 
>               }
>           }
>       }
>       scalar(MyEntity::capaticy) {
>           db {
>               column {
>                   name = "CAPACITY"
>
>                   // "precision" and "sclae" can only be used for string type, 
>                   // otherwise a compilation error will occur
>                   precision = 10 
>                   scale = 2
>               }
>           }
>       }
>   }
>   ```

## 2. BookStoreMapper

```kt
package com.example.demo.mapper.entity

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import com.example.demo.model.BookStore
import org.springframework.stereotype.Component
import java.util.*

@Component // α
class BookStoreMapper: EntityMapper<BookStore, UUID>() {

    override fun EntityTypeDSL<BookStore, UUID>.config() { // β
        mappedList(BookStore::books, Book::store) // γ
    }
}
```

- α: *EntityMapper* must be managed by spring.

- β: *EntityMapper* must inherit *org.babyfish.graphql.provider.EntityMapper*, the first generic parameter must be specified as the entity interface, and the second generic parameter must specified as the id type of the entity.

- γ: *BookStore.books* is the mirror image of *Book.store*
    In practical work, we often encounter bidirectional associations. We should map one end first, and then configure the other end as a mirror.
    
    Note: For a one-to-many association, configuring it as a mirror is the only option.
    
    > Comparison with JPA annotation
    > The above code 
    > ```kt
    > mappedList(BookStore::books, Book::store)
    > ```
    > is actually equivalent to JPA's 
    > ```java
    > @OneToMany(mappedBy = "store")
    > private List<Book> books;
    > ```
    > The difference is that when there is a typo, the kotlin DSL will cause a compilation error, while JPA will trigger a runtime exception.

## 3.AuthorMapper

```kt
package com.example.demo.mapper.entity

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import com.example.demo.model.Author
import org.springframework.stereotype.Component
import java.util.*
    
@Component // α
class AuthorMapper: EntityMapper<Author, UUID>() {

    override fun EntityTypeDSL<Author, UUID>.config() { // β
        mappedList(Author::books, Book::authors) // γ
    }
}
```
- α: *EntityMapper* must be managed by spring.

- β: *EntityMapper* must inherit *org.babyfish.graphql.provider.EntityMapper*, the first generic parameter must be specified as the entity interface, and the second generic parameter must specified as the id type of the entity.

- γ: *Author.books* is the mirror image of *Book.authors*
    
## 4. Add query
    
After the mapping is complete, we can add the query.
    
Create a package named *com.example.demo.query*, create *BookQuery.kt* under it
```kt
package com.example.demo.query
    
import org.babyfish.graphql.provider.Query
import com.example.demo.model.Book
   
@Service // α
class BookQuery: Query() { // β

    fun findAllBooks(): List<Book> =
        runtime.queryList {} // γ
}
```
    
- α: *Query* must be managed by spring.

- β: *Query* must inherit *org.babyfish.graphql.provider.Query*.
    
- γ: This is a query function
    1. Adding arguments to the query function is a topic to be discussed in the following chapters, so here we use a query without arguments to query all books
    2. *runtime* is a protected property declared in the superclass *org.babyfish.graphql.provider.Query*
    3. The current query has no arguemnts, so *runtime.queryList* does not have any code
    
---------------
[< Previous: Create project & Define entities](./entities.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: Configure batch size >](./batch-size.md)
