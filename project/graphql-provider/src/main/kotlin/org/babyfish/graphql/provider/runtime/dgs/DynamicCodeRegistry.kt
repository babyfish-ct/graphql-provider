package org.babyfish.graphql.provider.runtime.dgs

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.language.ImplementingTypeDefinition
import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@DgsComponent
class DynamicCodeRegistry {

    @DgsCodeRegistry
    open fun registry(
        builder: GraphQLCodeRegistry.Builder,
        registry: TypeDefinitionRegistry
    ): GraphQLCodeRegistry.Builder {

        val queryType = registry.getType("Query").orElse(null) as ImplementingTypeDefinition
        if (queryType !== null) {
            for (field in queryType.fieldDefinitions) {
                val coordinates = FieldCoordinates.coordinates("Query", "findBooks");
                val dataFetcher = DataFetcher {
                    mono(Dispatchers.Unconfined) {
                        "Implementation of Query.${field.name}"
                    }.toFuture()
                }
                builder.dataFetcher(coordinates, dataFetcher)
            }
        }
        return builder
    }
}