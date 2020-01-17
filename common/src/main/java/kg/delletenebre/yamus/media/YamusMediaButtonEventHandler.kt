package kg.delletenebre.yamus.media

import android.content.Intent
import android.view.KeyEvent
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.actions.CustomActionsHelper

class YamusMediaButtonEventHandler : MediaSessionConnector.MediaButtonEventHandler {
    override fun onMediaButtonEvent(player: Player, controlDispatcher: ControlDispatcher, mediaButtonEvent: Intent): Boolean {
        var canHandle = false
        if (mediaButtonEvent.extras?.containsKey(Intent.EXTRA_KEY_EVENT) == true) {
            val keyEvent = mediaButtonEvent.extras!!.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        CustomActionsHelper.next(player)
                        canHandle = true
                    }
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        CustomActionsHelper.previous(player)
                        canHandle = true
                    }
                }
            }
        }
        return canHandle
    }
}