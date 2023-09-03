package com.redis.spring.batch.writer.operation;

import java.util.List;
import java.util.function.Function;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;

public class Zadd<K, V, T> extends AbstractOperation<K, V, T, Zadd<K, V, T>> {

    private Function<T, ScoredValue<V>> value;

    private Function<T, ZAddArgs> args = t -> null;

    public Zadd<K, V, T> args(Function<T, ZAddArgs> args) {
        this.args = args;
        return this;
    }

    public Zadd<K, V, T> value(Function<T, ScoredValue<V>> value) {
        this.value = value;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(BaseRedisAsyncCommands<K, V> commands, T item, List<RedisFuture<?>> futures) {
        futures.add(((RedisSortedSetAsyncCommands<K, V>) commands).zadd(key(item), args(item), value(item)));
    }

    private ScoredValue<V> value(T item) {
        return value.apply(item);
    }

    private ZAddArgs args(T item) {
        return args.apply(item);
    }

}