package themusicplayer.audioplayer.mp3player.retromusic.adapter.song

import android.app.ActivityOptions
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.CabHolder
import themusicplayer.audioplayer.mp3player.retromusic.model.Song
import themusicplayer.audioplayer.mp3player.retromusic.util.NavigationUtil
import com.google.android.material.button.MaterialButton

open class PlaylistSongAdapter(
    activity: AppCompatActivity,
    dataSet: MutableList<Song>,
    itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsOffsetSongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    init {
        this.setMultiSelectMenuRes(R.menu.menu_cannot_delete_single_songs_playlist_songs_selection)
    }

    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        if (holder.itemViewType == OFFSET_ITEM) {
            val viewHolder = holder as ViewHolder
            viewHolder.playAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openQueue(dataSet, 0, true)
                }
            }
            viewHolder.shuffleAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(dataSet, true)
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }
    }

    open inner class ViewHolder(itemView: View) : AbsOffsetSongAdapter.ViewHolder(itemView) {

        val playAction: MaterialButton? = itemView.findViewById(R.id.playAction)
        val shuffleAction: MaterialButton? = itemView.findViewById(R.id.shuffleAction)

        override var songMenuRes: Int
            get() = R.menu.menu_item_cannot_delete_single_songs_playlist_song
            set(value) {
                super.songMenuRes = value
            }

        override fun onSongMenuItemClick(item: MenuItem): Boolean {
            if (item.itemId == R.id.action_go_to_album) {
                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    imageContainerCard ?: image,
                    "${activity.getString(R.string.transition_album_art)}_${song.albumId}"
                )
                NavigationUtil.goToAlbumOptions(activity, song.albumId, activityOptions)
                return true
            }
            return super.onSongMenuItemClick(item)
        }
    }

    companion object {
        val TAG: String = PlaylistSongAdapter::class.java.simpleName
    }
}