# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/Map inputs

This article prepares for subsequent mutation development.

## Overview

Unlike queries that return entity objects directly, mutations do not directly take entity objects as input. GraphQL requires developers to define some objects called **input** objects.

> In GraphQL, this is called **Input**; in more traditional techniques, this is called **DTO**

This requirement is reasonable. Unlike query, mutation needs to validate the user's input. Only when the input given by the user is valid and meets the expectations of the server, the modification business can be successfully executed.

1. For the query used for output, due to the flexible and dynamic nature of the GraphQL object type, it naturally eliminates the need to define many DTOs to ensure the diversity of returned data.

2. For mutation used for input, GraphQL still does not eliminate DTOs because input objects are essentially some static DTOs.

While the requirements are reasonable, this makes development unpleasant. Developers have to deal with two kinds of objects, entity objects and Input/DTO objects. They look alike but are different, and the developer had to write a lot of code to convert between the two objects, this job is onerous and unconstructive.

The mutation implementation mechanism of graphql-provider does its best to eliminate Input/DTO and provide developers with a development experience that only focuses on entity objects. This requires an important tool: *InputMapper*

## InputMapper
