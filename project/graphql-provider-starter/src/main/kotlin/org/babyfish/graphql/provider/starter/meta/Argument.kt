package org.babyfish.graphql.provider.starter.meta

import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.Input
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class Argument internal constructor(
    val name: String,
    val type: KClass<*>,
    val isNullable: Boolean,
    val isList: Boolean,
    val elementType: KClass<*>?,
    val isElementNullable: Boolean
) {
    companion object {
        @JvmStatic
        fun of(function: KFunction<*>): List<Argument> =
            function
                .parameters
                .let { it.subList(1, it.size) }
                .map {
                    val elementType = validateAndGetElementType(
                        function,
                        it.name!!,
                        it.type,
                        false
                    )
                    Argument(
                        it.name!!,
                        it.type.classifier as KClass<*>,
                        it.type.isMarkedNullable,
                        elementType !== null,
                        elementType?.classifier as KClass<*>?,
                        elementType?.isMarkedNullable ?:false
                    )
                }

        @JvmStatic
        private fun validateAndGetElementType(
            function: KFunction<*>,
            name: String,
            type: KType,
            isListElement: Boolean
        ): KType? {
            val err: (message: String) -> Nothing = {
                if (isListElement) {
                    throw ModelException("Illegal function, the type of the element of its parameter 'name' $it")
                } else {
                    throw ModelException("Illegal function, the type of its parameter 'name' $it")
                }
            }
            val classifier = type.classifier as? KClass<*>
                ?: err("is not class")
            if (classifier.java.isArray) {
                err("cannot be array")
            }
            if (Map::class.java.isAssignableFrom(classifier.java)) {
                err("cannot be map")
            }
            if (Immutable::class.java.isAssignableFrom(classifier.java) &&
                !Input::class.java.isAssignableFrom(classifier.java)) {
                err("cannot be type inherits 'Immutable' but does not inherits 'Input'")
            }
            if (Collection::class.java.isAssignableFrom(classifier.java)) {
                if (isListElement) {
                    err("can not be list whose type argument is specified by nested list")
                }
                if (List::class != classifier) {
                    err("is collection but not 'kotlin.collections.List'")
                }
            }
            val isList = List::class == classifier
            return if (isList) {
                (type.arguments[0].type ?: error("Internal bug")).also {
                    validateAndGetElementType(function, name, type, true)
                }
            } else {
                null
            }
        }
    }
}
