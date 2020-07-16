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

    //版本降序
    private val versionComparator = Comparator<String> { v1, v2 ->
        //对比索引记录
        var v1CompareCursor = 0
        var v2CompareCursor = 0
        val digitRange = '0'..'9'
        while (v1CompareCursor < v1.length && v2CompareCursor < v2.length) {
            //数字结束索引
            var v1DigitSegmentEndIndex = v1CompareCursor
            for (index in v1CompareCursor until v1.length) {
                if (v1[index] !in digitRange) break
                v1DigitSegmentEndIndex++
            }
            var v2DigitSegmentEndIndex = v2CompareCursor
            for (index in v2CompareCursor until v2.length) {
                if (v2[index] !in digitRange) break
                v2DigitSegmentEndIndex++
            }
            if (v1DigitSegmentEndIndex > v1CompareCursor && v2DigitSegmentEndIndex > v2CompareCursor) {
                //数字长度
                val v1DigitSegmentCount = v1DigitSegmentEndIndex - v1CompareCursor
                val v2DigitSegmentCount = v2DigitSegmentEndIndex - v2CompareCursor
                if (v1DigitSegmentCount != v2DigitSegmentCount)
                //长度不相等
                    return@Comparator v2DigitSegmentCount - v1DigitSegmentCount

                repeat(v1DigitSegmentCount) { index ->
                    if (v1[v1CompareCursor + index] != v2[v2CompareCursor + index])
                    //数字不相等
                        return@Comparator v2[v2CompareCursor + index] - v1[v1CompareCursor + index]
                }
                v1CompareCursor = v1DigitSegmentEndIndex
                v2CompareCursor = v2DigitSegmentEndIndex
            } else {
                //其中一方无数字
                if (v1[v1CompareCursor] != v2[v2CompareCursor])
                    return@Comparator v2[v2CompareCursor] - v1[v1CompareCursor]
                v1CompareCursor++
                v2CompareCursor++
            }
        }
        //部分内容完全相同
        v2.length - v1.length
    }

    override fun actionPerformed(event: AnActionEvent) {
        thread {
            event.getData(CommonDataKeys.EDITOR)?.let { editor ->
                val lineStartPosition = editor.caretModel.visualLineStart
                val lineEndPosition = editor.caretModel.visualLineEnd
                val currentLineContent = editor.document.getText(TextRange(lineStartPosition, lineEndPosition))

                val semicolon = ":"
                if (!currentLineContent.contains(semicolon)) {
                    showLibraryInfoNotFoundDialog()
                    return@thread
                }
                val singleQuotation = "'"
                val doubleQuotation = "\""

                val isSingleQuotation = currentLineContent.contains(singleQuotation)
                val quotation = if (isSingleQuotation) singleQuotation else doubleQuotation

                val libraryStartIndex = currentLineContent.indexOf(quotation)
                val libraryEndIndex = currentLineContent.lastIndexOf(quotation)

                if (libraryStartIndex < 0 || libraryEndIndex < libraryStartIndex) {
                    showLibraryInfoNotFoundDialog()
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
                    }?.sortedWith(versionComparator)

                    versionList?.let {
                        if (it.isEmpty()) {
                            showLibraryNotFoundDialog()
                        } else {
                            VersionSelectorDialog.show(DefaultListModel<String>().apply { it.forEach { e -> addElement(e) } }) { selectedVersion ->
                                val oldVersionStart = currentLineContent.lastIndexOf(semicolon)
                                val oldVersionEnd = currentLineContent.lastIndexOf(if (isSingleQuotation) singleQuotation else doubleQuotation)
                                val oldVersionString = currentLineContent.substring(oldVersionStart, oldVersionEnd)
                                val newLineContent = currentLineContent.replace(oldVersionString, semicolon + selectedVersion)

                                WriteCommandAction.runWriteCommandAction(event.project) {
                                    editor.document.replaceString(lineStartPosition, lineEndPosition, newLineContent)
                                }
                            }
                        }
                    } ?: showLibraryNotFoundDialog()
                } catch (e: Exception) {
                    showErrorDialog(e)
                }
            }
        }
    }

    private fun showErrorDialog(e: Exception) = EventQueue.invokeAndWait {
        Messages.showErrorDialog(StringWriter().use { sw ->
            PrintWriter(sw).use { pw ->
                pw.println("查找依赖库历史版本时出错！")
                pw.println("若不能通过以下log分析到具体原因（如出现UnknownHostException、SocketTimeoutException等Exception，请检查网络是否正常）\n" +
                        "请复制以下log到 https://github.com/wuyr/GoogleLibraryVersionQuerier 上提issue:\n")
                e.printStackTrace(pw)
            }
            sw.toString()
        }.toString(), "出错了")
    }

    private fun showLibraryNotFoundDialog() = EventQueue.invokeAndWait {
        Messages.showErrorDialog("请检查关键词是否正确，以及确保该依赖库已发布到Google、Maven、Jcenter仓库上！", "找不到该依赖库")
    }

    private fun showLibraryInfoNotFoundDialog() = EventQueue.invokeAndWait {
        Messages.showErrorDialog("请将光标定位到目标依赖库所在行！", "找不到有效信息")
    }

}