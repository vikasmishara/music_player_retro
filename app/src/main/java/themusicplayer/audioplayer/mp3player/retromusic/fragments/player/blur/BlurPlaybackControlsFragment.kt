package themusicplayer.audioplayer.mp3player.retromusic.fragments.player.blur

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ColorUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.MaterialValueHelper
import themusicplayer.audioplayer.mp3player.appthemehelper.util.TintHelper
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.extensions.hide
import themusicplayer.audioplayer.mp3player.retromusic.extensions.show
import themusicplayer.audioplayer.mp3player.retromusic.fragments.base.AbsPlayerControlsFragment
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicProgressViewUpdateHelper
import themusicplayer.audioplayer.mp3player.retromusic.helper.PlayPauseButtonOnClickHandler
import themusicplayer.audioplayer.mp3player.retromusic.service.MusicService
import themusicplayer.audioplayer.mp3player.retromusic.util.MusicUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import kotlinx.android.synthetic.main.fragment_blur_player_playback_controls.*

class BlurPlaybackControlsFragment : AbsPlayerControlsFragment() {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blur_player_playback_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMusicControllers()

        playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            showBonceAnimation()
        }
        title.isSelected = true
        text.isSelected = true
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        title.text = song.title
        text.text = String.format("%s • %s", song.artistName, song.albumName)

        if (PreferenceUtil.getInstance(requireContext()).isSongInfo) {
            songInfo.show()
            songInfo?.text = getSongInfo(song)
        } else {
            songInfo?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper!!.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper!!.stop()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setDark(color: Int) {
        lastPlaybackControlsColor = Color.WHITE
        lastDisabledPlaybackControlsColor =
            ContextCompat.getColor(requireContext(), R.color.md_grey_500)

        title.setTextColor(lastPlaybackControlsColor)

        songCurrentProgress.setTextColor(lastPlaybackControlsColor)
        songTotalTime.setTextColor(lastPlaybackControlsColor)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()

        text.setTextColor(lastDisabledPlaybackControlsColor)
        songInfo.setTextColor(lastDisabledPlaybackControlsColor)

        TintHelper.setTintAuto(progressSlider, lastPlaybackControlsColor, false)
        volumeFragment?.setTintableColor(lastPlaybackControlsColor)
        setFabColor(lastPlaybackControlsColor)
    }

    private fun setFabColor(i: Int) {
        TintHelper.setTintAuto(
            playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(i)),
            false
        )
        TintHelper.setTintAuto(playPauseButton, i, true)
    }

    private fun setUpPlayPauseFab() {
        playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor() {
        nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        previousButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> shuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }

    override fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton.setColorFilter(
                    lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN
                )
            }
            MusicService.REPEAT_MODE_ALL -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_THIS -> {
                repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp)
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    public override fun show() {
        playPauseButton!!.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        if (playPauseButton != null) {
            playPauseButton!!.apply {
                scaleX = 0f
                scaleY = 0f
                rotation = 0f
            }
        }
    }

    private fun showBonceAnimation() {
        playPauseButton.apply {
            clearAnimation()
            scaleX = 0.9f
            scaleY = 0.9f
            visibility = View.VISIBLE
            pivotX = (width / 2).toFloat()
            pivotY = (height / 2).toFloat()

            animate().setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    animate().setDuration(200)
                        .setInterpolator(AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f).start()
                }.start()
        }
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        progressSlider.valueTo = total.toFloat()
        progressSlider.value = progress.toFloat()
        songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }

    override fun setUpProgressSlider() {
        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                MusicPlayerRemote.seekTo(value.toInt())
                onUpdateProgressViews(
                    MusicPlayerRemote.songProgressMillis,
                    MusicPlayerRemote.songDurationMillis
                )
            }
        }
    }
}
