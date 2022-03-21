package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.input.InputTypeDSL
import org.babyfish.kimmer.sql.Entity

interface InputMapper<E: Entity<ID>, ID: Comparable<ID>> {

    fun InputTypeDSL<E, ID>.config()
}