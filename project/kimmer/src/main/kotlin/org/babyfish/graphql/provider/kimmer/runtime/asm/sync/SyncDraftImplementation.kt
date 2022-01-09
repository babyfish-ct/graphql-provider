package org.babyfish.graphql.provider.kimmer.runtime.asm.sync

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.asm.defineClass
import org.babyfish.graphql.provider.kimmer.runtime.asm.draft.draftImplementationOf
import org.springframework.asm.ClassWriter
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal fun syncDraftImplementationOf(modelType: Class<out Immutable>): Class<out SyncDraft<*>> =
    cacheLock.read {
        cacheMap[modelType]
    } ?: cacheLock.write {
        cacheMap[modelType]
            ?: createSyncDraftImplementation(modelType).let {
                cacheMap[modelType] = it
                it
            }
    }

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out SyncDraft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createSyncDraftImplementation(
    modelType: Class<out Immutable>
): Class<out SyncDraft<*>> {
    return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES).apply {
        writeType(ImmutableType.of(modelType))
    }.toByteArray().let {
        modelType.classLoader.defineClass(it)
    } as Class<out SyncDraft<*>>
}