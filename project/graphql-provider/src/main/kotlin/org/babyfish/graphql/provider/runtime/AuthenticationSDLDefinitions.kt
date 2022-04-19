package org.babyfish.graphql.provider.runtime

import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeName
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationResult

internal data class AuthenticationSDLDefinitions(
    val objectTypes: List<ObjectTypeDefinition>,
    val queryFields: List<FieldDefinition>,
    val mutationFields: List<FieldDefinition>
)

internal fun createAuthenticationSDLDefinitions(
    properties: GraphQLProviderProperties
): AuthenticationSDLDefinitions {
    val objectTypes = listOf(
        generateAuthenticationResultType()
    )
    val queryFields = mutableListOf<FieldDefinition>()
    val mutationFields = mutableListOf<FieldDefinition>()
    if (properties.security.jwt.enabled) {
        val api = properties.security.api
        if (api.graphql) {
            api.login.trim().takeIf { it.isNotEmpty() }?.let {
                queryFields += FieldDefinition.newFieldDefinition().apply {
                    name(it)
                    type(TypeName(AUTHENTICATION_RESULT))
                    inputValueDefinition(InputValueDefinition(api.usernameArgName, TypeName("String")))
                    inputValueDefinition(InputValueDefinition("password", TypeName("String")))
                }.build()
            }
            api.updatePassword.trim().takeIf { it.isNotEmpty() }?.let {
                mutationFields += FieldDefinition.newFieldDefinition().apply {
                    name(it)
                    type(TypeName(AUTHENTICATION_RESULT))
                    inputValueDefinition(InputValueDefinition("oldPassword", TypeName("String")))
                    inputValueDefinition(InputValueDefinition("newPassword", TypeName("String")))
                }.build()
            }
            api.refreshAccessToken.trim().takeIf { it.isNotEmpty() }?.let {
                mutationFields += FieldDefinition.newFieldDefinition().apply {
                    name(it)
                    type(TypeName(AUTHENTICATION_RESULT))
                    inputValueDefinition(InputValueDefinition("refreshToken", TypeName("String")))
                }.build()
            }
        }
    }
    return AuthenticationSDLDefinitions(objectTypes, queryFields, mutationFields)
}

private fun generateAuthenticationResultType(): ObjectTypeDefinition =
    ObjectTypeDefinition.newObjectTypeDefinition().apply {
        name(AUTHENTICATION_RESULT)
        fieldDefinition(
            FieldDefinition(
                JwtAuthenticationResult::accessToken.name,
                TypeName("String")
            )
        )
        fieldDefinition(
            FieldDefinition(
                JwtAuthenticationResult::refreshToken.name,
                TypeName("String")
            )
        )
    }.build()

private val AUTHENTICATION_RESULT = JwtAuthenticationResult::class.simpleName