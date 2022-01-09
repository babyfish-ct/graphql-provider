package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.draftImplInternalName
import org.babyfish.graphql.provider.kimmer.runtime.implInternalName
import org.springframework.asm.Type

interface data class GeneratorArgs(
    val draftJavaType: Class<*>,
    val immutableType: ImmutableType
) {
    val draftImplInternalName = draftImplInternalName(draftJavaType)
    val draftInternalName = Type.getInternalName(draftJavaType)
    val draftDescriptor = Type.getDescriptor(draftJavaType)
    val draftContextInternalName = Type.getInternalName(DraftContext::class.java)
    val draftContextDescriptor = Type.getDescriptor(DraftContext::class.java)
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)
    val modelDescriptor = Type.getDescriptor(immutableType.kotlinType.java)
    val modelImplInternalName = implInternalName(immutableType.kotlinType.java)
    val modelImplDescriptor = "L$modelImplInternalName;"
}