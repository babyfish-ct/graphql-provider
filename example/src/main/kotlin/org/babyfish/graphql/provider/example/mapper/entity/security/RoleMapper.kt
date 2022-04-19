package org.babyfish.graphql.provider.example.mapper.entity.security

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.graphql.provider.example.model.security.Role
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component
class RoleMapper: EntityMapper<Role, UUID>() {

    override fun EntityTypeDSL<Role, UUID>.config() {

        db {
            idGenerator(UUIDIdGenerator())
        }

        mappedList(Role::appUsers, AppUser::roles)
    }
}