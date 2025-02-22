package code.name.monkey.retromusic

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import code.name.monkey.retromusic.activities.WhatsNewFragment
import code.name.monkey.retromusic.activities.base.AbsCastActivity
import code.name.monkey.retromusic.extensions.currentFragment
import code.name.monkey.retromusic.extensions.findNavController
import code.name.monkey.retromusic.extensions.hideStatusBar
import code.name.monkey.retromusic.extensions.setTaskDescriptionColorAuto
import code.name.monkey.retromusic.model.CategoryInfo
import code.name.monkey.retromusic.util.AppRater
import code.name.monkey.retromusic.util.PreferenceUtil

class MainActivity : AbsCastActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val EXPAND_PANEL = "expand_panel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTaskDescriptionColorAuto()
        hideStatusBar()
        AppRater.appLaunched(this)
        setupNavigationController()

        WhatsNewFragment.showChangeLog(this)
    }

    private fun setupNavigationController() {
        val navController = findNavController(R.id.fragment_container)
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.main_graph)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            if (!navGraph.contains(PreferenceUtil.lastTab)) PreferenceUtil.lastTab =
                categoryInfo.category.id
            navGraph.setStartDestination(
                if (PreferenceUtil.rememberLastTab) {
                    PreferenceUtil.lastTab.let {
                        if (it == 0) {
                            categoryInfo.category.id
                        } else {
                            it
                        }
                    }
                } else categoryInfo.category.id
            )
        }
        navController.graph = navGraph
        navigationView.setupWithNavController(navController)
        // Scroll Fragment to top
        navigationView.setOnItemReselectedListener {
            currentFragment(R.id.fragment_container).apply {
                if (this is IScrollHelper) {
                    scrollToTop()
                }
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == navGraph.startDestinationId) {
                currentFragment(R.id.fragment_container)?.enterTransition = null
            }
            when (destination.id) {
                R.id.action_home, R.id.action_song, R.id.action_album, R.id.action_artist, R.id.action_folder, R.id.action_playlist, R.id.action_genre, R.id.action_search -> {
                    // Save the last tab
                    if (PreferenceUtil.rememberLastTab) {
                        saveTab(destination.id)
                    }
                    // Show Bottom Navigation Bar
                    setBottomNavVisibility(visible = true, animate = true)
                }
                R.id.playing_queue_fragment -> {
                    setBottomNavVisibility(visible = false, hideBottomSheet = true)
                }
                else -> setBottomNavVisibility(
                    visible = false,
                    animate = true
                ) // Hide Bottom Navigation Bar
            }
        }
    }

    fun updateTabs() {
//        binding.navigationView.menu.clear()
//        val currentTabs: List<CategoryInfo> = PreferenceUtil.libraryCategory
//        for (tab in currentTabs) {
//            if (tab.visible) {
//                val menu = tab.category
//                binding.navigationView.menu.add(0, menu.id, 0, menu.stringRes)
//                    .setIcon(menu.icon)
//            }
//        }
//        if (binding.navigationView.menu.size() == 1) {
//            isInOneTabMode = true
//            binding.navigationView.isVisible = false
//        } else {
//            isInOneTabMode = false
//        }
    }

    override fun onServiceConnected() {
        TODO("Not yet implemented")
    }

    override fun onServiceDisconnected() {
        TODO("Not yet implemented")
    }

    override fun onQueueChanged() {
        TODO("Not yet implemented")
    }

    override fun onFavoriteStateChanged() {
        TODO("Not yet implemented")
    }

    override fun onPlayingMetaChanged() {
        TODO("Not yet implemented")
    }

    override fun onPlayStateChanged() {
        TODO("Not yet implemented")
    }

    override fun onRepeatModeChanged() {
        TODO("Not yet implemented")
    }

    override fun onShuffleModeChanged() {
        TODO("Not yet implemented")
    }

    override fun onMediaStoreChanged() {
        TODO("Not yet implemented")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        TODO("Not yet implemented")
    }


}