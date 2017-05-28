package io.netty.util.pool;

import io.netty.util.concurrent.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class DefaultAsyncObjectPoolTest {

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

    private static DefaultEventExecutorGroup EXECUTOR_GROUP;

    @BeforeClass
    public static void setUpBeforeClass() {
        EXECUTOR_GROUP = new DefaultEventExecutorGroup(4);
    }

    @Before
    public void setUp() {
        pool = new DefaultAsyncObjectPool<Integer>((OrderedEventExecutor) EXECUTOR_GROUP.next(),
                new PooledIntegerLifecycleManager());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        EXECUTOR_GROUP.shutdownGracefully().await();
    }

    @Test
    public void testAcquire() throws Exception {
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
    public void testReleaseObjectNotFromPool() throws Exception {
        final OrderedEventExecutor executor = (OrderedEventExecutor) EXECUTOR_GROUP.next();
        final DefaultAsyncObjectPool<Integer> pool = new DefaultAsyncObjectPool<Integer>(executor, new PooledIntegerLifecycleManager());

        final Future<Void> releaseFuture = pool.releaseObject(12).await();

        assertFalse(releaseFuture.isSuccess());
        assertTrue(releaseFuture.cause() instanceof NoSuchElementException);
    }
}
