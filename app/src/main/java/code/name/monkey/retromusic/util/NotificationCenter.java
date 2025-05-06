package code.name.monkey.retromusic.util;

import java.util.ArrayList;
import java.util.HashMap;

import code.name.monkey.retromusic.BuildConfig;
import timber.log.Timber;

public class NotificationCenter {

    private static int totalEvents = 1;
    public static final int dialogsNeedReload = totalEvents++;
    public static final int closeChats = totalEvents++;
    public static final int mediaDidLoaded = totalEvents++;
    public static final int messagesDidLoaded = totalEvents++;
    public static final int mediaCountDidLoaded = totalEvents++;

    private HashMap<Integer, ArrayList<Object>> observers = new HashMap<>();
    private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<>();
    private HashMap<Integer, Object> addAfterBroadcast = new HashMap<>();
    private ArrayList<DelayedPost> delayedPosts = new ArrayList<>(10);

    private int broadcasting = 0;

    private boolean animationInProgress;

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }

    private static class DelayedPost {

        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        private int id;
        private Object[] args;
    }

    private static volatile NotificationCenter Instance = null;

    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public void setAnimationInProgress(boolean value) {
        animationInProgress = value;
        if (!animationInProgress && !delayedPosts.isEmpty()) {
            for (DelayedPost delayedPost : delayedPosts) {
                postNotificationNameInternal(delayedPost.id, true, delayedPost.args);
            }
            delayedPosts.clear();
        }
    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        if (id == dialogsNeedReload || id == closeChats || id == messagesDidLoaded || id == mediaCountDidLoaded || id == mediaDidLoaded) {
            allowDuringAnimation = true;
        }
        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    public void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPost delayedPost = new DelayedPost(id, args);
            delayedPosts.add(delayedPost);
            if (BuildConfig.DEBUG) {
                Timber.tag("NotificationCenter").e("delay post notification " + id + " with args count = " + args.length);
            }
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            for (Object obj : objects) {
                ((NotificationCenterDelegate) obj).didReceivedNotification(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (!removeAfterBroadcast.isEmpty()) {
                for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
                    removeObserver(entry.getValue(), entry.getKey());
                }
                removeAfterBroadcast.clear();
            }
            if (!addAfterBroadcast.isEmpty()) {
                for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
                    addObserver(entry.getValue(), entry.getKey());
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public void addObserver(Object observer, int id) {

        if (broadcasting != 0) {
            addAfterBroadcast.put(id, observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    public void removeObserver(Object observer, int id) {
        if (broadcasting != 0) {
            removeAfterBroadcast.put(id, observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            objects.remove(observer);
            if (objects.isEmpty()) {
                observers.remove(id);
            }
        }
    }
}
