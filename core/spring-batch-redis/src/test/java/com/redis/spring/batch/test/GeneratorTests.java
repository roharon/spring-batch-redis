package com.redis.spring.batch.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.redis.spring.batch.KeyValue;
import com.redis.spring.batch.gen.CollectionOptions;
import com.redis.spring.batch.gen.GeneratorItemReader;
import com.redis.spring.batch.gen.StreamOptions;

class GeneratorTests {

    @Test
    void defaults() throws UnexpectedInputException, ParseException, Exception {
        int count = 123;
        GeneratorItemReader reader = new GeneratorItemReader();
        reader.setMaxItemCount(count);
        List<KeyValue<String>> list = readAll(reader);
        Assertions.assertEquals(count, list.size());
    }

    private List<KeyValue<String>> readAll(GeneratorItemReader reader)
            throws UnexpectedInputException, ParseException, Exception {
        List<KeyValue<String>> list = new ArrayList<>();
        KeyValue<String> ds;
        while ((ds = reader.read()) != null) {
            list.add(ds);
        }
        return list;
    }

    @Test
    void options() throws Exception {
        int count = 123;
        GeneratorItemReader reader = new GeneratorItemReader();
        reader.setMaxItemCount(count);
        List<KeyValue<String>> list = readAll(reader);
        Assertions.assertEquals(count, list.size());
        for (KeyValue<String> ds : list) {
            switch (ds.getType()) {
                case KeyValue.SET:
                case KeyValue.LIST:
                case KeyValue.ZSET:
                    Assertions.assertEquals(CollectionOptions.DEFAULT_MEMBER_COUNT.getMax(),
                            ((Collection<?>) ds.getValue()).size());
                    break;
                case KeyValue.STREAM:
                    Assertions.assertEquals(StreamOptions.DEFAULT_MESSAGE_COUNT.getMax(),
                            ((Collection<?>) ds.getValue()).size());
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void read() throws Exception {
        int count = 456;
        GeneratorItemReader reader = new GeneratorItemReader();
        reader.open(new ExecutionContext());
        reader.setMaxItemCount(456);
        KeyValue<String> ds1 = reader.read();
        assertEquals("gen:1", ds1.getKey());
        int actualCount = 1;
        while (reader.read() != null) {
            actualCount++;
        }
        assertEquals(count, actualCount);
        reader.close();
    }

}