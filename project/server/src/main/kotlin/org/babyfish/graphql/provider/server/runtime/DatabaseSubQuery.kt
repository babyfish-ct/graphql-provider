package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable

interface DatabaseSubQuery<P: Immutable, T: Immutable, R>: DatabaseQuery<T>, Expression<R> {
    val parentTable: Table<P>
}