package org.babyfish.graphql.provider.starter.meta

interface QueryType : GraphQLType {

    override val props: Map<String, QueryProp>
}