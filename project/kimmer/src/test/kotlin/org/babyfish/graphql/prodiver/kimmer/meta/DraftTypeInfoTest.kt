package org.babyfish.graphql.prodiver.kimmer.meta

import org.babyfish.graphql.provider.kimmer.runtime.DraftTypeInfo
import kotlin.test.Test

class DraftTypeInfoTest {

    @Test
    fun test() {
        println(DraftTypeInfo.of(BookDraft.Async::class))
        println(DraftTypeInfo.of(BookDraft::class))
    }
}