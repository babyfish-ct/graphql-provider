package org.babyfish.graphql.provider.starter.meta

import org.babyfish.kimmer.meta.ImmutableType

class MetaProvider internal constructor(
    val queryType: QueryType,
    val entityTypes: Map<ImmutableType, EntityType>
)