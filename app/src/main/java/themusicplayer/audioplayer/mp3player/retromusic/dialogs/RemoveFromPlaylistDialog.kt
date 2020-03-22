/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package themusicplayer.audioplayer.mp3player.retromusic.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.R.string
import themusicplayer.audioplayer.mp3player.retromusic.model.PlaylistSong
import themusicplayer.audioplayer.mp3player.retromusic.util.PlaylistsUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet

class RemoveFromPlaylistDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs = requireArguments().getParcelableArrayList<PlaylistSong>("songs")

        var title = 0
        var content: CharSequence = ""
        if (songs != null) {
            if (songs.size > 1) {
                title = R.string.remove_songs_from_playlist_title
                content = HtmlCompat.fromHtml(
                    getString(string.remove_x_songs_from_playlist, songs.size),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } else {
                title = R.string.remove_song_from_playlist_title
                content = HtmlCompat.fromHtml(
                    getString(
                        themusicplayer.audioplayer.mp3player.retromusic.R.string.remove_song_x_from_playlist,
                        songs[0].title
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }


        return MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .show {
                title(title)
                message(text = content)
                negativeButton(android.R.string.cancel)
                positiveButton(R.string.remove_action) {
                    if (activity == null)
                        return@positiveButton
                    PlaylistsUtil.removeFromPlaylist(
                        requireContext(),
                        songs as MutableList<PlaylistSong>
                    )
                }
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            }
    }

    companion object {

        fun create(song: PlaylistSong): RemoveFromPlaylistDialog {
            val list = ArrayList<PlaylistSong>()
            list.add(song)
            return create(list)
        }

        fun create(songs: ArrayList<PlaylistSong>): RemoveFromPlaylistDialog {
            val dialog = RemoveFromPlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", songs)
            dialog.arguments = args
            return dialog
        }
    }
}