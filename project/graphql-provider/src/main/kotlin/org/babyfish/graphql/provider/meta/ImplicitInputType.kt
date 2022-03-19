package org.babyfish.graphql.provider.meta

interface ImplicitInputType {
    val name: String
    val props: Map<String, ImplicitInputProp>
    val modelType: ModelType
}