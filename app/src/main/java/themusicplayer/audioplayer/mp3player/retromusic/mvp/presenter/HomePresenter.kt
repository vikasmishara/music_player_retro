/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package themusicplayer.audioplayer.mp3player.retromusic.mvp.presenter

import themusicplayer.audioplayer.mp3player.retromusic.Result
import themusicplayer.audioplayer.mp3player.retromusic.model.Home
import themusicplayer.audioplayer.mp3player.retromusic.mvp.BaseView
import themusicplayer.audioplayer.mp3player.retromusic.mvp.Presenter
import themusicplayer.audioplayer.mp3player.retromusic.mvp.PresenterImpl
import themusicplayer.audioplayer.mp3player.retromusic.providers.interfaces.Repository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface HomeView : BaseView {
    fun sections(sections: List<Home>)
}

interface HomePresenter : Presenter<HomeView> {
    fun loadSections()

    class HomePresenterImpl @Inject constructor(
        private val repository: Repository
    ) : PresenterImpl<HomeView>(), HomePresenter, CoroutineScope {

        private val job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun detachView() {
            super.detachView()
            job.cancel()
        }

        override fun loadSections() {
            launch {
                val list = ArrayList<Home>()
                val recentArtistResult = listOf(
                    repository.topArtists(),
                    repository.topAlbums(),
                    repository.recentArtists(),
                    repository.recentAlbums(),
                    repository.favoritePlaylist()
                )
                for (r in recentArtistResult) {
                    when (r) {
                        is Result.Success -> list.add(r.data)
                    }
                }
                withContext(Dispatchers.Main) {
                    if (list.isNotEmpty()) view?.sections(list) else view?.showEmptyView()
                }
            }
        }
    }
}