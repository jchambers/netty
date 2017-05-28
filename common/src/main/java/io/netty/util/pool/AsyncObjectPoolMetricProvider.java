package io.netty.util.pool;

/**
 * @author <a href="https://github.com/jchambers">Jon Chambers</a>
 */
public interface AsyncObjectPoolMetricProvider {
    AsyncObjectPoolMetric metric();
}
