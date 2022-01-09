package org.babyfish.graphql.provider.kimmer.meta

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface ImmutableType {

    val kotlinType: KClass<out Immutable>

    val name: String
        get() = kotlinType.qualifiedName!!

    val superTypes: Set<ImmutableType>

    val declaredProps: Map<String, ImmutableProp>

    val props: Map<String, ImmutableProp>

    val draftInfo: DraftInfo

    companion object {

        @JvmStatic
        fun of(type: KClass<out Immutable>): ImmutableType =
            getImmutableType(type.java)

        @JvmStatic
        fun of(type: Class<out Immutable>): ImmutableType =
            getImmutableType(type)

        @JvmStatic
        fun of(o: Immutable): ImmutableType =
            (o as? ImmutableSpi ?:
                throw IllegalArgumentException(
                    "does not accept argument which implements '${Immutable::class.qualifiedName}'" +
                        "but does not implement '${ImmutableSpi::class.qualifiedName}'"
                )
            ).`{type}`()

        @JvmStatic
        fun of(o: Any): ImmutableType? =
            (o as? ImmutableSpi)?.`{type}`()

        @JvmStatic
        fun fromDraftType(draftType: KClass<out Draft<*>>): ImmutableType =
            fromDraftType(draftType.java)

        @JvmStatic
        fun fromDraftType(draftType: Class<out Draft<*>>): ImmutableType =
            getImmutableTypeByDraftType(draftType)
    }
}

class DraftInfo(
    val abstractType: Class<out Draft<*>>,
    val syncType: Class<out SyncDraft<*>>?,
    val asyncType: Class<out AsyncDraft<*>>?
)

private val cacheMap = WeakHashMap<Class<*>, TypeImpl>()

private val cacheLock = ReentrantReadWriteLock()

private fun getImmutableType(type: Class<out Immutable>): ImmutableType =
    cacheLock.read {
        cacheMap[type]
    } ?: cacheLock.write {
        cacheMap[type]
            ?: Parser(cacheMap).let {
                val result = it.get(type)
                cacheMap += it.terminate()
                result
            }
    }

private fun getImmutableTypeByDraftType(draftType: Class<*>): ImmutableType =
    draftCacheLock.read {
        draftCacheMap[draftType]
    } ?: draftCacheLock.write {
        draftCacheMap[draftType]
            ?: createImmutableTypeByDraftType(draftType)?.also {
                draftCacheMap[draftType] = it
            }
    }

private fun createImmutableTypeByDraftType(draftType: Class<*>): ImmutableType {
    val ctx = DraftScanContext()
    ctx.accept(draftType)
    if (ctx.immutableJavaTypes.isEmpty()) {
        throw IllegalArgumentException("No immutable interface is extended by '${draftType.name}'")
    }
    if (ctx.immutableJavaTypes.size > 1) {
        throw IllegalArgumentException("'${
            draftType.name
        }' extends conflict immutable interfaces: ${
            ctx.immutableJavaTypes.joinToString { it.name }
        }")
    }
    val immutableType = getImmutableType(ctx.immutableJavaTypes[0] as Class<out Immutable>)
    if (immutableType.draftInfo.abstractType === draftType ||
        immutableType.draftInfo.syncType === draftType ||
        immutableType.draftInfo.asyncType === draftType
    ) {
        return immutableType
    }
    throw MetadataException(
        "'${draftType.name}' extends '${immutableType.kotlinType.qualifiedName}'," +
            "but '${immutableType.kotlinType.qualifiedName}' does not consider '${draftType.name}' as its draft type"
    )
}

private val draftCacheMap = WeakHashMap<Class<*>, ImmutableType>()

private val draftCacheLock = ReentrantReadWriteLock()

private class DraftScanContext {

    val immutableJavaTypes = mutableListOf<Class<*>>()

    fun accept(type: Class<*>) {
        if (type.isInterface) {
            if (Draft::class.java.isAssignableFrom(type)) {
                for (superItf in type.interfaces) {
                    accept(superItf)
                }
            } else if (Immutable::class.java.isAssignableFrom(type)) {
                if (Immutable::class.java !== type) {
                    acceptImmutable(type)
                }
                for (superItf in type.interfaces) {
                    accept(superItf)
                }
            }
        }
    }

    private fun acceptImmutable(type: Class<*>) {
        val itr = immutableJavaTypes.iterator()
        while (itr.hasNext()) {
            val existingType = itr.next()
            if (type.isAssignableFrom(existingType)) {
                return
            }
            if (existingType.isAssignableFrom(type)) {
                itr.remove()
            }
        }
        immutableJavaTypes.add(type)
    }
}

