package org.babyfish.graphql.provider.runtime

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable
import kotlin.reflect.KProperty1

class FilterExecutionContext(
    val query: Filterable<out Entity<*>, *>,
    val dependencies: MutableSet<KProperty1<out Entity<*>, *>>
)