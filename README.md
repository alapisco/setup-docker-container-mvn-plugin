# A Docker plugin for maven

This plugin sets up docker containers, then waits for them to be up and running , and
finally it stops them and clears everything up after we finish using the containers 

It supports any of the following containers:

- Cassandra, version 3.11 on port 9042
- ElasticSearch, version 2.4.1 on port 9200 and 9300
- Mongo DB, version 3.6.5 on port 27017
- Zookeeper, version 3.2,1 on port 2181
- Kafka, version 3.2.1 on port 39092
- Schema Registry, version 3.2.1 on port 8051

It includes a custom made docker container of Alpine Linux created on the fly, which uses a script
(entrypoint.sh) that waits for a given list of services to be available.



## Usage

To compile the plugin just go to the root directory (setup-docker-containers-maven-plugin)
and execute:  mvn clean install

To use the plugin in your maven project you need to include the plugin and configure it in your pom

See the "example" directory for a simple maven project that uses the plugin to initialize
Cassandra , execute a simple integration test, and stops and clears the container afterwards


### Example 
![alt tag](https://user-images.githubusercontent.com/9953482/57903650-97af6e80-7834-11e9-8a95-c8022d939271.png)

![alt tag](https://user-images.githubusercontent.com/9953482/57903760-2623f000-7835-11e9-99c5-12650064d78b.png)




