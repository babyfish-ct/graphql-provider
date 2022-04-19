package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Input
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class Arguments(
    val function: KFunction<*>?,
    private val list: List<Argument>
) : Iterable<Argument> {

    val size: Int
        get() = list.size

    override fun iterator(): Iterator<Argument> =
        list.iterator()

    operator fun get(index: Int): Argument =
        list[index]

    companion object {

        @JvmStatic
        val EMPTY = Arguments(null, emptyList())

        @JvmStatic
        fun of(function: KFunction<*>): Arguments =
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
                    val inputMapperType = inputMapperTypeOf(function, it)
                    Argument(
                        it,
                        inputMapperType,
                        elementType?.classifier as KClass<*>?,
                        elementType !== null || it.type.classifier == ImplicitInputs::class,
                        elementType?.isMarkedNullable ?: false
                    )
                }
                .let {
                    Arguments(function, it)
                }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        private fun inputMapperTypeOf(
            function: KFunction<*>,
            parameter: KParameter
        ): KClass<out InputMapper<*, *>>? {
            val cls = parameter.type.classifier as? KClass<*>
            if (cls != ImplicitInput::class && cls != ImplicitInputs::class) {
                return null
            }
            val inputMapperType = parameter.type.arguments[1].type!!.classifier
                as KClass<out InputMapper<*, *>>
            if (inputMapperType.typeParameters.isNotEmpty()) {
                throw ModelException(
                    "Illegal function '$function', " +
                        "its parameter '${parameter.name}' is ${cls.qualifiedName}, " +
                        "but the second type argument is not a derived input mapper " +
                        "type without type arguments"
                )
            }
            return inputMapperType
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
            if (Immutable::class.java.isAssignableFrom(classifier.java)) {
                if (!Input::class.java.isAssignableFrom(classifier.java)) {
                    err(
                        "cannot be any immutable types except ${Input::class.qualifiedName}"
                    )
                }
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
                    validateAndGetElementType(
                        function,
                        name,
                        it,
                        true
                    )
                }
            } else {
                null
            }
        }
    }
}