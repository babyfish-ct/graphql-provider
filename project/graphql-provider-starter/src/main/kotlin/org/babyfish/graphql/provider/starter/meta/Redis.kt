package org.babyfish.graphql.provider.starter.meta

import kotlin.time.Duration

interface Redis {
    val enabled: Boolean
    val timeout: Duration?
    val nullTimeout: Duration?
}