# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Pagination Query

The pagination provided by graphql-provider conforms to the specification defined in https://graphql.org/learn/pagination and https://relay.dev/graphql/connections.htm.

Using pagination in graphql-provider is very simple, you just need to replace the return value of the query from *kotlin.collections.List* to [*org.babyfish.kimmer.graphql.Connection*](https://github.com/babyfish-ct/kimmer/blob/main/project/kimmer/src/main/kotlin/org/babyfish/kimmer/graphql/Connection.kt).

That means, change the old code
```kt
@Service
class BookQuery: Query() {

    suspend fun books(
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

    suspend fun books(
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

1. graphql-provider automatically adds 4 optional arguments to the query *books* in the GraphQL Schema: first, after, last and before.
    
2. *Connection&lt;N&gt;* supports a integer field *totalCount*, this is the count of data that meets the conditions before pagination, not the count of data after pagination.
    
    Although more pagination mechanisms will be added in the future, currently the first version only supports one, which is the most classic rowIndex-based pagination.

    Before pagination, kimmer-sql will automatically generate a new SQL for data count based on the SQL for data list. The new SQL does not include sorting and paging, and removes unnecessary table joins as much as possible to optimize performance. Finally the returned count will be filled back into *Connection.totalRowCount*

    This powerful feature is implemented based on [kimmer-sql paging query](https://github.com/babyfish-ct/kimmer/blob/main/doc/kimmer-sql/pagination.md).
    
3. The cursors for result(*pageInfo.startCursor*, *pageInfo.endCursor* and *Edge.cursor*) are base64 encoding results of rowIndexs before pagination.
    
    
Start app, access http://localhost:8080/graphiql and execute
```
query {
  books(first: 2) {
    totalCount
    pageInfo {
      hasNextPage
      hasPreviousPage
      startCursor
      endCursor
    }
    edges {
      node {
        name
        store {
          name
        }
        authors {
          fullName
        }
      }
    }
  }
}
```
The response is
```
{
  "data": {
    "books": {
      "totalCount": 4,
      "pageInfo": {
        "hasNextPage": true,
        "hasPreviousPage": false,
        "startCursor": "MA==",
        "endCursor": "MQ=="
      },
      "edges": [
        {
          "node": {
            "name": "Effective TypeScript",
            "store": {
              "name": "O'REILLY"
            },
            "authors": [
              {
                "fullName": "Dan Vanderkam"
              }
            ]
          }
        },
        {
          "node": {
            "name": "GraphQL in Action",
            "store": {
              "name": "MANNING"
            },
            "authors": [
              {
                "fullName": "Samer Buna"
              }
            ]
          }
        }
      ]
    }
  }
}
```

- *totalCount* is 4 menas there are 4 rows before pagination
- The length of *edges* is 2 means there are 2 rows after pagination
- *pageInfo.hasNextPage* is true

Copy the value "MQ==" of *pageInfo.endCusor*, use it as the argument "after", execute
```
query {
  books(first: 2, after: "MQ==") {
    totalCount
    pageInfo {
      hasNextPage
      hasPreviousPage
      startCursor
      endCursor
    }
    edges {
      node {
        name
        store {
          name
        }
        authors {
          fullName
        }
      }
    }
  }
}
```
The response is
```
{
  "data": {
    "books": {
      "totalCount": 4,
      "pageInfo": {
        "hasNextPage": false,
        "hasPreviousPage": true,
        "startCursor": "Mg==",
        "endCursor": "Mw=="
      },
      "edges": [
        {
          "node": {
            "name": "Learning GraphQL",
            "store": {
              "name": "O'REILLY"
            },
            "authors": [
              {
                "fullName": "Eve Procello"
              },
              {
                "fullName": "Alex Banks"
              }
            ]
          }
        },
        {
          "node": {
            "name": "Programming TypeScript",
            "store": {
              "name": "O'REILLY"
            },
            "authors": [
              {
                "fullName": "Boris Cherny"
              }
            ]
          }
        }
      ]
    }
  }
}
```
    
Copy the value "Mw==" of *pageInfo.endCusor*, use it as the argument "after", execute
```
query {
  books(first: 2, after: "Mw==") {
    totalCount
    pageInfo {
      hasNextPage
      hasPreviousPage
      startCursor
      endCursor
    }
    edges {
      node {
        name
        store {
          name
        }
        authors {
          fullName
        }
      }
    }
  }
}
```
The response is
```
{
  "data": {
    "books": {
      "totalCount": 4,
      "pageInfo": {
        "hasNextPage": false,
        "hasPreviousPage": true,
        "startCursor": "NA==",
        "endCursor": "Mw=="
      },
      "edges": []
    }
  }
}
```

------------
    
[< Previous: User implementation fields](./user-implementation.md) | [Home](https://github.com/babyfish-ct/graphql-provider) | [Next: Map inputs >](./input-mapper.md)
