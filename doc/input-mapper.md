# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Map inputs

This article prepares for subsequent mutation development.

## 1. Overview

Unlike queries that return entity objects directly, mutations do not directly take entity objects as input. GraphQL requires developers to define some objects called **input** objects.

> In GraphQL, this is called **Input**; in more traditional techniques, this is called **DTO**

This requirement is reasonable. Unlike query, mutation needs to validate the user's input. Only when the input given by the user is valid and meets the expectations of the server, the modification business can be successfully executed.

1. For the query used for output, due to the flexible and dynamic nature of the GraphQL object type, it naturally eliminates the need to define many DTOs to ensure the diversity of returned data.

2. For mutation used for input, GraphQL still does not eliminate DTOs because input objects are essentially some static DTOs.

While the requirements are reasonable, this makes development unpleasant. Developers have to deal with two kinds of objects, entity objects and Input/DTO objects. They look alike but are different, and the developer had to write a lot of code to convert between the two objects, this job is onerous and unconstructive.

The mutation implementation mechanism of graphql-provider does its best to eliminate Input/DTO and provide developers with a development experience that only focuses on entity objects. This requires an important tool: *InputMapper*

## 2. InputMapper

*InputMapper* tells graphql-provider how to extract the input type from the entity type.

In this example, for the entity type *Book*, we provide three Input types

1. *BookInput*:

    Only modify the scala fields of the *Book* object itself
    
2. *BookShallowTreeInput*

    It can modify
    - Scalar fields of the *Book* object itself
    - Associations between the current Book object and other objects

3. *BookShallowTreeInput*

    It can modify
    - Scalar fields of the *Book* object itself
    - Associations between the current Book object and other objects
    - Scalar fields of associated objects.

We need to define three InputMappers so that graphql-provider can automatically generate these three Input types based on the Book type. So, we create a new package: *com.example.demo.mapper.input*

### 2.1 BookInputMapper

Add a class under the package: *com.example.demo.mapper.input*

```
package com.example.demo.mapper.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import com.example.demo.mapper.model.Book
import org.springframework.stereotype.Component
import java.util.*

@Component // α
class BookInputMapper: InputMapper<Book, UUID> { // β

    override fun InputTypeDSL<Book, UUID>.config() {
        // γ
        keyProps(Book::name) // δ
        allScalars() // ε
    }
}
```

- α

    The object must be managed by spring
   
- β

    The superclass must be *org.babyfish.graphql.provider.InputMapper*
    
- γ

    We did not use code like `name("BookInput")` to define the name of the input type, graphql-provider will automatically infer the name of the input type
    
    - If the class name of the mapper ends with "InputMapper", then the result of removing the "Mapper" at the end of the class name is the name of the input type. *(This is the case for this example: BookInputMapper ➤ BookInput)*
    
    - If the class name of the mapper ends with "Mapper", then the result of removing "Mapper" at the end of the class name plus "Input" is the name of the input type. *(BookMapper ➤ BookInput)*
    
    - Otherwise, the entity type is extracted according to the generic parameter of InputMappper, and the class name plus "Input" is the name of the input type. *(BadName : InputMapper<Book, UUID> ➤ BookInput)*
    
- δ

    By default, the *id* of *BookInput* cannot be null, graphql-provider will determine whether the mutation operation should perform insert or update based on the *id* field.
    
    However, `keyProps(Book::name)` changes that, which makes the *BookInput*'s *id* nullable. If the user does not specify the *id* for the *BookInput* object, graphql-provider will determine whether the mutation operation should perform insert or update based on the *name* field. (Of course, if the user specifies the *id*, *id* is still used to judge)
    
    > By default, the mutation of graphql-provider will perform an upsert (insert or update) operation. However, you can explicitly define by `insertOnly()` or `updateOnly()`
    
- ε

    `allScalars()` maps all the scalar fields of *Book* to *BookInput*
    
    In addition to `allScalars()`, you can perform many other mappings on scalar fields
    
    - `allNonNullScalar()`: Map all the non-null scalar fields of *Book* to *BookInput*
    - `+Book::name`: Map the `name` field of *Book* to *BookInput*
    - `-Book::name`: Do not map the *name* field of *Book*, should be used after `allScalars()` or `allNonNullScalars()`
    - `scalar(Book::name, "bookName")`: Map the `name` field of *Book* to *BookInput* and specify the field name in the input type
