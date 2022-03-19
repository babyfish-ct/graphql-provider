package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass

class MetaProvider internal constructor(
    val queryType: QueryType,
    val mutationType: MutationType,
    val modelTypes: Map<KClass<out Entity<*>>, ModelType>,
    val rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    val allImplicitInputTypes: List<ImplicitInputType>
)