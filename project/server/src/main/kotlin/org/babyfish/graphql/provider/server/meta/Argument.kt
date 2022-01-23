package org.babyfish.graphql.provider.server.meta

import org.babyfish.graphql.provider.server.ModelException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class Argument internal constructor(
    val name: String,
    val type: KClass<*>,
    val list: Boolean,
    val nullable: Boolean,
    val isTargetNullable: Boolean
) {
    companion object {
        @JvmStatic
        fun of(function: KFunction<*>): List<Argument> =
            function
                .parameters
                .let { it.subList(1, it.size) }
                .map {
                    val name = it.name ?: throw ModelException("Illegal function, each parameter must have name")
                    val classifier = it.type.classifier
                    if (classifier !is KClass<*>) {
                        throw ModelException("Illegal function, the type of its parameter '$name' is not class")
                    }
                    if (classifier.java.isArray) {
                        throw ModelException("Illegal function, the type of its parameter '$name' cannot be array")
                    }
                    if (Map::class.java.isAssignableFrom(classifier.java)) {
                        throw ModelException("Illegal function, the type of its parameter '$name' cannot be map")
                    }
                    if (Collection::class.java.isAssignableFrom(classifier.java)) {
                        if (List::class != classifier) {
                            throw ModelException(
                                "Illegal function, the type of its parameter '$name' " +
                                    "is collection but not 'kotlin.collections.List'"
                            )
                        }
                    }
                    val isList = List::class == classifier
                    val type = if (isList) {
                        val targetType = it.type.arguments[0].type?.classifier
                        if (targetType !is KClass<*>) {
                            throw ModelException(
                                "Illegal function, the type of its parameter '$name' " +
                                    "is list but the type argument is not class"
                            )
                        }
                        targetType
                    } else {
                        classifier
                    }
                    val isTargetNullable = if (isList) {
                        it.type.arguments[0].type?.isMarkedNullable == true
                    } else {
                        function.returnType.isMarkedNullable
                    }
                    Argument(
                        name,
                        type,
                        false,
                        it.type.isMarkedNullable,
                        isTargetNullable
                    )
                }
    }
}
