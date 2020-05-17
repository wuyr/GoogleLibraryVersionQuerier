@file:Suppress("UNCHECKED_CAST")

package com.wuyr.google_library_query

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import java.awt.EventQueue
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.DefaultListModel
import kotlin.concurrent.thread


/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-04 23:13
 */
@Suppress("ConstantConditionIf")
class QueryAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        thread {
            event.getData(CommonDataKeys.EDITOR)?.let { editor ->
                val lineStartPosition = editor.caretModel.visualLineStart
                val lineEndPosition = editor.caretModel.visualLineEnd
                val currentLineContent = editor.document.getText(TextRange(lineStartPosition, lineEndPosition))

                val semicolon = ":"
                if (!currentLineContent.contains(semicolon)) {
                    EventQueue.invokeAndWait {
                        Messages.showErrorDialog("请将光标定位到目标依赖库所在行！", "找不到有效信息")
                    }
                    return@thread
                }
                val singleQuotation = "'"
                val doubleQuotation = "\""

                val isSingleQuotation = currentLineContent.contains(singleQuotation)
                val quotation = if (isSingleQuotation) singleQuotation else doubleQuotation

                val libraryStartIndex = currentLineContent.indexOf(quotation)
                val libraryEndIndex = currentLineContent.lastIndexOf(quotation)

                if (libraryStartIndex < 0 || libraryEndIndex < libraryStartIndex) {
                    EventQueue.invokeAndWait {
                        Messages.showErrorDialog("请将光标定位到目标依赖库所在行！", "找不到有效信息")
                    }
                    return@thread
                }
                val libraryInfo = currentLineContent.substring(libraryStartIndex + 1, libraryEndIndex).split(semicolon).toTypedArray()
                val libraryGroup = libraryInfo[0]
                val libraryName = libraryInfo[1]

                try {
                    val versionList = getAvailableVersions(libraryGroup, libraryName).run {
                        if (isNullOrEmpty()) {
                            getAvailableVersions2(libraryGroup, libraryName)
                        } else this
                    }

                    versionList?.let {
                        if (it.isNotEmpty()) {
                            VersionSelectorDialog.show(DefaultListModel<String>().apply { it.forEach { e -> addElement(e) } }) { selectedVersion ->
                                val oldVersionStart = currentLineContent.lastIndexOf(semicolon)
                                val oldVersionEnd = currentLineContent.lastIndexOf(if (isSingleQuotation) singleQuotation else doubleQuotation)
                                val oldVersionString = currentLineContent.substring(oldVersionStart, oldVersionEnd)
                                val newLineContent = currentLineContent.replace(oldVersionString, semicolon + selectedVersion)

                                WriteCommandAction.runWriteCommandAction(event.project) {
                                    editor.document.replaceString(lineStartPosition, lineEndPosition, newLineContent)
                                }
                            }
                        } else {
                            EventQueue.invokeAndWait {
                                Messages.showErrorDialog("请检查关键词是否正确，以及确保该依赖库已发布到Google、Maven、Jcenter仓库上！", "找不到该依赖库")
                            }
                        }
                    } ?: EventQueue.invokeAndWait {
                        Messages.showErrorDialog("请检查关键词是否正确，以及确保该依赖库已发布到Google、Maven、Jcenter仓库上！", "找不到该依赖库")
                    }
                } catch (e: Exception) {
                    EventQueue.invokeAndWait {
                        Messages.showErrorDialog(StringWriter().apply {
                            PrintWriter(this).apply {
                                println("查找依赖库历史版本时出错！")
                                println("若不能通过以下log分析到具体原因（如出现UnknownHostException、SocketTimeoutException等Exception，请检查网络是否正常）\n请复制以下log到 https://github.com/wuyr/GoogleLibraryVersionQuerier 上提issue:\n")
                                e.printStackTrace(this)
                            }
                        }.toString(), "出错了")
                    }
                }
            }
        }
    }
}