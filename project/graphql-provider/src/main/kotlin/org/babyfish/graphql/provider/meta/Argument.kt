package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import java.lang.UnsupportedOperationException
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class Argument internal constructor(
    val name: String,
    val type: KClass<*>,
    val inputMapperType: KClass<out InputMapper<*, *>>?,
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
                    val inputMapperType = inputMapperTypeOf(function, it)
                    Argument(
                        it.name!!,
                        it.type.classifier as KClass<*>,
                        inputMapperType,
                        it.type.isMarkedNullable,
                        elementType !== null || it.type.classifier == ImplicitInputs::class,
                        elementType?.classifier as KClass<*>?,
                        elementType?.isMarkedNullable ?: false
                    )
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

    @Suppress("UNCHECKED_CAST")
    fun defaultValue(): Any? =
        when {
            isNullable -> null
            ImplicitInput::class.java.isAssignableFrom(type.java) ->
                fakeImplicitInput
            ImplicitInputs::class.java.isAssignableFrom(type.java) ->
                fakeImplicitInputs
            else -> when (type) {
                Boolean::class -> false
                Char::class -> Char(0)
                Byte::class -> 0.toByte()
                Short::class -> 0.toShort()
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

    private val fakeImplicitInput = ImplicitInput<FakeEntity, FakeInputMapper>(
        FakeEntity
    ) {}

    private val fakeImplicitInputs = ImplicitInputs<FakeEntity, FakeInputMapper>(
        listOf(FakeEntity)
    ) {}

    private object FakeEntity: Entity<String> {
        override val id: String
            get() = throw UnsupportedOperationException()
    }

    private object FakeInputMapper: InputMapper<FakeEntity, String> {
        override fun InputTypeDSL<FakeEntity, String>.config() {
            throw UnsupportedOperationException()
        }
    }
}
