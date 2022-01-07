package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import java.util.*

interface DraftContext {

    fun <T: Immutable> toDraft(obj: T?): Draft<T>?

    fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>?
}

internal fun draftContext(): DraftContext =
    DraftContextImpl()

private class DraftContextImpl: DraftContext {

    private val objDraftMap = IdentityHashMap<Immutable, Draft<*>>()

    private val listDraftMap = IdentityHashMap<List<*>, MutableList<*>>()

    override fun <T: Immutable> toDraft(obj: T?): Draft<T>? {
        if (obj === null || obj is Draft<*>) {
            return obj as Draft<T>?
        }
        return objDraftMap.computeIfAbsent(obj) {
            val factory = Factory.of(ImmutableType.of(obj).draftInfo.abstractType) as Factory<T>
            factory.createDraft(this, obj)
        } as Draft<T>
    }

    override fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>? {
        if (list === null || list is ListDraft<*>) {
            return list as MutableList<E>
        }
        return listDraftMap.computeIfAbsent(list) {
            ListDraft(this, list)
        } as MutableList<E>
    }
}



