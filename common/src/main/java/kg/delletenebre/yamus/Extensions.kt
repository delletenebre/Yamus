package kg.delletenebre.yamus

import okhttp3.FormBody

fun FormBody.stringify(): String {
    val result = mutableListOf<String>()
    for (i in 0 until this.size()) {
        result.add("${this.name(i)}:${this.value(i)}")
    }
    return result.joinToString("|")
}