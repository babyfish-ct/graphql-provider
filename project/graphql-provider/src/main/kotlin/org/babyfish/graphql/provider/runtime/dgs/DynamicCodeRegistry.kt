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
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.DataFetchers
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@DgsComponent
internal class DynamicCodeRegistry(
    private val dataFetchers: DataFetchers,
    private val metaProvider: MetaProvider,
) {

    @DgsCodeRegistry
    open fun registry(
        builder: GraphQLCodeRegistry.Builder,
        registry: TypeDefinitionRegistry
    ): GraphQLCodeRegistry.Builder {

        val queryType = registry.getType("Query").orElse(null) as ImplementingTypeDefinition
        for (field in queryType.fieldDefinitions) {
            val coordinates = FieldCoordinates.coordinates("Query", "findBooks");
            val dataFetcher = DataFetcher {
                mono(Dispatchers.Unconfined) {
                    dataFetchers.fetch(
                        metaProvider.queryType.props[field.name]
                            ?: error("Non mapped query field: '${field.name}'"),
                        it
                    )
                }.toFuture()
            }
            builder.dataFetcher(coordinates, dataFetcher)
        }
        return builder
    }
}