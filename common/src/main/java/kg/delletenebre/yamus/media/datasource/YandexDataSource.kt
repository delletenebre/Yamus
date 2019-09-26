package kg.delletenebre.yamus.media.datasource

import android.net.Uri
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DataSpec.HttpMethod
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Predicate
import com.google.android.exoplayer2.util.Util
import kg.delletenebre.yamus.api.YandexMusic
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.ProtocolException
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

class YandexDataSource(
        private val userAgent: String,
        private val contentTypePredicate: Predicate<String>?,
        private val connectTimeoutMillis: Int = DEFAULT_CONNECT_TIMEOUT_MILLIS,
        private val readTimeoutMillis: Int = DEFAULT_READ_TIMEOUT_MILLIS,
        private val allowCrossProtocolRedirects: Boolean = false,
        private val defaultRequestProperties: HttpDataSource.RequestProperties? = null
) : BaseDataSource(true), HttpDataSource {
    private val requestProperties: HttpDataSource.RequestProperties = HttpDataSource.RequestProperties()

    private var dataSpec: DataSpec? = null
    private var connection: HttpURLConnection? = null
    private var inputStream: InputStream? = null
    private var opened: Boolean = false

    private var bytesToSkip: Long = 0
    private var bytesToRead: Long = 0

    private var bytesSkipped: Long = 0
    private var bytesRead: Long = 0

    override fun getUri(): Uri? {
        return if (connection == null) {
            null
        } else {
            Uri.parse(connection!!.url.toString())
        }
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return if (connection == null) {
            emptyMap()
        } else {
            connection!!.headerFields
        }
    }

    override fun setRequestProperty(name: String, value: String) {
        Assertions.checkNotNull(name)
        Assertions.checkNotNull(value)
        requestProperties.set(name, value)
    }

    override fun clearRequestProperty(name: String) {
        Assertions.checkNotNull(name)
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        this.bytesRead = 0
        this.bytesSkipped = 0
        transferInitializing(dataSpec)
        try {
            connection = makeConnection(dataSpec)
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e,
                    dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

        val responseCode: Int
        val responseMessage: String
        try {
            responseCode = connection!!.responseCode
            responseMessage = connection!!.responseMessage
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw HttpDataSource.HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e,
                    dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

        if (responseCode < 200 || responseCode > 299) {
            val headers = connection!!.headerFields
            closeConnectionQuietly()
            val exception = HttpDataSource.InvalidResponseCodeException(responseCode, responseMessage, headers, dataSpec)
            if (responseCode == 416) {
                exception.initCause(DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE))
            }
            throw exception
        }

        val contentType = connection!!.contentType
        if (contentTypePredicate != null && !contentTypePredicate.evaluate(contentType)) {
            closeConnectionQuietly()
            throw HttpDataSource.InvalidContentTypeException(contentType, dataSpec)
        }

        bytesToSkip = if (responseCode == 200 && dataSpec.position != 0L) {
            dataSpec.position
        } else {
            0
        }

        bytesToRead = if (!dataSpec.isFlagSet(DataSpec.FLAG_ALLOW_GZIP)) {
            if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                dataSpec.length
            } else {
                val contentLength = getContentLength(connection!!)
                if (contentLength != C.LENGTH_UNSET.toLong()) {
                    contentLength - bytesToSkip
                } else {
                    C.LENGTH_UNSET.toLong()
                }
            }
        } else {
            dataSpec.length
        }

        try {
            inputStream = connection!!.inputStream
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

        opened = true
        transferStarted(dataSpec)

        return bytesToRead
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        try {
            skipInternal()
            return readInternal(buffer, offset, readLength)
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_READ)
        }

    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun close() {
        try {
            if (inputStream != null) {
                maybeTerminateInputStream(connection, bytesRemaining())
                try {
                    inputStream!!.close()
                } catch (e: IOException) {
                    throw HttpDataSource.HttpDataSourceException(e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_CLOSE)
                }

            }
        } finally {
            inputStream = null
            closeConnectionQuietly()
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }

//    protected fun bytesSkipped(): Long {
//        return bytesSkipped
//    }
//
//    protected fun bytesRead(): Long {
//        return bytesRead
//    }

    private fun bytesRemaining(): Long {
        return if (bytesToRead == C.LENGTH_UNSET.toLong()) bytesToRead else bytesToRead - bytesRead
    }

    @Throws(IOException::class)
    private fun makeConnection(dataSpec: DataSpec): HttpURLConnection {
        Log.d("ahoha", dataSpec.uri.toString())
        var url = URL(YandexMusic.getDirectUrl(dataSpec.uri.toString()))//URL(dataSpec.uri.toString())

        @HttpMethod var httpMethod = dataSpec.httpMethod
        var httpBody = dataSpec.httpBody
        val position = dataSpec.position
        val length = dataSpec.length
        val allowGzip = dataSpec.isFlagSet(DataSpec.FLAG_ALLOW_GZIP)
        val allowIcyMetadata = dataSpec.isFlagSet(DataSpec.FLAG_ALLOW_ICY_METADATA)

        if (!allowCrossProtocolRedirects) {
            return makeConnection(
                    url,
                    httpMethod,
                    httpBody,
                    position,
                    length,
                    allowGzip,
                    allowIcyMetadata,
                    /* followRedirects= */ true)
        }

        var redirectCount = 0
        while (redirectCount++ <= MAX_REDIRECTS) {
            val connection = makeConnection(
                    url,
                    httpMethod,
                    httpBody,
                    position,
                    length,
                    allowGzip,
                    allowIcyMetadata,
                    /* followRedirects= */ false)
            val responseCode = connection.responseCode
            val location = connection.getHeaderField("Location")
            if ((httpMethod == DataSpec.HTTP_METHOD_GET || httpMethod == DataSpec.HTTP_METHOD_HEAD) && (responseCode == HttpURLConnection.HTTP_MULT_CHOICE
                            || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                            || responseCode == HTTP_STATUS_TEMPORARY_REDIRECT
                            || responseCode == HTTP_STATUS_PERMANENT_REDIRECT)) {
                connection.disconnect()
                url = handleRedirect(url, location)
            } else if (httpMethod == DataSpec.HTTP_METHOD_POST && (responseCode == HttpURLConnection.HTTP_MULT_CHOICE
                            || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode == HttpURLConnection.HTTP_SEE_OTHER)) {
                // POST request follows the redirect and is transformed into a GET request.
                connection.disconnect()
                httpMethod = DataSpec.HTTP_METHOD_GET
                httpBody = null
                url = handleRedirect(url, location)
            } else {
                return connection
            }
        }

        throw NoRouteToHostException("Too many redirects: $redirectCount")
    }

    @Throws(IOException::class)
    private fun makeConnection(
            url: URL,
            @HttpMethod httpMethod: Int,
            httpBody: ByteArray?,
            position: Long,
            length: Long,
            allowGzip: Boolean,
            allowIcyMetadata: Boolean,
            followRedirects: Boolean): HttpURLConnection {
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = connectTimeoutMillis
        connection.readTimeout = readTimeoutMillis
        if (defaultRequestProperties != null) {
            for ((key, value) in defaultRequestProperties.snapshot) {
                connection.setRequestProperty(key, value)
            }
        }
        for ((key, value) in requestProperties.snapshot) {
            connection.setRequestProperty(key, value)
        }
        if (!(position == 0L && length == C.LENGTH_UNSET.toLong())) {
            var rangeRequest = "bytes=$position-"
            if (length != C.LENGTH_UNSET.toLong()) {
                rangeRequest += position + length - 1
            }
            connection.setRequestProperty("Range", rangeRequest)
        }
        connection.setRequestProperty("User-Agent", userAgent)
        if (!allowGzip) {
            connection.setRequestProperty("Accept-Encoding", "identity")
        }
        if (allowIcyMetadata) {
            connection.setRequestProperty(
                    IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_NAME,
                    IcyHeaders.REQUEST_HEADER_ENABLE_METADATA_VALUE)
        }
        connection.instanceFollowRedirects = followRedirects
        connection.doOutput = httpBody != null
        connection.requestMethod = DataSpec.getStringForHttpMethod(httpMethod)
        if (httpBody != null) {
            connection.setFixedLengthStreamingMode(httpBody.size)
            connection.connect()
            val os = connection.outputStream
            os.write(httpBody)
            os.close()
        } else {
            connection.connect()
        }
        return connection
    }

    @Throws(IOException::class)
    private fun skipInternal() {
        if (bytesSkipped == bytesToSkip) {
            return
        }

        // Acquire the shared skip buffer.
        var skipBuffer: ByteArray? = skipBufferReference.getAndSet(null)
        if (skipBuffer == null) {
            skipBuffer = ByteArray(4096)
        }

        while (bytesSkipped != bytesToSkip) {
            val readLength = min(bytesToSkip - bytesSkipped, skipBuffer.size.toLong()).toInt()
            val read = inputStream!!.read(skipBuffer, 0, readLength)
            if (Thread.currentThread().isInterrupted) {
                throw InterruptedIOException()
            }
            if (read == -1) {
                throw EOFException()
            }
            bytesSkipped += read.toLong()
            bytesTransferred(read)
        }

        skipBufferReference.set(skipBuffer)
    }

    @Throws(IOException::class)
    private fun readInternal(buffer: ByteArray, offset: Int, readLength: Int): Int {
        var readLen = readLength
        if (readLen == 0) {
            return 0
        }
        if (bytesToRead != C.LENGTH_UNSET.toLong()) {
            val bytesRemaining = bytesToRead - bytesRead
            if (bytesRemaining == 0L) {
                return C.RESULT_END_OF_INPUT
            }
            readLen = min(readLength.toLong(), bytesRemaining).toInt()
        }

        val read = inputStream!!.read(buffer, offset, readLen)
        if (read == -1) {
            if (bytesToRead != C.LENGTH_UNSET.toLong()) {
                // End of stream reached having not read sufficient data.
                throw EOFException()
            }
            return C.RESULT_END_OF_INPUT
        }

        bytesRead += read.toLong()
        bytesTransferred(read)
        return read
    }


    /**
     * Closes the current connection quietly, if there is one.
     */
    private fun closeConnectionQuietly() {
        if (connection != null) {
            try {
                connection!!.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while disconnecting", e)
            }

            connection = null
        }
    }

    companion object {

        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 8 * 1000
        const val DEFAULT_READ_TIMEOUT_MILLIS = 8 * 1000

        private const val TAG = "YandexDataSource"
        private const val MAX_REDIRECTS = 20 // Same limit as okhttp.
        private const val HTTP_STATUS_TEMPORARY_REDIRECT = 307
        private const val HTTP_STATUS_PERMANENT_REDIRECT = 308
        private const val MAX_BYTES_TO_DRAIN: Long = 2048
        private val CONTENT_RANGE_HEADER = Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$")
        private val skipBufferReference = AtomicReference<ByteArray>()

        @Throws(IOException::class)
        private fun handleRedirect(originalUrl: URL, location: String?): URL {
            if (location == null) {
                throw ProtocolException("Null location redirect")
            }
            val url = URL(originalUrl, location)
            val protocol = url.protocol
            if ("https" != protocol && "http" != protocol) {
                throw ProtocolException("Unsupported protocol redirect: $protocol")
            }
            // Currently this method is only called if allowCrossProtocolRedirects is true, and so the code
            // below isn't required. If we ever decide to handle redirects ourselves when cross-protocol
            // redirects are disabled, we'll need to uncomment this block of code.
            // if (!allowCrossProtocolRedirects && !protocol.equals(originalUrl.getProtocol())) {
            //   throw new ProtocolException("Disallowed cross-protocol redirect ("
            //       + originalUrl.getProtocol() + " to " + protocol + ")");
            // }
            return url
        }

        private fun getContentLength(connection: HttpURLConnection): Long {
            var contentLength = C.LENGTH_UNSET.toLong()
            val contentLengthHeader = connection.getHeaderField("Content-Length")
            if (!TextUtils.isEmpty(contentLengthHeader)) {
                try {
                    contentLength = java.lang.Long.parseLong(contentLengthHeader)
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Unexpected Content-Length [$contentLengthHeader]")
                }

            }
            val contentRangeHeader = connection.getHeaderField("Content-Range")
            if (!TextUtils.isEmpty(contentRangeHeader)) {
                val matcher = CONTENT_RANGE_HEADER.matcher(contentRangeHeader)
                if (matcher.find()) {
                    try {
                        val contentLengthFromRange = java.lang.Long.parseLong(matcher.group(2)) - java.lang.Long.parseLong(matcher.group(1)) + 1
                        if (contentLength < 0) {
                            contentLength = contentLengthFromRange
                        } else if (contentLength != contentLengthFromRange) {
                            Log.w(TAG, "Inconsistent headers [" + contentLengthHeader + "] [" + contentRangeHeader
                                    + "]")
                            contentLength = max(contentLength, contentLengthFromRange)
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Unexpected Content-Range [$contentRangeHeader]")
                    }

                }
            }
            return contentLength
        }

        private fun maybeTerminateInputStream(connection: HttpURLConnection?, bytesRemaining: Long) {
//            if (Util.SDK_INT != 19 && Util.SDK_INT != 20) {
//                return
//            }
//
//            try {
//                val inputStream = connection!!.inputStream
//                if (bytesRemaining == C.LENGTH_UNSET.toLong()) {
//                    // If the input stream has already ended, do nothing. The socket may be re-used.
//                    if (inputStream.read() == -1) {
//                        return
//                    }
//                } else if (bytesRemaining <= MAX_BYTES_TO_DRAIN) {
//                    // There isn't much data left. Prefer to allow it to drain, which may allow the socket to be
//                    // re-used.
//                    return
//                }
//                val className = inputStream.javaClass.name
//                if ("com.android.okhttp.internal.http.HttpTransport\$ChunkedInputStream" == className || "com.android.okhttp.internal.http.HttpTransport\$FixedLengthInputStream" == className) {
//                    val superclass = inputStream.javaClass.superclass
//                    val unexpectedEndOfInput = superclass?.getDeclaredMethod("unexpectedEndOfInput")
//                    unexpectedEndOfInput?.isAccessible = true
//                    unexpectedEndOfInput?.invoke(inputStream)
//                }
//            } catch (e: Exception) {
//                // If an IOException then the connection didn't ever have an input stream, or it was closed
//                // already. If another type of exception then something went wrong, most likely the device
//                // isn't using okhttp.
//            }

        }
    }
}