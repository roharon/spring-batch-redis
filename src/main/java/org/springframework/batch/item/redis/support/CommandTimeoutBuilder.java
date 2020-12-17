package org.springframework.batch.item.redis.support;

import io.lettuce.core.RedisURI;
import org.springframework.util.Assert;

import java.time.Duration;

public class CommandTimeoutBuilder<B extends CommandTimeoutBuilder> {

    protected Duration commandTimeout = RedisURI.DEFAULT_TIMEOUT_DURATION;

    public B commandTimeout(Duration commandTimeout) {
        this.commandTimeout = commandTimeout;
        return (B) this;
    }

}
