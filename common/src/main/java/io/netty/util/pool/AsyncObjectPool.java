package io.netty.util.pool;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

/**
 * @author <a href="https://github.com/jchambers">Jon Chambers</a>
 */
public interface AsyncObjectPool<T> extends AsyncObjectPoolMetricProvider {
    /**
     * @return a {@link Future} that will be notified when the acquisition operation is complete
     */
    Future<T> acquireObject();

    Future<T> acquireObject(Promise<T> promise);

    Future<Void> releaseObject(T object);

    Future<Void> releaseObject(T object, Promise<Void> promise);
}
