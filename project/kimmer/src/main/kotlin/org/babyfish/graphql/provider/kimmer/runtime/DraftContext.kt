package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import java.util.*

internal interface DraftContext {

    fun <T: Immutable> toDraft(obj: T?): Draft<T>?

    fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>?

    fun <T: Immutable> resolve(obj: T?): T?

    fun <E: Immutable> resolve(obj: List<E>?): List<E>?
}

internal fun draftContext(): DraftContext =
    DraftContextImpl()

private class DraftContextImpl: DraftContext {

    private val objDraftMap = IdentityHashMap<Immutable, Draft<*>>()

    private val listDraftMap = IdentityHashMap<List<*>, ListDraft<*>>()

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

    override fun <T : Immutable> resolve(obj: T?): T? {
        if (obj === null) {
            return null
        }
        val draft = obj as? Draft<*> ?: objDraftMap[obj]
        if (draft === null) {
            return obj
        }
        return (draft as DraftSpi).`{resolve}`() as T
    }

    override fun <E : Immutable> resolve(list: List<E>?): List<E>? {
        if (list === null) {
            return null
        }
        val draft = list as? ListDraft<*> ?: listDraftMap[list]
        if (draft === null) {
            return list
        }
        return draft.resolve() as List<E>
    }
}



