package org.babyfish.graphql.provider.runtime.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring

@DgsComponent
open class LongScalarRegistration {

    @DgsRuntimeWiring
    open fun addScalar(builder: RuntimeWiring.Builder): RuntimeWiring.Builder {
        return builder
            .scalar(ExtendedScalars.GraphQLByte)
            .scalar(ExtendedScalars.GraphQLShort)
            .scalar(ExtendedScalars.GraphQLLong)
            .scalar(ExtendedScalars.GraphQLBigInteger)
            .scalar(ExtendedScalars.GraphQLBigDecimal)
            .scalar(ExtendedScalars.DateTime)
            .scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.Time)
    }
}