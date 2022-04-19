package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.ImplicitInput
import org.babyfish.graphql.provider.ImplicitInputs
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import java.lang.UnsupportedOperationException
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class Argument internal constructor(
    val parameter: KParameter,
    val inputMapperType: KClass<out InputMapper<*, *>>?,
    val elementType: KClass<*>?,
    val isList: Boolean,
    val isElementNullable: Boolean
) {
    val name: String
        get() = parameter.name!!

    val type: KClass<*>
        get() = parameter.type.classifier as KClass<*>

    val isNullable: Boolean
        get() = parameter.isOptional || parameter.type.isMarkedNullable

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
