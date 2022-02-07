package org.babyfish.graphql.provider.starter.runtime

import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

class FilterExecutionContext(
    val query: DatabaseQuery<out Immutable>,
    val dependencies: MutableSet<KProperty1<out Immutable, *>>
)