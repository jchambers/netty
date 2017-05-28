package io.netty.util.pool;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

/**
 * A pooled object factory creates, destroys, and manages the lifecycle of objects in an object pool.
 */
public interface PooledObjectLifecycleManager<T> {
    Future<T> createObject(Promise<T> promise);
    Future<Void> activateObject(T object, Promise<Void> promise);
    Future<Void> validateObject(T object, Promise<Void> promise);
    Future<Void> deactivateObject(T object, Promise<Void> promise);
    Future<Void> destroyObject(T object, Promise<Void> promise);
}
