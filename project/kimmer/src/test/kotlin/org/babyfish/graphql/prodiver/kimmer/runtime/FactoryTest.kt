package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.Author
import org.babyfish.graphql.prodiver.kimmer.meta.AuthorDraft
import org.babyfish.graphql.prodiver.kimmer.meta.Book
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.*
import kotlin.test.Test

class FactoryTest {

    @Test
    fun create() {
        val bookFactory = Factory.of(BookDraft.Sync::class)
        val authorFactory = Factory.of(AuthorDraft.Sync::class)
        val author = authorFactory.create()

        val book = bookFactory.create()
        println(Immutable.isLoaded(book, Book::name))
        println(Immutable.getThrowable(book, Book::name))
        println(ImmutableType.of(book).name)

        val ctx = draftContext()
        val draft = bookFactory.createDraft(ctx, book) as BookDraft<Book>
        draft.name = "book"
        draft.store().name = "store"
        draft.authors().apply {
            val author1 = authorFactory.createDraft(ctx, author) as AuthorDraft<Author>
            author1.name = "Jim"
            val author2 = authorFactory.createDraft(ctx, author) as AuthorDraft<Author>
            author2.name = "Kate"
            this += author1
            this += author2
        }
        val newBook = (draft as DraftSpi).`{resolve}`() as Book
        println(newBook.store?.name)
    }
}