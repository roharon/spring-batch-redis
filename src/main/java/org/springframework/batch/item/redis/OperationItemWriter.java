package org.springframework.batch.item.redis;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import io.lettuce.core.api.async.RedisTransactionalAsyncCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.batch.item.redis.support.AbstractPipelineItemWriter;
import org.springframework.batch.item.redis.support.CommandBuilder;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class OperationItemWriter<T> extends AbstractPipelineItemWriter<T> {

    private final RedisOperation<T> operation;

    public OperationItemWriter(Supplier<StatefulConnection<String, String>> connectionSupplier, GenericObjectPoolConfig<StatefulConnection<String, String>> poolConfig, Function<StatefulConnection<String, String>, BaseRedisAsyncCommands<String, String>> async, RedisOperation<T> operation) {
        super(connectionSupplier, poolConfig, async);
        Assert.notNull(operation, "A Redis operation is required");
        this.operation = operation;
    }

    @Override
    protected void write(BaseRedisAsyncCommands<String, String> commands, long timeout, List<? extends T> items) {
        List<RedisFuture<?>> futures = write(commands, items);
        commands.flushCommands();
        LettuceFutures.awaitAll(timeout, TimeUnit.MILLISECONDS, futures.toArray(new RedisFuture[0]));
    }

    protected List<RedisFuture<?>> write(BaseRedisAsyncCommands<String, String> commands, List<? extends T> items) {
        List<RedisFuture<?>> futures = new ArrayList<>(items.size());
        for (T item : items) {
            futures.add(operation.execute(commands, item));
        }
        return futures;
    }

    public static class TransactionItemWriter<T> extends OperationItemWriter<T> {

        public TransactionItemWriter(Supplier<StatefulConnection<String, String>> connectionSupplier, GenericObjectPoolConfig<StatefulConnection<String, String>> poolConfig, Function<StatefulConnection<String, String>, BaseRedisAsyncCommands<String, String>> async, RedisOperation<T> operation) {
            super(connectionSupplier, poolConfig, async, operation);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<RedisFuture<?>> write(BaseRedisAsyncCommands<String, String> commands, List<? extends T> items) {
            RedisFuture<String> multiFuture = ((RedisTransactionalAsyncCommands<String, String>) commands).multi();
            List<RedisFuture<?>> futures = super.write(commands, items);
            RedisFuture<TransactionResult> execFuture = ((RedisTransactionalAsyncCommands<String, String>) commands).exec();
            List<RedisFuture<?>> allFutures = new ArrayList<>(futures.size() + 2);
            allFutures.add(multiFuture);
            allFutures.addAll(futures);
            allFutures.add(execFuture);
            return allFutures;
        }

    }

    public interface RedisOperation<T> {

        RedisFuture<?> execute(BaseRedisAsyncCommands<String, String> commands, T item);

    }

    public static <T> OperationItemWriterBuilder<T> operation(RedisOperation<T> operation) {
        return new OperationItemWriterBuilder<>(operation);
    }

    public static class OperationItemWriterBuilder<T> {

        private final RedisOperation<T> operation;

        public OperationItemWriterBuilder(RedisOperation<T> operation) {
            this.operation = operation;
        }

        public CommandOperationItemWriterBuilder<T> client(RedisClient client) {
            return new CommandOperationItemWriterBuilder<>(client, operation);
        }

        public CommandOperationItemWriterBuilder<T> client(RedisClusterClient client) {
            return new CommandOperationItemWriterBuilder<>(client, operation);
        }

    }


    @Setter
    @Accessors(fluent = true)
    public static class CommandOperationItemWriterBuilder<T> extends CommandBuilder<CommandOperationItemWriterBuilder<T>> {

        private final RedisOperation<T> operation;
        private boolean transactional;

        public CommandOperationItemWriterBuilder(RedisClient client, RedisOperation<T> operation) {
            super(client);
            this.operation = operation;
        }

        public CommandOperationItemWriterBuilder(RedisClusterClient client, RedisOperation<T> operation) {
            super(client);
            this.operation = operation;
        }

        public OperationItemWriter<T> build() {
            if (transactional) {
                return new TransactionItemWriter<>(connectionSupplier, poolConfig, async, operation);
            }
            return new OperationItemWriter<>(connectionSupplier, poolConfig, async, operation);
        }

    }


}