package org.babyfish.graphql.provider.starter.runtime.dgs

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.QueryService
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@DgsComponent
class DynamicCodeRegistry(
    private val queryServices: List<QueryService>,
    private val entityMappers: List<EntityMapper<*>>
) {

    @DgsCodeRegistry
    open fun registry(
        builder: GraphQLCodeRegistry.Builder,
        registry: TypeDefinitionRegistry
    ): GraphQLCodeRegistry.Builder {

        for (queryService in queryServices) {
            for (function in queryService::class.declaredFunctions) {
                if (function.visibility == KVisibility.PUBLIC) {
                    val coordinates = FieldCoordinates.coordinates("Query", "findBooks");
                    val dataFetcher = DataFetcher {
                        mono(Dispatchers.Unconfined) {
                            "Implementation of ${function.name}"
                        }.toFuture()
                    }
                    builder.dataFetcher(coordinates, dataFetcher)
                }
            }
        }
        return builder
    }
}