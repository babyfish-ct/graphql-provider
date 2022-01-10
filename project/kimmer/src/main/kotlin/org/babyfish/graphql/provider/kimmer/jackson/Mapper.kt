package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder

fun immutableObjectMapper(): ObjectMapper =
    jacksonMapperBuilder().addModule(ImmutableModule()).build()