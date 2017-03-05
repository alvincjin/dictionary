package com.alvin.dict.service

import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.alvin.dict.cassandra.CassandraDao
import com.alvin.dict.model.Entry

/**
  * Created by alvin.jin on 3/3/2017.
  */
trait AkkaJSONProtocol extends DefaultJsonProtocol with CassandraDao {
  implicit val entryFormat = jsonFormat2(Entry.apply)
}

trait Routes extends AkkaJSONProtocol {

  val route =
  //Delete an entry by word
    path("entry" / Segment) { word =>
      delete {
        onSuccess(deleteEntry(word)) {
          case result: String =>
            complete(result)
        }
      }
    } ~ //Create an entry in the dictionary
      path("entry") {
        (post & entity(as[Entry])) { entry =>
          onSuccess(createEntry(entry)) {
            case result: String =>
              complete(result)
          }
        }
      } ~ //Retrieve entries with word starting with a specific prefix
      path("entry" / Segment) { prefix =>
        get {
          onSuccess(retrieveDescriptions(prefix)) {
            case result: List[Entry] =>
              complete(result)
          }
        }
      } ~ //Retrieve the description of a given word
      path("description" / Segment) { word =>
        get {
          onSuccess(retrieveEntries(word)) {
            case result: List[Entry] =>
              complete(result)
          }
        }
      } ~ //Count total entries in the dictionary
      path("count") {
        get {
          onSuccess(countEntries()) {
            case result: Long =>
              complete(result.toString)
          }
        }
      }

}
