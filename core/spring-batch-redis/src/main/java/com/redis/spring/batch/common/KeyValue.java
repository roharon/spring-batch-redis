package com.redis.spring.batch.common;

import java.util.Objects;

public class KeyValue<K> {

    public static final long TTL_KEY_DOES_NOT_EXIST = -2;

    private K key;

    private DataType type = DataType.NONE;

    private Object value;

    /**
     * Expiration POSIX time in milliseconds for this key.
     *
     */
    private long ttl;

    /**
     * Number of bytes that this key and its value require to be stored in Redis RAM. 0 means no memory usage information is
     * available.
     */
    private long memoryUsage;

    public KeyValue() {

    }

    private KeyValue(Builder<K> builder) {
        this.key = builder.key;
        this.type = builder.type;
        this.value = builder.value;
        this.ttl = builder.ttl;
        this.memoryUsage = builder.memoryUsage;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public boolean exists() {
        return type != DataType.NONE && ttl != KeyValue.TTL_KEY_DOES_NOT_EXIST;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, memoryUsage, ttl, type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyValue<?> other = (KeyValue<?>) obj;
        return Objects.equals(key, other.key) && memoryUsage == other.memoryUsage && ttl == other.ttl && type == other.type
                && Objects.equals(value, other.value);
    }

    public static Builder<String> key(String key) {
        return new Builder<>(key);
    }

    public static <K> Builder<K> key(K key) {
        return new Builder<>(key);
    }

    public static final class Builder<K> {

        private final K key;

        private DataType type = DataType.NONE;

        private Object value;

        private long ttl;

        private long memoryUsage;

        private Builder(K key) {
            this.key = key;
        }

        public Builder<K> type(DataType type) {
            this.type = type;
            return this;
        }

        public Builder<K> type(String type) {
            return type(DataType.of(type));
        }

        public Builder<K> value(Object value) {
            this.value = value;
            return this;
        }

        public Builder<K> ttl(long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder<K> memoryUsage(long memoryUsage) {
            this.memoryUsage = memoryUsage;
            return this;
        }

        public KeyValue<K> build() {
            return new KeyValue<>(this);
        }

    }

}