package org.babyfish.graphql.provider.kimmer.runtime.asm.sync

import org.babyfish.graphql.provider.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeNew() {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "new-IaNvmcQ",
        "($KCLASS_DESCRIPTOR)$KCLASS_DESCRIPTOR"
    ) {
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)
    }
}