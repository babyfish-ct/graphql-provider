package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.Book
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import kotlin.test.Test

class FactoryTest {

    @Test
    fun create() {
        val book = Factory.of(BookDraft.Sync::class).default
        println(Immutable.isLoaded(book, Book::name))
        println(Immutable.getThrowable(book, Book::name))
        println(ImmutableType.of(book).name)
    }
}