package org.babyfish.graphql.provider.meta

interface GraphQLType {
    val name: String
    val props: Map<String, GraphQLProp>
}