import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Random
object GsonDemo {

  val gson = Gson()
  val type = object : TypeToken<Map<Int, List<String>>>() {}.type
  val random = Random()
  fun main( args : Array<String> ) {
    println("This is test for Google Gson")
    val map = mutableMapOf<Int, List<String>>( 0 to listOf("これは", "テスト"))
    // serialize
    val json = gson.toJson(map)
    println( json )

    val recover:Map<String,List<String>> = gson.fromJson<Map<String, List<String>>>(json, type)
    println( recover )
  }
}
