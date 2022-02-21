package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.babyfish.graphql.provider.runtime.GraphQLTypeGenerator
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.sql.Entity
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

    final override val isTargetNullable: Boolean
    
    final override val targetType: ModelType?

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
        isTargetNullable = isList && function.returnType.arguments[0].type!!.isMarkedNullable

        val targetClass = when {
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
        targetType = targetClass?.let {
            convertTargetType(targetClass, generator, function)
        }
    }

    override val name: String =
        function.name

    override val returnType: KClass<*> =
        function.returnType.classifier as? KClass<*>
            ?: throw ModelException("Illegal function '${function}', its return type is not class")

    @Suppress("UNCHECKED_CAST")
    private fun convertTargetType(
        cls: KClass<*>,
        generator: GraphQLTypeGenerator,
        function: KFunction<*>
    ): ModelType {
        if (!Immutable::class.java.isAssignableFrom(cls.java)) {
            throw ModelException("Illegal function ${function}, its target type does not inherit Immutable")
        }
        if (Input::class.java.isAssignableFrom(cls.java)) {
            throw ModelException("Illegal function ${function}, its target type cannot not inherit Input")
        }
        return generator.modelTypeMap[cls as KClass<out Entity<*>>] ?:
            throw ModelException("Illegal function ${function}, its target type '${cls.qualifiedName}' is not an mapped model type")
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
