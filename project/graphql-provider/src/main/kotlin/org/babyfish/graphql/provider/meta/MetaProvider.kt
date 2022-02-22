package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass

class MetaProvider internal constructor(
    val queryType: QueryType,
    val modelTypes: Map<KClass<out Entity<*>>, ModelType>
)