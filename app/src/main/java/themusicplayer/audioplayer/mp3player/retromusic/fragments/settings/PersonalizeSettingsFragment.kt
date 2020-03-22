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

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil

class PersonalizeSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun invalidateSettings() {

        val toggleFullScreen: TwoStatePreference = findPreference("toggle_full_screen")!!
        toggleFullScreen.setOnPreferenceChangeListener { _, _ ->
            requireActivity().recreate()
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_ui)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.getInstance(requireContext()).registerOnSharedPreferenceChangedListener(this)

        var preference: Preference? = findPreference("home_artist_grid_style")
        setSummary(preference!!)
        preference = findPreference("tab_text_mode")
        setSummary(preference!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.getInstance(requireContext())
            .unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.CAROUSEL_EFFECT -> invalidateSettings()
        }
    }
}
