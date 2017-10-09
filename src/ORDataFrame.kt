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

@Serializable
data class ORData(val key:String, val a: Int, @Optional val b: String = "42", val c: Long, val d: Boolean, val e : String?)

object DataFrame : Table() {
  val id = integer("id").autoIncrement().primaryKey() // Column<Int>
  val text = varchar("text", 10000) // Column<String>
}

object HashDataFrame : Table() {
  val id = integer("hash").primaryKey() // Column<Int>
  val text = varchar("text", 10000) // Column<String>
}
fun main(args: Array<String>) = runBlocking<Unit> {

  // onmemory H2 database
  //Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
  
  // データを永続化するにはmysqlなどをバックエンドにする
  // DBはデータベース名で、必要に応じて変える
  Database.connect("jdbc:mysql://localhost:3306/DB", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "root")

  // 問題なく復元できることの確認
  transaction { 
    create( DataFrame, HashDataFrame ) // create table
    (0..100000).map { k ->
      val key = k.toString()
      val objSrc = Data(key, k, "Default", 1000L, true, null)
      val hash = objSrc.hashCode()
      val json = JSON.stringify(objSrc) 
      try {
        HashDataFrame.insert {
          it[id] = hash 
          it[text] = json
        } 
      } catch ( e : java.sql.SQLIntegrityConstraintViolationException ) {
        HashDataFrame.update( { HashDataFrame.id eq hash } ) {
          it[id] = hash 
          it[text] = json
        } 
      }
      val obj = JSON.parse<Data>(json)
      println( obj )
    }
    HashDataFrame.selectAll().forEach { 
      println(it[HashDataFrame.id])
      val obj = JSON.parse<Data>(it[HashDataFrame.text])
      println( obj )
      println( obj.hashCode() )
    }
  }
}
