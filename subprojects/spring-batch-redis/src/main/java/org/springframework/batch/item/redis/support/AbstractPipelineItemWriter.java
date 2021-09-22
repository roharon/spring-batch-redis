package org.springframework.batch.item.redis.support;

import com.redis.lettucemod.api.async.RedisModulesAsyncCommands;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.batch.item.ItemWriter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPipelineItemWriter<K, V, T> extends ConnectionPoolItemStream<K, V> implements ItemWriter<T> {

    private final Function<StatefulConnection<K, V>, RedisModulesAsyncCommands<K, V>> async;

    protected AbstractPipelineItemWriter(Supplier<StatefulConnection<K, V>> connectionSupplier, GenericObjectPoolConfig<StatefulConnection<K, V>> poolConfig, Function<StatefulConnection<K, V>, RedisModulesAsyncCommands<K, V>> async) {
        super(connectionSupplier, poolConfig);
        this.async = async;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        try (StatefulConnection<K, V> connection = pool.borrowObject()) {
            RedisModulesAsyncCommands<K, V> commands = async.apply(connection);
            commands.setAutoFlushCommands(false);
            try {
                write(commands, connection.getTimeout(), items);
            } finally {
                commands.setAutoFlushCommands(true);
            }
        }
    }

    protected abstract void write(RedisModulesAsyncCommands<K, V> commands, Duration timeout, List<? extends T> items);

    protected void flush(RedisModulesAsyncCommands<K, V> commands, Duration timeout, List<RedisFuture<?>> futures) {
        flush(commands, timeout, futures.toArray(new RedisFuture[0]));
    }

    protected void flush(RedisModulesAsyncCommands<K, V> commands, Duration timeout, RedisFuture<?>... futures) {
        commands.flushCommands();
        LettuceFutures.awaitAll(timeout.toMillis(), TimeUnit.MILLISECONDS, futures);
    }

}
