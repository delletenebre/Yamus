package kg.delletenebre.yamus.ui

import android.support.v4.media.MediaBrowserCompat

interface OnMediaItemClickListener {
    fun onClick(item: MediaBrowserCompat.MediaItem)
}