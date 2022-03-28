# Quickly build GraphQL services with kotlin based on R2DBC

If you use RDBMS to manage your persistent data, this framework can help you to quickly develop GraphQL services (based on kotlin and R2DBC) in the shortest time.

Its development speed is very fast, and the usage method is very simple.


1. It is a GRM (GraphQL-Relation mapping), and its usage is similar to ORM. When kotlin dsl is used to complete the mapping configuration between entities and tables, GraphQL objects and associations are automatically completed, including the runtime association-level DataLoader and related batch loading optimization.

2. It is easy to add user implemention fields to entity, where you can implement business-related calculations. User implementation fields can also enjoy the automatic generated DataLoader and related batch loading optimization at runtime.

3. Whether it is to implement query-level arguments or association-level arguments, you only need to use  strongly typed SQL DSL to specify some dynamic filtering and sorting, and the rest is done automatically.

4. If you need pagination query, there is no development cost except changing the return type of ordinary query from List&lt;T&gt; to Connection&lt;T&gt;.

5. For mutation operations, the inputs type can be automatically generated according to a simple configuration, develpers only need to focus on entity objects, not input objects. At runtime, the framework can automatically convert the input object to a dynamic entity object tree and you only need one sentence to save any complex entity object tree to the database.

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
