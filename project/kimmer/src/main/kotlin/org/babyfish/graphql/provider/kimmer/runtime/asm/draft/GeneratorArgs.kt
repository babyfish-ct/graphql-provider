package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.graphql.provider.kimmer.runtime.asm.implInternalName
import org.springframework.asm.Type

interface data class GeneratorArgs(
    val immutableType: ImmutableType
) {
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)
    val modelDescriptor = Type.getDescriptor(immutableType.kotlinType.java)
    val draftInternalName = Type.getInternalName(immutableType.draftInfo.abstractType)
    val draftDescriptor = Type.getDescriptor(immutableType.draftInfo.abstractType)
    val modelImplInternalName = implInternalName(immutableType)
    val modelImplDescriptor = "L$modelImplInternalName;"
    val draftImplInternalName = draftImplInternalName(immutableType)
}