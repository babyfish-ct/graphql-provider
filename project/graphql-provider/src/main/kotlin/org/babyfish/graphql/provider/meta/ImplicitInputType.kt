package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.AbstractSaveOptionsDSL

interface ImplicitInputType {
    val name: String
    val props: Map<String, ImplicitInputProp>
    val modelType: ModelType
    val saveOptionsBlock: AbstractSaveOptionsDSL<*>.() -> Unit
}