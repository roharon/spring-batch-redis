package org.springframework.batch.item.redis.support;

import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;
import java.util.function.Function;

@Setter
@Accessors(fluent = true)
public abstract class LiveKeyValueItemReaderBuilder<T extends AbstractKeyValueItemReader<String, String, ?>> extends AbstractKeyValueItemReader.AbstractKeyValueItemReaderBuilder<T, LiveKeyValueItemReaderBuilder<T>> {

    public static final int DEFAULT_DATABASE = 0;
    public static final int DEFAULT_NOTIFICATION_QUEUE_CAPACITY = 1000;
    public static final String DEFAULT_KEY_PATTERN = "*";
    protected static final Converter<String, String> DEFAULT_KEY_EXTRACTOR = m -> m.substring(m.indexOf(":") + 1);
    private static final String PUBSUB_PATTERN_FORMAT = "__keyspace@%s__:%s";

    protected Duration flushingInterval = FlushingStepBuilder.DEFAULT_FLUSHING_INTERVAL;
    protected Duration idleTimeout;
    protected int notificationQueueCapacity = DEFAULT_NOTIFICATION_QUEUE_CAPACITY;
    private String keyPattern = DEFAULT_KEY_PATTERN;
    private int database = DEFAULT_DATABASE;

    protected String pubSubPattern() {
        return String.format(PUBSUB_PATTERN_FORMAT, database, keyPattern);
    }

    public RedisKeyspaceNotificationItemReader<String, String> keyspaceNotificationReader(StatefulRedisPubSubConnection<String, String> connection) {
        return new RedisKeyspaceNotificationItemReader<>(connection, pubSubPattern(), DEFAULT_KEY_EXTRACTOR, notificationQueueCapacity);
    }

    public RedisClusterKeyspaceNotificationItemReader<String, String> keyspaceNotificationReader(StatefulRedisClusterPubSubConnection<String, String> connection) {
        return new RedisClusterKeyspaceNotificationItemReader<>(connection, pubSubPattern(), DEFAULT_KEY_EXTRACTOR, notificationQueueCapacity);
    }

    public Function<SimpleStepBuilder<String, String>, SimpleStepBuilder<String, String>> stepBuilderProvider() {
        return b -> new FlushingStepBuilder<>(b).flushingInterval(flushingInterval).idleTimeout(idleTimeout);
    }

}