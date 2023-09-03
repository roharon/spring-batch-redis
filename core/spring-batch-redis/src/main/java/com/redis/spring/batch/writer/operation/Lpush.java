package com.redis.spring.batch.writer.operation;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisListAsyncCommands;

public class Lpush<K, V, T> extends AbstractPushOperation<K, V, T, Lpush<K, V, T>> {

    @SuppressWarnings("unchecked")
    @Override
    protected RedisFuture<Long> doPush(RedisListAsyncCommands<K, V> commands, K key, V value) {
        return commands.lpush(key, value);
    }

}