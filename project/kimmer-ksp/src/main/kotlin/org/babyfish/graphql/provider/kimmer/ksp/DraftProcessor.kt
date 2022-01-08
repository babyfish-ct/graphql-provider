package org.babyfish.graphql.provider.kimmer.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class DraftProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true
        val sysTypes = SysTypes.of(resolver) ?: return mutableListOf()
        val modelMap = findModelMap(resolver, sysTypes)
        if (modelMap.isNotEmpty()) {
            for ((file, declarations) in modelMap) {
                DraftGenerator(codeGenerator, sysTypes, file, declarations)
                    .generate(resolver.getAllFiles().toList())
            }
        }
        return emptyList()
    }

    private fun findModelMap(resolver: Resolver, sysTypes: SysTypes): Map<KSFile, List<KSClassDeclaration>> {

        val modelMap = mutableMapOf<KSFile, MutableList<KSClassDeclaration>>()
        for (file in resolver.getAllFiles()) {
            for (classDeclaration in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                if (classDeclaration.classKind == ClassKind.INTERFACE &&
                    classDeclaration.typeParameters.isEmpty() &&
                    classDeclaration.qualifiedName !== null &&
                    sysTypes.immutableType.isAssignableFrom(classDeclaration.asStarProjectedType()) &&
                    !sysTypes.draftType.isAssignableFrom(classDeclaration.asStarProjectedType())
                ) {
                    modelMap.computeIfAbsent(file) {
                        mutableListOf()
                    } += classDeclaration
                }
            }
        }
        return modelMap
    }
}
