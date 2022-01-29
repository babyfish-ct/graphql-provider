package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable

interface TypedDatabaseQuery<T: Immutable, R>: DatabaseQuery<T>, Expression<R>
