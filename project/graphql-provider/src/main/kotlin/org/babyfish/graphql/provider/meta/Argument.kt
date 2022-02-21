package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.ModelException
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.produce
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
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

    @Suppress("UNCHECKED")
    fun defaultValue(): Any? =
        if (isNullable) {
            null
        } else {
            when (type) {
                Boolean::class -> false
                Char::class -> 0 as Char
                Byte::class -> 0 as Byte
                Short::class -> 0 as Short
                Int::class -> 0L
                Long::class -> 0F
                Double::class -> 0.0
                BigInteger::class -> BigInteger.ZERO
                BigDecimal::class -> BigDecimal.ZERO
                String::class -> ""
                UUID::class -> UUID.randomUUID()
                LocalDate::class -> LocalDate.MIN
                LocalDateTime::class -> LocalDateTime.MIN
                is Collection<*> -> emptyList<Any>()
                is Immutable -> produce(type as KClass<Immutable>) {}
                else -> error("Internal bug, cannot determine default value for class '$type'")
            }
        }
}
