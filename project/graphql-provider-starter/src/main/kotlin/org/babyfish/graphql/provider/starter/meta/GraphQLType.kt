package org.babyfish.graphql.provider.starter.meta

interface GraphQLType {
    val name: String
    val declaredProps: Map<String, EntityProp>
}