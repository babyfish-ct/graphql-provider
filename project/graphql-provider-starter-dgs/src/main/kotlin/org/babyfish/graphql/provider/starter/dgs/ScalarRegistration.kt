package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.language.StringValue
import graphql.scalars.ExtendedScalars
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.RuntimeWiring
import org.babyfish.graphql.provider.meta.MetaProvider
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.*

@DgsComponent
open class ScalarRegistration(
    private val metaProvider: MetaProvider
) {

    @DgsRuntimeWiring
    open fun addScalar(builder: RuntimeWiring.Builder): RuntimeWiring.Builder=
        builder.apply {
            for (scalarType in metaProvider.scalarKotlinTypes) {
                when (scalarType) {
                    UUID::class -> scalar(uuid)
                    Char::class -> scalar(ExtendedScalars.GraphQLChar)
                    Byte::class -> scalar(ExtendedScalars.GraphQLByte)
                    Short::class -> scalar(ExtendedScalars.GraphQLShort)
                    Long::class -> scalar(ExtendedScalars.GraphQLLong)
                    BigInteger::class -> scalar(ExtendedScalars.GraphQLBigInteger)
                    BigDecimal::class -> scalar(ExtendedScalars.GraphQLBigDecimal)
                    OffsetDateTime::class -> scalar(ExtendedScalars.DateTime)
                    LocalDate::class -> scalar(ExtendedScalars.Date)
                    OffsetTime::class -> scalar(ExtendedScalars.Time)
                    URL::class -> scalar(ExtendedScalars.Time)
                    Locale::class -> scalar(ExtendedScalars.Locale)
                    Any::class -> scalar(ExtendedScalars.Json)
                }
            }
        }
}

private val uuid = GraphQLScalarType.newScalar()
    .name("UUID")
    .description("UUID")
    .coercing(
        object : Coercing<UUID, String> {
            override fun serialize(dataFetcherResult: Any): String {
                if (dataFetcherResult is UUID) {
                    return dataFetcherResult.toString()
                }
                throw CoercingSerializeException("Not a valid UUID")
            }

            override fun parseValue(input: Any): UUID =
                UUID.fromString(input.toString())

            override fun parseLiteral(input: Any): UUID {
                if (input is StringValue) {
                    return UUID.fromString(input.value)
                }
                throw CoercingParseLiteralException("Value is not a valid UUID")
            }
        }
    )
    .build()