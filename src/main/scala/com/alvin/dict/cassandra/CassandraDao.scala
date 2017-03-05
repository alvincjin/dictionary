package com.alvin.dict.cassandra

import com.alvin.dict.model.Entry
import com.alvin.dict.util.Config
import com.datastax.driver.core.querybuilder.Select.Where
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

  //Initiate a Cassandra session
  implicit val session = new Cluster.Builder()
    .addContactPoints(hosts.toArray: _*)
    .withPort(port)
    .withQueryOptions(new QueryOptions().setConsistencyLevel(QueryOptions.DEFAULT_CONSISTENCY_LEVEL))
    .build
    .connect

  /**
    * Create a keyspace and table if not exist
    * @param keyspace
    * @param table
    */
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

  }

  /**
    * Execute query and return the records
    * @param query
    * @return
    */
  def executeAndReturn(query: Where) = Future {
    val resultSet: ResultSet = session.execute(query)
    resultSet.map { row =>
      Entry(row.getString("word"), row.getString("description"))
    }.toList
  }

  /**
    * Create a new entry in dictionary with word and its description
    * @param entry
    * @return
    */
  def createEntry(entry: Entry): Future[String] = {

    val query = QueryBuilder.insertInto(keyspace, table)
      .value("word", entry.word.toLowerCase)
      .value("description", entry.description)

    Future {
      session.execute(query)
      s"Inserted ${entry.word} Successfully"
    }
  }


  /**
    * Retrieve the description of a specific word
    * @param word
    * @return a list of entries
    */
  def retrieveDescriptions(word: String): Future[List[Entry]] = {

    val query = QueryBuilder.select()
      .from(keyspace, table)
      .where(QueryBuilder.eq("word", word.toLowerCase))

    executeAndReturn(query)
  }

  /**
    * Retrieve entries starting with a prefix
    * @param prefix
    * @return a list of entries
    */
  def retrieveEntries(prefix: String): Future[List[Entry]] = {

    val upperBound = prefix.last match {
      case c if (c >= 'a' && c < 'z') => prefix.take(prefix.length - 1) + (prefix.last + 1).toChar
      case 'z' if prefix.length > 1 => prefix.take(prefix.length - 2) + (prefix(prefix.length - 2) + 1).toChar
      case 'z' if prefix.length == 1 => "{"
    }

    val query = QueryBuilder.select()
      .from(keyspace, table)
      .allowFiltering()
      .where(QueryBuilder.gte("word", prefix)).and(QueryBuilder.lt("word", upperBound))

    executeAndReturn(query)
  }

  /**
    * Delete an entry by given word
    * @param word
    * @return
    */
  def deleteEntry(word: String): Future[String] = {

    val query = QueryBuilder.delete()
      .from(keyspace, table)
      .where(QueryBuilder.eq("word", word.toLowerCase))

    Future {
      session.execute(query)
      s"Deleted ${word} Successfully"
    }

  }


  /**
    * Count the number of entries in the dictionary
    * @return
    */
  def countEntries(): Future[Long] = {

    val query = s"SELECT COUNT(*) as count FROM $keyspace.$table"

    Future {
      val resultSet: ResultSet = session.execute(query)
      resultSet.one().getLong("count")
    }
  }


}

