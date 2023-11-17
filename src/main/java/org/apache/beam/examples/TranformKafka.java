package org.apache.beam.examples;

import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.beam.sdk.transforms.Filter;

public class KafkaWordCount2 {

    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        options.setJobName("kafka-word-count");

        Pipeline pipeline = Pipeline.create(options);

        pipeline
                .apply(KafkaIO.<String, String>read()
                        .withBootstrapServers("localhost:9092")
                        .withTopic("input-text")
                        .withKeyDeserializer(StringDeserializer.class)
                        .withValueDeserializer(StringDeserializer.class)
                        .withoutMetadata())
                .apply(Values.create())
                .apply(
                	    MapElements
                	        .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                	        .via((String line) -> {
                	            String[] columns = line.split(",");

                	            if (columns.length >= 3) {  // Assuming you want to concatenate columns 1, 2, and 3
                	                String key = columns[0];
                	                String value = columns[1] + " " + columns[2] + " " + columns[3];  // Adjust the indices based on your actual data
                	                return KV.of(key, value);
                	            } else {
                	                // Handle the case where there are not enough columns
                	                // You might want to log a warning or handle it based on your requirements
                	                return null;
                	            }
                	        }))
                .apply(KafkaIO.<String, String>write()
                        .withBootstrapServers("localhost:9092")
                        .withTopic("output-stream")
                        .withKeySerializer(StringSerializer.class)
                        .withValueSerializer(StringSerializer.class));

        pipeline.run().waitUntilFinish();
    }
}