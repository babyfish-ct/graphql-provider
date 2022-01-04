package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Connection
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.EntityPropImpl
import org.babyfish.graphql.provider.server.meta.RedisDependency
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class RedisConfiguration<E> internal constructor(
    private val dependencyMap: MutableMap<String, EntityPropImpl.RedisDependencyImpl>
) {

    fun dependsOn(prop: KProperty1<E, *>) {
        dependsOn(RedisDependency.Category.SCALAR, prop).dependencyMap
    }

    fun <T: Immutable> dependsOnReference(prop: KProperty1<E, T?>, block: RedisConfiguration<T>.() -> Unit) {
        RedisConfiguration<T>(
            dependsOn(RedisDependency.Category.REFERENCE, prop).dependencyMap
        ).block()
    }

    fun <T: Immutable> dependsOnList(prop: KProperty1<E, List<T>>, block: RedisConfiguration<T>.() -> Unit) {
        RedisConfiguration<T>(
            dependsOn(RedisDependency.Category.LIST, prop).dependencyMap
        ).block()
    }

    fun <T: Immutable> dependsOnConnection(prop: KProperty1<E, out Connection<T>>, block: RedisConfiguration<T>.() -> Unit) {
        RedisConfiguration<T>(
            dependsOn(RedisDependency.Category.CONNECTION, prop).dependencyMap
        ).block()
    }

    private fun dependsOn(category: RedisDependency.Category, prop: KProperty1<*, *>): EntityPropImpl.RedisDependencyImpl {
        if (this.dependencyMap.containsKey(prop.name)) {
            throw IllegalArgumentException("duplicated dependency prop name: '${prop.name}'")
        }
        val classifier = prop.returnType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("The prop '$prop' must return class")
        val isReference = Immutable::class.java.isAssignableFrom(classifier.java)
        val isList = !isReference && List::class.java.isAssignableFrom(classifier.java)
        val isConnection = !isReference && !isList && Connection::class.java.isAssignableFrom(classifier.java)
        val isScalar = !isReference && !isList && !isConnection
        when (category) {
            RedisDependency.Category.SCALAR -> if (!isScalar) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not scalar dependency")
            }
            RedisDependency.Category.REFERENCE -> if (!isReference) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not reference dependency")
            }
            RedisDependency.Category.LIST -> if (!isList) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not list dependency")
            }
            RedisDependency.Category.CONNECTION -> if (!isConnection) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not connection dependency")
            }
        }
        val dependency = EntityPropImpl.RedisDependencyImpl(category, prop)
        this.dependencyMap[prop.name] = dependency
        return dependency
    }
}