
# Apache Beam Examples Using SamzaRunner
## Introduction 
ETL sales data using Samza + Kafka. Then use Grafana to visualize the data

### Example Pipelines
The following examples are included:

1. (https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/blob/main/src/main/java/org/apache/beam/examples/TranformKafka.java)]  perform calculations taking only the columns needed to analyze the data. It uses a fixed 10 second window to aggregate counts.
2. https://github.com/lonGDiBo/ETL_ECommerce_Samza_Kafka/blob/main/src/main/java/org/apache/beam/examples/ConsumerKafka.java
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

```
$ scripts/grid install zookeeper
$ scripts/grid start zookeeper
```
Now let's create a Kafka topic named "input-text" for this example:

```
$ ./deploy/kafka/bin/kafka-topics.sh  --zookeeper localhost:2181 --create --topic input-text --partitions 10 --replication-factor 1
```
   
#### Run Locally
You can run directly within the project using maven:

```
$ mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.TranformKafka \
    -Dexec.args="--runner=SamzaRunner --experiments=use_deprecated_read" -P samza-runner
```

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
$ deploy/examples/bin/run-beam-standalone.sh org.apache.beam.examples.KafkaWordCount \
    --configFilePath=$PWD/deploy/examples/config/standalone.properties --maxSourceParallelism=1024
```

#### Run Yarn Cluster
Similar to running standalone, we can use the `run-beam-yarn.sh` to run the examples
in Yarn cluster. The config file is provided as `config/yarn.properties`. To run the 
KafkaWordCount example in yarn:

```
$ deploy/examples/bin/run-beam-yarn.sh org.apache.beam.examples.KafkaWordCount \
    --configFilePath=$PWD/deploy/examples/config/yarn.properties --maxSourceParallelism=1024
```

#### Validate the Pipeline Results
Now the pipeline is deployed to either locally, standalone or Yarn. Let's check out the results. First we start a kakfa consumer to listen to the output:

```
$ ./deploy/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic word-count --property print.key=true
```

Then let's publish a few lines to the input Kafka topic:

```
$ ./deploy/kafka/bin/kafka-console-producer.sh --topic input-text --broker-list localhost:9092
Nory was a Catholic because her mother was a Catholic, and Nory’s mother was a Catholic because her father was a Catholic, and her father was a Catholic because his mother was a Catholic, or had been.
```


### Beyond Examples
Feel free to build more complex pipelines based on the examples above, and reach out to us:

* Subscribe and mail to [user@beam.apache.org](mailto:user@beam.apache.org) for any Beam questions.

* Subscribe and mail to [user@samza.apache.org](mailto:user@samza.apache.org) for any Samza questions.

### More Information

* [Apache Beam](http://beam.apache.org)
* [Apache Samza](https://samza.apache.org/)
* Quickstart: [Java](https://beam.apache.org/get-started/quickstart-java), [Python](https://beam.apache.org/get-started/quickstart-py), [Go](https://beam.apache.org/get-started/quickstart-go)
