package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable

interface DatabaseSubQuery<P: Immutable, T: Immutable>: AbstractDatabaseQuery<T> {
    val parentTable: Table<P>
}
