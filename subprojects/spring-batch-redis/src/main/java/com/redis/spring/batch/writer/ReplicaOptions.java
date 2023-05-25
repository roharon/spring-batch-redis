package com.redis.spring.batch.writer;

import java.time.Duration;

public class ReplicaOptions {

	public static final int DEFAULT_REPLICAS = 1;
	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);

	private int replicas = DEFAULT_REPLICAS;
	private Duration timeout = DEFAULT_TIMEOUT;

	public ReplicaOptions() {

	}

	private ReplicaOptions(Builder builder) {
		this.replicas = builder.replicas;
		this.timeout = builder.timeout;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private int replicas = DEFAULT_REPLICAS;
		private Duration timeout = DEFAULT_TIMEOUT;

		private Builder() {
		}

		public Builder replicas(int replicas) {
			this.replicas = replicas;
			return this;
		}

		public Builder timeout(Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		public ReplicaOptions build() {
			return new ReplicaOptions(this);
		}
	}

}