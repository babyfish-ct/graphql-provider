package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.dsl.BatchImplementationContext
import org.babyfish.graphql.provider.dsl.ImplementationContext

interface UserImplementation {
    var batchSize: Int?
    var single: (suspend (ImplementationContext<*>) -> Any?)?
    var batch: (suspend (BatchImplementationContext<*>) -> Map<*, *>)?
}