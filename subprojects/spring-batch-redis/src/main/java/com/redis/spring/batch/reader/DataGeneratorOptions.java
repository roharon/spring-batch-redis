package com.redis.spring.batch.reader;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redis.spring.batch.common.DataStructure.Type;
import com.redis.spring.batch.common.DoubleRange;
import com.redis.spring.batch.common.IntRange;

public class DataGeneratorOptions {

	public static final String DEFAULT_KEYSPACE = "gen";
	public static final MapOptions DEFAULT_HASH_OPTIONS = MapOptions.builder().build();
	public static final StreamOptions DEFAULT_STREAM_OPTIONS = StreamOptions.builder().build();
	public static final TimeSeriesOptions DEFAULT_TIMESERIES_OPTIONS = TimeSeriesOptions.builder().build();
	public static final MapOptions DEFAULT_JSON_OPTIONS = MapOptions.builder().build();
	public static final SetOptions DEFAULT_SET_OPTIONS = SetOptions.builder().build();
	public static final ListOptions DEFAULT_LIST_OPTIONS = ListOptions.builder().build();
	public static final StringOptions DEFAULT_STRING_OPTIONS = StringOptions.builder().build();
	public static final ZsetOptions DEFAULT_ZSET_OPTIONS = ZsetOptions.builder().build();
	private static final Type[] DEFAULT_TYPES = { Type.HASH, Type.LIST, Type.SET, Type.STREAM, Type.STRING, Type.ZSET };
	public static final IntRange DEFAULT_RANGE = IntRange.between(0, 100);

	private Optional<IntRange> expiration = Optional.empty();
	private MapOptions hashOptions = DEFAULT_HASH_OPTIONS;
	private StreamOptions streamOptions = DEFAULT_STREAM_OPTIONS;
	private TimeSeriesOptions timeSeriesOptions = DEFAULT_TIMESERIES_OPTIONS;
	private MapOptions jsonOptions = DEFAULT_JSON_OPTIONS;
	private ListOptions listOptions = DEFAULT_LIST_OPTIONS;
	private SetOptions setOptions = DEFAULT_SET_OPTIONS;
	private StringOptions stringOptions = DEFAULT_STRING_OPTIONS;
	private ZsetOptions zsetOptions = DEFAULT_ZSET_OPTIONS;
	private String keyspace = DEFAULT_KEYSPACE;
	private Set<Type> types = defaultTypes();
	private IntRange keyRange = DEFAULT_RANGE;

	public static Set<Type> defaultTypes() {
		return Stream.of(DEFAULT_TYPES).collect(Collectors.toSet());
	}

	public static Builder builder() {
		return new Builder();
	}

	public DataGeneratorOptions(Builder builder) {
		this.keyRange = builder.keyRange;
		this.keyspace = builder.keyspace;
		this.types = builder.types;
		this.expiration = builder.expiration;
		this.hashOptions = builder.hashOptions;
		this.streamOptions = builder.streamOptions;
		this.jsonOptions = builder.jsonOptions;
		this.setOptions = builder.setOptions;
		this.zsetOptions = builder.zsetOptions;
		this.listOptions = builder.listOptions;
		this.timeSeriesOptions = builder.timeSeriesOptions;
		this.stringOptions = builder.stringOptions;
	}

	public IntRange getKeyRange() {
		return keyRange;
	}

	public void setKeyRange(IntRange keyRange) {
		this.keyRange = keyRange;
	}

	public Optional<IntRange> getExpiration() {
		return expiration;
	}

	public void setExpiration(Optional<IntRange> expiration) {
		this.expiration = expiration;
	}

	public MapOptions getHashOptions() {
		return hashOptions;
	}

	public void setHashOptions(MapOptions hashOptions) {
		this.hashOptions = hashOptions;
	}

	public StreamOptions getStreamOptions() {
		return streamOptions;
	}

	public void setStreamOptions(StreamOptions streamOptions) {
		this.streamOptions = streamOptions;
	}

	public MapOptions getJsonOptions() {
		return jsonOptions;
	}

	public void setJsonOptions(MapOptions jsonOptions) {
		this.jsonOptions = jsonOptions;
	}

	public TimeSeriesOptions getTimeSeriesOptions() {
		return timeSeriesOptions;
	}

	public void setTimeSeriesOptions(TimeSeriesOptions options) {
		this.timeSeriesOptions = options;
	}

	public ListOptions getListOptions() {
		return listOptions;
	}

	public void setListOptions(ListOptions options) {
		this.listOptions = options;
	}

	public SetOptions getSetOptions() {
		return setOptions;
	}

	public void setSetOptions(SetOptions options) {
		this.setOptions = options;
	}

	public ZsetOptions getZsetOptions() {
		return zsetOptions;
	}

	public void setZsetOptions(ZsetOptions options) {
		this.zsetOptions = options;
	}

	public StringOptions getStringOptions() {
		return stringOptions;
	}

	public void setStringOptions(StringOptions options) {
		this.stringOptions = options;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public Set<Type> getTypes() {
		return types;
	}

	public void setTypes(Set<Type> types) {
		this.types = types;
	}

	public static class Builder {
		private IntRange keyRange = DEFAULT_RANGE;
		private String keyspace = DEFAULT_KEYSPACE;
		private Set<Type> types = defaultTypes();
		private Optional<IntRange> expiration = Optional.empty();
		private MapOptions hashOptions = DEFAULT_HASH_OPTIONS;
		private StreamOptions streamOptions = DEFAULT_STREAM_OPTIONS;
		private MapOptions jsonOptions = DEFAULT_JSON_OPTIONS;
		private TimeSeriesOptions timeSeriesOptions = DEFAULT_TIMESERIES_OPTIONS;
		private SetOptions setOptions = DEFAULT_SET_OPTIONS;
		private ZsetOptions zsetOptions = DEFAULT_ZSET_OPTIONS;
		private ListOptions listOptions = DEFAULT_LIST_OPTIONS;
		private StringOptions stringOptions = DEFAULT_STRING_OPTIONS;

		public Builder keyspace(String keyspace) {
			this.keyspace = keyspace;
			return this;
		}

		public Builder range(IntRange range) {
			this.keyRange = range;
			return this;
		}

		public Builder expiration(IntRange expiration) {
			this.expiration = Optional.of(expiration);
			return this;
		}

		public Builder hashOptions(MapOptions options) {
			this.hashOptions = options;
			return this;
		}

		public Builder streamOptions(StreamOptions options) {
			this.streamOptions = options;
			return this;
		}

		public Builder jsonOptions(MapOptions options) {
			this.jsonOptions = options;
			return this;
		}

		public Builder setOptions(SetOptions options) {
			this.setOptions = options;
			return this;
		}

		public Builder zsetOptions(ZsetOptions options) {
			this.zsetOptions = options;
			return this;
		}

		public Builder listOptions(ListOptions options) {
			this.listOptions = options;
			return this;
		}

		public Builder timeSeriesOptions(TimeSeriesOptions options) {
			this.timeSeriesOptions = options;
			return this;
		}

		public Builder stringOptions(StringOptions options) {
			this.stringOptions = options;
			return this;
		}

		public Builder types(Type... types) {
			this.types = Stream.of(types).collect(Collectors.toSet());
			return this;
		}

		public Builder count(int count) {
			return range(IntRange.to(count));
		}

		public DataGeneratorOptions build() {
			return new DataGeneratorOptions(this);
		}

	}

	public static class ZsetOptions extends CollectionOptions {

		public static final DoubleRange DEFAULT_SCORE = DoubleRange.between(0, 100);

		private DoubleRange score = DEFAULT_SCORE;

		private ZsetOptions(Builder builder) {
			super(builder);
			this.score = builder.score;
		}

		public DoubleRange getScore() {
			return score;
		}

		public void setScore(DoubleRange score) {
			this.score = score;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder extends CollectionOptions.Builder<Builder> {

			private DoubleRange score = DEFAULT_SCORE;

			private Builder() {
			}

			public Builder score(DoubleRange score) {
				this.score = score;
				return this;
			}

			public ZsetOptions build() {
				return new ZsetOptions(this);
			}
		}

	}

	public static class TimeSeriesOptions {

		public static final IntRange DEFAULT_SAMPLE_COUNT = IntRange.is(10);

		private IntRange sampleCount = DEFAULT_SAMPLE_COUNT;
		private long startTime;

		private TimeSeriesOptions(Builder builder) {
			this.sampleCount = builder.sampleCount;
			this.startTime = builder.startTime;
		}

		public IntRange getSampleCount() {
			return sampleCount;
		}

		public void setSampleCount(IntRange sampleCount) {
			this.sampleCount = sampleCount;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private IntRange sampleCount = DEFAULT_SAMPLE_COUNT;
			private long startTime;

			private Builder() {
			}

			public Builder sampleCount(IntRange sampleCount) {
				this.sampleCount = sampleCount;
				return this;
			}

			public Builder startTime(long startTime) {
				this.startTime = startTime;
				return this;
			}

			public TimeSeriesOptions build() {
				return new TimeSeriesOptions(this);
			}
		}
	}

	public static class StreamOptions {

		public static final IntRange DEFAULT_MESSAGE_COUNT = IntRange.is(10);
		private static final MapOptions DEFAULT_BODY_OPTIONS = MapOptions.builder().build();

		private IntRange messageCount = DEFAULT_MESSAGE_COUNT;
		private MapOptions bodyOptions = DEFAULT_BODY_OPTIONS;

		private StreamOptions(Builder builder) {
			this.messageCount = builder.messageCount;
			this.bodyOptions = builder.bodyOptions;
		}

		public IntRange getMessageCount() {
			return messageCount;
		}

		public void setMessageCount(IntRange count) {
			this.messageCount = count;
		}

		public MapOptions getBodyOptions() {
			return bodyOptions;
		}

		public void setBodyOptions(MapOptions bodyOptions) {
			this.bodyOptions = bodyOptions;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {

			private IntRange messageCount = DEFAULT_MESSAGE_COUNT;
			private MapOptions bodyOptions = DEFAULT_BODY_OPTIONS;

			private Builder() {
			}

			public Builder messageCount(IntRange count) {
				this.messageCount = count;
				return this;
			}

			public Builder messageCount(int count) {
				return messageCount(IntRange.is(count));
			}

			public Builder bodyOptions(MapOptions bodyOptions) {
				this.bodyOptions = bodyOptions;
				return this;
			}

			public StreamOptions build() {
				return new StreamOptions(this);
			}
		}

	}

	protected static class CollectionOptions {

		public static final IntRange DEFAULT_MEMBER_COUNT = IntRange.is(10);

		private IntRange memberCount = DEFAULT_MEMBER_COUNT;

		protected CollectionOptions(Builder<?> builder) {
			this.memberCount = builder.memberCount;
		}

		public IntRange getMemberCount() {
			return memberCount;
		}

		public void setMemberCount(IntRange count) {
			this.memberCount = count;
		}

		public static class Builder<B extends Builder<B>> {

			private IntRange memberCount = DEFAULT_MEMBER_COUNT;

			@SuppressWarnings("unchecked")
			public B memberCount(IntRange memberCount) {
				this.memberCount = memberCount;
				return (B) this;
			}

		}

	}

	public static class ListOptions extends CollectionOptions {

		private ListOptions(Builder builder) {
			super(builder);
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder extends CollectionOptions.Builder<Builder> {

			public ListOptions build() {
				return new ListOptions(this);
			}

		}

	}

	public static class SetOptions extends CollectionOptions {

		private SetOptions(Builder builder) {
			super(builder);
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder extends CollectionOptions.Builder<Builder> {

			public SetOptions build() {
				return new SetOptions(this);
			}

		}

	}

	public static class StringOptions {

		private IntRange length = IntRange.is(100);

		private StringOptions(Builder builder) {
			this.length = builder.length;
		}

		public IntRange getLength() {
			return length;
		}

		public void setLength(IntRange length) {
			this.length = length;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {

			private IntRange length = IntRange.is(100);

			private Builder() {
			}

			public Builder length(IntRange length) {
				this.length = length;
				return this;
			}

			public StringOptions build() {
				return new StringOptions(this);
			}
		}

	}

	public static class MapOptions {

		public static final IntRange DEFAULT_FIELD_COUNT = IntRange.is(10);
		public static final IntRange DEFAULT_FIELD_LENGTH = IntRange.is(100);

		private IntRange fieldCount = DEFAULT_FIELD_COUNT;
		private IntRange fieldLength = DEFAULT_FIELD_LENGTH;

		private MapOptions(Builder builder) {
			this.fieldCount = builder.fieldCount;
			this.fieldLength = builder.fieldLength;
		}

		public IntRange getFieldCount() {
			return fieldCount;
		}

		public void setFieldCount(IntRange count) {
			this.fieldCount = count;
		}

		public IntRange getFieldLength() {
			return fieldLength;
		}

		public void setFieldLength(IntRange length) {
			this.fieldLength = length;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {

			private IntRange fieldCount = DEFAULT_FIELD_COUNT;
			private IntRange fieldLength = DEFAULT_FIELD_LENGTH;

			private Builder() {
			}

			public Builder fieldCount(IntRange count) {
				this.fieldCount = count;
				return this;
			}

			public Builder fieldLength(IntRange length) {
				this.fieldLength = length;
				return this;
			}

			public MapOptions build() {
				return new MapOptions(this);
			}
		}

	}

}