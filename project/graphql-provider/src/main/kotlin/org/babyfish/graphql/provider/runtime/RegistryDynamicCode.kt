package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelProp

fun GraphQLCodeRegistry.Builder.registryDynamicCodeRegistry(
    dataFetchers: DataFetchers,
    metaProvider: MetaProvider
) {

    for (prop in metaProvider.queryType.props.values) {
        val coordinates = FieldCoordinates.coordinates("Query", prop.name)
        val dataFetcher = DataFetcher {
            mono(Dispatchers.Unconfined) {
                dataFetchers.fetch(prop, it)
            }.toFuture()
        }
        dataFetcher(coordinates, dataFetcher)
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
                dataFetcher(coordinates, dataFetcher)
            }
        }
    }
}