package com.alvin.dict.util

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

/**
  * Created by alvin.jin on 3/3/2017.
  */

trait Config {

  val appConfig = ConfigFactory.load()

  val cassandraConfig = appConfig.getConfig("cassandra")
  val keyspace = cassandraConfig.getString("keyspace")
  val table = cassandraConfig.getString("table")
  val hosts: List[String] = cassandraConfig.getStringList("hostList").toList
  val port = cassandraConfig.getInt("port")
}
