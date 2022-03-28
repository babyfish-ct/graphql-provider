# Quickly build GraphQL services with kotlin based on R2DBC

If you use RDBMS to manage your persistent data, this framework can help you to quickly develop GraphQL services (based on kotlin and R2DBC) in the shortest time.

Its development speed is very fast, and the usage method is very simple.

## Run the example
Use intellij the open the [example](https://github.com/babyfish-ct/graphql-provider/tree/main/example), after waiting for gradle to finish all tasks, start the program and visit http://localhost:8080/graphiql.

> The query "findBooks" is pagination query, so you must specify the argument "first" or "last". Otherwise, exeption will be thrown

## User guide & Documentation

These links are not only user guides, but also documentation.

Although the [example](https://github.com/babyfish-ct/graphql-provider/tree/main/example) allows you to quickly experience the working effect and quickly learn how to use the framework, these links still discuss some details that are not shown in the example.

1. [Create project & Define entities](./doc/entities.md)
2. [Map entities](./doc/entity-mapper.md)
3. [Configure batch size](./doc/batch-size.md)
4. [Add arguments to query](./doc/query-arguments.md)
5. [Add arguments to association](./doc/association-arguments.md)
6. [User-implemented fields](./doc/user-implementation.md)
7. [Pagination query](./doc/pagination.md)
8. [Map inputs](./doc/input-mapper.md)
9. [Execute Mutation](./doc/mutation.md)
-----------

## Other projects
1. [kimmer](https://github.com/babyfish-ct/kimmer): A strongly typed SQL DSL base on kotlin, that is the basis of this framework.
2. [graphql-ts-client](https://github.com/babyfish-ct/graphql-ts-client). A strongly typed GraphQL DSL base on TypeScript, it's designed for web developers.
