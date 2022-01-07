package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import java.util.*

interface DraftContext {

    fun <T: Immutable> toDraft(obj: T?): Draft<T>?

    fun <E: Immutable> toDraft(list: List<Immutable>?): MutableList<E>?
}

internal fun draftContext(): DraftContext =
    DraftContextImpl()

private class DraftContextImpl: DraftContext {

    private val objDraftMap = IdentityHashMap<Any, Any>()

    private val listDraftMap = IdentityHashMap<Any, Any>()

    override fun <T : Immutable> toDraft(obj: T?): Draft<T>? {
        if (obj === null || obj is Draft<*>) {
            return obj as Draft<T>
        }
        TODO()
    }

    override fun <E : Immutable> toDraft(list: List<Immutable>?): MutableList<E>? {
        TODO("Not yet implemented")
    }
}



