package com.alvin.dict.cassandra

import com.alvin.dict.model.Entry
import com.alvin.dict.util.Config
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select}
import com.datastax.driver.core.{Cluster, QueryOptions, ResultSet}
import scala.collection.JavaConversions._
import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by alvin.jin on 3/3/2017.
  */
trait CassandraDao extends Config {

  implicit val session = new Cluster.Builder()
    .addContactPoints(hosts.toArray: _*)
    .withPort(port)
    .withQueryOptions(new QueryOptions().setConsistencyLevel(QueryOptions.DEFAULT_CONSISTENCY_LEVEL))
    .build
    .connect


  def createTables(keyspace: String, table: String): Unit = {

    session.execute(s"DROP KEYSPACE IF EXISTS $keyspace")
    session.execute(s"CREATE KEYSPACE $keyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute(
      s"""
         | CREATE TABLE IF NOT EXISTS $keyspace.$table (
         |         word text PRIMARY KEY,
         |         description text
         |         )
       """.stripMargin
    )
/*
    session.execute(
      s"""
         |CREATE CUSTOM INDEX word_idx ON $keyspace.$table (word)
         |USING 'org.apache.cassandra.index.sasi.SASIIndex'
         |WITH OPTIONS = {
         |'mode': 'CONTAINS',
         |'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer',
         |'case_sensitive': 'false'
         |}
       """.stripMargin)*/
  }


  def createEntry(entry: Entry): Future[String] = {

    val query = QueryBuilder.insertInto(keyspace, table)
      .value("word", entry.word.toLowerCase)
      .value("description", entry.description)

    Future {
      session.execute(query)
      s"Inserted ${entry.word} Successfully"
    }
  }

/*
  def updateDescription(word: String, description: String): Future[String] = {

    val query = QueryBuilder.update(keyspace, table)
      .`with`(QueryBuilder.set("description", description))
      .where(QueryBuilder.eq("word", word))

    Future {
      session.execute(query)
      s"Inserted ${word} Successfully"
    }
  }
*/
  def readEntry(word: String): Future[List[Entry]] = {

    val query = QueryBuilder.select()
      .from(keyspace, table)
      .where(QueryBuilder.eq("word", word.toLowerCase))

    Future {
      val resultSet: ResultSet = session.execute(query)
      resultSet.map { row =>
        Entry(row.getString("word"), row.getString("description"))}.toList
    }
  }

  def readDescription(word: String): Future[List[Entry]] = {


    val upperBound = word.last match {
      case c if (c >='a' && c < 'z') => word.take(word.length-1)+(word.last +1).toChar
      case 'z' if word.length >1 => word.take(word.length-2)+(word(word.length-2) +1).toChar
      case 'z' if word.length == 1 => "{"
    }

    val query = QueryBuilder.select()
      .from(keyspace, table)
      .allowFiltering()
      .where(QueryBuilder.gte("word", word)).and(QueryBuilder.lt("word", upperBound))

    Future {
      val resultSet: ResultSet = session.execute(query)
      resultSet.map { row =>
        Entry(row.getString("word"), row.getString("description"))}.toList
    }
  }


  def deleteEntry(word: String): Future[String] = {

    val query = QueryBuilder.delete()
      .from(keyspace, table)
      .where(QueryBuilder.eq("word", word.toLowerCase))

    Future {
      session.execute(query)
      s"Deleted ${word} Successfully"
    }

  }

  def countEntries(): Future[Long] = {

    val query = s"SELECT COUNT(*) as count FROM $keyspace.$table"

    Future {
      val resultSet: ResultSet = session.execute(query)
      resultSet.one().getLong("count")
    }
  }

/*
  def queryDescription(keyword: String): Future[List[Entry]] = {

    val query = QueryBuilder.select()
      .from(keyspace, table)
      .where(QueryBuilder.like("description", keyword))
      .orderBy(QueryBuilder.asc("word"))

    Future {
      val resultSet: ResultSet = session.execute(query)
      resultSet.map { row =>
        Entry(row.getString("word"), row.getString("description"))}.toList
    }
  }
*/

}

