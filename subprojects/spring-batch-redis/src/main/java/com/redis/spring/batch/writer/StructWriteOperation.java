package com.redis.spring.batch.writer;

import java.util.List;
import java.util.function.Function;

import com.redis.lettucemod.timeseries.AddOptions;
import com.redis.lettucemod.timeseries.AddOptions.Builder;
import com.redis.spring.batch.common.KeyValue;
import com.redis.spring.batch.writer.operation.Del;
import com.redis.spring.batch.writer.operation.ExpireAt;
import com.redis.spring.batch.writer.operation.Hset;
import com.redis.spring.batch.writer.operation.JsonSet;
import com.redis.spring.batch.writer.operation.Noop;
import com.redis.spring.batch.writer.operation.Restore;
import com.redis.spring.batch.writer.operation.RpushAll;
import com.redis.spring.batch.writer.operation.SaddAll;
import com.redis.spring.batch.writer.operation.Set;
import com.redis.spring.batch.writer.operation.TsAddAll;
import com.redis.spring.batch.writer.operation.XAddAll;
import com.redis.spring.batch.writer.operation.ZaddAll;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;

public class StructWriteOperation<K, V> implements WriteOperation<K, V, KeyValue<K>> {

    private static final XAddArgs EMPTY_XADD_ARGS = new XAddArgs();

    private final ExpireAt<K, V, KeyValue<K>> expire = new ExpireAt<>(key(), KeyValue::getTtl);

    private final Noop<K, V, KeyValue<K>> noop = new Noop<>();

    private final Del<K, V, KeyValue<K>> del = new Del<>(key());

    private final Hset<K, V, KeyValue<K>> hset = new Hset<>(key(), value());

    private final Set<K, V, KeyValue<K>> set = new Set<>(key(), value());

    private final JsonSet<K, V, KeyValue<K>> jsonSet = new JsonSet<>(key(), value());

    private final RpushAll<K, V, KeyValue<K>> rpush = new RpushAll<>(key(), value());

    private final SaddAll<K, V, KeyValue<K>> sadd = new SaddAll<>(key(), value());

    private final ZaddAll<K, V, KeyValue<K>> zadd = new ZaddAll<>(key(), value());

    private final TsAddAll<K, V, KeyValue<K>> tsAdd = new TsAddAll<>(key(), value(), tsAddOptions());

    private final XAddAll<K, V, KeyValue<K>> xadd = new XAddAll<>(value(), this::xaddArgs);

    private WriterOptions options = WriterOptions.builder().build();

    private static <K, V> AddOptions<K, V> tsAddOptions() {
        Builder<K, V> builder = AddOptions.builder();
        return builder.build();
    }

    private static <K> Function<KeyValue<K>, K> key() {
        return KeyValue::getKey;
    }

    @SuppressWarnings("unchecked")
    private static <K, T> Function<KeyValue<K>, T> value() {
        return kv -> (T) kv.getValue();
    }

    public WriterOptions getOptions() {
        return options;
    }

    public void setOptions(WriterOptions options) {
        this.options = options;
    }

    @Override
    public void execute(BaseRedisAsyncCommands<K, V> commands, KeyValue<K> item, List<RedisFuture<Object>> futures) {
        if (shouldSkip(item)) {
            return;
        }
        if (shouldDelete(item)) {
            del.execute(commands, item, futures);
            return;
        }
        if (isOverwrite() && !KeyValue.isString(item)) {
            del.execute(commands, item, futures);
        }
        operation(item).execute(commands, item, futures);
        if (options.getTtlPolicy() == TtlPolicy.PROPAGATE && item.getTtl() > 0) {
            expire.execute(commands, item, futures);
        }
    }

    private boolean isOverwrite() {
        return options.getMergePolicy() == MergePolicy.OVERWRITE;
    }

    public static boolean shouldSkip(KeyValue<?> item) {
        return item == null || item.getKey() == null || (item.getValue() == null && item.getMemoryUsage() > 0);
    }

    private boolean shouldDelete(KeyValue<K> item) {
        return item.getValue() == null || Restore.TTL_KEY_DOES_NOT_EXIST == item.getTtl() || KeyValue.isNone(item);
    }

    private WriteOperation<K, V, KeyValue<K>> operation(KeyValue<K> item) {
        switch (item.getType()) {
            case KeyValue.HASH:
                return hset;
            case KeyValue.JSON:
                return jsonSet;
            case KeyValue.LIST:
                return rpush;
            case KeyValue.SET:
                return sadd;
            case KeyValue.STREAM:
                return xadd;
            case KeyValue.STRING:
                return set;
            case KeyValue.TIMESERIES:
                return tsAdd;
            case KeyValue.ZSET:
                return zadd;
            default:
                return noop;
        }
    }

    private XAddArgs xaddArgs(StreamMessage<K, V> message) {
        if (options.getStreamIdPolicy() == StreamIdPolicy.PROPAGATE) {
            XAddArgs args = new XAddArgs();
            String id = message.getId();
            if (id != null) {
                args.id(id);
            }
            return args;
        }
        return EMPTY_XADD_ARGS;
    }

}
