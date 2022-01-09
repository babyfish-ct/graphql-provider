package org.babyfish.graphql.provider.kimmer.runtime.asm.sync

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.asm.BYTECODE_VERSION
import org.babyfish.graphql.provider.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.graphql.provider.kimmer.runtime.asm.syncDraftImplInternalName
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(type: ImmutableType) {

    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        syncDraftImplInternalName(type),
        null,
        draftImplInternalName(type),
        arrayOf(
            Type.getInternalName(type.draftInfo.syncType)
        )
    )

    writeConstructor(type)
    writeNew(type)

    visitEnd()
}

