package com.redis.spring.batch.test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.util.RedisModulesUtils;
import com.redis.spring.batch.RedisItemReader;
import com.redis.spring.batch.RedisItemWriter;
import com.redis.spring.batch.RedisItemReader.ReaderMode;
import com.redis.spring.batch.common.DataType;
import com.redis.spring.batch.common.KeyComparison;
import com.redis.spring.batch.common.KeyComparison.Status;
import com.redis.spring.batch.common.KeyComparisonItemReader;
import com.redis.spring.batch.common.KeyValue;
import com.redis.spring.batch.common.Range;
import com.redis.spring.batch.gen.GeneratorItemReader;
import com.redis.spring.batch.reader.StructItemReader;
import com.redis.spring.batch.util.BatchUtils;
import com.redis.testcontainers.RedisServer;

import io.lettuce.core.AbstractRedisClient;

public abstract class AbstractTargetTestBase extends AbstractTestBase {

    protected abstract RedisServer getTargetRedisServer();

    protected AbstractRedisClient targetClient;

    protected StatefulRedisModulesConnection<String, String> targetConnection;

    protected RedisModulesCommands<String, String> targetCommands;

    @BeforeAll
    void targetSetup() throws Exception {
        // Target Redis setup
        getTargetRedisServer().start();
        targetClient = client(getTargetRedisServer());
        targetConnection = RedisModulesUtils.connection(targetClient);
        targetCommands = targetConnection.sync();
    }

    @AfterAll
    void targetTeardown() {
        targetConnection.close();
        targetClient.shutdown();
        targetClient.getResources().shutdown();
        getTargetRedisServer().close();
    }

    @BeforeEach
    void targetFlushAll() {
        targetCommands.flushall();
        awaitEquals(() -> 0L, targetCommands::dbsize);
    }

    protected void awaitCompare(TestInfo info) {
        awaitUntil(() -> compare(info).isEmpty());
    }

    /**
     * 
     * @param left
     * @param right
     * @return
     * @return list of differences
     * @throws Exception
     */
    protected List<KeyComparison> compare(TestInfo info) throws Exception {
        if (commands.dbsize().equals(0L)) {
            Assertions.fail("Source database is empty");
        }
        Long sourceSize = commands.dbsize();
        Long targetSize = targetCommands.dbsize();
        if (!sourceSize.equals(targetSize)) {
            Assertions.fail(String.format("Source and target databases have different sizes. Expected %s but was %s",
                    sourceSize, targetSize));
        }
        KeyComparisonItemReader reader = comparisonReader(new SimpleTestInfo(info, "compare", "reader"));
        reader.open(new ExecutionContext());
        List<KeyComparison> comparisons = BatchUtils.readAll(reader);
        reader.close();
        Assertions.assertFalse(comparisons.isEmpty());
        return comparisons.stream().filter(c -> c.getStatus() != Status.OK).collect(Collectors.toList());
    }

    protected KeyComparisonItemReader comparisonReader(TestInfo info) throws Exception {
        StructItemReader<String, String> sourceReader = RedisItemReader.struct(client);
        StructItemReader<String, String> targetReader = RedisItemReader.struct(targetClient);
        KeyComparisonItemReader reader = new KeyComparisonItemReader(sourceReader, targetReader);
        reader.setName(name(info));
        reader.setTtlTolerance(Duration.ofMillis(100));
        return reader;
    }

    protected <K, V, T extends KeyValue<K>> List<KeyComparison> replicateLive(TestInfo info, RedisItemReader<K, V, T> reader,
            RedisItemWriter<K, V, T> writer, RedisItemReader<K, V, T> liveReader, RedisItemWriter<K, V, T> liveWriter)
            throws Exception {
        configureReader(new SimpleTestInfo(info, "reader"), reader);
        configureReader(new SimpleTestInfo(info, "liveReader"), liveReader);
        liveReader.setMode(ReaderMode.LIVE);
        GeneratorItemReader gen = generator(300);
        generate(new SimpleTestInfo(info, "generate"), gen);
        TaskletStep step = step(new SimpleTestInfo(info, "step"), reader, writer).build();
        SimpleFlow flow = new FlowBuilder<SimpleFlow>(name(new SimpleTestInfo(info, "snapshotFlow"))).start(step).build();
        TaskletStep liveStep = flushingStep(new SimpleTestInfo(info, "liveStep"), liveReader, liveWriter).build();
        SimpleFlow liveFlow = new FlowBuilder<SimpleFlow>(name(new SimpleTestInfo(info, "liveFlow"))).start(liveStep).build();
        Job job = job(info).start(new FlowBuilder<SimpleFlow>(name(new SimpleTestInfo(info, "flow")))
                .split(new SimpleAsyncTaskExecutor()).add(liveFlow, flow).build()).build().build();
        JobExecution execution = runAsync(job);
        awaitOpen(reader);
        awaitOpen(writer);
        awaitOpen(liveReader);
        awaitOpen(liveWriter);
        Thread.sleep(100);
        GeneratorItemReader liveGen = generator(700, DataType.HASH, DataType.LIST, DataType.SET, DataType.STRING,
                DataType.ZSET);
        liveGen.setExpiration(Range.of(100));
        liveGen.setKeyRange(Range.from(300));
        generate(new SimpleTestInfo(info, "generateLive"), liveGen);
        try {
            awaitTermination(execution);
        } catch (ConditionTimeoutException e) {
            // ignore
        }
        awaitClosed(reader);
        awaitClosed(writer);
        awaitClosed(liveReader);
        awaitClosed(liveWriter);
        return compare(info);
    }

}
