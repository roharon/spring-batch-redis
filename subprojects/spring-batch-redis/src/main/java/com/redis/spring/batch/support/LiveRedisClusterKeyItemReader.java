package com.redis.spring.batch.support;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubAdapter;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveRedisClusterKeyItemReader<K, V> extends LiveKeyItemReader<K> {

	private final Listener listener = new Listener();
	private final Supplier<StatefulRedisClusterPubSubConnection<K, V>> connectionSupplier;
	private StatefulRedisClusterPubSubConnection<K, V> connection;

	public LiveRedisClusterKeyItemReader(Supplier<StatefulRedisClusterPubSubConnection<K, V>> connectionSupplier,
			Converter<K, K> keyExtractor, List<K> patterns) {
		super(keyExtractor, patterns);
		Assert.notNull(connectionSupplier, "A pub/sub connection supplier is required");
		this.connectionSupplier = connectionSupplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected synchronized void doOpen() {
		connection = connectionSupplier.get();
		log.debug("Adding pub/sub listener");
		connection.addListener(listener);
		connection.setNodeMessagePropagation(true);
		log.debug("Subscribing to channel patterns {}", patterns);
		connection.sync().upstream().commands().psubscribe((K[]) patterns.toArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected synchronized void doClose() {
		if (connection == null) {
			return;
		}
		log.debug("Unsubscribing from channel patterns {}", patterns);
		connection.sync().upstream().commands().punsubscribe((K[]) patterns.toArray());
		log.debug("Removing pub/sub listener");
		connection.removeListener(listener);
		connection.close();
		connection = null;
	}

	private class Listener extends RedisClusterPubSubAdapter<K, V> {

		@Override
		public void message(RedisClusterNode node, K channel, V message) {
			LiveRedisClusterKeyItemReader.this.message(channel);
		}

		@Override
		public void message(RedisClusterNode node, K pattern, K channel, V message) {
			LiveRedisClusterKeyItemReader.this.message(channel);
		}
	}

}