package com.redis.spring.batch.operation;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;

public interface InitializingOperation<K, V, I, O> extends Operation<K, V, I, O> {

	void afterPropertiesSet(StatefulRedisModulesConnection<K, V> connection) throws Exception;

}