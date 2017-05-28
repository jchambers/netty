package io.netty.util.pool;

/**
 */
public interface AsyncObjectPoolMetric {
    int numIdleObjects();
    int numTotalObjects();
    int numWaitingToAcquire();
}
