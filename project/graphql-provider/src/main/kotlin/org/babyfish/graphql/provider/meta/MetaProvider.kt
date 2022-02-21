package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.meta.ImmutableType

class MetaProvider internal constructor(
    val queryType: QueryType,
    val modelTypes: Map<ImmutableType, ModelType>
)