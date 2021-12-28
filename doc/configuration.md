# Configuration

API of this framework, configuration the information data such as about RDBMS, Redis, GraphQL base on user-defined immutable data mode.

## 1. Simple ORM

If the association fields have no parameters, simple ORM configuration is engouh, like this
```kt
    entity(BookStore::class) {
        db {
            table("BOOK_STORE")
        }
        mappedList(BookStore::books, Book::store)
    }

    entity(Book::class) {
        db {
            table("BOOK")
        }
        reference(Book::store) {
            db {
                foreignKey("BOOK_ID", onDelete = OnDeleteAction.CASCADE)
            }
        }
        list(Book::authors) {
            db {
                middleTable("BOOK_AUTHOR_MAPPING", "BOOK_ID", "AUTHOR_ID")
            }
        }
    }

    entity(Author::class) {
        db {
            table("AUTHOR")
        }
        mappedList(Author::books, Book::authors)
    }
```

We see that this is a very simple ORM configuration, no difference from the traditional ORM framework.

After this configuration, developer need not need to implement SQL queries, need not to implement implement DataLoader/BatchLoader. 

This framework supports built-in redis cache, GraphQL queries first query redis instead of the database. This is an example of redis cache

|key          |Value             | Description|
|-------------|------------------|------------|
|gp_BookStore-1 | {id: "1", name: "O'REILLY"} | object cache |
|gp_BookStore-2 | {id: "2", name: "MANNING"} | object cache |
|gp_BookStore-1-books | [3, 4, 5] | association cache |
|gp_BookStore-2-books | [6] | association cache |

We have seen two types of caches here: object-cache and assocation-cache. In fact, there is a third redis cache: computed-cache, which we will be disucssed later.

**Developers can modify the database at will in the mutation, and the framework will automatically capture the developers' modifications to the database and automatically evict the affected from redis cache.** In order to achieve this function, the framework has few restrictions on developers, as follows

> 1. Must use R2DBC instead of JDBC to modify the database
> 
> 2. Must use ConnectionFactory proxied by this framework
