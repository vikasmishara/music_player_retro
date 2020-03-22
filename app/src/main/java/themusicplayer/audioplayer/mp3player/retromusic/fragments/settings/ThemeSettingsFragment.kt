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

package themusicplayer.audioplayer.mp3player.retromusic.fragments.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import themusicplayer.audioplayer.mp3player.appthemehelper.ACCENT_COLORS
import themusicplayer.audioplayer.mp3player.appthemehelper.ACCENT_COLORS_SUB
import themusicplayer.audioplayer.mp3player.appthemehelper.ThemeStore
import themusicplayer.audioplayer.mp3player.appthemehelper.common.prefs.supportv7.ATEColorPreference
import themusicplayer.audioplayer.mp3player.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ColorUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.VersionUtils
import themusicplayer.audioplayer.mp3player.retromusic.App
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.appshortcuts.DynamicShortcutManager
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.colorChooser


/**
 * @author Hemanth S (h4h13).
 */

class ThemeSettingsFragment : AbsSettingsFragment() {
    override fun invalidateSettings() {
        val generalTheme: Preference? = findPreference("general_theme")
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                val theme = newValue as String
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    requireActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(theme))
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                }
                requireActivity().recreate()
                true
            }
        }

        val accentColorPref: ATEColorPreference = findPreference("accent_color")!!
        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor))

        accentColorPref.setOnPreferenceClickListener {
            MaterialDialog(requireActivity(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
                title(R.string.accent_color)
                positiveButton(R.string.set)
                colorChooser(
                    colors = ACCENT_COLORS,
                    allowCustomArgb = true,
                    subColors = ACCENT_COLORS_SUB
                ) { _, color ->
                    ThemeStore.editTheme(requireContext()).accentColor(color).commit()
                    if (VersionUtils.hasNougatMR())
                        DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    requireActivity().recreate()
                }
            }
            return@setOnPreferenceClickListener true
        }
        val blackTheme: ATESwitchPreference? = findPreference("black_theme")
        blackTheme?.setOnPreferenceChangeListener { _, _ ->
            if (!App.isProVersion()) {
                showProToastAndNavigate("Just Black theme")
                return@setOnPreferenceChangeListener false
            }
            ThemeStore.markChanged(requireContext())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                requireActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue("black"))
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            }
            requireActivity().recreate()
            true
        }

        val desaturatedColor: ATESwitchPreference? =
            findPreference(PreferenceUtil.DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit().putBoolean("desaturated_color", desaturated)
                .apply()
            PreferenceUtil.getInstance(requireContext()).setDesaturatedColor(desaturated)
            requireActivity().recreate()
            true
        }


        val colorAppShortcuts: TwoStatePreference = findPreference("should_color_app_shortcuts")!!
        if (!VersionUtils.hasNougatMR()) {
            colorAppShortcuts.isVisible = false
        } else {
            colorAppShortcuts.isChecked =
                PreferenceUtil.getInstance(requireContext()).coloredAppShortcuts()
            colorAppShortcuts.setOnPreferenceChangeListener { _, newValue ->
                // Save preference
                PreferenceUtil.getInstance(requireContext())
                    .setColoredAppShortcuts(newValue as Boolean)
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }
}
