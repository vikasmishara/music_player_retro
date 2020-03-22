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

package themusicplayer.audioplayer.mp3player.retromusic.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.LayoutRes
import themusicplayer.audioplayer.mp3player.appthemehelper.ThemeStore
import themusicplayer.audioplayer.mp3player.appthemehelper.util.TintHelper
import com.google.android.material.slider.Slider

@Suppress("UNCHECKED_CAST")
fun <T : View> ViewGroup.inflate(@LayoutRes layout: Int): T {
    return LayoutInflater.from(context).inflate(layout, this, false) as T
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.hidden() {
    visibility = View.INVISIBLE
}

fun View.showOrHide(show: Boolean) = if (show) show() else hide()

fun EditText.appHandleColor(): EditText {
    TintHelper.colorHandles(this, ThemeStore.accentColor(context))
    return this
}

fun Slider.setRange(progress: Float, to: Float) {
    valueFrom = 0F
    valueTo = to
    value = progress
}