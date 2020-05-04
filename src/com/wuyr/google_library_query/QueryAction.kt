@file:Suppress("UNCHECKED_CAST")

package com.wuyr.google_library_query

import com.google.gson.Gson
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.wuyr.google_library_query.QueryAction.Companion.getLatestVersionByJson
import com.wuyr.google_library_query.QueryAction.Companion.getLatestVersionByJsoup
import org.jsoup.Jsoup
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection


/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-04 23:13
 */
@Suppress("ConstantConditionIf")
class QueryAction : AnAction() {

    private val useJson = true

    override fun actionPerformed(event: AnActionEvent) {
        event.getData(CommonDataKeys.EDITOR)?.let { editor ->
            val lineStartPosition = editor.caretModel.visualLineStart
            val lineEndPosition = editor.caretModel.visualLineEnd
            val currentLineContent = editor.document.getText(TextRange(lineStartPosition, lineEndPosition))

            val semicolon = ":"
            if (!currentLineContent.contains(semicolon)) {
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
                (if (useJson) getLatestVersionByJsoup(libraryGroup, libraryName)
                else getLatestVersionByJsoup(libraryGroup, libraryName))?.let { latestVersion ->
                    val oldVersionStart = currentLineContent.lastIndexOf(semicolon)
                    val oldVersionEnd = currentLineContent.lastIndexOf(if (isSingleQuotation) singleQuotation else doubleQuotation)
                    val oldVersionString = currentLineContent.substring(oldVersionStart, oldVersionEnd)
                    val newLineContent = currentLineContent.replace(oldVersionString, semicolon + latestVersion)

                    if (Messages.showOkCancelDialog(
                                    "The latest version is:\r\n$latestVersion\r\nDo you want to replace it?",
                                    "Library Found", "OK", "&No", Messages.getQuestionIcon()) == Messages.OK) {
                        WriteCommandAction.runWriteCommandAction(event.project) {
                            editor.document.replaceString(lineStartPosition, lineEndPosition, newLineContent)
                        }
                    }
                }
                        ?: Messages.showErrorDialog("Please check if the library belongs to Google.", "Library Not Found!")
            } catch (e: Exception) {
                Messages.showErrorDialog(StringWriter().apply { PrintWriter(this).apply { RuntimeException().printStackTrace(this) } }.toString(), "Error")
            }
        }

    }

    companion object {
        private const val BASE_URL = "https://wanandroid.com/maven_pom/"

        @Throws(java.lang.Exception::class)
        fun getLatestVersionByJsoup(libraryGroup: String, libraryName: String): String? {
            var libraryFound = false
            var latestVersion: String? = null
            Jsoup.connect(BASE_URL + "index?k=$libraryGroup:$libraryName").get().select("li[class=pom_item]")[0]
                    .children()[1].children()[1].children().forEach {
                val children = it.children()
                if (children.size == 3) {
                    if (libraryFound) {
                        return@forEach
                    }
                    if (children[0].text() == libraryName) {
                        libraryFound = true
                    }
                }
                if (libraryFound) {
                    latestVersion = children[children.size - 2].text()
                }
            }

            return latestVersion
        }

        @Throws(java.lang.Exception::class)
        fun getLatestVersionByJson(libraryGroup: String, libraryName: String): String? {
            (URL(BASE_URL + "search/json?k=$libraryGroup:$libraryName").openConnection() as HttpsURLConnection).run {
                if (responseCode == 200) {
                    val json = inputStream.readBytes().toString(Charset.forName("utf-8"))
                    ((Gson().fromJson(json, Map::class.java)["data"]) as? List<Map<String, Any>>)?.run {
                        find { it["groupName"] == libraryGroup }?.run {
                            (this["artifactMap"] as? Map<String, Any>)?.run {
                                return ((this[libraryName] as? List<Map<String, Any>>)?.last()?.get("version") as? String)
                            }
                        }
                    }
                }
            }
            return null
        }
    }
}

fun main() {
    val libraryGroup = "android.arch.persistence.room"
    val libraryName = "runtime"
    println("json: " + getLatestVersionByJson(libraryGroup, libraryName))
    println("jsoup: " + getLatestVersionByJsoup(libraryGroup, libraryName))
}