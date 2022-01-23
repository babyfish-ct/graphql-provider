package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.ModelException
import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.graphql.provider.server.meta.QueryProp
import org.babyfish.graphql.provider.server.runtime.EntityTypeGenerator
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class QueryPropImpl internal constructor(
    private val function: KFunction<*>
): QueryProp {

    override val name: String = function.name

    override val returnType: KClass<*> = function.returnType.classifier as KClass<*>

    override var targetType: EntityType? = null

    override val isNullable: Boolean = function.returnType.isMarkedNullable

    override var isTargetNullable: Boolean = false

    override var isReference: Boolean = false

    override var isList: Boolean = false

    override var isConnection: Boolean = false

    @Suppress("UNCHECKED_CAST")
    internal fun resolve(generator: EntityTypeGenerator) {
        if (Connection::class.java.isAssignableFrom(returnType.java)) {
            val nodeType = if (function.returnType.arguments.isNotEmpty()) {
                function
                    .returnType
                    .arguments[0]
                    .type
                    ?.classifier
                    ?.let {
                        if (it is KClass<*>) {
                            it.java
                        } else {
                            null
                        }
                    }
            } else {
                GenericTypeResolver.resolveTypeArgument(returnType.java, Connection::class.java)
            } ?: throw ModelException(
                "Illegal function '${function}', its return type " +
                    "is '${Connection::class.qualifiedName}' but it's node type " +
                    "is not class"
            )
            targetType = generator[ImmutableType.of(nodeType as Class<out Immutable>)]
            isConnection = true
        } else if (Collection::class.java.isAssignableFrom(returnType.java)) {
            if (returnType != List::class) {
                throw ModelException(
                    "Illegal function '${function}', its return type " +
                        "is collection but not 'kotlin.collections.List'"
                )
            }
            val classifier = function.returnType.arguments[0].type?.classifier
            if (classifier !is KClass<*>) {
                throw ModelException(
                    "Illegal function '${function}', its return type " +
                        "is collection but its type argument is not class"
                )
            }
            if (!Immutable::class.java.isAssignableFrom(classifier.java)) {
                throw ModelException(
                    "Illegal function '${function}', its return type " +
                        "is collection but its type argument is not derived interface of " +
                        "'${Immutable::class.qualifiedName}'"
                )
            }
            isList = true
            targetType = generator[ImmutableType.of(classifier as KClass<out Immutable>)]
            isTargetNullable = function.returnType.arguments[0].type!!.isMarkedNullable
        } else if (Immutable::class.java.isAssignableFrom(returnType.java)) {
            isReference = true
            targetType = generator[ImmutableType.of(returnType as KClass<out Immutable>)]
            isTargetNullable = isNullable
        }
    }
}