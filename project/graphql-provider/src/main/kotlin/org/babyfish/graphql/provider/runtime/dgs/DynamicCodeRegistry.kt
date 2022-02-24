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
import org.babyfish.graphql.provider.meta.ModelProp
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

        for (prop in metaProvider.queryType.props.values) {
            val coordinates = FieldCoordinates.coordinates("Query", prop.name)
            val dataFetcher = DataFetcher {
                mono(Dispatchers.Unconfined) {
                    dataFetchers.fetch(prop, it)
                }.toFuture()
            }
            builder.dataFetcher(coordinates, dataFetcher)
        }
        for (modelType in metaProvider.modelTypes.values) {
            for (prop in modelType.declaredProps.values) {
                if (prop.isReference || prop.isList || prop.isConnection) {
                    val coordinates = FieldCoordinates.coordinates(modelType.name, prop.name)
                    val dataFetcher = DataFetcher {
                        mono(Dispatchers.Unconfined) {
                            dataFetchers.fetch(prop as ModelProp, it)
                        }.toFuture()
                    }
                    builder.dataFetcher(coordinates, dataFetcher)
                }
            }
        }
        return builder
    }
}