package io.netty.util.pool;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.OrderedEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class DefaultAsyncObjectPoolTest {

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

    private static DefaultEventExecutorGroup EXECUTOR_GROUP;

    @BeforeClass
    public static void setUpBeforeClass() {
        EXECUTOR_GROUP = new DefaultEventExecutorGroup(4);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        EXECUTOR_GROUP.shutdownGracefully().await();
    }

    @Test
    public void testAcquire() throws Exception {
        DefaultAsyncObjectPool<Integer> pool =
                new DefaultAsyncObjectPool<Integer>((OrderedEventExecutor) EXECUTOR_GROUP.next(),
                new PooledIntegerLifecycleManager(), 16);

        Integer first = pool.acquireObject().get();

        assertEquals(1, pool.metric().numTotalObjects());
        assertEquals(0, pool.metric().numIdleObjects());

        assertTrue(pool.releaseObject(first).await().isSuccess());
        assertEquals(1, pool.metric().numTotalObjects());
        assertEquals(1, pool.metric().numIdleObjects());

        first = pool.acquireObject().get();
        Integer second = pool.acquireObject().get();

        assertNotEquals(first, second);
        assertEquals(2, pool.metric().numTotalObjects());
        assertEquals(0, pool.metric().numIdleObjects());
    }

    @Test
    public void testAcquireWithLimitedPoolSize() throws Exception {
        DefaultAsyncObjectPool<Integer> pool = new DefaultAsyncObjectPool<Integer>(
                (OrderedEventExecutor) EXECUTOR_GROUP.next(),
                new PooledIntegerLifecycleManager(), 1);

        final Integer objectFromPool = pool.acquireObject().get();

        assertEquals(1, pool.metric().numTotalObjects());
        assertEquals(0, pool.metric().numIdleObjects());
        assertEquals(0, pool.metric().numWaitingToAcquire());

        final Future<Integer> blockedAcquireFuture = pool.acquireObject();

        assertEquals(1, pool.metric().numTotalObjects());
        assertEquals(0, pool.metric().numIdleObjects());
        assertEquals(1, pool.metric().numWaitingToAcquire());

        assertTrue(pool.releaseObject(objectFromPool).await().isSuccess());
        assertTrue(blockedAcquireFuture.await().isSuccess());

        assertEquals(1, pool.metric().numTotalObjects());
        assertEquals(0, pool.metric().numIdleObjects());
        assertEquals(0, pool.metric().numWaitingToAcquire());
    }

    @Test
    public void testReleaseObjectNotFromPool() throws Exception {
        final OrderedEventExecutor executor = (OrderedEventExecutor) EXECUTOR_GROUP.next();
        final DefaultAsyncObjectPool<Integer> pool =  new DefaultAsyncObjectPool<Integer>(
                executor, new PooledIntegerLifecycleManager(), 16);

        final Future<Void> releaseFuture = pool.releaseObject(12).await();

        assertFalse(releaseFuture.isSuccess());
        assertTrue(releaseFuture.cause() instanceof NoSuchElementException);
    }
}
