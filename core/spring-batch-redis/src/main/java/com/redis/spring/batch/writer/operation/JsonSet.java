package com.redis.spring.batch.writer.operation;

import java.util.List;
import java.util.function.Function;

import com.redis.lettucemod.api.async.RedisJSONAsyncCommands;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;

public class JsonSet<K, V, T> extends AbstractOperation<K, V, T, JsonSet<K, V, T>> {

    private Function<T, String> path = rootPath();

    private Function<T, V> value;

    public JsonSet<K, V, T> path(String path) {
        return path(t -> path);
    }

    public JsonSet<K, V, T> path(Function<T, String> path) {
        this.path = path;
        return this;
    }

    public JsonSet<K, V, T> value(Function<T, V> value) {
        this.value = value;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(BaseRedisAsyncCommands<K, V> commands, T item, List<RedisFuture<?>> futures) {
        futures.add(((RedisJSONAsyncCommands<K, V>) commands).jsonSet(key(item), path(item), value(item)));
    }

    private V value(T item) {
        return value.apply(item);
    }

    private String path(T item) {
        return path.apply(item);
    }

    public static <T> Function<T, String> rootPath() {
        return t -> "$";
    }

}