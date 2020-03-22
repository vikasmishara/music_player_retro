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

package themusicplayer.audioplayer.mp3player.retromusic.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ATHUtil
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.extensions.getAdaptiveIconDrawable
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.RetroColorUtil


class ColorIconsImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : AppCompatImageView(context, attrs, defStyleAttr) {


    init {
        // Load the styled attributes and set their properties
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.ColorIconsImageView, 0, 0)
        val color =
            attributes.getColor(R.styleable.ColorIconsImageView_iconBackgroundColor, Color.RED);
        setIconBackgroundColor(color)
        attributes.recycle()
    }

    fun setIconBackgroundColor(color: Int) {
        background = getAdaptiveIconDrawable(context)
        if (ATHUtil.isWindowBackgroundDark(context) && PreferenceUtil.getInstance(context).desaturatedColor()) {
            val desaturatedColor = RetroColorUtil.desaturateColor(color, 0.4f)
            backgroundTintList = ColorStateList.valueOf(desaturatedColor)
            imageTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(context, R.attr.colorSurface))
        } else {
            backgroundTintList = ColorStateList.valueOf(color)
            imageTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(context, R.attr.colorSurface))
        }
        requestLayout()
        invalidate()
    }
}
