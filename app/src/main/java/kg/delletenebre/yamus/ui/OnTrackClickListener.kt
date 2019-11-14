package kg.delletenebre.yamus.ui

import android.support.v4.media.MediaMetadataCompat
import android.view.View


interface OnTrackClickListener {
    fun onClick(item: MediaMetadataCompat)

    fun onMenuClick(view: View, item: MediaMetadataCompat)
}