# ETL Ecommerce data from csv to MySQL using Kafka with Samza. Visualize data with Grafana

## Introduction 

ETL sales data using Samza + Kafka. Then use Grafana to visualize the data
 - Data source: https://data.mendeley.com/datasets/8gx2fvg2k6/5/files/72784be5-36d3-44fe-b75d-0edbf1999f65
 - Introduction dataset: DataCo Global's supply chain dataset. Includes the company's transactions with customers. The data set includes 53 attributes ranging from order and shipping information to sales information, 180,519 rows, and features are a mix of text and numeric data, with the exception of row data. be positioned and sold. Specifically, there are 24 character columns and 28 numeric columns.

![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/afab84d9-3737-4f82-9c55-868b634b36b2)

Get ideas from: (https://github.com/apache/samza-beam-examples) The examples in that repository serve to demonstrate running Beam pipelines with SamzaRunner locally, in Yarn cluster, or in standalone cluster with Zookeeper. More complex pipelines can be built from here and run in similar manner.

### Example Pipelines
The following examples are included:

1. [TranfomrKafka](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/blob/main/src/main/java/org/apache/beam/examples/TranformKafka.java) Perform calculations taking only the columns needed to analyze the data. It uses a fixed 10 second window to aggregate counts.
2. [ConsumerKafa](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/blob/main/src/main/java/org/apache/beam/examples/ConsumerKafka.java) Receive data from topic "output-stream", analyze and insert data into MySQL database ("coSale" database, table "orders").
### Run the Examples

Each example can be run locally, in Yarn cluster or in standalone cluster. Here we use KafkaWordCount as an example.

#### Set Up
1. Download and install [JDK version 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Verify that the JAVA_HOME environment variable is set and points to your JDK installation.

2. Download and install [Apache Maven](http://maven.apache.org/download.cgi) by following Maven’s [installation guide](http://maven.apache.org/install.html) for your specific operating system.

Check out the `samza-beam-examples` repo:

```
$ git clone https://github.com/apache/samza-beam-examples.git
$ cd samza-beam-examples
```

A script named "grid" is included in this project which allows you to easily download and install Zookeeper, Kafka, and Yarn.
You can run the following to bring them all up running in your local machine:

```
$ scripts/grid bootstrap
```

All the downloaded package files will be put under `deploy` folder. Once the grid command completes, 
you can verify that Yarn is up and running by going to http://localhost:8088. You can also choose to
bring them up separately, e.g.:

Create a Kafka topic named "input-text" for this example:

```
$ ./deploy/kafka/bin/kafka-topics.sh  --zookeeper localhost:2181 --create --topic input-text --partitions 10 --replication-factor 1
```
   
#### Run Locally
You can run directly within the project using maven:

```
$ mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.TranformKafka \
    -Dexec.args="--runner=SamzaRunner --experiments=use_deprecated_read" -P samza-runner
```

![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/724b350b-0192-4bc3-8919-268cdb30baf7)

#### Packaging Your Application
To execute the example in either Yarn or standalone, you need to package it first.
After packaging, we deploy and explode the tgz in the deploy folder:

```
 $ mkdir -p deploy/examples
 $ mvn package && tar -xvf target/samza-beam-examples-0.1-dist.tar.gz -C deploy/examples/
```

#### Run in Standalone Cluster with Zookeeper
You can use the `run-beam-standalone.sh` script included in this repo to run an example
in standalone mode. The config file is provided as `config/standalone.properties`. Note by
default we create one single split for the whole input (--maxSourceParallelism=1). To 
set each Kafka partition in a split, we can set a large "maxSourceParallelism" value which 
is the upper bound of the number of splits.

```
$ deploy/examples/bin/run-beam-standalone.sh org.apache.beam.examples.TranformKafka \
    --configFilePath=$PWD/deploy/examples/config/standalone.properties --maxSourceParallelism=1024
```

#### Run Yarn Cluster
Similar to running standalone, we can use the `run-beam-yarn.sh` to run the examples
in Yarn cluster. The config file is provided as `config/yarn.properties`. To run the 
TranformKafka example in yarn:

```
$ deploy/examples/bin/run-beam-yarn.sh org.apache.beam.examples.TranformKafka \
    --configFilePath=$PWD/deploy/examples/config/yarn.properties --maxSourceParallelism=1024
```

### Run Consumer
Compile and run the Java program defined in the org.apache.beam.examples.ConsumerKafka class using the Maven project management tool.
```
$ mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.ConsumerKafka
```

![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/c655079a-b09f-402e-a5d8-f29cdf8f2f5e)

### Run Producer
Command to send data from CSV file (CoDataset.csv) into Kafka topic ("input-text"):
./deploy/kafka/bin/kafka-console-producer.sh: Run Kafka Console Producer.

--topic input-text: Send message to Kafka topic "input-text".

--broker-list localhost:9092: Connect to the Kafka broker at localhost and port 9092.

--property "parse.key=true": Enable parsing for messages, ensuring key usage.

--property "key.separator=,": Use comma as separator between key and value in message.

```
$ ./deploy/kafka/bin/kafka-console-producer.sh --topic input-text --broker-list localhost:9092 --property "parse.key=true" --property "key.separator=," < /home/minhlong/Downloads/CoDataset.csv
```
![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/fef8c529-922d-4922-bba6-e9f57a290d53)

### Project Outcome
After successfully extracting data. Check if the database "coSale" has successfully loaded data
+ COnsumer
![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/5e9ccadb-cc47-400f-90e6-4402eff1c989)
+ Mysql
![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/430c87a9-9026-48de-bd75-8aee750b848d)
![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/6efac853-e452-4b43-93fa-30510d7803d1)

+ Next, we use Grafana to analyze connection data from Mysql
  + How to install Grafana for ubuntu 22.04 --> https://www.youtube.com/watch?v=fcFfOoDEQH4&t=456s
   ![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/0f1e545f-76a2-4f2d-9c30-1db1fac977c9)

After successfully connecting Mysql to Grafana, we visualize that data as a chart to support analysis.
![image](https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/assets/115699195/0d2f00d8-de37-43bb-91d4-c9fe02aa9816)

### More Information
- Apache Beam
- Apache Samza
- Quickstart: Java, Python, Go
