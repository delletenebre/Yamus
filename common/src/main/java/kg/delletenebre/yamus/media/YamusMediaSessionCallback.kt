package kg.delletenebre.yamus.media

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class YamusMediaSessionCallback: MediaSessionCompat.Callback() {
    override fun onCustomAction(action: String?, extras: Bundle?) {
        super.onCustomAction(action, extras)
    }
}