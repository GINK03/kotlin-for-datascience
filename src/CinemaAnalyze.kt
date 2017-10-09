import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

import redis.clients.jedis.*

import kotlinx.coroutines.experimental.*
import kotlin.concurrent.thread

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop


import java.io.File
import java.nio.file.*
import java.io.PrintWriter
import java.util.Random
import java.lang.Exception

/*
object DataFrame : Table() {
  val id = integer("id").autoIncrement().primaryKey() // Column<Int>
  val text = varchar("text", 10000) // Column<String>
}*/
@Serializable
data class CinemaData(val reviewTitle:String, val stars: Double?, val review : String, val title: String)

object CinemaDataFrame : Table() {
  //val id = integer("id").autoIncrement().primaryKey() // Column<Int>
  val hash = integer("hash").primaryKey() // Column<Int>
  val text = varchar("json", 100000) // Column<String>
}

fun flashDB() {
  val inputStream = File("/home/gimpei/sdb/kotlinx-multithread-dataframe-design/resources/reveiws.json").inputStream()
  val lines = mutableListOf<String>() 
  inputStream.bufferedReader().useLines { xs -> xs.forEach { lines.add(it)} }
  val objs = lines.map { json ->
    println(json)
    val obj = JSON.parse<CinemaData>(json)
    obj
  }
  transaction { 
    create( CinemaDataFrame ) // create table
    objs.map { obj -> 
      val hashO = obj.hashCode()
      val textO = JSON.stringify( obj )
      try {
        CinemaDataFrame.insert {
          it[hash] = hashO
          it[text] = textO
        } 
      } catch ( e : java.sql.SQLIntegrityConstraintViolationException ) {
        println(e)
      }
      println( hashO )
    }
  }
}

fun getHistgram() {
  transaction { 
    create( CinemaDataFrame ) // create table
    CinemaDataFrame.selectAll().map {
      val obj = JSON.parse<CinemaData>( it[CinemaDataFrame.text] )
      obj
    }.map { 
      //println(it) 
      val stars = it.stars.toString().slice(0..2)
      //println( stars )
      stars
    }.groupBy { 
      it
    }.toList()
    .sortedBy {
      it.first
    }.map {
      val stars = it.first
      val arr = it.second
      val size = arr.size
      println("$stars $size")
    }
  }
}
fun main(args: Array<String>) = runBlocking<Unit> {
  val kargs = args.toList().map { it.toString() }
  // データを永続化するにはmysqlなどをバックエンドにする
  // DBはデータベース名で、必要に応じて変える
  Database.connect("jdbc:mysql://localhost:3306/cinema", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "root")
  
  when {
    kargs.contains("flashDB") -> flashDB()
    kargs.contains("getHistgram") -> getHistgram()
    else -> null
  }
  if( kargs.contains("flashDB") )  {
    flashDB() 
  }
}
