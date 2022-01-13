package org.babyfish.graphql.provider.kimmer.runtime.list

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext

internal interface ListDraft<E: Immutable>: MutableList<E?> {
    val draftContext: DraftContext
    fun resolve(): List<E?>
}