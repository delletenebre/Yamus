package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.R
import com.google.android.exoplayer2.util.RepeatModeUtil
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class RepeatModeActionProvider(
        context: Context,
        @property:RepeatModeUtil.RepeatToggleModes private val repeatToggleModes: Int = DEFAULT_REPEAT_TOGGLE_MODES)
    : MediaSessionConnector.CustomActionProvider {

    companion object {
        @RepeatModeUtil.RepeatToggleModes
        const val DEFAULT_REPEAT_TOGGLE_MODES =
                RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
        private const val ACTION_REPEAT_MODE = "ACTION_EXO_REPEAT_MODE"
    }

    private var repeatAllDescription: CharSequence =
            context.getString(R.string.exo_media_action_repeat_all_description)
    private var repeatOneDescription: CharSequence =
            context.getString(R.string.exo_media_action_repeat_one_description)
    private var repeatOffDescription: CharSequence =
            context.getString(R.string.exo_media_action_repeat_off_description)

    override fun onCustomAction(
            player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
        val mode = player.repeatMode
        val proposedMode = RepeatModeUtil.getNextRepeatMode(mode, repeatToggleModes)
        if (mode != proposedMode) {
            controlDispatcher.dispatchSetRepeatMode(player, proposedMode)
        }
    }

    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
        val actionEnabled = App.instance.getBooleanPreference("show_repeat")
        if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION || !actionEnabled) {
            return null
        }

        val actionLabel: CharSequence
        val iconResourceId: Int
        when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                actionLabel = repeatOneDescription
                iconResourceId = R.drawable.exo_media_action_repeat_one
            }
            Player.REPEAT_MODE_ALL -> {
                actionLabel = repeatAllDescription
                iconResourceId = R.drawable.exo_media_action_repeat_all
            }
            Player.REPEAT_MODE_OFF -> {
                actionLabel = repeatOffDescription
                iconResourceId = R.drawable.exo_media_action_repeat_off
            }
            else -> {
                actionLabel = repeatOffDescription
                iconResourceId = R.drawable.exo_media_action_repeat_off
            }
        }
        val repeatBuilder = PlaybackStateCompat.CustomAction.Builder(ACTION_REPEAT_MODE, actionLabel, iconResourceId)
        return repeatBuilder.build()
    }
}