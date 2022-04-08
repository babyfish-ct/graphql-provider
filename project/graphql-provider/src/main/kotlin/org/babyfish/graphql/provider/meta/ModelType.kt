package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityType

interface ModelType : GraphQLType, EntityType {

    val graphql: ModelGraphQL

    override val superType: ModelType?

    override val derivedTypes: List<ModelType>

    override val idProp: ModelProp

    override val versionProp: ModelProp?

    override val declaredProps: Map<String, ModelProp>

    override val props: Map<String, ModelProp>

    override val backProps: Set<ModelProp>

    val securityPredicate: SecurityPredicate?
}

