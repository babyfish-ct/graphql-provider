package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.babyfish.graphql.provider.security.cfg.toGraphQLErrorOrNull
import org.springframework.stereotype.Component

@Component
open class DgsExceptionHandler : DataFetcherExceptionHandler {

    private val defaultHandler = DefaultDataFetcherExceptionHandler()

    override fun onException(
        handlerParameters: DataFetcherExceptionHandlerParameters
    ): DataFetcherExceptionHandlerResult =
        handlerParameters
            .exception
            .toGraphQLErrorOrNull()
            ?.let {
                DataFetcherExceptionHandlerResult.newResult()
                    .error(it)
                    .build();
            }
            ?: defaultHandler.onException(handlerParameters)
}