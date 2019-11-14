package kg.delletenebre.yamus.api

interface DownloadProgressListener {
    fun onUpdate(progress: Long)
    fun onFinish()
}