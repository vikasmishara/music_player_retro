package themusicplayer.audioplayer.mp3player.retromusic.fragments.mainactivity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import themusicplayer.audioplayer.mp3player.retromusic.App
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.adapter.playlist.PlaylistAdapter
import themusicplayer.audioplayer.mp3player.retromusic.fragments.base.AbsLibraryPagerRecyclerViewFragment
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.MainActivityFragmentCallbacks
import themusicplayer.audioplayer.mp3player.retromusic.model.Playlist
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.PlaylistView
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.PlaylistsPresenter
import javax.inject.Inject

class PlaylistsFragment :
    AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager>(), PlaylistView,
    MainActivityFragmentCallbacks {

    override fun handleBackPress(): Boolean {
        return false
    }

    @Inject
    lateinit var playlistsPresenter: PlaylistsPresenter

    override val emptyMessage: Int
        get() = R.string.no_playlists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.musicComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistsPresenter.attachView(this)
    }

    override fun createLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(activity)
    }

    override fun createAdapter(): PlaylistAdapter {
        return PlaylistAdapter(
            mainActivity,
            ArrayList(),
            R.layout.item_list,
            mainActivity
        )
    }

    override fun onResume() {
        super.onResume()
        if (adapter!!.dataSet.isNullOrEmpty()) {
            playlistsPresenter.playlists()
        }
    }

    override fun onDestroyView() {
        playlistsPresenter.detachView()
        super.onDestroyView()
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        playlistsPresenter.playlists()
    }

    override fun showEmptyView() {
        adapter?.swapDataSet(ArrayList())
    }

    override fun playlists(playlists: List<Playlist>) {
        adapter?.swapDataSet(playlists)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.apply {
            removeItem(R.id.action_sort_order)
            removeItem(R.id.action_grid_size)
        }
    }

    companion object {
        @JvmField
        val TAG: String = PlaylistsFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PlaylistsFragment {
            val args = Bundle()
            val fragment = PlaylistsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
