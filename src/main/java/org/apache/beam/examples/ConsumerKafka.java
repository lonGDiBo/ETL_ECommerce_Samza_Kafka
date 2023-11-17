package org.apache.beam.examples;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException; 
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Date;


public class ConsumerKafka {

    public static void main(String[] args) {
        // Kafka Consumer configuration
        Properties kafkaProps = new Properties();
        
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "your_consumer_group_id");
	kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Adjusted for consuming from the beginning

        // MySQL connection configuration
        String jdbcUrl = "jdbc:mysql://localhost:3306/coSale";
        String dbUser = "root";
        String dbPassword = "Minhlong2@";
	
	Consumer<String, String> kafkaConsumer = null;
	try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            kafkaConsumer = new KafkaConsumer<>(kafkaProps);
            kafkaConsumer.subscribe(Collections.singletonList("output-stream"));

            while (true) {
                // Poll for records
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    String kafkaValue = record.value();
                    String[] substrings = kafkaValue.split(",");
                  try {
                        insertIntoMySQL(connection, substrings);
                        System.out.println("Received from Kafka: " + kafkaValue);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (kafkaConsumer != null) {
                kafkaConsumer.close();
            }
        }
    }

    private static void insertIntoMySQL(Connection connection, String[] data) throws SQLException {
        String insertQuery = "INSERT INTO `orders` (id, category, date, productId, quantity, price, customerId, productName, total) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
      		if (data[0].matches("\\d+")) {
   			preparedStatement.setInt(1, Integer.parseInt(data[0])); // Assuming the ID is at index 0 in the data array
		}else {
			return;
				}
				
            preparedStatement.setString(2, data[1]); // Assuming the CATEGORY is at index 1

            // Parse and set the date
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                Date parsedDate = dateFormat.parse(data[2]);
                preparedStatement.setTimestamp(3, new java.sql.Timestamp(parsedDate.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing exception as needed
            }

            preparedStatement.setInt(4, Integer.parseInt(data[3])); // Assuming the PRODUCT_ID is at index 3
            preparedStatement.setInt(5, Integer.parseInt(data[4])); // Assuming the QUANTITY is at index 4
            preparedStatement.setFloat(6, Float.parseFloat(data[5])); // Assuming the PRICE is at index 5
            preparedStatement.setInt(7, Integer.parseInt(data[6])); // Assuming the CUSTOMER_ID is at index 6
            preparedStatement.setString(8, data[7]); // Assuming the PRODUCT_NAME is at index 7
            preparedStatement.setFloat(9, Float.parseFloat(data[8])); // Assuming the TOTAL is at index 8

            preparedStatement.executeUpdate();
        }
    }
}