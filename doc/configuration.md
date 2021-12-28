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

This framework supports built-in redis cache, GraphQL queries first query redis instead of the database.

Redis cache example

|key          |Value             | Description|
|-------------|------------------|------------|
|gp_BookStore-1 | {id: "1", name: "O'REILLY"} | object cache |
|gp_BookStore-2 | {id: "2", name: "MANNING"} | object cache |
|gp_BookStore-1-books | [3, 4, 5] | association cache |
|gp_BookStore-2-books | [6] | association cache |
