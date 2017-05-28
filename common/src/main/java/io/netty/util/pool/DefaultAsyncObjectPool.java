package io.netty.util.pool;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.OrderedEventExecutor;
import io.netty.util.concurrent.Promise;

import java.util.*;

/**
 */
public class DefaultAsyncObjectPool<T> implements AsyncObjectPool<T> {
    private final OrderedEventExecutor eventExecutor;
    private final PooledObjectLifecycleManager<T> lifecycleManager;

    private final Set<T> allObjects = new HashSet<T>();
    private final Deque<T> idlePooledObjects = new ArrayDeque<T>();

    // We don't just rely on allObjects.size() for this because we may have objects that are still in the process of
    // being created.
    private int pooledObjectCount = 0;

    private static final NoSuchElementException NOT_IN_POOL_EXCEPTION =
            new NoSuchElementException("Released object not in object pool");

    public DefaultAsyncObjectPool(final OrderedEventExecutor eventExecutor, final PooledObjectLifecycleManager<T> lifecycleManager) {
        this.eventExecutor = eventExecutor;
        this.lifecycleManager = lifecycleManager;
    }

    public Future<T> acquireObject() {
        return acquireObject(eventExecutor.<T>newPromise());
    }

    public Future<T> acquireObject(final Promise<T> promise) {
        if (eventExecutor.inEventLoop()) {
            getIdleOrCreateObjectForAcquisition(promise);
        } else {
            eventExecutor.submit(new Runnable() {

                @Override
                public void run() {
                    acquireObject(promise);
                }
            });
        }

        return promise;
    }

    private void getIdleOrCreateObjectForAcquisition(final Promise<T> promise) {
        assert eventExecutor.inEventLoop();

        if (!promise.isCancelled()) {
            final T objectFromIdlePool = idlePooledObjects.poll();

            if (objectFromIdlePool != null) {
                activateObjectForAcquisition(objectFromIdlePool, false, promise);
            } else {
                pooledObjectCount++;

                lifecycleManager.createObject(eventExecutor.<T>newPromise()).addListener(new GenericFutureListener<Future<T>>() {

                    @Override
                    public void operationComplete(final Future<T> createFuture) throws Exception {
                        if (createFuture.isSuccess()) {
                            final T createdObject = createFuture.getNow();

                            allObjects.add(createdObject);
                            activateObjectForAcquisition(createdObject, true, promise);
                        } else {
                            // We failed to create a new object and should fail the whole acquisition attempt
                            promise.tryFailure(createFuture.cause());
                        }
                    }
                });
            }
        }
    }

    private void activateObjectForAcquisition(final T object, final boolean objectIsNewlyCreated, final Promise<T> promise) {
        assert eventExecutor.inEventLoop();

        if (!promise.isCancelled()) {
            lifecycleManager.activateObject(object, eventExecutor.<Void>newPromise()).addListener(new GenericFutureListener<Future<Void>>() {

                @Override
                public void operationComplete(final Future<Void> activateFuture) throws Exception {
                    if (activateFuture.isSuccess()) {
                        validateObjectForAcquisition(object, objectIsNewlyCreated, promise);
                    } else {
                        // We failed to activate the object; it's not active, so all we need to do is remove it from
                        // the pool.
                        destroyObject(object);

                        if (objectIsNewlyCreated) {
                            // We're happy to retry if we failed to activate a stale object, but should bail out if we
                            // failed to activate a newly-created object to avoid spinning forever.
                            promise.tryFailure(activateFuture.cause());
                        } else {
                            acquireObject(promise);
                        }
                    }
                }
            });
        } else {
            // The caller cancelled their request for an object. At this point, we have the object (either new or from
            // the idle pool), but it's not active yet and just need to put it back.

            // TODO Decide whether we should consider newly-created objects to be "active" or "passive"
            returnDeactivatedObjectToPool(object);
        }
    }

    private void validateObjectForAcquisition(final T object, final boolean objectIsNewlyCreated, final Promise<T> promise) {
        assert eventExecutor.inEventLoop();

        if (!promise.isCancelled()) {
            lifecycleManager.validateObject(object, eventExecutor.<Void>newPromise()).addListener(new GenericFutureListener<Future<Void>>() {

                @Override
                public void operationComplete(final Future<Void> validateFuture) throws Exception {
                    if (validateFuture.isSuccess()) {
                        if (!promise.isCancelled()) {
                            promise.trySuccess(object);
                        } else {
                            deactivateObjectForCancellation(object);
                        }
                    } else {
                        destroyObject(object);

                        if (objectIsNewlyCreated) {
                            promise.tryFailure(validateFuture.cause());
                        } else {
                            acquireObject(promise);
                        }
                    }
                }
            });
        } else {
            // The caller cancelled their request for an object, but we've already activated an object from the pool.
            // We need to deactivate it before we can put it in the idle pool.
            deactivateObjectForCancellation(object);
        }
    }

    private void deactivateObjectForCancellation(final T object) {
        assert eventExecutor.inEventLoop();

        lifecycleManager.deactivateObject(object, eventExecutor.<Void>newPromise()).addListener(new GenericFutureListener<Future<Void>>() {

            @Override
            public void operationComplete(final Future<Void> future) throws Exception {
                if (future.isSuccess()) {
                    returnDeactivatedObjectToPool(object);
                } else {
                    // TODO Log that something bad happened? Report this in metrics?
                    destroyObject(object);
                }
            }
        });
    }

    @Override
    public Future<Void> releaseObject(final T object) {
        return releaseObject(object, eventExecutor.<Void>newPromise());
    }

    @Override
    public Future<Void> releaseObject(final T object, final Promise<Void> promise) {
        if (eventExecutor.inEventLoop()) {
            deactivateObjectForRelease(object, promise);
        } else {
            eventExecutor.submit(new Runnable() {

                @Override
                public void run() {
                    deactivateObjectForRelease(object, promise);
                }
            });
        }

        return promise;
    }

    private void deactivateObjectForRelease(final T object, final Promise<Void> promise) {
        assert eventExecutor.inEventLoop();

        if (!allObjects.contains(object)) {
            promise.tryFailure(NOT_IN_POOL_EXCEPTION);
        } else if (promise.setUncancellable()) {
            // The promise hasn't been cancelled already, and it doesn't make sense to cancel the release
            // process once it's started.
            lifecycleManager.deactivateObject(object, eventExecutor.<Void>newPromise()).addListener(new GenericFutureListener<Future<Void>>() {

                @Override
                public void operationComplete(final Future<Void> deactivateFuture) throws Exception {
                    if (deactivateFuture.isSuccess()) {
                        returnDeactivatedObjectToPool(object);
                        promise.trySuccess(null);
                    } else {
                        destroyObject(object);
                        promise.tryFailure(deactivateFuture.cause());
                    }
                }
            });
        }
    }

    private void returnDeactivatedObjectToPool(T object) {
        idlePooledObjects.addLast(object);
    }

    private void destroyObject(final T object) {
        assert eventExecutor.inEventLoop();
        assert pooledObjectCount > 0;

        allObjects.remove(object);
        pooledObjectCount--;

        lifecycleManager.destroyObject(object, eventExecutor.<Void>newPromise());
    }

    @Override
    public AsyncObjectPoolMetric metric() {
        // TODO Don't create a new object on every call to this method
        return new AsyncObjectPoolMetric() {

            @Override
            public int numIdleObjects() {
                return idlePooledObjects.size();
            }

            @Override
            public int numTotalObjects() {
                return pooledObjectCount;
            }

            @Override
            public int numWaitingToAcquire() {
                return 0;
            }
        };
    }
}
