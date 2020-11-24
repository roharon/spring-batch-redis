package org.springframework.batch.item.redis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRedisItemWriter<T> extends AbstractItemStreamItemWriter<T> {

	private final GenericObjectPool<? extends StatefulConnection<String, String>> pool;
	private final Function<StatefulConnection<String, String>, BaseRedisAsyncCommands<String, String>> commands;
	private final long commandTimeout;

	protected AbstractRedisItemWriter(GenericObjectPool<? extends StatefulConnection<String, String>> pool,
			Function<StatefulConnection<String, String>, BaseRedisAsyncCommands<String, String>> commands,
			long commandTimeout) {
		setName(ClassUtils.getShortName(getClass()));
		Assert.notNull(pool, "A connection pool is required.");
		Assert.notNull(commands, "A commands supplier is required.");
		Assert.isTrue(commandTimeout > 0, "Command timeout must be greater than zero.");
		this.pool = pool;
		this.commands = commands;
		this.commandTimeout = commandTimeout;
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		try (StatefulConnection<String, String> connection = pool.borrowObject()) {
			BaseRedisAsyncCommands<String, String> commands = this.commands.apply(connection);
			commands.setAutoFlushCommands(false);
			List<RedisFuture<?>> futures = write(commands, items);
			commands.flushCommands();
			for (RedisFuture<?> future : futures) {
				try {
					future.get(commandTimeout, TimeUnit.SECONDS);
				} catch (ExecutionException e) {
					log.error("Could not write item", e.getCause());
				} catch (TimeoutException e) {
					log.error("Command timed out", e);
				}
			}
			commands.setAutoFlushCommands(true);
		}
	}

	protected List<RedisFuture<?>> write(BaseRedisAsyncCommands<String, String> commands, List<? extends T> items) {
		List<RedisFuture<?>> futures = new ArrayList<>();
		for (T item : items) {
			futures.add(write(commands, item));
		}
		return futures;
	}

	protected abstract RedisFuture<?> write(BaseRedisAsyncCommands<String, String> commands, T item);

}
