package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.MutationType

internal class MutationTypeImpl : MutationType {

    override val name: String
        get() = "Mutation"

    override val props = mutableMapOf<String, MutationPropImpl>()
}