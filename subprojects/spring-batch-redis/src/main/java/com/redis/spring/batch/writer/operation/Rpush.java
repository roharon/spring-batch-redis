package com.redis.spring.batch.writer.operation;

import java.util.function.Predicate;

import org.springframework.core.convert.converter.Converter;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.BaseRedisAsyncCommands;
import io.lettuce.core.api.async.RedisListAsyncCommands;

public class Rpush<K, V, T> extends AbstractCollectionAdd<K, V, T> {

	private final Converter<T, V> memberConverter;

	public Rpush(Converter<T, K> key, Predicate<T> delete, Predicate<T> remove, Converter<T, V> member) {
		super(key, delete, remove);
		this.memberConverter = member;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected RedisFuture<Long> add(BaseRedisAsyncCommands<K, V> commands, T item, K key) {
		V member = memberConverter.convert(item);
		if (member == null) {
			return null;
		}
		return ((RedisListAsyncCommands<K, V>) commands).rpush(key, member);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected RedisFuture<Long> remove(BaseRedisAsyncCommands<K, V> commands, T item, K key) {
		V member = this.memberConverter.convert(item);
		if (member == null) {
			return null;
		}
		return ((RedisListAsyncCommands<K, V>) commands).lrem(key, -1, member);
	}

	public static <K, T> MemberBuilder<K, T> key(K key) {
		return key(t -> key);
	}

	public static <K, T> MemberBuilder<K, T> key(Converter<T, K> key) {
		return new MemberBuilder<>(key);
	}

	public static class MemberBuilder<K, T> {

		private final Converter<T, K> key;

		public MemberBuilder(Converter<T, K> key) {
			this.key = key;
		}

		public <V> Builder<K, V, T> member(Converter<T, V> member) {
			return new Builder<>(key, member);
		}
	}

	public static class Builder<K, V, T> extends RemoveBuilder<K, V, T, Builder<K, V, T>> {

		private final Converter<T, K> key;
		private final Converter<T, V> member;

		public Builder(Converter<T, K> key, Converter<T, V> member) {
			this.key = key;
			this.member = member;
			onNull(member);
		}

		public Rpush<K, V, T> build() {
			return new Rpush<>(key, del, remove, member);
		}

	}
}
