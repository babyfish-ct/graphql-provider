package org.babyfish.graphql.provider.starter.meta

interface GraphQLType {
    val name: String
    val props: Map<String, GraphQLProp>
}