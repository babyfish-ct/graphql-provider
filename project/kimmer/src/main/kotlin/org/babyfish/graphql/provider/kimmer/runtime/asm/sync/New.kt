package org.babyfish.graphql.provider.kimmer.runtime.asm.sync

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.asm.*
import org.babyfish.graphql.provider.kimmer.runtime.asm.DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.IMMUTABLE_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.KFUNCTION1_DESCRITPOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.SYNC_DRAFT_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeNew(type: ImmutableType) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "new",
        "($KCLASS_DESCRIPTOR$IMMUTABLE_DESCRIPTOR$KFUNCTION1_DESCRITPOR)$SYNC_DRAFT_DESCRIPTOR"
    ) {

        val draftSlot = 4

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            draftImplInternalName(type),
            "{draftContext}",
            DRAFT_CONTEXT_DESCRIPTOR
        )
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            DRAFT_CONTEXT_INTERNAL_NAME,
            "createDraft",
            "($KCLASS_DESCRIPTOR$IMMUTABLE_DESCRIPTOR)$DRAFT_DESCRIPTOR",
            true
        )
        visitVarInsn(Opcodes.ASTORE, draftSlot)

        visitVarInsn(Opcodes.ALOAD, 3)
        visitVarInsn(Opcodes.ALOAD, draftSlot)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            KFUNCTION1_INTERNAL_NAME,
            "invoke",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            true
        )

        visitVarInsn(Opcodes.ALOAD, draftSlot)
        visitInsn(Opcodes.ARETURN)
    }
}