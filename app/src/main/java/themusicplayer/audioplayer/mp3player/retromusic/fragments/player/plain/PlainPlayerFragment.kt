package themusicplayer.audioplayer.mp3player.retromusic.fragments.player.plain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ATHUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ToolbarContentTintHelper
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.fragments.base.AbsPlayerFragment
import themusicplayer.audioplayer.mp3player.retromusic.fragments.player.PlayerAlbumCoverFragment
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.model.Song
import kotlinx.android.synthetic.main.fragment_plain_player.*

class PlainPlayerFragment : AbsPlayerFragment() {
    override fun playerToolbar(): Toolbar {
        return playerToolbar
    }

    private lateinit var plainPlaybackControlsFragment: PlainPlaybackControlsFragment
    private var lastColor: Int = 0
    override val paletteColor: Int
        get() = lastColor

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        title.text = song.title
        text.text = song.artistName
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plain_player, container, false)
    }

    private fun setUpPlayerToolbar() {
        playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            setOnMenuItemClickListener(this@PlainPlayerFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                this,
                ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
                requireActivity()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSubFragments()
        setUpPlayerToolbar()
        title.isSelected = true
        text.isSelected = true
    }

    private fun setUpSubFragments() {
        plainPlaybackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as PlainPlaybackControlsFragment
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        playerAlbumCoverFragment.setCallbacks(this)
    }

    override fun onShow() {
        plainPlaybackControlsFragment.show()
    }

    override fun onHide() {
        plainPlaybackControlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun toolbarIconColor(): Int {
        return ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal)
    }

    override fun onColorChanged(color: Int) {
        plainPlaybackControlsFragment.setDark(color)
        lastColor = color
        callbacks!!.onPaletteColorChanged()
        ToolbarContentTintHelper.colorizeToolbar(
            playerToolbar,
            ATHUtil.resolveColor(requireContext(), R.attr.colorControlNormal),
            requireActivity()
        )
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }
}
