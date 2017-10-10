import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

import redis.clients.jedis.*

import kotlinx.coroutines.experimental.*
import kotlin.concurrent.thread

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop

import org.apache.commons.math3.stat.StatUtils.*

import com.atilika.kuromoji.ipadic.neologd.Tokenizer
import com.atilika.kuromoji.ipadic.neologd.Token
import com.atilika.kuromoji.ipadic.Tokenizer.Builder

import java.io.File
import java.nio.file.*
import java.io.PrintWriter
import java.util.Random
import java.lang.Exception

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

fun rankingTitleStarMean() {
  transaction { 
    create( CinemaDataFrame ) // create table
    CinemaDataFrame.selectAll().map {
      val obj = JSON.parse<CinemaData>( it[CinemaDataFrame.text] )
      obj
    }.map { 
      val stars = it.stars 
      val title = it.title
      Pair(title, stars)
    }.filter {
      it.second != null
    }.groupBy { 
      it.first
    }.toList()
    .sortedBy {
      it.first
    }.filter {
      it.second.size >= 4
    }.map {
      val title = it.first
      val arr = it.second
      val size = arr.size
      val valarr = arr.map { it.second!! }
      val meanval = mean( valarr.toDoubleArray() )
      val maxval = max( valarr.toDoubleArray() )
      val minval = max( valarr.toDoubleArray() )
      val varval = variance( valarr.toDoubleArray() ) 
      //mean( ArrayList(valarr) )
      println("$title ${meanval}")
      Pair(title, listOf<Any>(meanval, maxval, minval, varval, size))
    }.sortedBy {
      it.second[0] as Double
    }.map {
      println("${it.first}, ${it.second}")
    }
  }
}

object CinemaTFDataFrame : Table() {
  val hash = integer("hash").primaryKey() // Column<Int>
  val text = varchar("json", 100000) // Column<String>
}

@Serializable
data class StarTF(val stars:Double?, val tf:Map<String, Double>)
fun wakati() {

  val tokenizer = Tokenizer()
  val startfs = mutableListOf<StarTF>()
  transaction { 
    create( CinemaDataFrame ) // create table
    CinemaDataFrame.selectAll().map {
      val obj = JSON.parse<CinemaData>( it[CinemaDataFrame.text] )
      obj
    }.map { 
      val stars = it.stars 
      val title = it.title
      val review = it.review
      //println( review )
      val terms = tokenizer.tokenize(review).map {
        Pair(it.getSurface(), it.getAllFeatures())
      }.map {  
        val (term, feats) = it
        term
      }
      val tf = terms.groupBy  {
        it
      }.toList()
      .map {
        val term = it.first
        val freq = it.second.size!!
        Pair(term, freq.toDouble())
      }.toMap()
      
      val startf = StarTF(stars, tf)
      startfs.add( startf )
    }
  } // end sql transaction
  val termSet = startfs.map { startf ->
    startf.tf.keys.toList()
  }.flatten().toSet()
  
  val termIndex = mutableMapOf<String, Int>()
  termSet.mapIndexed { i, term ->
    termIndex[term] = i
  }
  //println(termIndex)
  
  val dataSource = startfs.filter {
    it.stars != null 
  }

  val ys = dataSource.map { 
    when { 
      it.stars!! > 3.5 -> 1.0
      else -> 0.0
    }
  }
  val Xs = dataSource.map {
    val indexWeight = it.tf.toList().map {
      val term = it.first
      val weight = it.second
      Pair( termIndex[term]!!, weight )
    }.toMap<Int, Double>()
    val X = (0..termSet.size - 1).map { i ->
      when { 
        indexWeight[i] != null -> Pair(i, indexWeight[i]!!)
        else -> null
      }
    }.filter { 
      it != null
    }
    X
  } as List<List<Pair<Int, Double>>>
  //prepare dataset
  //LogRegWrapperKt.exec(ys, Xs)
  val weights = LogRegWrapper.exec(ys, Xs)
  val indexTerm = termIndex.toList().map {
    val term = it.first
    val index = it.second
    Pair(index, term)
  }.toMap()
  weights.mapIndexed { index, weight ->
    val term = indexTerm[index]
    when { 
      term != null -> Pair(term, weight)
      else -> null
    }
  }.filter {
    it != null
  }.map { 
    it!! 
  }.sortedBy {
    it.second*-1
  }.map {
    println(it)
  }
}

object CinemaAnalyze {
  fun main(args: Array<String >)  {
    val kargs = args.toList().map { it.toString() }
    // データを永続化するにはmysqlなどをバックエンドにする
    // DBはデータベース名で、必要に応じて変える
    Database.connect("jdbc:mysql://localhost:3306/cinema", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "root")
    
    when {
      kargs.contains("flashDB") -> flashDB()
      kargs.contains("getHistgram") -> getHistgram()
      kargs.contains("rankingTitleStarMean") -> rankingTitleStarMean()
      kargs.contains("wakati") -> wakati()
      else -> null
    }
    if( kargs.contains("flashDB") )  {
      flashDB() 
    }
  }
}
