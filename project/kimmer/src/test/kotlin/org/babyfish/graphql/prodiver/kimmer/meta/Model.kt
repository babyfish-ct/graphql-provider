package org.babyfish.graphql.prodiver.kimmer.meta

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import java.math.BigDecimal

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

interface NodeDraft<T: Node>: Node, Draft<T> {
    override var id: String
    interface Sync: NodeDraft<Node>, SyncDraft<Node>
    interface Async: NodeDraft<Node>, AsyncDraft<Node>
}

interface BookStoreDraft<T: BookStore>: BookStore, NodeDraft<T> {
    override var books: MutableList<BookDraft<out Book>>
    override var avgPrice: BigDecimal
    interface Sync: BookStoreDraft<BookStore>, SyncDraft<BookStore>
    interface Async: BookStoreDraft<BookStore>, AsyncDraft<BookStore>
}

interface BookDraft<T: Book>: Book, NodeDraft<T> {
    override var name: String
    override var price: BigDecimal
    override var store: BookStore?
    override var authors: MutableList<AuthorDraft<out Author>>
    interface Sync: BookDraft<Book>, SyncDraft<Book>
    interface Async: BookDraft<Book>, AsyncDraft<Book>
}

interface AuthorDraft<T: Author>: Author, NodeDraft<T> {
    override var name: String
    override var books: MutableList<BookDraft<out Book>>
    interface Sync: AuthorDraft<Author>, SyncDraft<Author>
    interface Async: AuthorDraft<Author>, AsyncDraft<Author>
}