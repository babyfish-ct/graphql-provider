# Kimmer: [immer](https://github.com/immerjs/immer) for kotlin

Kotlin's data class does not match the goal of this framework. For better development experience, we introduce [https://github.com/immerjs/immer](https://github.com/immerjs/immer) for Kotlin

## 1. User defines immutate data model
```kt
interface Node: Immutable {
    val id: String
}

interface BookStore: Node {
    val name: String
    val books: List<Book>
    val avgPrice: BigDecimal
}

interface Book: Node {
    val name: String
    val price: BigDecimal
    val store: BookStore?
    val authors: List<Author>
}

interface Author: Node {
    val name: String
    val books: List<Book>
}
```


## 2. The gradle plugin of the framework generates mutable data model
```kt
interface NodeDraft<T: Node>: Node, Draft<T> {
    override var id: String
    interface Sync: NodeDraft<Node>, SyncDraft<Node>
    interface Async: NodeDraft<Node>, AsyncDraft<Node>
}

interface BookStoreDraft<T: BookStore>: BookStore, NodeDraft<T> {
    override var books: MutableList<BookDraft<out Book>>
    override var avgPrice: BigDecimal
    fun books(): MutableList<BookDraft<out Book>> // Get or create
    interface Sync: BookStoreDraft<BookStore>, SyncDraft<BookStore>
    interface Async: BookStoreDraft<BookStore>, AsyncDraft<BookStore>
}

interface BookDraft<T: Book>: Book, NodeDraft<T> {
    override var name: String
    override var price: BigDecimal
    override var store: BookStore?
    override var authors: MutableList<AuthorDraft<out Author>>
    fun store(): BookStore // Get or create
    fun authors(): MutableList<AuthorDraft<out Book>> // Get or create
    interface Sync: BookDraft<Book>, SyncDraft<Book>
    interface Async: BookDraft<Book>, AsyncDraft<Book>
}

interface AuthorDraft<T: Author>: Author, NodeDraft<T> {
    override var name: String
    override var books: MutableList<BookDraft<out Book>>
    fun books(): MutableList<BookDraft<out Book>> // Get or create
    interface Sync: AuthorDraft<Author>, SyncDraft<Author>
    interface Async: AuthorDraft<Author>, AsyncDraft<Author>
}
```

1. User interface uses **"val"** to define readonly properties, but the  generated interface uses **"var"** to define writable properties.
2. User interface uses immutable list **"kotlin.List"**, but the generated interface uses writable list **"kotlin.MutableList"**.

## 3. Usage
1. Create new data from scratch
```kt
val book = new(BookDraft.Sync::class) {
    id = "00001"
    name = "Learning GraphQL"
    authors() += new(AuthorDraft.Sync::class) {
        id = "00002"
        name = "Jim"
    }
    authors() += new(AuthorDraft.Sync::class) {
        id = "00003"
        name = "Kate"
    }
}
```
2. Create new data based on existing data
```kt
val book2 = new(BookDraft.Sync::class, book) {
    name += "!"
    for (author in authors) {
        author.name += "!"
    }
}
```

## 4. Partial objects
When creating the object, it is not necessary to assign values to all fields of draft object, unassigned fields are called unloaded fields. In the Kotlin language, trying to access unloaded fields will cause an exception, but JSON serialization will not cause exception, it will ignore them. This design implements dynamic shape required by GraphQL.

----------------------

[< Back](https://github.com/babyfish-ct/graphql-provider)
