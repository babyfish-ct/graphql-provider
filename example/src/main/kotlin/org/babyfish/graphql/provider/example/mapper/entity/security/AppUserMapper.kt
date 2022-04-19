package org.babyfish.graphql.provider.example.mapper.entity.security

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppUserMapper: EntityMapper<AppUser, UUID>() {

    override fun EntityTypeDSL<AppUser, UUID>.config() {

        db {
            idGenerator(UUIDIdGenerator())
        }

        list(AppUser::roles) {
            db {
                middleTable {
                    tableName = "APP_USER_ROLE_MAPPING"
                    joinColumnName = "APP_USER_ID"
                    targetJoinColumnName = "ROLE_ID"
                }
            }
        }
    }
}