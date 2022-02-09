package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable

interface DatabaseQuery<T: Immutable>: AbstractDatabaseQuery<T>

