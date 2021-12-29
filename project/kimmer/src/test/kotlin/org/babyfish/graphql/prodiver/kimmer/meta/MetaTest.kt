package org.babyfish.graphql.prodiver.kimmer.meta

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import kotlin.test.Test

class MetaTest {

    @Test
    fun test() {
        ImmutableType.of(Book::class)
    }
}