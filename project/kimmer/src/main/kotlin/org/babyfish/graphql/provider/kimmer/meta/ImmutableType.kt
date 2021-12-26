package org.babyfish.graphql.provider.kimmer.meta

import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface ImmutableType {

    val kotlinType: KClass<*>

    val superTypes: Set<ImmutableType>

    val declaredProps: Map<String, ImmutableProp>

    val props: Map<String, ImmutableProp>

    companion object {

        fun of(type: KClass<*>): ImmutableType =
            getImmutableType(type.java)
    }
}

private val cacheMap = WeakHashMap<Class<*>, ImmutableType>()

private val cacheLock = ReentrantReadWriteLock()

private fun getImmutableType(type: Class<*>): ImmutableType =
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


