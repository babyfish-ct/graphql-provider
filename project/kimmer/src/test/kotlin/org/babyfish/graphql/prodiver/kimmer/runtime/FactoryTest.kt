package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import kotlin.test.Test

class FactoryTest {

    @Test
    fun create() {
        val book = Factory.of(BookDraft.Sync::class).default
        println(book)
    }
}