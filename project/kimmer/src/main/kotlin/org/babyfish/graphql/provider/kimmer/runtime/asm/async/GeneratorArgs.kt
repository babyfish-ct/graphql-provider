package org.babyfish.graphql.provider.kimmer.runtime.asm.async

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.asm.asyncDraftImplInternalName
import org.babyfish.graphql.provider.kimmer.runtime.asm.draftImplInternalName
import org.springframework.asm.Type

data class GeneratorArgs(
    val immutableType: ImmutableType
) {
    val internalName = asyncDraftImplInternalName(immutableType)
    val rawDraftImplInternalName = draftImplInternalName(immutableType)
    val rawDraftImplDescriptor = "L$rawDraftImplInternalName;"
    val modelDescriptor: String = Type.getDescriptor(immutableType.kotlinType.java)
}