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
public interface BookStoreDraft<T : BookStore> : BookStore, NodeDraft<T> {
  public override var name: String
  public override var books: List<Book>
  public override var avgPrice: BigDecimal
  public fun books(): MutableList<BookDraft<out Book>>
  ... other members ...
}

public interface BookDraft<T : Book> : Book, NodeDraft<T> {
  public override var name: String
  public override var price: BigDecimal
  public override var store: BookStore?
  public override var authors: List<Author>
  public fun store(): BookStoreDraft<out BookStore>
  public fun authors(): MutableList<AuthorDraft<out Author>>
  ... other members ...
}

public interface BookDraft<T : Book> : Book, NodeDraft<T> {
  public override var name: String
  public override var price: BigDecimal
  public override var store: BookStore?
  public override var authors: List<Author>
  public fun store(): BookStoreDraft<out BookStore>
  public fun authors(): MutableList<AuthorDraft<out Author>>
  ... other members ...
}

public interface AuthorDraft<T : Author> : Author, NodeDraft<T> {
  public override var name: String
  public override var books: List<Book>
  public fun books(): MutableList<BookDraft<out Book>>
  ... other members ...
}
```

1. User interface uses **"val"** to define readonly properties, but the  generated interface uses **"var"** to define writable properties.
2. User interface uses immutable list **"kotlin.List"**, but the generated interface uses writable list **"kotlin.MutableList"**.

## 3. Usage
1. Create new data from scratch
```kt
val book = new(Book::class).by {
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
val book2 = new(Book::class). by(book) {
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
