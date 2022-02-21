package org.babyfish.graphql.provider.meta

interface QueryType : GraphQLType {

    override val props: Map<String, QueryProp>
}