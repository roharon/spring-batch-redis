package org.springframework.batch.item.redis.support;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.function.Function;

public abstract class AbstractCollectionCommandItemWriter<K, V, T> extends AbstractKeyCommandItemWriter<K, V, T> {

    private final Converter<T, V> memberIdConverter;

    public AbstractCollectionCommandItemWriter(GenericObjectPool<? extends StatefulConnection<K, V>> pool, Function<StatefulConnection<K, V>, BaseRedisAsyncCommands<K, V>> commands, Duration commandTimeout, Converter<T, K> keyConverter, Converter<T, V> memberIdConverter) {
        super(pool, commands, commandTimeout, keyConverter);
        Assert.notNull(memberIdConverter, "A member id converter is required.");
        this.memberIdConverter = memberIdConverter;
    }

    @Override
    protected RedisFuture<?> write(BaseRedisAsyncCommands<K, V> commands, T item, K key) {
        return write(commands, item, key, memberIdConverter.convert(item));
    }

    protected abstract RedisFuture<?> write(BaseRedisAsyncCommands<K, V> commands, T item, K key, V memberId);
}