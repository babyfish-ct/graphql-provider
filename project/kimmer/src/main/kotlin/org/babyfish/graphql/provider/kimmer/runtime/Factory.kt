package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.springframework.util.ReflectionUtils
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface Factory<T: Immutable> {
    val default: T
    fun draft(ctx: DraftContext): Draft<T>
    companion object {
        fun <T: Immutable> of(draftType: KClass<out Draft<T>>): Factory<T> =
            factoryOf(draftType.java) as Factory<T>
    }
}

private val cacheMap = mutableMapOf<Class<*>, Factory<*>>()

private val cacheLock = ReentrantReadWriteLock()

private fun factoryOf(draftType: Class<*>): Factory<*> =
    cacheLock.read {
        cacheMap[draftType]
    } ?: cacheLock.write {
        cacheMap[draftType]
            ?: createFactory(draftType).also {
                cacheMap[draftType] = it
            }
    }

private fun createFactory(draftType: Class<*>): Factory<*> {
    val immutableType = ImmutableType.fromDraftType(draftType as Class<out Draft<*>>)
    val clazz = implementationOf(immutableType.kotlinType.java)
    val default = clazz.getConstructor().newInstance() as Immutable
    return FactoryImpl<Immutable>(default)
}

private class FactoryImpl<T: Immutable>(
    override val default: T
): Factory<T> {

    override fun draft(ctx: DraftContext): Draft<T> {
        TODO("Not yet implemented")
    }
}