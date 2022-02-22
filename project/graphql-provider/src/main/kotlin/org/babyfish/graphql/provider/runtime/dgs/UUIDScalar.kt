package org.babyfish.graphql.provider.runtime.dgs

import com.netflix.graphql.dgs.DgsScalar
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import java.util.*

@DgsScalar(name="UUID")
class UUIDScalar: Coercing<UUID, String> {

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