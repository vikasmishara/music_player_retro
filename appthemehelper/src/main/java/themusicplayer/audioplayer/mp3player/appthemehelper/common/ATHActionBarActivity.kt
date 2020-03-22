package themusicplayer.audioplayer.mp3player.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import themusicplayer.audioplayer.mp3player.appthemehelper.util.ToolbarContentTintHelper

class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
