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

package kg.delletenebre.yamus.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.delletenebre.yamus.common.MediaSessionConnection
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.isPlayEnabled
import kg.delletenebre.yamus.media.extensions.isPlaying
import kg.delletenebre.yamus.media.extensions.isPrepared
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.utils.Event
import kotlinx.coroutines.Job

/**
 * Small [ViewModel] that watches a [MediaSessionConnection] to become connected
 * and provides the root/initial media ID of the underlying [MediaBrowserCompat].
 */
class MainActivityViewModel(
    val mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    /**
     * [navigateToMediaItem] acts as an "event", rather than state. [Observer]s
     * are notified of the change as usual with [LiveData], but only one [Observer]
     * will actually read the data. For more information, check the [Event] class.
     */
//    val navigateToMediaItem: LiveData<Event<String>> get() = _navigateToMediaItem
//    private val _navigateToMediaItem = MutableLiveData<Event<String>>()

    /**
     * This [LiveData] object is used to notify the MainActivity that the main
     * content fragment needs to be swapped. Information about the new fragment
     * is conveniently wrapped by the [Event] class.
     */
//    val navigateToFragment: LiveData<Event<FragmentNavigationRequest>> get() = _navigateToFragment
//    private val _navigateToFragment = MutableLiveData<Event<FragmentNavigationRequest>>()

    private var currentJob: Job? = null
//    fun trackClicked(clickedTrack: Track, tracks: List<Track>) {
//        currentJob?.cancel()
//        currentJob = viewModelScope.launch {
//            CurrentPlaylist.batchId = ""
//            CurrentPlaylist.updatePlaylist("playlist", tracks, CurrentPlaylist.TYPE_TRACKS)
//            playTrack(clickedTrack, pauseAllowed = true)
//            currentJob = null
//        }
//    }

//    fun trackClicked(clickedTrack: Track, tracks: List<Track>, playlistIdentifier: String) {
//        if (CurrentPlaylist.id == playlistIdentifier) {
//            playTrack(clickedTrack, pauseAllowed = true)
//        } else {
//            currentJob?.cancel()
//            currentJob = viewModelScope.launch {
//                CurrentPlaylist.batchId = ""
//                CurrentPlaylist.updatePlaylist(playlistIdentifier, tracks, CurrentPlaylist.TYPE_TRACKS)
//                playTrack(clickedTrack, pauseAllowed = true)
//                currentJob = null
//            }
//        }
//    }
//
//    fun stationClicked(station: Station) {
//        viewModelScope.launch {
//            playStation(station.getId())
//        }
//    }

    fun playTrack(trackId: String, pauseAllowed: Boolean = true) {
        val currentTrack = CurrentPlaylist.currentTrack.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && trackId == currentTrack?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (pauseAllowed) {
                            transportControls.pause()
                        } else {
                            Unit
                        }
                    }
                    playbackState.isPlayEnabled -> {
                        transportControls.play()
                    }
                    else -> {
                        Log.w(
                                TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                " (mediaId=$trackId)"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(trackId, null)
        }
    }

    fun playStation(mediaId: String) {
        val transportControls = mediaSessionConnection.transportControls
        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == CurrentPlaylist.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                                TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                " (Station: $mediaId"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun playMediaId(mediaId: String) {
        val currentTrack = CurrentPlaylist.currentTrack.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == currentTrack?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=$mediaId)"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun playerViewPlayPauseClick() {
        mediaSessionConnection.playbackState.value?.let { playbackState ->
            val transportControls = mediaSessionConnection.transportControls
            when {
                playbackState.isPlaying -> {
                    transportControls.pause()
                }
                playbackState.isPlayEnabled -> {
                    transportControls.play()
                }
                else -> {
                    Log.w(TAG, "Playable item clicked but neither play nor pause are enabled!")
                }
            }
        }
    }

    class Factory(
        private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainActivityViewModel(mediaSessionConnection) as T
        }
    }
}

private const val TAG = "MainActivitytVM"
