@file:Suppress("UNCHECKED_CAST")

package com.wuyr.google_library_query

import com.google.gson.Gson
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection
import javax.swing.DefaultListModel

/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-05 15:02
 */

const val BASE_URL = "https://wanandroid.com/maven_pom/"

@Throws(java.lang.Exception::class)
fun getAvailableVersions(libraryGroup: String, libraryName: String) = search("$libraryGroup:$libraryName") { dataList ->
    dataList.find { it["groupName"] == libraryGroup }?.run {
        (this["artifactMap"] as? Map<String, Any>)?.run {
            (this[libraryName] as? List<Map<String, Any>>)?.map {
                it["version"].toString()
            }
        }
    }
}

@Throws(java.lang.Exception::class)
fun matchingLibraries(keyword: String) = ArrayList<Pair<String, String>>().apply {
    runSafely {
        search(keyword) { dataList ->
            dataList.map {
                (it["artifactMap"] as? Map<String, List<Map<String, Any>>>)?.values?.map { e ->
                    e.last().run {
                        this["content"].toString() to this["group"].toString()
                    }
                }
            }.forEach { it?.let { addAll(it) } }
        }
    }
}

@Throws(java.lang.Exception::class)
inline fun <O> search(keyword: String, block: (List<Map<String, Any>>) -> O): O? = (URL(BASE_URL + "search/json?k=$keyword").openConnection() as HttpsURLConnection).run {
    if (responseCode == 200) {
        val json = inputStream.readBytes().toString(Charset.forName("utf-8"))
        ((Gson().fromJson(json, Map::class.java)["data"]) as? List<Map<String, Any>>)?.run {
            block(this)
        }
    } else null
}

inline fun <T, R> T.runSafely(block: (T) -> R) = try {
    block(this)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun main() {
    val libraryGroup = "androidx.recyclerview"
    val libraryName = "recyclerview"
    println("json: " + getAvailableVersions(libraryGroup, libraryName))
    println("matchingLibraries: " + matchingLibraries("recy"))
    VersionSelectorDialog.show(DefaultListModel<String>().apply {
        getAvailableVersions(libraryGroup, libraryName)?.forEach {
            addElement(it)
        }
    }) {
        println(it)
    }
}