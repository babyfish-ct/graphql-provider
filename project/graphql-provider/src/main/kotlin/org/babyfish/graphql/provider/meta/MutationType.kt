package org.babyfish.graphql.provider.meta

interface MutationType: GraphQLType {

    override val props: Map<String, MutationProp>
}