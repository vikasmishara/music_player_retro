package themusicplayer.audioplayer.mp3player.retromusic.adapter.playlist

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import themusicplayer.audioplayer.mp3player.appthemehelper.ThemeStore
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ATHUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.TintHelper
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.adapter.base.AbsMultiSelectAdapter
import themusicplayer.audioplayer.mp3player.retromusic.adapter.base.MediaEntryViewHolder
import themusicplayer.audioplayer.mp3player.retromusic.dialogs.ClearSmartPlaylistDialog
import themusicplayer.audioplayer.mp3player.retromusic.dialogs.DeletePlaylistDialog
import themusicplayer.audioplayer.mp3player.retromusic.extensions.hide
import themusicplayer.audioplayer.mp3player.retromusic.extensions.show
import themusicplayer.audioplayer.mp3player.retromusic.helper.menu.PlaylistMenuHelper
import themusicplayer.audioplayer.mp3player.retromusic.helper.menu.SongsMenuHelper
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.CabHolder
import themusicplayer.audioplayer.mp3player.retromusic.loaders.PlaylistSongsLoader
import themusicplayer.audioplayer.mp3player.retromusic.model.AbsCustomPlaylist
import themusicplayer.audioplayer.mp3player.retromusic.model.Playlist
import themusicplayer.audioplayer.mp3player.retromusic.model.Song
import themusicplayer.audioplayer.mp3player.retromusic.model.smartplaylist.AbsSmartPlaylist
import themusicplayer.audioplayer.mp3player.retromusic.model.smartplaylist.LastAddedPlaylist
import themusicplayer.audioplayer.mp3player.retromusic.util.MusicUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.NavigationUtil
import java.util.*

class PlaylistAdapter(
    private val activity: AppCompatActivity,
    var dataSet: List<Playlist>,
    private var itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist>(
    activity,
    cabHolder,
    R.menu.menu_playlists_selection
) {


    init {
        setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<Playlist>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view)
    }

    fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    private fun getPlaylistTitle(playlist: Playlist): String {
        return if (TextUtils.isEmpty(playlist.name)) "-" else playlist.name
    }

    private fun getPlaylistText(playlist: Playlist): String {
        return MusicUtil.getPlaylistInfoString(activity, getSongs(playlist))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]
        holder.itemView.isActivated = isChecked(playlist)
        holder.title?.text = getPlaylistTitle(playlist)
        holder.text?.text = getPlaylistText(playlist)
        holder.image?.setImageDrawable(getIconRes(playlist))
        val isChecked = isChecked(playlist)
        if (isChecked) {
            holder.menu?.hide()
        } else {
            holder.menu?.show()
        }
    }

    private fun getIconRes(playlist: Playlist): Drawable {
        return if (MusicUtil.isFavoritePlaylist(activity, playlist))
            TintHelper.createTintedDrawable(
                activity,
                R.drawable.ic_favorite_white_24dp,
                ThemeStore.accentColor(activity)
            )
        else TintHelper.createTintedDrawable(
            activity,
            R.drawable.ic_playlist_play_white_24dp,
            ATHUtil.resolveColor(activity, R.attr.colorControlNormal)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position] is AbsSmartPlaylist) SMART_PLAYLIST else DEFAULT_PLAYLIST
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): Playlist? {
        return dataSet[position]
    }

    override fun getName(playlist: Playlist): String {
        return playlist.name
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: ArrayList<Playlist>) {
        when (menuItem.itemId) {
            R.id.action_delete_playlist -> {
                var i = 0
                while (i < selection.size) {
                    val playlist = selection[i]
                    if (playlist is AbsSmartPlaylist) {
                        ClearSmartPlaylistDialog.create(playlist).show(
                            activity.supportFragmentManager, "CLEAR_PLAYLIST_" + playlist.name
                        )
                        selection.remove(playlist)
                        i--
                    }
                    i++
                }
                if (selection.size > 0) {
                    DeletePlaylistDialog.create(selection)
                        .show(activity.supportFragmentManager, "DELETE_PLAYLIST")
                }
            }
            else -> SongsMenuHelper.handleMenuClick(
                activity,
                getSongList(selection),
                menuItem.itemId
            )
        }
    }

    private fun getSongList(playlists: List<Playlist>): ArrayList<Song> {
        val songs = ArrayList<Song>()
        for (playlist in playlists) {
            if (playlist is AbsCustomPlaylist) {
                songs.addAll(playlist.getSongs(activity))
            } else {
                songs.addAll(PlaylistSongsLoader.getPlaylistSongList(activity, playlist.id))
            }
        }
        return songs
    }

    private fun getSongs(playlist: Playlist): ArrayList<Song> {
        val songs = ArrayList<Song>()
        if (playlist is AbsSmartPlaylist) {
            songs.addAll(playlist.getSongs(activity))
        } else {
            songs.addAll(PlaylistSongsLoader.getPlaylistSongList(activity, playlist.id))
        }
        return songs
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        init {

            image?.apply {
                val iconPadding =
                    activity.resources.getDimensionPixelSize(R.dimen.list_item_image_icon_padding)
                setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            }

            menu?.setOnClickListener { view ->
                val playlist = dataSet[adapterPosition]
                val popupMenu = PopupMenu(activity, view)
                popupMenu.inflate(
                    if (itemViewType == SMART_PLAYLIST) R.menu.menu_item_smart_playlist
                    else R.menu.menu_item_playlist
                )
                if (playlist is LastAddedPlaylist) {
                    popupMenu.menu.findItem(R.id.action_clear_playlist).isVisible = false
                }
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.action_clear_playlist) {
                        if (playlist is AbsSmartPlaylist) {
                            ClearSmartPlaylistDialog.create(playlist).show(
                                activity.supportFragmentManager,
                                "CLEAR_SMART_PLAYLIST_" + playlist.name
                            )
                            return@setOnMenuItemClickListener true
                        }
                    }
                    PlaylistMenuHelper.handleMenuClick(
                        activity, dataSet[adapterPosition], item
                    )
                }
                popupMenu.show()
            }

            imageTextContainer?.apply {
                cardElevation = 0f
                setCardBackgroundColor(Color.TRANSPARENT)
            }
        }

        override fun onClick(v: View?) {
            if (isInQuickSelectMode) {
                toggleChecked(adapterPosition)
            } else {
                val playlist = dataSet[adapterPosition]
                NavigationUtil.goToPlaylistNew(activity, playlist)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            toggleChecked(adapterPosition)
            return true
        }
    }

    companion object {
        val TAG: String = PlaylistAdapter::class.java.simpleName
        private const val SMART_PLAYLIST = 0
        private const val DEFAULT_PLAYLIST = 1
    }
}
