import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

import redis.clients.jedis.*

import kotlinx.coroutines.experimental.*

import java.io.File
import java.nio.file.*
import java.io.PrintWriter
import java.util.Random
import java.lang.Exception
@Serializable
data class Data(val key:String, val a: Int, @Optional val b: String = "42", val c: Long, val d: Boolean, val e : String?)

fun main(args: Array<String>) = runBlocking<Unit> {
  val fileNames = Files.newDirectoryStream(Paths.get("/home/ubuntu/xvdf/okeya/raw_data/listing"), "*").map { name ->
    println(name)
    name.toString()
  }
  fileScan@for ( fileName in fileNames )  { 
    val lines = mutableListOf<String>() 
    try { 
      val inputStream = File(fileName).inputStream()
      inputStream.bufferedReader().useLines { xs -> xs.forEach { lines.add(it)} }
    } catch ( e : java.lang.Exception ) {
      continue
    }
    val iter = lines.iterator()
    try {
      val keys = when {
        fileName.contains("ADWORDS") -> { iter.next(); iter.next().split(",") }
        else -> continue@fileScan
      }
      println(fileName)
      println(keys)
      val df = mutableListOf<Map<String,String>>()
      iter
        .forEach { line ->
          if ( Math.random() < 0.1 ) {
            val vals = line.split(",")
            val obj = keys.zip(vals).toMap()
            //println(obj)  
            df.add( obj )
          }
      }
      df.map { 
        val keyword = it["Keyword"]
        val imps = it["Impressions"]?.toDouble() ?: 0.0
        val campid = it["Campaign ID"]
        Pair( keyword, imps )
      }.groupBy {
        it.first
      }.toList().map {
        val keyword = it.first
        val arr = it.second
        val sum = arr.map { it.second }.reduce{ y,x -> y+x }
        Pair( keyword, sum )
      }.let {
        val text = it.toString()
        val saveName = "/home/ubuntu/xvdf/okeya/raw_data/kotlinx-multithread-dataframe-design/outputs/" + fileName.split("/").last()
        println( text )
        PrintWriter(saveName).append(text).close()
      }
    } catch ( e : java.lang.Exception ) {
      println(e)
    }
    
  }

  val json = JSON.stringify(Data("Key", 42, "Default", 1000L, true, null)) 
  println( json )

  // フィールドが存在しないと、kotlinx.serialization.MissingFieldException を送出する
  try {
    val obj = JSON.parse<Data>("""{"a":42}""") // Data(a=42, b="42")
  } catch( e : kotlinx.serialization.MissingFieldException  ) {
    println( e )
  }
  // 問題なく復元できることの確認
  val obj = JSON.parse<Data>(json)
  println( obj )

  (0..1000).map { key ->
    val job = launch { // launch new coroutine
      delay(1L) // non-blocking delay for 0.001 second (default time unit is ms)
      val jedis = Jedis("localhost")
      val keyStr = key.toString()
      val json = JSON.stringify(Data(keyStr, 42, "Default", 1000L, true, null)) 
      jedis.set(keyStr, json)
      //println( jedis.get(keyStr) )
    }
    job
  }.let { 
    it.map { it.join() }
  }

}
