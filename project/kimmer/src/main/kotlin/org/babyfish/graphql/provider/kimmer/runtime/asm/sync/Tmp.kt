package org.babyfish.graphql.provider.kimmer.runtime.asm.sync

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter

fun main(args: Array<String>) {

    ClassReader(SyncDraft::class.qualifiedName)
        .accept(TraceClassVisitor(PrintWriter(System.out)), 0)

    ClassReader(AsyncDraft::class.qualifiedName)
        .accept(TraceClassVisitor(PrintWriter(System.out)), 0)
}