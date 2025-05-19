package code.name.monkey.retromusic.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <reified T> Flow<T>.launchAndCollectIn(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit = {}
): Job = lifecycleOwner.lifecycleScope.launch {
    lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
        collect(action)
    }
}

inline fun <reified T> Flow<T>.launchAndCollectInActivity(
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit = {}
): Job = lifecycleCoroutineScope.launch {
    lifecycleCoroutineScope.launchWhenStarted {
        collect(action)
    }
}