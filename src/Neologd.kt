import com.atilika.kuromoji.ipadic.neologd.Tokenizer
import com.atilika.kuromoji.ipadic.neologd.Token
import com.atilika.kuromoji.ipadic.Tokenizer.Builder

object Neologd {
  val tokenizer = Tokenizer()
  fun main( args : Array<String> ) {
    tokenizer.tokenize("一夜一夜に人見頃").map {
      Pair(it.getSurface(), it.getAllFeatures())
    }.map { 
      println(it)
    }
  }
}
