package kg.delletenebre.yamus.media.datasource

import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory

class YandexDataSourceFactory(
        var userAgent: String,
        private var listener: TransferListener? = null,
        private var connectTimeoutMillis: Int = YandexDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
        private var readTimeoutMillis: Int = YandexDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
        private var allowCrossProtocolRedirects: Boolean = false) : BaseFactory() {

    override fun createDataSourceInternal(
            defaultRequestProperties: HttpDataSource.RequestProperties): YandexDataSource {
        val dataSource = YandexDataSource(
                userAgent, null,
                connectTimeoutMillis,
                readTimeoutMillis,
                allowCrossProtocolRedirects,
                defaultRequestProperties)
        if (listener != null) {
            dataSource.addTransferListener(listener)
        }
        return dataSource
    }
}