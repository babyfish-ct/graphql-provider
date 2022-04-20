package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.meta.impl.FilterImpl
import org.babyfish.graphql.provider.meta.impl.UserImplementationImpl
import org.babyfish.kimmer.sql.Entity
import org.springframework.security.core.Authentication
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

internal class DynamicConfigurationRegistry {

    private val filterMap = mutableMapOf<String, FilterImpl>()

    private val userImplementationMap = mutableMapOf<String, UserImplementationImpl>()

    fun addFilter(
        prop: KProperty1<out Entity<*>, *>,
        mapper: EntityMapper<out Entity<*>, *>,
        fn: KFunction<*>,
        raw: FilterDSL<*, *>.(Authentication?) -> Unit
    ) {
        val path = prop.path
        filterMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$fn'")
        }
        filterMap[path] = FilterImpl(
            mapper,
            fn,
            raw
        )
    }

    fun addUserImplementation(
        prop: KProperty1<out Entity<*>, *>,
        mapper: EntityMapper<out Entity<*>, *>,
        fn: KFunction<*>
    ) {
        val path = prop.path
        userImplementationMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$fn'")
        }
        userImplementationMap[path] = UserImplementationImpl(
            mapper,
            fn
        )
    }

    fun filter(prop: KProperty1<*, *>): Filter? =
        filterMap[prop.path]

    fun userImplementation(prop: KProperty1<*, *>): UserImplementation? =
        userImplementationMap[prop.path]

    companion object {

        private val KProperty1<*, *>.path: String
            get() {
                val getter = javaGetter ?: throw IllegalArgumentException("Illegal prop '$this', no java getter")
                return "${getter.declaringClass.name}.${name}"
            }
    }
}

