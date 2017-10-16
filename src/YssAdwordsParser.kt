import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

import redis.clients.jedis.*

import kotlinx.coroutines.experimental.*

import java.io.File
import java.nio.file.*
import java.io.PrintWriter
import java.util.Random
import java.lang.Exception
//@Serializable
//data class Data(val key:String, val a: Int, @Optional val b: String = "42", val c: Long, val d: Boolean, val e : String?)

object YssAdwordsParser {
  fun Adword( fileName:String, keys:List<String>, iter:Iterator<String>) {
    println(fileName)
    println(keys)
    val df = mutableListOf<Map<String,String>>()
    iter
      .forEach { line ->
        if ( Math.random() < 0.65 ) {
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
  }
  
  fun Yss( fileName:String, keys:List<String>, iter:Iterator<String>) {
    println(fileName)
    println(keys)
    val df = mutableListOf<Map<String,String>>()
    iter
      .forEach { line ->
        if ( Math.random() < 0.65 ) {
          val vals = line.split(",")
          val obj = keys.zip(vals).toMap()
          //println(obj)  
          df.add( obj )
        }
    }
    df.map { 
      val keyword = it["キーワード"]
      val imps = it["インプレッション"]?.toDouble() ?: 0.0
      //val campid = it["Campaign ID"]
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
  }

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
      val iter:Iterator<String> = lines.iterator()
      try {
        when {
          fileName.contains("ADWORDS") -> { 
            iter.next() 
            val keys = iter.next().split(",") 
            Adword(fileName, keys, iter)
          }
          fileName.contains("YSS_KEYWORDS_PERFORMANCE_REPORT") -> {
            val keys = iter.next().split(",") 
            Yss(fileName, keys, iter)
          }
          else -> continue@fileScan
        }
      } catch ( e : java.lang.Exception ) {
        println(e)
      }
    }
  }
}
