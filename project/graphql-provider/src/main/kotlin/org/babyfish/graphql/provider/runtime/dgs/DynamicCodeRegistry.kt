package org.babyfish.graphql.provider.runtime.dgs

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.DataFetchers
import org.babyfish.graphql.provider.runtime.registryDynamicCodeRegistry

@DgsComponent
internal class DynamicCodeRegistry(
    private val dataFetchers: DataFetchers,
    private val metaProvider: MetaProvider,
) {
    @DgsCodeRegistry
    open fun registry(
        builder: GraphQLCodeRegistry.Builder,
        typeDefinitionRegistry: TypeDefinitionRegistry
    ): GraphQLCodeRegistry.Builder =
        builder.apply {
            registryDynamicCodeRegistry(dataFetchers, metaProvider)
        }
}