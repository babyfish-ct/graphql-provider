package com.babyfish.graphql.provider

import com.babyfish.graphql.provider.mapper.entity.AuthorMapper
import com.babyfish.graphql.provider.mapper.entity.BookMapper
import com.babyfish.graphql.provider.mapper.entity.BookStoreMapper
import com.babyfish.graphql.provider.mapper.input.BookDeepTreeInputMapper
import com.babyfish.graphql.provider.mapper.input.BookInputMapper
import com.babyfish.graphql.provider.mapper.input.BookShallowTreeInputMapper
import com.babyfish.graphql.provider.mutation.BookMutation
import com.babyfish.graphql.provider.query.BookQuery
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.cfg.KimmerSQLAutoConfiguration
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderAutoConfiguration
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.Test

@SpringBootTest(classes = [
    KimmerSQLAutoConfiguration::class,
    GraphQLProviderAutoConfiguration::class,
    AppConfiguration::class,

    BookStoreMapper::class,
    BookMapper::class,
    AuthorMapper::class,

    BookInputMapper::class,
    BookShallowTreeInputMapper::class,
    BookDeepTreeInputMapper::class,

    BookQuery::class,
    BookMutation::class
])
@EnableWebFluxSecurity
@RunWith(SpringRunner::class)
open class AppTest {

    @Autowired
    private lateinit var metaProvider: MetaProvider

    @Test
    fun test() {
        println(metaProvider.mutationType)
        println(metaProvider.allImplicitInputTypes)
    }
}