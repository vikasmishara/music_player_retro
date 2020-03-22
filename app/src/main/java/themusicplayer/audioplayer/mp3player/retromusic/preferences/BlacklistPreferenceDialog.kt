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

package themusicplayer.audioplayer.mp3player.retromusic.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import themusicplayer.audioplayer.mp3player.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.dialogs.BlacklistFolderChooserDialog
import themusicplayer.audioplayer.mp3player.retromusic.extensions.colorControlNormal
import themusicplayer.audioplayer.mp3player.retromusic.providers.BlacklistStore
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import java.io.File
import java.util.*

class BlacklistPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                colorControlNormal(context),
                SRC_IN
            )
    }
}

class BlacklistPreferenceDialog : DialogFragment(), BlacklistFolderChooserDialog.FolderCallback {
    companion object {
        fun newInstance(): BlacklistPreferenceDialog {
            return BlacklistPreferenceDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val blacklistFolderChooserDialog =
            childFragmentManager.findFragmentByTag("FOLDER_CHOOSER") as BlacklistFolderChooserDialog?
        blacklistFolderChooserDialog?.setCallback(this)
        refreshBlacklistData()
        return MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(themusicplayer.audioplayer.mp3player.retromusic.R.string.blacklist)
            cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            positiveButton(android.R.string.ok) {
                dismiss()
            }
            neutralButton(text = getString(R.string.clear_action)) {
                MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    title(themusicplayer.audioplayer.mp3player.retromusic.R.string.clear_blacklist)
                    message(themusicplayer.audioplayer.mp3player.retromusic.R.string.do_you_want_to_clear_the_blacklist)
                    cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
                    positiveButton(themusicplayer.audioplayer.mp3player.retromusic.R.string.clear_action) {
                        BlacklistStore.getInstance(context).clear()
                        refreshBlacklistData()
                    }
                    negativeButton(android.R.string.cancel)
                }
            }
            negativeButton(R.string.add_action) {
                val dialog = BlacklistFolderChooserDialog.create()
                dialog.setCallback(this@BlacklistPreferenceDialog)
                dialog.show(childFragmentManager, "FOLDER_CHOOSER")
            }
            listItems(items = paths, waitForPositiveButton = false) { _, _, text ->
                MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
                    title(themusicplayer.audioplayer.mp3player.retromusic.R.string.remove_from_blacklist)
                    message(
                        text = HtmlCompat.fromHtml(
                            getString(
                                themusicplayer.audioplayer.mp3player.retromusic.R.string.do_you_want_to_remove_from_the_blacklist,
                                text
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    positiveButton(themusicplayer.audioplayer.mp3player.retromusic.R.string.remove_action) {
                        BlacklistStore.getInstance(context).removePath(File(text.toString()))
                        refreshBlacklistData()
                    }
                    negativeButton(android.R.string.cancel)
                }
            }
            noAutoDismiss()
        }
    }

    private lateinit var paths: ArrayList<String>

    private fun refreshBlacklistData() {
        this.paths = BlacklistStore.getInstance(context!!).paths
        val dialog = dialog as MaterialDialog?
        dialog?.listItems(items = paths)
    }

    override fun onFolderSelection(dialog: BlacklistFolderChooserDialog, folder: File) {
        BlacklistStore.getInstance(context!!).addPath(folder)
        refreshBlacklistData()
    }
}
