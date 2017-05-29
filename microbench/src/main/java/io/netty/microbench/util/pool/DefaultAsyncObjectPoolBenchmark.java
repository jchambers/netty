package io.netty.microbench.util.pool;

import io.netty.microbench.util.AbstractMicrobenchmark;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.OrderedEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.pool.DefaultAsyncObjectPool;
import io.netty.util.pool.PooledObjectLifecycleManager;
import org.openjdk.jmh.annotations.*;

/**
 */
@State(Scope.Benchmark)
@Threads(8)
public class DefaultAsyncObjectPoolBenchmark extends AbstractMicrobenchmark {

    private OrderedEventExecutor eventExecutor;
    private DefaultAsyncObjectPool<Integer> pool;

    private static class PooledIntegerLifecycleManager implements PooledObjectLifecycleManager<Integer> {

        private int i = 0;

        @Override
        public Future<Integer> createObject(final Promise<Integer> promise) {
            promise.trySuccess(i++);
            return promise;
        }

        @Override
        public Future<Void> activateObject(final Integer object, final Promise<Void> promise) {
            promise.trySuccess(null);
            return promise;
        }

        @Override
        public Future<Void> validateObject(final Integer object, final Promise<Void> promise) {
            promise.trySuccess(null);
            return promise;
        }

        @Override
        public Future<Void> deactivateObject(final Integer object, final Promise<Void> promise) {
            promise.trySuccess(null);
            return promise;
        }

        @Override
        public Future<Void> destroyObject(final Integer object, final Promise<Void> promise) {
            promise.trySuccess(null);
            return promise;
        }
    }

    @Param({ "1", "8", "16", "32" })
    public int capacity;

    @Setup
    public void setup() {
        eventExecutor = new DefaultEventExecutor();
        pool = new DefaultAsyncObjectPool<Integer>(eventExecutor, new PooledIntegerLifecycleManager(), capacity);
    }

    @TearDown
    public void tearDown() throws Exception {
        eventExecutor.shutdownGracefully().await();
    }

    @Benchmark
    public void acquireAndRelease() throws Exception {
        pool.releaseObject(pool.acquireObject().get()).await();
    }
}
