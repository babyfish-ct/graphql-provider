package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
abstract class AbstractRedisDependencyDSL<E> internal constructor() {

    fun dependsOn(prop: KProperty1<E, *>) {
        dependsOn(Kind.SCALAR, prop)
    }

    fun <T: Immutable> dependsOnReference(
        prop: KProperty1<E, T?>,
        block: (AbstractRedisDependencyDSL<T>.() -> Unit) ?= null
    ) {
        dependsOn(Kind.REFERENCE, prop).let {
            RedisDependencyDSL<T>(it).apply {
                block?.invoke(this)
            }
        }
    }

    fun <T: Immutable> dependsOnList(
        prop: KProperty1<E, List<T>>,
        block: (AbstractRedisDependencyDSL<T>.() -> Unit)? = null
    ) {
        dependsOn(Kind.LIST, prop).let {
            RedisDependencyDSL<T>(it).apply {
                block?.invoke(this)
            }
        }
    }

    fun <T: Immutable> dependsOnConnection(
        prop: KProperty1<E, out Connection<T>>,
        block: (AbstractRedisDependencyDSL<T>.() -> Unit) ?= null
    ) {
        dependsOn(Kind.CONNECTION, prop).let {
            RedisDependencyDSL<T>(it).apply {
                block?.invoke(this)
            }
        }
    }

    internal abstract val dependencyMap: MutableMap<String, EntityPropImpl.RedisDependencyImpl>

    private fun dependsOn(
        kind: Kind,
        prop: KProperty1<*, *>
    ): EntityPropImpl.RedisDependencyImpl {
        if (this.dependencyMap.containsKey(prop.name)) {
            throw MetadataException("Cannot depends on '${prop.name}' twice")
        }
        val classifier = prop.returnType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("The prop '$prop' must return class")
        val isReference = Immutable::class.java.isAssignableFrom(classifier.java)
        val isList = !isReference && List::class.java.isAssignableFrom(classifier.java)
        val isConnection = !isReference && !isList && Connection::class.java.isAssignableFrom(classifier.java)
        val isScalar = !isReference && !isList && !isConnection
        when (kind) {
            Kind.SCALAR -> if (!isScalar) {
                throw MetadataException("Cannot add '$prop' as scalar dependency because it's not scalar dependency")
            }
            Kind.REFERENCE -> if (!isReference) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not reference dependency")
            }
            Kind.LIST -> if (!isList) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not list dependency")
            }
            Kind.CONNECTION -> if (!isConnection) {
                throw IllegalArgumentException("Cannot add '$prop' as scalar dependency because it's not connection dependency")
            }
        }
        val dependency = EntityPropImpl.RedisDependencyImpl(prop)
        this.dependencyMap[prop.name] = dependency
        return dependency
    }
    
    private enum class Kind {
        SCALAR,
        REFERENCE,
        LIST,
        CONNECTION,
    }
}