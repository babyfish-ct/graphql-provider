# [graphql-provider](https://github.com/babyfish-ct/graphql-provider)/User implementation fields

In actual projects, in addition to the fields that can be automatically mapped as discussed earlier, entity objects should also have user-defined fields whose functions are determined by the business and should also be implemented by users.

In this article we demonstrate two user-implemented fields

1. *Author.fullName*

This is a simple user implementation field that requires neither access the database nor batch load using the DataLoader

2. *BookStore.avgPrice*

This is a complex user implementation field that requires not only access to the database, but also batch loading using DataLoader

## 1. Author.fullName

1. Add new kotlin property in entity interface

    ```kt
    interface Author {

        ...other properties...

        val fullName: String
    }
    ```

2. Change *AuthorMapper*

    ```kt
    @Component
    class AuthorMapper: EntityMapper<Author, UUID> {

        override fun EntityTypeDSL<Author, UUID>.config() {

            ...other configuration...

            userImplementation(Author::fullName) // α
        }

        fun fullName(separator: String?) = // β
            runtime.implementation(Author::fullName) { // γ
                "${it.firstName}${separator ?: " "}${it.lastName}" // δ
            }
    }
    ```

    - α

        Map *Author.fullName* to user implementation field

    - β

        Provides a public function.

        - In theory, the function name is arbitrary, but for readability it is recommended to keep the same as the name of *Author::fullName* at γ

        - The arguments of this function will be converted to the arguments of the *Author.fullName* field in the GraphQL Schema

        - This function has no return type

    - γ

        - *runtime* is a protected property provided by the superclass *org.babyfish.graphql.provider.EntityMapper*

        - *Auhor::fullName* represents the field for which you want to implement by your code


    - δ

        - *it* is an implicit parameter provided by the lambda expression of the runtime.implement function, representing the current Author object
    
## 1. BookStore.avgPrice

1. Add new kotlin property in entity interface

    ```kt
    interface BookStore {

        ...other properties...

        val avgPrice: BigDecimal
    }
    ```

2. Add your repository

    

3. Change BookStoreMapper

    
    
