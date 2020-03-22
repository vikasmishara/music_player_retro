package themusicplayer.audioplayer.mp3player.retromusic.fragments.mainactivity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import themusicplayer.audioplayer.mp3player.retromusic.App
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.adapter.song.ShuffleButtonSongAdapter
import themusicplayer.audioplayer.mp3player.retromusic.adapter.song.SongAdapter
import themusicplayer.audioplayer.mp3player.retromusic.fragments.base.AbsLibraryPagerRecyclerViewCustomGridSizeFragment
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.MainActivityFragmentCallbacks
import themusicplayer.audioplayer.mp3player.retromusic.model.Song
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.SongPresenter
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.SongView
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import java.util.*
import javax.inject.Inject

class SongsFragment :
    AbsLibraryPagerRecyclerViewCustomGridSizeFragment<SongAdapter, GridLayoutManager>(),
    SongView, MainActivityFragmentCallbacks {

    @Inject
    lateinit var songPresenter: SongPresenter

    override val emptyMessage: Int
        get() = R.string.no_songs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.musicComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songPresenter.attachView(this)
    }

    override fun createLayoutManager(): GridLayoutManager {
        println("createLayoutManager: ${getGridSize()}")
        return GridLayoutManager(requireActivity(), getGridSize()).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) {
                        getGridSize()
                    } else {
                        1
                    }
                }
            }
        }
    }

    override fun createAdapter(): SongAdapter {
        val dataSet = if (adapter == null) mutableListOf() else adapter!!.dataSet
        return ShuffleButtonSongAdapter(
            mainActivity,
            dataSet,
            itemLayoutRes(),
            mainActivity
        )
    }

    override fun songs(songs: List<Song>) {
        adapter?.swapDataSet(songs)
    }

    override fun onMediaStoreChanged() {
        songPresenter.loadSongs()
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.getInstance(requireContext()).getSongGridSize(requireContext())
    }

    override fun saveGridSize(gridColumns: Int) {
        PreferenceUtil.getInstance(requireContext()).setSongGridSize(gridColumns)
    }

    override fun loadGridSizeLand(): Int {
        return PreferenceUtil.getInstance(requireContext()).getSongGridSizeLand(requireContext())
    }

    override fun saveGridSizeLand(gridColumns: Int) {
        PreferenceUtil.getInstance(requireContext()).setSongGridSizeLand(gridColumns)
    }

    override fun setGridSize(gridSize: Int) {
        adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        if (adapter?.dataSet.isNullOrEmpty())
            songPresenter.loadSongs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        songPresenter.detachView()
    }

    override fun showEmptyView() {
        adapter?.swapDataSet(ArrayList())
    }

    override fun loadSortOrder(): String {
        return PreferenceUtil.getInstance(requireContext()).songSortOrder
    }

    override fun saveSortOrder(sortOrder: String) {
        PreferenceUtil.getInstance(requireContext()).songSortOrder = sortOrder
    }

    override fun setSortOrder(sortOrder: String) {
        songPresenter.loadSongs()
    }

    companion object {

        @JvmField
        var TAG: String = SongsFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): SongsFragment {
            val args = Bundle()
            val fragment = SongsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun setLayoutRes(@LayoutRes layoutRes: Int) {
    }

    @LayoutRes
    override fun loadLayoutRes(): Int {
        return PreferenceUtil.getInstance(requireContext()).songGridStyle
    }

    override fun saveLayoutRes(@LayoutRes layoutRes: Int) {
        PreferenceUtil.getInstance(requireContext()).songGridStyle = layoutRes
    }

    override fun handleBackPress(): Boolean {
        return false
    }
}
