package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.runtime.asm.loadedName
import org.babyfish.graphql.provider.kimmer.runtime.asm.visitPropNameSwitch
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeUnload(args: GeneratorArgs) {

    val mutableLocal = 2

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{unload}",
        "(Ljava/lang/String;)V"
    ) {
        visitMutableModelStorage(mutableLocal, args)

        visitPropNameSwitch(args.immutableType, {
            visitVarInsn(Opcodes.ALOAD, 1)
        }) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, mutableLocal)
            visitInsn(Opcodes.ICONST_0)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.modelImplInternalName,
                loadedName(prop),
                "Z"
            )
        }
        visitInsn(Opcodes.RETURN)
    }
}