package com.wuyr.google_library_query

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.util.PlatformIcons

/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-05 02:58
 */
class GradleContributor : CompletionContributor() {

    private var needShow = false
    private val acceptFilesName = arrayOf("build.gradle")

    override fun beforeCompletion(context: CompletionInitializationContext) {
        needShow = acceptFilesName.contains(context.file.name)
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
                    var removeImplementation = false
                    val keyword = if (it.size == 1) {
                        it.first()
                    } else {
                        if (it.first() == "implementation") removeImplementation = true
                        it.last()
                    }
                    matchingLibraries(keyword).forEach { e ->
                        result.addElement(e.toLookupElement(removeImplementation))
                    }
                }
            }
            needShow = false
        }
        super.fillCompletionVariants(parameters, result)
    }

    private fun Pair<String, String>.toLookupElement(removeImplementation: Boolean) = LookupElementBuilder
            .create(if (removeImplementation) first.replace("implementation ", "") else first)
            .bold().withIcon(PlatformIcons.LIBRARY_ICON).withTypeText(second, true)
            .withTypeIconRightAligned(true)
}