@file:Suppress("UNCHECKED_CAST")

package com.wuyr.google_library_query

import com.google.gson.Gson
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLDecoder
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
            }?.reversed()
        }
    }
}

@Throws(java.lang.Exception::class)
fun getAvailableVersions2(libraryGroup: String, libraryName: String) =
        getSearchAvailableVersionsUrl(libraryGroup, libraryName).request { result ->
            result.filter { it["nodeType"] == "FOLDER" }.map {
                (it["nodeName"] as String).run {
                    (if (contains('%')) {
                        try {
                            URLDecoder.decode(this, "utf-8")
                        } catch (e: Exception) {
                            this
                        }
                    } else this).run { substring(0, lastIndex) }
                }
            }.reversed()
        }

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
fun matchingLibraries2(keyword: String) = ArrayList<Pair<String, String>>().apply {
    runSafely {
        keyword.split(":").let { libraryInfo ->
            if (libraryInfo.size > 1) {
                val libraryGroup = when {
                    libraryInfo.size > 2 && libraryInfo[0].isEmpty() -> libraryInfo[1]
                    libraryInfo[0].isEmpty() -> "*"
                    else -> "*${libraryInfo[0]}*"
                }.run { if (isEmpty()) "*" else this }
                val libraryName = when {
                    libraryInfo.size > 3 && libraryInfo[3].isEmpty() -> libraryInfo[2]
                    libraryInfo.size > 2 && libraryInfo[2].isEmpty() -> libraryInfo[1]
                    libraryInfo.size > 2 && libraryInfo[0].isEmpty() -> if (libraryInfo[2].isEmpty()) "*" else "*${libraryInfo[2]}*"
                    else -> if (libraryInfo[1].isEmpty()) "*" else "*${libraryInfo[1]}*"
                }.run { if (isEmpty()) "*" else this }

                if (libraryGroup.length >= 5 || libraryName.length >= 5) {
                    getSearchLibrariesUrl(libraryGroup, libraryName).request { result ->
                        result.filter {
                            (it["packaging"] == "pom").also { hit ->
                                if (hit) {
                                    it["content"] = "${it["groupId"]}:${it["artifactId"]}"
                                }
                            }
                        }.groupBy { it["content"] }.forEach {
                            if (it.value.isNotEmpty()) {
                                it.value.first().let { item ->
                                    add("implementation '${item["content"]}:${item["version"]}'" to item["groupId"].toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Throws(java.lang.Exception::class)
inline fun <O> search(keyword: String, block: (List<Map<String, Any>>) -> O): O? =
        (BASE_URL + "search/json?k=$keyword").getAPIResponse(1)?.let { json ->
            ((Gson().fromJson(json, Map::class.java)["data"]) as? List<Map<String, Any>>)?.run {
                block(this)
            }
        }

const val BASE_URL2 = "https://maven.aliyun.com/"

fun getSearchLibrariesUrl(libraryGroup: String, libraryName: String) =
        BASE_URL2 + "artifact/aliyunMaven/searchArtifactByGav?_input_charset=utf-8&groupId=$libraryGroup&repoId=jcenter&artifactId=$libraryName&version=*"

fun getSearchAvailableVersionsUrl(libraryGroup: String, libraryName: String) =
        BASE_URL2 + "browse/tree?_input_charset=utf-8&repoId=jcenter&path=${libraryGroup.replace('.', '/')}/$libraryName/"

@Throws(java.lang.Exception::class)
inline fun <O> String.request(block: (List<MutableMap<String, Any>>) -> O): O? = getAPIResponse(1)?.let { json ->
    (Gson().fromJson(json, Map::class.java) as? Map<String, Any>)?.run {
        if (this["successful"] == true) {
            block(this["object"] as List<MutableMap<String, Any>>)
        } else null
    }
}

@Throws(java.lang.Exception::class)
fun String.getAPIResponse(retryCount: Int): String? {
    var currentRetryCount = 0
    while (currentRetryCount <= retryCount) {
        try {
            (URL(this).openConnection() as HttpsURLConnection).run {
                connectTimeout = 5000
                readTimeout = 5000
                if (responseCode == 200) {
                    return inputStream.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                } else {
                    println(responseCode)
                    currentRetryCount++
                }
            }
        } catch (e: Exception) {
            if (e is SocketTimeoutException) {
                println("timeout, retrying...")
                currentRetryCount++
            }
        }
    }
    return null
}

inline fun <T, R> T.runSafely(block: (T) -> R) = try {
    block(this)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun main() {
    val libraryGroup = "com.squareup.okhttp3"
    val libraryName = "okhttp"
    println("json: " + getAvailableVersions(libraryGroup, libraryName))
    println("json: " + getAvailableVersions2(libraryGroup, libraryName))
    println("matchingLibraries: " + matchingLibraries2(":com.squareup.okhttp3:okhttp:"))
    VersionSelectorDialog.show(DefaultListModel<String>().apply {
        getAvailableVersions2(libraryGroup, libraryName)?.forEach {
            addElement(it)
        }
    }) {
        println(it)
    }
}