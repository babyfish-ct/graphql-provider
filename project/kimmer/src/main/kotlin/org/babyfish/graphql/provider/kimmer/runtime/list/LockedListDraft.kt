package org.babyfish.graphql.provider.kimmer.runtime.list

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.runtime.AsyncDraftContext
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext

internal class LockedListDraft<E: Immutable>(
    draftContext: AsyncDraftContext,
    base: List<E>
): LockedList<E?>(
    SimpleListDraft(draftContext, base),
    draftContext
), ListDraft<E> {

    override val draftContext: DraftContext
        get() = (target as ListDraft<*>).draftContext

    override fun resolve(): List<E?> =
        (target as ListDraft<E>).resolve()
}