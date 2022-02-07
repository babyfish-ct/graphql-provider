package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.Argument
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.GraphQLProp
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.*

abstract class GraphQLPropImpl internal constructor(
    function: KFunction<*>
): GraphQLProp {

    final override val isReference: Boolean

    final override val isList: Boolean

    final override val isConnection: Boolean

    final override val isNullable: Boolean

    final override val isTargetNullable: Boolean

    private var _targetType: Any?

    init {

        val pc = primaryClass(function)

        isConnection = Connection::class == pc
        isList = Collection::class == pc
        isReference = Immutable::class == pc

        if (isList && List::class != pc) {
            error(
                "Illegal function '${function}', collection function " +
                    "can only return 'kotlin.collections.List'")
        }
        isNullable = function.returnType.isMarkedNullable
        isTargetNullable = isList && function.returnType.arguments[0].type!!.isMarkedNullable

        _targetType = when {
            isReference -> function.returnType.classifier as KClass<*>
            isList -> function.returnType.arguments[0].type!!.classifier as KClass<*>
            isConnection -> if (function.returnType.arguments.isNotEmpty()) {
                function.returnType.arguments[0].type!!.classifier as KClass<*>
            } else {
                GenericTypeResolver.resolveTypeArgument(
                    (function.returnType.classifier as KClass<*>).java,
                    Connection::class.java
                )!!.kotlin
            }
            else -> null
        }
    }

    override val name: String =
        function.name

    override val returnType: KClass<*> =
        function.returnType.classifier as? KClass<*>
            ?: throw ModelException("Illegal function '${function}', its return type is not class")

    override val targetType: EntityType?
        get() = _targetType as EntityType?

    override val arguments: List<Argument> = Argument.of(function)
}

private fun primaryClass(function: KFunction<*>): KClass<*>? {
    var primaryClass: KClass<*>? = null
    val cls = function.returnType.classifier as? KClass<*>
        ?: error("Illegal function '${function}', the return type is not class")
    for (c in PRIMARY_CLASSES) {
        if (c.java.isAssignableFrom(cls.java)) {
            if (primaryClass !== null) {
                throw ModelException(
                    "Illegal function '${function}', its return type cannot be " +
                        "both '${primaryClass.qualifiedName}' and '${c.qualifiedName}'"
                )
            }
            primaryClass = c
        }
    }
    return primaryClass
}

private val PRIMARY_CLASSES = arrayOf(Immutable::class, Collection::class, Connection::class)
