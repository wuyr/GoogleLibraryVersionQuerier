package com.wuyr.google_library_query

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.util.PlatformIcons
import java.awt.EventQueue
import java.io.StringReader
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-05 02:58
 */
class GradleContributor : CompletionContributor() {

    private var needShow = false
    private var elementList = ArrayList<Pair<String, String>>()
    private val acceptFilesType = arrayOf(".gradle")
    private val threadPool = Executors.newSingleThreadExecutor()
    private var runningQueryThread: Future<*>? = null
    private var lastCurrentLineContent = ""

    override fun beforeCompletion(context: CompletionInitializationContext) {
        needShow = acceptFilesType.any { context.file.name.endsWith(it) }
        if (needShow) {
            needShow = isInDependenciesBlock(context)
        } else {
            cancelQuery()
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
            if (elementList.isNotEmpty()) {
                if (currentLineContent.contains(lastCurrentLineContent)) {
                    elementList.forEach { e -> result.addElement(e.toLookupElement()) }
                } else {
                    cancelQuery()
                    result.stopHere()
                }
                elementList.clear()
                needShow = false
            } else {
                if (currentLineContent.length < 4 || currentLineContent == lastCurrentLineContent) {
                    return
                }
                lastCurrentLineContent = currentLineContent
                startQuery(parameters.editor)
            }
        } else {
            if (elementList.isNotEmpty()) elementList.clear()
        }
        super.fillCompletionVariants(parameters, result)
    }

    private fun startQuery(editor: Editor) {
        lastCurrentLineContent.split("\\s+".toRegex()).let { lineContent ->
            if (lineContent.isNotEmpty()) {
                val keyword = lineContent.run { if (size == 1) first() else last() }
                runningQueryThread?.cancel(true)
                var task: Future<*>? = null
                task = threadPool.submit {
                    task?.let { task ->
                        val result = matchingLibraries(keyword) +
                                matchingLibraries2(if (keyword.contains(":")) keyword else ":$keyword")
                        if (!task.isCancelled) {
                            elementList.clear()
                            elementList.addAll(result)
                            EventQueue.invokeLater {
                                editor.project?.let { project ->
                                    AutoPopupController.getInstance(project)
                                            .autoPopupMemberLookup(editor, null)
                                }
                            }
                            runningQueryThread = null
                        }
                    }
                }.also { runningQueryThread = it }
            }
        }
    }

    private fun cancelQuery() {
        runningQueryThread?.let { if (!it.isCancelled && !it.isDone) it.cancel(true) }
        elementList.clear()
        needShow = false
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
            }
}