package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.graphql.provider.starter.runtime.expression.Expression
import org.babyfish.kimmer.Immutable

interface TypedDatabaseSubQuery<P: Immutable, T: Immutable, R> : DatabaseSubQuery<P, T>, Expression<R>