package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.Book
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import org.babyfish.graphql.provider.kimmer.runtime.draftContext
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
            .newInstance(draftContext(), book)
            as BookDraft<Book>
        println(draft.hashCode())
        draft.name = "a"
        println(draft.name)
        println(draft.hashCode())
        val draft2 = type.getConstructor(DraftContext::class.java, Book::class.java)
            .newInstance(draftContext(), draft)
            as BookDraft<Book>
        println(draft2.name)
        println(draft.hashCode())
        println(draft == draft2)
        draft2.name = "b"
        println(draft == draft2)
        println(draft2.name)
        println(draft2.hashCode())
    }
}