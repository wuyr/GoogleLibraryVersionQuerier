package com.wuyr.google_library_query

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.util.PlatformIcons
import java.io.StringReader

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
        if (needShow) {
            needShow = isInDependenciesBlock(context)
        }
    }

    private fun isInDependenciesBlock(context: CompletionInitializationContext): Boolean {
        val lines = StringReader(context.editor.document.text).readLines()
        var currentLineNumber = context.editor.run { document.getLineNumber(caretModel.offset) }
        var braceStartCount = 0
        var braceEndCount = 0
        while (currentLineNumber >= 0 && currentLineNumber < lines.size) {
            val currentLine = lines[currentLineNumber]
            braceStartCount += currentLine.count { it == '{' }
            braceEndCount += currentLine.count { it == '}' }
            if (currentLine.let { it.contains("dependencies") && it.contains('{') }) {
                return braceStartCount - braceEndCount == 1
            }
            currentLineNumber--
        }
        return false
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
                    val keyword = it.run { if (size == 1) first() else last() }

                    matchingLibraries(keyword).forEach { e -> result.addElement(e.toLookupElement()) }

                    matchingLibraries2(if (keyword.contains(":")) keyword else ":$keyword").forEach { e ->
                        result.addElement(e.toLookupElement())
                    }
                }
            }
            needShow = false
        }
        super.fillCompletionVariants(parameters, result)
    }

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