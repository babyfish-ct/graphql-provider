package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.GraphQLProp
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.*

internal abstract class AbstractRootPropImpl internal constructor(
    generator: GraphQLTypeGenerator,
    function: KFunction<*>
): GraphQLProp {

    final override val isReference: Boolean

    final override val isList: Boolean

    final override val isConnection: Boolean

    final override val isNullable: Boolean

    final override val isElementNullable: Boolean

    final override val targetType: KClass<*>?

    final override val targetEntityType: EntityType?

    init {

        val pc = primaryClass(function)

        isConnection = Connection::class == pc
        isList = Collection::class == pc
        isReference = Immutable::class == pc

        if (isList && List::class != function.returnType.classifier) {
            error(
                "Illegal function '${function}', collection function " +
                    "can only return 'kotlin.collections.List'")
        }
        isNullable = function.returnType.isMarkedNullable
        isElementNullable = isList && function.returnType.arguments[0].type!!.isMarkedNullable

        targetType = when {
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
        targetEntityType = targetType?.let {
            convertTargetEntityType(targetType, generator, function)
        }
    }

    override val name: String =
        function.name

    override val returnType: KClass<*> =
        function.returnType.classifier as? KClass<*>
            ?: throw ModelException("Illegal function '${function}', its return type is not class")

    @Suppress("UNCHECKED_CAST")
    private fun convertTargetEntityType(
        cls: KClass<*>,
        generator: GraphQLTypeGenerator,
        function: KFunction<*>
    ): EntityType {
        if (!Immutable::class.java.isAssignableFrom(cls.java)) {
            throw ModelException("Illegal function ${function}, its target type does not inherit Immutable")
        }
        if (Input::class.java.isAssignableFrom(cls.java)) {
            throw ModelException("Illegal function ${function}, its target type cannot not inherit Input")
        }
        return generator[ImmutableType.of(cls as KClass<out Immutable>)]
    }
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
