# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Configure batch size

Graphql-provider automatically applies DataLoaders for all associated fields, the user can't cancel this behavior *(because not using DataLoader is harmful and not beneficial)*, so graphql-provider does not have performance problems caused by N+1 query.

The only operation the user is allowed to do is to configure the *batchSize* of the *DataLoader*

## 1. Field level configuration

```kt
@Component
class BookMapper: EntityMapper<Book, UUID>() {

    override fun EntityTypeDSL<Book, UUID>.config() {
    
        list(Book::authors) {
            db { ... }
            graphql {
                batchSize = 32 // Here it is
            }
        }
        
        ... other configuration ...
    }
}
```

For *EntityMapper*'s *reference*, *list*, *connection*, *mappedReference*, *mappedList* and *mappedConnection* configurations, allows users to configure *batchSize* in a code block named *graphql*.

## 2. Entity Level configuration

If the user does not configure field-level *batchSize*, grapqhl-provider will look for entity-level *batchSize* configuration.

```kt
@Component
class BookMapper: EntityMapper<Book, UUID>() {

    override fun EntityTypeDSL<Book, UUID>.config() {
    
        graphql {
            defaultBatchSize = 128 // α
            defaultCollectionBatchSize = 16 // β
        }
        
        ... other configuration ...
    }
}
```

- α: Provides default *batchSize* for all associated fields mapped by *reference* and *mappedReference* in the current entity type
- β: Provides default *batchSize* for all associated fields mapped by *list*, *connection*, *mappedList* and *mappedConnection* in the current entity type
