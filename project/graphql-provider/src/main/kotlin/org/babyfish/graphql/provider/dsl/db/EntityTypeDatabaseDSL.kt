package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.UserIdGenerator
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class EntityTypeDatabaseDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor() {

    var tableName: String? = null

    internal var _idGenerator: IdGenerator? = null

    var versionProp: KProperty1<E, Int>? = null

    fun idGenerator(generator: IdGenerator) {
        _idGenerator = generator
    }

    fun idGenerator(generator: UserIdGenerator<ID>) {
        _idGenerator = generator
    }

}