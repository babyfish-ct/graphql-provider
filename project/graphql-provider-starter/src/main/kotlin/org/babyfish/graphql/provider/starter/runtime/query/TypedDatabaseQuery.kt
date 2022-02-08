package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable

interface TypedDatabaseQuery<T: Immutable, R>: DatabaseQuery<T>, Expression<R>
