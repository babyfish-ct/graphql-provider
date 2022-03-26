# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Create project & Define entities

## 1. Create project

Visit https://start.spring.io/, create a Spring boot project, select **Gradle project** and **Kotlin**

![image](./spring-starter.jpg)


## 2. UML diagram of entity objects

![image](./uml.png)

1. There are three entities: *BookStore*, *Book* and *Author*.
2. There is a many-to-one association from *Book* to *BookStore*: *Book.store*. 
3. There is a one-to-many association from *BookStore* to *Book*: *BookStore.books*.
4. There is a many-to-many association from *Book* to *Author*: *Book.authors*.
5. There is a many-to-many association from *Author* to *Book*: *Author.books*.

## Add denpendencies
