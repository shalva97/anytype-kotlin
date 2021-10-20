package com.anytypeio.anytype.ui.splash

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    @Inject
    lateinit var factory: SplashViewModelFactory
    private val vm by viewModels<SplashViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showVersion()
    }

    override fun onResume() {
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { observe(it) }
        }
        super.onResume()
    }

    private fun observe(command: SplashViewModel.Command) {
        when (command) {
            SplashViewModel.Command.CheckFirstInstall -> {
                val isFirstInstall = isFirstInstall()
                vm.setDefaultUserSettings(isFirstInstall = isFirstInstall)
            }
            is SplashViewModel.Command.Error -> {
                toast(command.msg)
                error.visible()
            }
            SplashViewModel.Command.NavigateToDashboard -> {
                findNavController().navigate(
                    R.id.action_splashScreen_to_desktopScreen
                )
            }
            is SplashViewModel.Command.NavigateToObject -> {
                findNavController().navigate(
                    R.id.action_splashScreen_to_objectScreen,
                    bundleOf(EditorFragment.ID_KEY to command.id),
                )
            }
            is SplashViewModel.Command.NavigateToObjectSet -> {
                findNavController().navigate(
                    R.id.action_splashScreen_to_objectSetScreen,
                    bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to command.id),
                )
            }
            SplashViewModel.Command.NavigateToLogin -> {
                findNavController().navigate(
                    R.id.action_splashFragment_to_login_nav
                )
            }
        }
    }

    private fun showVersion() {
        version.text = "${BuildConfig.VERSION_NAME}-alpha"
    }

    private fun isFirstInstall(): Boolean {
        return try {
            val firstInstallTime: Long =
                requireContext().packageManager.getPackageInfo(
                    BuildConfig.APPLICATION_ID,
                    0
                ).firstInstallTime
            val lastUpdateTime: Long =
                requireContext().packageManager.getPackageInfo(
                    BuildConfig.APPLICATION_ID,
                    0
                ).lastUpdateTime
            firstInstallTime == lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Error while checking first install time")
            false
        }
    }

    override fun injectDependencies() {
        componentManager().splashLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().splashLoginComponent.release()
    }
}