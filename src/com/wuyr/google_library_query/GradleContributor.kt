package com.wuyr.google_library_query

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons

/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-05 02:58
 */
class GradleContributor : CompletionContributor() {

    private var needShow = false
    private val acceptFilesType = arrayOf(".gradle")

    override fun beforeCompletion(context: CompletionInitializationContext) {
        needShow = acceptFilesType.any { context.file.name.endsWith(it) }
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (needShow) {
            val lineStartPosition = parameters.editor.caretModel.visualLineStart
            val lineEndPosition = parameters.editor.caretModel.visualLineEnd
            val currentLineContent = parameters.editor.document.getText(TextRange(lineStartPosition, lineEndPosition)).replace("\n".toRegex(), "").trim()
            if (currentLineContent.length < 4) {
                return
            }
            currentLineContent.split("\\s+".toRegex()).let {
                if (it.isNotEmpty()) {
                    val keyword = if (it.size == 1) {
                        it.first()
                    } else {
                        it.last()
                    }
                    matchingLibraries(keyword).forEach { e ->
                        result.addElement(e.toLookupElement())
                    }
                    matchingLibraries2(if (keyword.contains(":")) keyword else ":$keyword").forEach { e ->
                        result.addElement(e.toLookupElement())
                    }
                }
            }
            needShow = false
        }
        super.fillCompletionVariants(parameters, result)
    }

    override fun invokeAutoPopup(position: PsiElement, typeChar: Char) = true

    private fun Pair<String, String>.toLookupElement() = LookupElementBuilder.create(first).bold()
            .withIcon(PlatformIcons.LIBRARY_ICON).withTypeText(second, true).withInsertHandler { context, item ->
                val lineStartPosition = context.editor.caretModel.visualLineStart
                val lineEndPosition = context.editor.caretModel.visualLineEnd
                val currentLineContent = context.editor.document.getText(TextRange(lineStartPosition, lineEndPosition))
                var additionalIndex = 0
                run {
                    currentLineContent.forEachIndexed { index, char ->
                        if (!char.isWhitespace()) {
                            additionalIndex = index
                            return@run
                        }
                    }
                }
                WriteCommandAction.runWriteCommandAction(context.project) {
                    context.document.replaceString(lineStartPosition + additionalIndex, lineEndPosition - 1, item.lookupString)
                }
            }.withTypeIconRightAligned(true)
}