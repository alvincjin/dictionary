# Word Dictionary
This word dictionary service is built by Scala, Akka Http and Cassandra,etc.

The architecture is designed to provide large volume storage capability and low response latency.

* Scala is a type-safe high-level programming language combining both OOP and functional style.
It's a perfect language to handle concurrent and parallel data processing.

* Akka Http is an Actor-based toolkit for interacting with web services and clients built on Akka Streams.
It can handle large throughput in sub-second response latency.
 
* Cassandra is Peer-to-Peer distributed Key-value store, which has outstanding read/write preformance.

## Prerequisites

* Install Cassandra

Download Cassadra from:

http://httpd-mirror.frgl.pw/apache/cassandra/3.0.11/apache-cassandra-3.0.11-bin.tar.gz

Unzip it to your local and start Cassandra service

$ ./pathToCassandra/bin/cassandra

* Install Java 8, Scala and sbt

Please follow my blog:

http://alvincjin.blogspot.ca/2017/01/install-java-and-scala-in-ubuntu.html

* Build the executable jar

$ sbt assembly

The jar file is packaged to ~/dictionary/target/scala-2.11/dictionary-1.0.0.jar 

## Execution

* Start the dictionary service

java -Dconfig.file=application.conf  -classpath dictionary-1.0.0.jar com.alvin.dict.ServiceBootStrap

## API Examples

To test the APIs, you can use either Google Postman or curl.

* Create an entry in the dictionary
```
POST localhost:8080/entry

Body:
{
	"word":"azz",
	"description":"zza is test"
}
```    

* Retrieve entries with word starting with a specific prefix

```
GET localhost:8080/description/az

Response:
[
  {
    "word": "azza",
    "description": "azza is test"
  },
  {
    "word": "azz",
    "description": "azz is also a test"
  }
]
```
* Retrieve the description of a given word
```
GET localhost:8080/entry/azz

Response:
{
  "word": "azz",
   "description": "azz is also a test"
}
```


* Delete an entry by word
```
DELETE localhost:8080/entry/azz

Response:
Deleted azz Successfully
```

* Count total entries in the dictionary

```
GET localhost:8080/count

Response:
2
```