import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

import redis.clients.jedis.*

import kotlinx.coroutines.experimental.*

@Serializable
data class Data(val key:String, val a: Int, @Optional val b: String = "42", val c: Long, val d: Boolean, val e : String?)

fun main(args: Array<String>) = runBlocking<Unit> {
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
      println( jedis.get(keyStr) )
    }
    job
  }.let { 
    it.map { it.join() }
  }

}
