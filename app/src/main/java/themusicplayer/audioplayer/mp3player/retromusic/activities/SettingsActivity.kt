package themusicplayer.audioplayer.mp3player.retromusic.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ATHUtil
import themusicplayer.audioplayer.mp3player.appthemehelper.util.ToolbarContentTintHelper
import themusicplayer.audioplayer.mp3player.retromusic.R
import themusicplayer.audioplayer.mp3player.retromusic.activities.base.AbsBaseActivity
import themusicplayer.audioplayer.mp3player.retromusic.fragments.settings.MainSettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AbsBaseActivity() {

    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setLightNavigationBar(true)

        setupToolbar()

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, MainSettingsFragment())
                .commit()
        }
    }

    private fun setupToolbar() {
        setTitle(R.string.action_settings)
        toolbar.apply {
            setBackgroundColor(ATHUtil.resolveColor(this@SettingsActivity, R.attr.colorSurface))
            setNavigationOnClickListener { onBackPressed() }
            ToolbarContentTintHelper.colorBackButton(toolbar)
        }
        setSupportActionBar(toolbar)
    }

    fun setupFragment(fragment: Fragment, @StringRes titleName: Int) {
        val fragmentTransaction = fragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.sliding_in_left,
                R.anim.sliding_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        fragmentTransaction.replace(R.id.contentFrame, fragment, fragment.tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        setTitle(titleName)
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            setTitle(R.string.action_settings)
            fragmentManager.popBackStack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val TAG: String = "SettingsActivity"
    }
}
