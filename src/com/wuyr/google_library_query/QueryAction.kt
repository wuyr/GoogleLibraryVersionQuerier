@file:Suppress("UNCHECKED_CAST")

package com.wuyr.google_library_query

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.DefaultListModel


/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-04 23:13
 */
@Suppress("ConstantConditionIf")
class QueryAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        event.getData(CommonDataKeys.EDITOR)?.let { editor ->
            val lineStartPosition = editor.caretModel.visualLineStart
            val lineEndPosition = editor.caretModel.visualLineEnd
            val currentLineContent = editor.document.getText(TextRange(lineStartPosition, lineEndPosition))

            val semicolon = ":"
            if (!currentLineContent.contains(semicolon)) {
                Messages.showErrorDialog("Please locate the line of the target library.", "Library Not Found")
                return
            }

            val singleQuotation = "'"
            val doubleQuotation = "\""

            val isSingleQuotation = currentLineContent.contains(singleQuotation)
            val quotation = if (isSingleQuotation) singleQuotation else doubleQuotation

            val libraryStartIndex = currentLineContent.indexOf(quotation)
            val libraryEndIndex = currentLineContent.lastIndexOf(quotation)

            if (libraryStartIndex < 0 || libraryEndIndex < libraryStartIndex) {
                return
            }
            val libraryInfo = currentLineContent.substring(libraryStartIndex + 1, libraryEndIndex).split(semicolon).toTypedArray()
            val libraryGroup = libraryInfo[0]
            val libraryName = libraryInfo[1]

            try {
                getAvailableVersions(libraryGroup, libraryName)?.let {
                    VersionSelectorDialog.show(DefaultListModel<String>().apply { it.forEach { addElement(it) } }) { selectedVersion ->
                        val oldVersionStart = currentLineContent.lastIndexOf(semicolon)
                        val oldVersionEnd = currentLineContent.lastIndexOf(if (isSingleQuotation) singleQuotation else doubleQuotation)
                        val oldVersionString = currentLineContent.substring(oldVersionStart, oldVersionEnd)
                        val newLineContent = currentLineContent.replace(oldVersionString, semicolon + selectedVersion)

                        WriteCommandAction.runWriteCommandAction(event.project) {
                            editor.document.replaceString(lineStartPosition, lineEndPosition, newLineContent)
                        }
                    }
                }
                        ?: Messages.showErrorDialog("Please check if the library belongs to Google.", "Library Not Found")
            } catch (e: Exception) {
                Messages.showErrorDialog(StringWriter().apply { PrintWriter(this).apply {
                    println("Please copy the following log and go to https://github.com/wuyr/GoogleLibraryVersionQuerier to create a issues:\n")
                    e.printStackTrace(this)
                } }.toString(), "Error")
            }
        }
    }
}