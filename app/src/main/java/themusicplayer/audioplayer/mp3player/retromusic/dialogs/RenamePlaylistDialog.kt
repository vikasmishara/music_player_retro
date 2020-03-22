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
import android.provider.MediaStore.Audio.Playlists.Members.PLAYLIST_ID
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import themusicplayer.audioplayer.mp3player.appthemehelper.util.MaterialUtil
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.R.layout
import themusicplayer.audioplayer.mp3player.retromusic.R.string
import themusicplayer.audioplayer.mp3player.retromusic.extensions.appHandleColor
import themusicplayer.audioplayer.mp3player.retromusic.util.PlaylistsUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RenamePlaylistDialog : DialogFragment() {
    private lateinit var playlistView: TextInputEditText
    private lateinit var actionNewPlaylistContainer: TextInputLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val materialDialog = MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .show {
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
                title(string.rename_playlist_title)
                customView(layout.dialog_playlist)
                negativeButton(android.R.string.cancel)
                positiveButton(string.action_rename) {
                    if (playlistView.toString().trim { it <= ' ' } != "") {
                        val playlistId = requireArguments().getLong(PLAYLIST_ID)
                        PlaylistsUtil.renamePlaylist(
                            context,
                            playlistId,
                            playlistView.text!!.toString()
                        )
                    }
                }
            }

        val dialogView = materialDialog.getCustomView()
        playlistView = dialogView.findViewById(R.id.actionNewPlaylist)
        actionNewPlaylistContainer = dialogView.findViewById(R.id.actionNewPlaylistContainer)

        MaterialUtil.setTint(actionNewPlaylistContainer, false)

        val playlistId = arguments!!.getLong(PLAYLIST_ID)
        playlistView.appHandleColor()
            .setText(
                PlaylistsUtil.getNameForPlaylist(context!!, playlistId),
                TextView.BufferType.EDITABLE
            )
        return materialDialog
    }

    companion object {

        fun create(playlistId: Long): RenamePlaylistDialog {
            val dialog = RenamePlaylistDialog()
            val args = Bundle()
            args.putLong(PLAYLIST_ID, playlistId)
            dialog.arguments = args
            return dialog
        }
    }
}