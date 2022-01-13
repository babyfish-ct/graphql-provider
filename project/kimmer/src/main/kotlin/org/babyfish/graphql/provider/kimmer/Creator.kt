package org.babyfish.graphql.provider.kimmer

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.babyfish.graphql.provider.kimmer.runtime.AsyncDraftContext
import org.babyfish.graphql.provider.kimmer.runtime.DraftSpi
import org.babyfish.graphql.provider.kimmer.runtime.SyncDraftContext
import java.lang.UnsupportedOperationException
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

inline fun<T: Immutable> new(type: KClass<T>): SyncCreator<T> =
    SyncCreator(type)

inline fun<T: Immutable> newAsync(type: KClass<T>): AsyncCreator<T> =
    AsyncCreator(type)

@JvmInline
value class SyncCreator<T: Immutable>(
    val type: KClass<T>
)

@JvmInline
value class AsyncCreator<T: Immutable>(
    val type: KClass<T>
)

@JvmInline
value class SyncDraftCreator<T: Immutable>(
    val type: KClass<T>
)

@JvmInline
value class AsyncDraftCreator<T: Immutable>(
    val type: KClass<T>
)

fun <T: Immutable> produce(
    type: KClass<T>,
    base: T? = null,
    block: Draft<T>.() -> Unit
): T =
    draftContextLocal
        .get()
        ?.let {
            produce(it, type, base, block)
        } ?: SyncDraftContext()
        .let {
            draftContextLocal.set(it)
            try {
                produce(it, type, base, block)
            } finally {
                draftContextLocal.remove()
            }
        }

fun <T: Immutable, D: SyncDraft<T>> produceDraft(
    type: KClass<T>,
    base: T? = null,
    block: D.() -> Unit
): D =
    draftContextLocal
        .get()
        ?.let {
            val draft = it.createDraft(type, base) as D
            draft.block()
            return draft
        } ?: throw UnsupportedOperationException(
            "'produceDraft' can only be used in the lambda of 'produce'"
        )

private inline fun <T: Immutable> produce(
    ctx: SyncDraftContext,
    type: KClass<T>,
    base: T?,
    block: Draft<T>.() -> Unit
): T {
    val draft = ctx.createDraft(type, base)
    draft.block()
    return (draft as DraftSpi).`{resolve}`() as T
}

private val draftContextLocal = ThreadLocal<SyncDraftContext>()

suspend fun <T: Immutable> produceAsync(
    type: KClass<T>,
    base: T?,
    block: suspend Draft<T>.() -> Unit
): T =
    currentCoroutineContext()[DraftContextElement]?.ctx?.let {
        produceAsync(it, type, base, block)
    } ?: AsyncDraftContext().let {
        withContext(DraftContextElement(it)) {
            produceAsync(it, type, base, block)
        }
    }

suspend fun <T: Immutable, D: AsyncDraft<T>> produceDraftAsync(
    type: KClass<T>,
    base: T? = null,
    block: suspend D.() -> Unit
): D =
    currentCoroutineContext()[DraftContextElement]
        ?.let { it.ctx }
        ?.let {
            val draft = it.createDraft(type, base) as D
            draft.block()
            return draft
        } ?: throw UnsupportedOperationException(
        "'produceDraftAsync' can only be used in the lambda of 'produceAsync'"
    )

private suspend inline fun <T: Immutable> produceAsync(
    ctx: AsyncDraftContext,
    type: KClass<T>,
    base: T?,
    block: suspend Draft<T>.() -> Unit
): T {
    val draft = ctx.createDraft(type, base)
    draft.block()
    return (draft as DraftSpi).`{resolve}`() as T
}

private data class DraftContextElement(
    val ctx: AsyncDraftContext
) : AbstractCoroutineContextElement(DraftContextElement) {
    companion object Key : CoroutineContext.Key<DraftContextElement>
}