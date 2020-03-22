package themusicplayer.audioplayer.mp3player.retromusic.fragments.base

import android.os.Bundle
import themusicplayer.audioplayer.mp3player.retromusic.activities.MainActivity

open class AbsLibraryPagerFragment : AbsMusicServiceFragment() {

    val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }
}
