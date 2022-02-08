package org.babyfish.graphql.provider.starter.meta.impl

import java.lang.RuntimeException

internal class NoReturnValue: RuntimeException(
    "The wrapper functions of Query.queryReference, Query.queryList and Query.queryConnection " +
        "cannot be invoked directly in your code " +
        "because they can only be invoked by the framework internally."
)