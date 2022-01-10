package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import java.util.*
import kotlin.reflect.KClass

internal interface DraftContext {

    fun <T: Immutable> createDraft(draftType: KClass<out Draft<T>>, base: T?): Draft<T>

    fun <T: Immutable> toDraft(obj: T?): Draft<T>?

    fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>?

    fun <T: Immutable> resolve(obj: T?): T?

    fun <E: Immutable> resolve(obj: List<E>?): List<E>?
}

internal abstract class AbstractDraftContext: DraftContext {

    private val objDraftMap = IdentityHashMap<Immutable, Draft<*>>()

    private val listDraftMap = IdentityHashMap<List<*>, ListDraft<*>>()

    override fun <T : Immutable> createDraft(draftType: KClass<out Draft<T>>, base: T?): Draft<T> {
        val raw = base
            ?: this.createFactory(ImmutableType.fromDraftType(draftType))
                .let {
                    (it as Factory<T>).create()
                }
        return toDraft(raw)!!
    }

    override fun <T: Immutable> toDraft(obj: T?): Draft<T>? {
        if (obj === null) {
            return null
        }
        if (obj is Draft<*>) {
            if ((obj as DraftSpi).`{draftContext}`() !== this) {
                throw IllegalArgumentException("Cannot accept draft object created by another DraftContext")
            }
            return obj as Draft<T>
        }
        return objDraftMap.computeIfAbsent(obj) {
            val factory = this.createFactory(ImmutableType.fromInstance(obj)) as Factory<T>
            factory.createDraft(this, obj)
        } as Draft<T>
    }

    override fun <E: Immutable> toDraft(list: List<E>?): MutableList<E>? {
        if (list === null) {
            return null
        }
        if (list is ListDraft<*>) {
            if (list.draftContext !== this) {
                throw IllegalArgumentException("Cannot accept draft list created by another DraftContext")
            }
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

    protected abstract fun createFactory(immutableType: ImmutableType): Factory<*>
}

internal class SyncDraftContext: AbstractDraftContext() {

    override fun createFactory(immutableType: ImmutableType): Factory<*> =
        Factory.of(immutableType.draftInfo.syncType)
}

internal class AsyncDraftContext: AbstractDraftContext() {
    override fun createFactory(immutableType: ImmutableType): Factory<*> =
        Factory.of(immutableType.draftInfo.asyncType)
}
