package org.springframework.batch.item.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.batch.item.redis.support.AbstractKeyValueItemReader;
import org.springframework.batch.item.redis.support.DumpReader;
import org.springframework.batch.item.redis.support.KeyValue;
import org.springframework.batch.item.redis.support.LiveKeyItemReader;
import org.springframework.batch.item.redis.support.LiveReaderOptions;
import org.springframework.batch.item.redis.support.RedisConnectionPoolBuilder;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.api.StatefulConnection;
import lombok.Setter;
import lombok.experimental.Accessors;

public class LiveKeyDumpItemReader extends AbstractKeyValueItemReader<KeyValue<byte[]>> {

	public LiveKeyDumpItemReader(AbstractRedisClient client,
			GenericObjectPoolConfig<StatefulConnection<String, String>> poolConfig, LiveReaderOptions options) {
		super(new LiveKeyItemReader(client, options.getLiveKeyReaderOptions()), new DumpReader(client, poolConfig),
				options.getTransferOptions(), options.getQueueOptions());
	}

	public static LiveKeyDumpItemReaderBuilder builder(AbstractRedisClient client) {
		return new LiveKeyDumpItemReaderBuilder(client);
	}

	@Setter
	@Accessors(fluent = true)
	public static class LiveKeyDumpItemReaderBuilder extends RedisConnectionPoolBuilder<LiveKeyDumpItemReaderBuilder> {

		public LiveKeyDumpItemReaderBuilder(AbstractRedisClient client) {
			super(client);
		}

		private LiveReaderOptions options = LiveReaderOptions.builder().build();

		public LiveKeyDumpItemReader build() {
			return new LiveKeyDumpItemReader(client, poolConfig, options);
		}

	}

}
