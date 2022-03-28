# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Pagination Query

The pagination provided by graphql-provider conforms to the specification defined in https://graphql.org/learn/pagination and https://relay.dev/graphql/connections.htm.

Using pagination in graphql-provider is very simple, you just need to replace the return value of the query from *kotlin.collections.List* to [*org.babyfish.kimmer.graphql.Connection*](https://github.com/babyfish-ct/kimmer/blob/main/project/kimmer/src/main/kotlin/org/babyfish/kimmer/graphql/Connection.kt).

That means, change the old code
```kt
@Service
class BookQuery: Query() {

    fun findBooks(
        name: String?,
        storeName: String?,
        authorFirstName: String?,
        authorLastName: String?
    ): List<Book> =
        runtime.queryList {
            ... logic about filter and sorting ...
        }
}
```
to the new code
```kt
@Service
class BookQuery: Query() {

    fun findBooks(
        name: String?,
        storeName: String?,
        authorFirstName: String?,
        authorLastName: String?
    ): Connection<Book> = // α
        runtime.queryConnection { // β
            ... logic about filter and sorting ...
        }
}
```

- α: Change the return type from *kotlin.collections.List<Book>* to *org.babyfish.kimmer.graphql.Connection<Book>*  
- β: Change *runtime.queryList* to *runtime.queryConnection*
    
This is all the work you need.
