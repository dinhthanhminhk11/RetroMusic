package code.name.monkey.retromusic.util

import android.os.Looper
import android.util.SparseArray
import androidx.annotation.UiThread
import code.name.monkey.retromusic.BuildConfig
import timber.log.Timber
import androidx.core.util.isNotEmpty
import androidx.core.util.size

class EventCenter private constructor(private val currentAccount: Int) {
    enum class EventType {
        STOP_ALL_HEAVY_OPERATIONS,
        START_ALL_HEAVY_OPERATIONS,
        UPLOAD_PROGRESS_ACTION,
        UPLOAD_ACTION_FAILED,
        UPLOAD_START_UPLOAD_PROGRESS
    }

    companion object {
        private val instance = arrayOfNulls<EventCenter>(3)
        private var globalInstance: EventCenter? = null

        @UiThread
        @JvmStatic
        fun getInstance(num: Int): EventCenter {
            var localInstance = instance[num]
            if (localInstance == null) {
                synchronized(EventCenter::class.java) {
                    localInstance = instance[num]
                    if (localInstance == null) {
                        instance[num] = EventCenter(num).also { localInstance = it }
                    }
                }
            }
            return localInstance!!
        }

        @UiThread
        @JvmStatic
        fun getGlobalInstance(): EventCenter {
            var localInstance = globalInstance
            if (localInstance == null) {
                synchronized(EventCenter::class.java) {
                    localInstance = globalInstance
                    if (localInstance == null) {
                        globalInstance = EventCenter(-1).also { localInstance = it }
                    }
                }
            }
            return localInstance!!
        }
    }

    private val observers = SparseArray<ArrayList<EventCenterDelegate>>()
    private val removeAfterBroadcast = SparseArray<ArrayList<EventCenterDelegate>>()
    private val addAfterBroadcast = SparseArray<ArrayList<EventCenterDelegate>>()
    private val delayedPosts = ArrayList<DelayedEventPost>(10)
    private val delayedPostsTmp = ArrayList<DelayedEventPost>(10)
    private val delayedRunnables = ArrayList<Runnable>(10)
    private val delayedRunnablesTmp = ArrayList<Runnable>(10)
    private val postponeCallbackList = ArrayList<PostponeEventCallback>(10)

    private var broadcasting = 0
    private var animationInProgressCount = 0
    private var animationInProgressPointer = 1
    private val heavyOperationsCounter = HashSet<Int>()
    private val allowedNotifications = HashMap<Int, IntArray>()
    private var currentHeavyOperationFlags = 0

    interface EventCenterDelegate {
        fun didReceivedNotification(id: Int, account: Int, vararg args: Any?)
    }

    interface PostponeEventCallback {
        fun needPostpone(id: Int, currentAccount: Int, args: Array<out Any?>): Boolean
    }

    private data class DelayedEventPost(val id: Int, val args: Array<out Any?>)

    /**
     * không cho phép các event được hoạt động trong lúc animation
     * */
    fun setAnimationInProgress(
        oldIndex: Int,
        allowedNotifications: IntArray? = null,
        stopHeavyOperations: Boolean = true
    ): Int {
        onAnimationFinish(oldIndex)

        if (heavyOperationsCounter.isEmpty() && stopHeavyOperations) {
            getGlobalInstance().postNotificationName(
                EventType.STOP_ALL_HEAVY_OPERATIONS.ordinal, 512
            )
        }

        animationInProgressCount++
        animationInProgressPointer++

        if (stopHeavyOperations) {
            heavyOperationsCounter.add(animationInProgressPointer)
        }

        this.allowedNotifications[animationInProgressPointer] = allowedNotifications ?: intArrayOf()

        return animationInProgressPointer
    }


    /**
     * cho phép các event được hoạt động trong lúc animation
     * */
    fun updateAllowedNotifications(transitionAnimationIndex: Int, allowedNotifications: IntArray?) {
        if (this.allowedNotifications.containsKey(transitionAnimationIndex)) {
            this.allowedNotifications[transitionAnimationIndex] =
                allowedNotifications ?: intArrayOf()
        }
    }

    /**
     * kết thúc việc animation
     * */
    fun onAnimationFinish(index: Int) {
        allowedNotifications.remove(index)?.let { notifications ->
            animationInProgressCount--
            if (heavyOperationsCounter.isNotEmpty()) {
                heavyOperationsCounter.remove(index)
                if (heavyOperationsCounter.isEmpty()) {
                    getGlobalInstance().postNotificationName(
                        EventType.START_ALL_HEAVY_OPERATIONS.ordinal, 512
                    )
                }
            }
            if (animationInProgressCount == 0) {
                runDelayedNotifications()
            }
        }
    }

    fun runDelayedNotifications() {
        if (delayedPosts.isNotEmpty()) {
            delayedPostsTmp.clear()
            delayedPostsTmp.addAll(delayedPosts)
            delayedPosts.clear()
            delayedPostsTmp.forEach { delayedPost ->
                postNotificationNameInternal(delayedPost.id, true, *delayedPost.args)
            }
            delayedPostsTmp.clear()
        }

        if (delayedRunnables.isNotEmpty()) {
            delayedRunnablesTmp.clear()
            delayedRunnablesTmp.addAll(delayedRunnables)
            delayedRunnables.clear()
            delayedRunnablesTmp.forEach { it.run() }
            delayedRunnablesTmp.clear()
        }
    }

    fun isAnimationInProgress(): Boolean = animationInProgressCount > 0

    fun getCurrentHeavyOperationFlags(): Int = currentHeavyOperationFlags

    fun postNotificationName(id: Int, vararg args: Any?) {
        var allowDuringAnimation =
            id == EventType.START_ALL_HEAVY_OPERATIONS.ordinal || id == EventType.STOP_ALL_HEAVY_OPERATIONS.ordinal
        if (!allowDuringAnimation && allowedNotifications.isNotEmpty()) {
            var allowedCount = 0
            for (key in allowedNotifications.keys) {
                allowedNotifications[key]?.let { allowed ->
                    if (allowed.contains(id)) {
                        allowedCount++
                    }
                } ?: break
            }
            allowDuringAnimation = allowedNotifications.size == allowedCount
        }
        if (id == EventType.START_ALL_HEAVY_OPERATIONS.ordinal) {
            val flags = args[0] as Int
            currentHeavyOperationFlags = currentHeavyOperationFlags and flags.inv()
        } else if (id == EventType.STOP_ALL_HEAVY_OPERATIONS.ordinal) {
            val flags = args[0] as Int
            currentHeavyOperationFlags = currentHeavyOperationFlags or flags
        }
        postNotificationNameInternal(id, allowDuringAnimation, *args)
    }

    @UiThread
    fun postNotificationNameInternal(id: Int, allowDuringAnimation: Boolean, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("postNotificationName allowed only from MAIN thread")
            }
        }
        if (!allowDuringAnimation && isAnimationInProgress()) {
            delayedPosts.add(DelayedEventPost(id, args))
            if (BuildConfig.DEBUG) {
                Timber.tag("EventCenter")
                    .e("delay post notification $id with args count = ${args.size}")
            }
            return
        }
        if (postponeCallbackList.isNotEmpty()) {
            postponeCallbackList.forEach { callback ->
                if (callback.needPostpone(id, currentAccount, args)) {
                    delayedPosts.add(DelayedEventPost(id, args))
                    return
                }
            }
        }
        broadcasting++
        observers[id]?.let { objects ->
            if (objects.isNotEmpty()) {
                objects.forEach { obj ->
                    obj.didReceivedNotification(id, currentAccount, *args)
                }
            }
        }
        broadcasting--
        if (broadcasting == 0) {
            if (removeAfterBroadcast.isNotEmpty()) {
                for (a in 0 until removeAfterBroadcast.size) {
                    val key = removeAfterBroadcast.keyAt(a)
                    removeAfterBroadcast[key]?.forEach { removeObserver(it, key) }
                }
                removeAfterBroadcast.clear()
            }
            if (addAfterBroadcast.isNotEmpty()) {
                for (a in 0 until addAfterBroadcast.size) {
                    val key = addAfterBroadcast.keyAt(a)
                    addAfterBroadcast[key]?.forEach { addObserver(it, key) }
                }
                addAfterBroadcast.clear()
            }
        }
    }

    fun addObserver(observer: EventCenterDelegate, id: Int) {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("addObserver allowed only from MAIN thread")
            }
        }
        if (broadcasting != 0) {
            val arrayList = addAfterBroadcast[id]
                ?: ArrayList<EventCenterDelegate>().also { addAfterBroadcast.put(id, it) }
            arrayList.add(observer)
            return
        }
        val objects =
            observers[id] ?: ArrayList<EventCenterDelegate>().also { observers.put(id, it) }
        if (!objects.contains(observer)) {
            objects.add(observer)
        }
    }

    fun removeObserver(observer: EventCenterDelegate, id: Int) {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("removeObserver allowed only from MAIN thread")
            }
        }
        if (broadcasting != 0) {
            val arrayList = removeAfterBroadcast[id]
                ?: ArrayList<EventCenterDelegate>().also { removeAfterBroadcast.put(id, it) }
            arrayList.add(observer)
            return
        }
        observers[id]?.remove(observer)
    }

    fun hasObservers(id: Int): Boolean = observers.indexOfKey(id) >= 0

    fun addPostponeNotificationsCallback(callback: PostponeEventCallback) {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("PostponeNotificationsCallback allowed only from MAIN thread")
            }
        }
        if (!postponeCallbackList.contains(callback)) {
            postponeCallbackList.add(callback)
        }
    }

    fun removePostponeNotificationsCallback(callback: PostponeEventCallback) {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("removePostponeNotificationsCallback allowed only from MAIN thread")
            }
        }
        if (postponeCallbackList.remove(callback)) {
            runDelayedNotifications()
        }
    }

    fun doOnIdle(runnable: Runnable) {
        if (isAnimationInProgress()) {
            delayedRunnables.add(runnable)
        } else {
            runnable.run()
        }
    }
}