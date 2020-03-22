package themusicplayer.audioplayer.mp3player.retromusic.util

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.PowerManager
import androidx.annotation.StyleRes
import themusicplayer.audioplayer.mp3player.retromusic.R

/**
 * @author Paolo Valerdi
 */
object ThemeManager {

    @StyleRes
    fun getThemeResValue(context: Context): Int =
        when (PreferenceUtil.getInstance(context).generalThemeValue) {
            "light" -> R.style.Theme_RetroMusic_Light
            "dark" -> R.style.Theme_RetroMusic_Base
            "auto" -> R.style.Theme_RetroMusic_FollowSystem
            "black" -> R.style.Theme_RetroMusic_Black
            else -> R.style.Theme_RetroMusic_FollowSystem
        }

    private fun isSystemDarkModeEnabled(context: Context): Boolean {
        val isBatterySaverEnabled =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isPowerSaveMode
                ?: false
        val isDarkModeEnabled =
            (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

        return isBatterySaverEnabled or isDarkModeEnabled
    }

}