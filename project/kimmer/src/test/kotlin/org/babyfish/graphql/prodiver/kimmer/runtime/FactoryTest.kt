package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.Book
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import org.babyfish.graphql.provider.kimmer.runtime.draftImplementationOf
import kotlin.test.Test

class FactoryTest {

    @Test
    fun create() {
        val book = Factory.of(BookDraft.Sync::class).default
        println(Immutable.isLoaded(book, Book::name))
        println(Immutable.getThrowable(book, Book::name))
        println(ImmutableType.of(book).name)

        val type = draftImplementationOf(BookDraft::class.java)
        val draft = type.getConstructor(DraftContext::class.java, Book::class.java)
            .newInstance(DraftContext(), book)
            as BookDraft<Book>
        draft.name = "a"
        println(draft.name)
        val draft2 = type.getConstructor(DraftContext::class.java, Book::class.java)
            .newInstance(DraftContext(), draft)
            as BookDraft<Book>
        println(draft2.name)
        draft2.name = "b"
        println(draft2.name)
    }
}