package themusicplayer.audioplayer.mp3player.retromusic.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ATHUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.MaterialUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ToolbarContentTintHelper
import themusicplayer.audioplayer.mp3player.retromusic.App
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.activities.base.AbsSlidingMusicPanelActivity
import themusicplayer.audioplayer.mp3player.retromusic.activities.tageditor.AbsTagEditorActivity
import themusicplayer.audioplayer.mp3player.retromusic.activities.tageditor.AlbumTagEditorActivity
import themusicplayer.audioplayer.mp3player.retromusic.adapter.album.HorizontalAlbumAdapter
import themusicplayer.audioplayer.mp3player.retromusic.adapter.song.SimpleSongAdapter
import themusicplayer.audioplayer.mp3player.retromusic.dialogs.AddToPlaylistDialog
import themusicplayer.audioplayer.mp3player.retromusic.dialogs.DeleteSongsDialog
import themusicplayer.audioplayer.mp3player.retromusic.extensions.ripAlpha
import themusicplayer.audioplayer.mp3player.retromusic.extensions.show
import themusicplayer.audioplayer.mp3player.retromusic.glide.AlbumGlideRequest
import themusicplayer.audioplayer.mp3player.retromusic.glide.ArtistGlideRequest
import themusicplayer.audioplayer.mp3player.retromusic.glide.RetroMusicColoredTarget
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.helper.SortOrder.AlbumSongSortOrder
import themusicplayer.audioplayer.mp3player.retromusic.interfaces.CabHolder
import themusicplayer.audioplayer.mp3player.retromusic.model.Album
import themusicplayer.audioplayer.mp3player.retromusic.model.Artist
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.AlbumDetailsPresenter
import themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter.AlbumDetailsView
import themusicplayer.audioplayer.mp3player.retromusic.rest.model.LastFmAlbum
import themusicplayer.audioplayer.mp3player.retromusic.util.*
import com.afollestad.materialcab.MaterialCab
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.activity_album_content.*
import java.util.*
import javax.inject.Inject
import android.util.Pair as UtilPair

class AlbumDetailsActivity : AbsSlidingMusicPanelActivity(), AlbumDetailsView, CabHolder {
    override fun openCab(menuRes: Int, callback: MaterialCab.Callback): MaterialCab {
        cab?.let {
            if (it.isActive) it.finish()
        }
        cab = MaterialCab(this, R.id.cab_stub)
            .setMenu(menuRes)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(
                RetroColorUtil.shiftBackgroundColorForLightText(
                    ATHUtil.resolveColor(
                        this,
                        R.attr.colorSurface
                    )
                )
            )
            .start(callback)
        return cab as MaterialCab
    }

    private lateinit var simpleSongAdapter: SimpleSongAdapter
    private lateinit var album: Album
    private lateinit var artistImage: ImageView
    private var cab: MaterialCab? = null
    private val savedSortOrder: String
        get() = PreferenceUtil.getInstance(this).albumDetailSongSortOrder

    override fun createContentView(): View {
        return wrapSlidingMusicPanel(R.layout.activity_album)
    }

    @Inject
    lateinit var albumDetailsPresenter: AlbumDetailsPresenter

    private fun windowEnterTransition() {
        val slide = Slide()
        slide.excludeTarget(R.id.appBarLayout, true)
        slide.excludeTarget(R.id.status_bar, true)
        slide.excludeTarget(android.R.id.statusBarBackground, true)
        slide.excludeTarget(android.R.id.navigationBarBackground, true)

        window.enterTransition = slide
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        toggleBottomNavigationView(true)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setLightNavigationBar(true)
        window.sharedElementsUseOverlay = true

        App.musicComponent.inject(this)
        albumDetailsPresenter.attachView(this)

        if (intent.extras!!.containsKey(EXTRA_ALBUM_ID)) {
            intent.extras?.getInt(EXTRA_ALBUM_ID)?.let {
                albumDetailsPresenter.loadAlbum(it)
                albumCoverContainer?.transitionName =
                    "${getString(R.string.transition_album_art)}_$it"
            }
        } else {
            finish()
        }

        windowEnterTransition()
        ActivityCompat.postponeEnterTransition(this)


        artistImage = findViewById(R.id.artistImage)

        setupRecyclerView()

        artistImage.setOnClickListener {
            val artistPairs = ActivityOptions.makeSceneTransitionAnimation(
                this,
                UtilPair.create(
                    artistImage,
                    "${getString(R.string.transition_artist_image)}_${album.artistId}"
                )
            )
            NavigationUtil.goToArtistOptions(this, album.artistId, artistPairs)
        }
        playAction.apply {
            setOnClickListener { MusicPlayerRemote.openQueue(album.songs!!, 0, true) }
        }
        shuffleAction.apply {
            setOnClickListener { MusicPlayerRemote.openAndShuffleQueue(album.songs!!, true) }
        }

        aboutAlbumText.setOnClickListener {
            if (aboutAlbumText.maxLines == 4) {
                aboutAlbumText.maxLines = Integer.MAX_VALUE
            } else {
                aboutAlbumText.maxLines = 4
            }
        }
    }

    private fun setupRecyclerView() {
        simpleSongAdapter = SimpleSongAdapter(this, ArrayList(), R.layout.item_song, this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AlbumDetailsActivity)
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = false
            adapter = simpleSongAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        albumDetailsPresenter.detachView()
    }

    override fun complete() {
        ActivityCompat.startPostponedEnterTransition(this)
    }

    override fun album(album: Album) {
        complete()
        if (album.songs!!.isEmpty()) {
            finish()
            return
        }
        this.album = album

        albumTitle.text = album.title
        if (MusicUtil.getYearString(album.year) == "-") {
            albumText.text = String.format(
                "%s • %s",
                album.artistName,
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs))
            )
        } else {
            albumText.text = String.format(
                "%s • %s • %s",
                album.artistName,
                MusicUtil.getYearString(album.year),
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs))
            )
        }
        loadAlbumCover()
        simpleSongAdapter.swapDataSet(album.songs)
        albumDetailsPresenter.loadMore(album.artistId)
        albumDetailsPresenter.aboutAlbum(album.artistName!!, album.title!!)
    }

    override fun moreAlbums(albums: List<Album>) {
        moreTitle.show()
        moreRecyclerView.show()
        moreTitle.text = String.format(getString(R.string.label_more_from), album.artistName)

        val albumAdapter = HorizontalAlbumAdapter(this, albums, null)
        moreRecyclerView.layoutManager = GridLayoutManager(
            this,
            1,
            GridLayoutManager.HORIZONTAL,
            false
        )
        moreRecyclerView.adapter = albumAdapter
    }

    override fun aboutAlbum(lastFmAlbum: LastFmAlbum) {
        if (lastFmAlbum.album != null) {
            if (lastFmAlbum.album.wiki != null) {
                aboutAlbumText.show()
                aboutAlbumTitle.show()
                aboutAlbumTitle.text = String.format("About %s", lastFmAlbum.album.name)
                aboutAlbumText.text = lastFmAlbum.album.wiki.content
            }
            if (lastFmAlbum.album.listeners.isNotEmpty()) {
                listeners.show()
                listenersLabel.show()
                scrobbles.show()
                scrobblesLabel.show()

                listeners.text = RetroUtil.formatValue(lastFmAlbum.album.listeners.toFloat())
                scrobbles.text = RetroUtil.formatValue(lastFmAlbum.album.playcount.toFloat())
            }
        }
    }

    override fun loadArtistImage(artist: Artist) {
        ArtistGlideRequest.Builder.from(Glide.with(this), artist)
            .generatePalette(this)
            .build()
            .dontAnimate()
            .dontTransform()
            .into(object : RetroMusicColoredTarget(artistImage) {
                override fun onColorReady(color: Int) {
                }
            })
    }

    private fun loadAlbumCover() {
        AlbumGlideRequest.Builder.from(Glide.with(this), album.safeGetFirstSong())
            .checkIgnoreMediaStore(this)
            .ignoreMediaStore(PreferenceUtil.getInstance(this).ignoreMediaStoreArtwork())
            .generatePalette(this)
            .build()
            .dontAnimate()
            .dontTransform()
            .into(object : RetroMusicColoredTarget(image) {
                override fun onColorReady(color: Int) {
                    setColors(color)
                }
            })
    }

    private fun setColors(color: Int) {
        val textColor = if (PreferenceUtil.getInstance(this).adaptiveColor)
            color.ripAlpha()
        else
            ATHUtil.resolveColor(this, android.R.attr.textColorPrimary)

        songTitle.setTextColor(textColor)
        moreTitle.setTextColor(textColor)
        aboutAlbumTitle.setTextColor(textColor)

        val buttonColor = if (PreferenceUtil.getInstance(this).adaptiveColor)
            color.ripAlpha()
        else
            ATHUtil.resolveColor(this, R.attr.colorSurface)

        MaterialUtil.setTint(button = shuffleAction, color = buttonColor)
        MaterialUtil.setTint(button = playAction, color = buttonColor)

        val toolbarColor = ATHUtil.resolveColor(this, R.attr.colorSurface)
        toolbar.setBackgroundColor(toolbarColor)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_album_detail, menu)
        val sortOrder = menu.findItem(R.id.action_sort_order)
        setUpSortOrderMenu(sortOrder.subMenu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            this,
            toolbar,
            menu,
            getToolbarBackgroundColor(toolbar)
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return handleSortOrderMenuItem(item)
    }

    private fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        var sortOrder: String? = null
        val songs = simpleSongAdapter.dataSet
        when (item.itemId) {
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(songs)
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(songs)
                return true
            }
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(songs).show(supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(songs).show(supportFragmentManager, "DELETE_SONGS")
                return true
            }
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(this, AlbumTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    albumCoverContainer,
                    "${getString(R.string.transition_album_art)}_${album.id}"
                )
                startActivityForResult(intent, TAG_EDITOR_REQUEST, options.toBundle())
                return true
            }
            /*Sort*/
            R.id.action_sort_order_title -> sortOrder = AlbumSongSortOrder.SONG_A_Z
            R.id.action_sort_order_title_desc -> sortOrder = AlbumSongSortOrder.SONG_Z_A
            R.id.action_sort_order_track_list -> sortOrder = AlbumSongSortOrder.SONG_TRACK_LIST
            R.id.action_sort_order_artist_song_duration -> sortOrder =
                AlbumSongSortOrder.SONG_DURATION
        }
        if (sortOrder != null) {
            item.isChecked = true
            setSaveSortOrder(sortOrder)
        }
        return true
    }

    private fun setUpSortOrderMenu(sortOrder: SubMenu) {
        when (savedSortOrder) {
            AlbumSongSortOrder.SONG_A_Z -> sortOrder.findItem(R.id.action_sort_order_title)
                .isChecked = true
            AlbumSongSortOrder.SONG_Z_A -> sortOrder.findItem(R.id.action_sort_order_title_desc)
                .isChecked = true
            AlbumSongSortOrder.SONG_TRACK_LIST -> sortOrder.findItem(R.id.action_sort_order_track_list)
                .isChecked =
                true
            AlbumSongSortOrder.SONG_DURATION -> sortOrder.findItem(R.id.action_sort_order_artist_song_duration)
                .isChecked = true
        }
    }

    private fun setSaveSortOrder(sortOrder: String?) {
        PreferenceUtil.getInstance(this).albumDetailSongSortOrder = sortOrder
        reload()
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        reload()
    }

    private fun reload() {
        if (intent.extras!!.containsKey(EXTRA_ALBUM_ID)) {
            intent.extras?.getInt(EXTRA_ALBUM_ID)?.let { albumDetailsPresenter.loadAlbum(it) }
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        if (cab != null && cab!!.isActive) {
            cab?.finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        const val EXTRA_ALBUM_ID = "extra_album_id"
        private const val TAG_EDITOR_REQUEST = 2001
    }
}