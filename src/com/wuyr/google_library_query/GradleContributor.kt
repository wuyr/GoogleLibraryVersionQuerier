package com.wuyr.google_library_query

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
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
        println(parameters.process.isAutopopupCompletion)
        if (needShow) {
            //TODO: 在这里记住原来的行内容
            val lineStartPosition = parameters.editor.caretModel.visualLineStart
            val lineEndPosition = parameters.editor.caretModel.visualLineEnd
            val currentLineContent = parameters.editor.document.getText(TextRange(lineStartPosition, lineEndPosition)).replace("\n".toRegex(), "").trim()
            if (currentLineContent.length < 4) {
                return
            }
            currentLineContent.split("\\s+".toRegex()).let {
                if (it.isNotEmpty()) {
                    var removeImplementation = false
                    val keyword = if (it.size == 1) {
                        it.first()
                    } else {
                        if (it.first() == "implementation") removeImplementation = true
                        it.last()
                    }
                    //TODO： 记住keyword的startPos和endPos，等下插入时remove掉
                    if (keyword.contains(":")) {
                        matchingLibraries2(keyword).forEach { e ->
                            result.addElement(e.toLookupElement(removeImplementation))
                        }
                    } else {
                        matchingLibraries(keyword).forEach { e ->
                            result.addElement(e.toLookupElement(removeImplementation))
                        }

                    }
                }
            }
            needShow = false
        }
        super.fillCompletionVariants(parameters, result)
    }

    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        println("invokeAutoPopup")
//        return super.invokeAutoPopup(position, typeChar)
        return needShow
    }

    private fun Pair<String, String>.toLookupElement(removeImplementation: Boolean) = LookupElementBuilder
            .create(if (removeImplementation) first.replace("implementation ", "") else first)
            .bold().withIcon(PlatformIcons.LIBRARY_ICON).withTypeText(second, true).setInsertHandler { p0, p1 ->
                val lineStartPosition = p0.editor.caretModel.visualLineStart
                val lineEndPosition = p0.editor.caretModel.visualLineEnd
                val currentLineContent = p0.editor.document.getText(TextRange(lineStartPosition, lineEndPosition)).replace("\n".toRegex(), "").trim()
                p1.lookupString
                println(currentLineContent)
            }
            .withTypeIconRightAligned(true)
}