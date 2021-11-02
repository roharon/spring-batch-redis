package com.redis.spring.batch.support;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.PlatformTransactionManager;

import com.redis.spring.batch.RedisItemReader;
import com.redis.spring.batch.support.LiveKeyItemReader.KeyListener;

public class LiveRedisItemReader<K, T extends KeyValue<K, ?>> extends RedisItemReader<K, T>
		implements PollableItemReader<T> {

	private final KeyDeduplicator deduplicator = new KeyDeduplicator();
	private final LiveKeyItemReader<K> keyReader;
	private Duration flushingInterval = FlushingStepBuilder.DEFAULT_FLUSHING_INTERVAL;
	private Duration idleTimeout;
	private boolean open;

	public LiveRedisItemReader(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			LiveKeyItemReader<K> keyReader, ItemProcessor<List<? extends K>, List<T>> valueReader) {
		super(jobRepository, transactionManager, keyReader, valueReader);
		this.keyReader = keyReader;
	}

	public void setFlushingInterval(Duration flushingInterval) {
		this.flushingInterval = flushingInterval;
	}

	public void setIdleTimeout(Duration idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	@Override
	public synchronized void open(ExecutionContext executionContext) throws ItemStreamException {
		super.open(executionContext);
		keyReader.addListener(deduplicator);
		this.open = true;
	}

	@Override
	public synchronized void close() {
		super.close();
		this.open = false;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		return valueQueue.poll(timeout, unit);
	}

	@Override
	protected FaultTolerantStepBuilder<K, K> faultTolerantStepBuilder(SimpleStepBuilder<K, K> stepBuilder) {
		return new FlushingStepBuilder<>(stepBuilder).flushingInterval(flushingInterval).idleTimeout(idleTimeout);
	}

	private class KeyDeduplicator implements KeyListener<K> {

		@Override
		public void key(K key) {
			enqueuer.filter(key);
		}

	}

}
