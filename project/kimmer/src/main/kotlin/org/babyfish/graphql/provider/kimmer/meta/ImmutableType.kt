package org.babyfish.graphql.provider.kimmer.meta

import org.babyfish.graphql.provider.kimmer.Immutable
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

    companion object {

        fun of(type: KClass<out Immutable>): ImmutableType =
            getImmutableType(type.java)
    }
}

private val cacheMap = WeakHashMap<Class<*>, ImmutableType>()

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


