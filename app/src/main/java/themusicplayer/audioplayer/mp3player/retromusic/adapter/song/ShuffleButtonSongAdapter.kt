package themusicplayer.audioplayer.mp3player.retromusic.adapter.song

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.CabHolder
import themusicplayer.audioplayer.mp3player.retromusic.model.Song
import com.google.android.material.button.MaterialButton

class ShuffleButtonSongAdapter(
    activity: AppCompatActivity,
    dataSet: MutableList<Song>,
    itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsOffsetSongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        if (holder.itemViewType == OFFSET_ITEM) {
            val viewHolder = holder as ViewHolder
            viewHolder.playAction?.setOnClickListener {
                MusicPlayerRemote.openQueue(dataSet, 0, true)
            }
            viewHolder.shuffleAction?.setOnClickListener {
                MusicPlayerRemote.openAndShuffleQueue(dataSet, true)
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }
    }

    inner class ViewHolder(itemView: View) : AbsOffsetSongAdapter.ViewHolder(itemView) {
        val playAction: MaterialButton? = itemView.findViewById(R.id.playAction)
        val shuffleAction: MaterialButton? = itemView.findViewById(R.id.shuffleAction)

        override fun onClick(v: View?) {
            if (itemViewType == OFFSET_ITEM) {
                MusicPlayerRemote.openAndShuffleQueue(dataSet, true)
                return
            }
            super.onClick(v)
        }
    }
}