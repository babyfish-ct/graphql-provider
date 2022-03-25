package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import org.babyfish.graphql.provider.meta.MetaProvider

fun GraphQLCodeRegistry.Builder.registryDynamicCodeRegistry(
    dataFetchers: DataFetchers,
    metaProvider: MetaProvider
) {

    for (prop in metaProvider.queryType.props.values) {
        val coordinates = FieldCoordinates.coordinates("Query", prop.name)
        val dataFetcher = DataFetcher {
            dataFetchers.fetch(prop, it)
        }
        dataFetcher(coordinates, dataFetcher)
    }
    for (prop in metaProvider.mutationType.props.values) {
        val coordinates = FieldCoordinates.coordinates("Mutation", prop.name)
        val dataFetcher = DataFetcher {
            dataFetchers.fetch(prop, it)
        }
        dataFetcher(coordinates, dataFetcher)
    }
    for (modelType in metaProvider.modelTypes.values) {
        for (prop in modelType.declaredProps.values) {
            if (prop.userImplementation !== null || prop.isAssociation) {
                val coordinates = FieldCoordinates.coordinates(modelType.name, prop.name)
                val dataFetcher = DataFetcher {
                    dataFetchers.fetch(prop, it)
                }
                dataFetcher(coordinates, dataFetcher)
            }
        }
    }
}