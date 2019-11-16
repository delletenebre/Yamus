/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kg.delletenebre.yamus.utils

import android.net.Uri
import java.math.BigInteger
import java.security.MessageDigest

/**
 * This file contains extension methods for the java.lang package.
 */

/**
 * Helper extension to convert a potentially null [String] to a [Uri] falling back to [Uri.EMPTY]
 */
fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.toCoverUrl(size: Int = 200): String {
    return when {
        this.startsWith("https://") || this.isEmpty() -> this
        else -> "https://${this.replace("/%%", "/${size}x$size")}"
    }
}

fun String.toCoverUri(size: Int = 200): Uri {
    return this.toCoverUrl(size).toUri()
}
