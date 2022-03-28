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
    
**This is all the work you need!**

There are three points to note

1. graphql-provider automatically adds 4 optional arguments to the query *findBooks* in the GraphQL Schema: first, after, last and before.
    
2. *Connection&lt;N&gt;* supports a integer field *totalCount*, this is the count of data that meets the conditions before pagination, not the count of data after pagination.
    
    Although more pagination mechanisms will be added in the future, currently the first version only supports one, which is the most classic rowIndex-based pagination.

    Before pagination, kimmer-sql will automatically generate a new SQL for data count based on the SQL for data list. The new SQL does not include sorting and paging, and removes unnecessary table joins as much as possible to optimize performance. Finally the returned count will be filled back into *Connection.totalRowCount*

    This powerful feature is implemented based on [kimmer-sql paging query](https://github.com/babyfish-ct/kimmer/blob/main/doc/kimmer-sql/pagination.md).
    
3. The cursors for result(*pageInfo.startCursor*, *pageInfo.endCursor* and *Edge.cursor*) are base64 encoding result of rowIndex before pagination.
