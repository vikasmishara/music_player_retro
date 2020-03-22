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

package themusicplayer.audioplayer.mp3player.retromusic.dialogs

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import themusicplayer.audioplayer.mp3player.appthemehelper.ThemeStore
import themusicplayer.audioplayer.mp3player.appthemehelper.util.TintHelper
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.helper.MusicPlayerRemote
import themusicplayer.audioplayer.mp3player.retromusic.service.MusicService
import themusicplayer.audioplayer.mp3player.retromusic.service.MusicService.ACTION_PENDING_QUIT
import themusicplayer.audioplayer.mp3player.retromusic.service.MusicService.ACTION_QUIT
import themusicplayer.audioplayer.mp3player.retromusic.util.MusicUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.PreferenceUtil
import themusicplayer.audioplayer.mp3player.retromusic.util.ViewUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

class SleepTimerDialog : DialogFragment() {

    private var seekArcProgress: Int = 0
    private lateinit var timerUpdater: TimerUpdater
    private lateinit var materialDialog: MaterialDialog
    private lateinit var shouldFinishLastSong: CheckBox
    private lateinit var seekBar: SeekBar
    private lateinit var timerDisplay: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        timerUpdater = TimerUpdater()

        materialDialog = MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .title(R.string.action_sleep_timer)
            .cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            .positiveButton(R.string.action_set) {
                PreferenceUtil.getInstance(requireContext()).sleepTimerFinishMusic =
                    shouldFinishLastSong.isChecked

                val minutes = seekArcProgress

                val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)

                val nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000
                PreferenceUtil.getInstance(requireContext())
                    .setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime)
                val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime, pi)

                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.sleep_timer_set, minutes),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .negativeButton(android.R.string.cancel) {
                if (activity == null) {
                    return@negativeButton
                }
                val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
                if (previous != null) {
                    val am =
                        requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    am.cancel(previous)
                    previous.cancel()
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.sleep_timer_canceled),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val musicService = MusicPlayerRemote.musicService
                if (musicService != null && musicService.pendingQuit) {
                    musicService.pendingQuit = false
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.sleep_timer_canceled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .customView(R.layout.dialog_sleep_timer, scrollable = false)
            .show {
                onShow {
                    if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
                        timerUpdater.start()
                    }
                }
            }

        if (activity == null || materialDialog.getCustomView() == null) {
            return materialDialog
        }

        shouldFinishLastSong =
            materialDialog.getCustomView().findViewById(R.id.shouldFinishLastSong)
        seekBar = materialDialog.getCustomView().findViewById(R.id.seekBar)
        timerDisplay = materialDialog.getCustomView().findViewById(R.id.timerDisplay)
        TintHelper.setTintAuto(
            shouldFinishLastSong,
            ThemeStore.accentColor(requireContext()),
            false
        )

        val finishMusic = PreferenceUtil.getInstance(requireContext()).sleepTimerFinishMusic
        shouldFinishLastSong.isChecked = finishMusic


        seekArcProgress = PreferenceUtil.getInstance(requireContext()).lastSleepTimerValue
        updateTimeDisplayTime()
        seekBar.progress = seekArcProgress

        setProgressBarColor(ThemeStore.accentColor(requireContext()))

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 1) {
                    seekBar.progress = 1
                    return
                }
                seekArcProgress = i
                updateTimeDisplayTime()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferenceUtil.getInstance(requireContext()).lastSleepTimerValue = seekArcProgress
            }
        })

        return materialDialog
    }

    private fun updateTimeDisplayTime() {
        timerDisplay.text = "$seekArcProgress min"
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(requireActivity(), 0, makeTimerIntent(), flag)
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(requireActivity(), MusicService::class.java)
        return if (shouldFinishLastSong.isChecked) {
            intent.setAction(ACTION_PENDING_QUIT)
        } else intent.setAction(ACTION_QUIT)
    }

    private fun updateCancelButton() {
        val musicService = MusicPlayerRemote.musicService
        if (musicService != null && musicService.pendingQuit) {
            materialDialog.getActionButton(WhichButton.NEGATIVE).text =
                materialDialog.context.getString(R.string.cancel_current_timer)
        } else {
            materialDialog.getActionButton(WhichButton.NEGATIVE).text = null
        }
    }

    private inner class TimerUpdater internal constructor() : CountDownTimer(
        PreferenceUtil.getInstance(requireContext()).nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime(),
        1000
    ) {

        override fun onTick(millisUntilFinished: Long) {
            materialDialog.getActionButton(WhichButton.NEGATIVE).text = String.format(
                "%s %s",
                materialDialog.context.getString(R.string.cancel_current_timer),
                " (" + MusicUtil.getReadableDurationString(millisUntilFinished) + ")"
            )
        }

        override fun onFinish() {
            updateCancelButton()
        }
    }

    private fun setProgressBarColor(dark: Int) {
        ViewUtil.setProgressDrawable(progressSlider = seekBar, newColor = dark)
    }
}