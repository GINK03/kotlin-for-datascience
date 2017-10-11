# Example Kotlin dataframe desings

# Requirements
- OracleJDK 8(not openjdk or Oracle JDK9)
- redis-server

# ArchLinux Install of JDK
Oracleは別途AURにあるらしいがyaourtより自分でパッケージをビルドしてしまった方がいい
例えば、OracleJDK8はこのように行う
```console
$ git clone https://aur.archlinux.org/jdk8.git
$ cd jdk8
$ makepkg
$ sudo pacman -U  jdk8-8u144-1-x86_64.pkg.tar.xz
$ archlinix-java status
$ sudo archlinux-java set java-8-jdk
```
このようなオペレーションが別途必要

# Run With Argments
GradleでArgmentsを指定する方法がよくわからなかったが、このように実行することで実現できる
```console
$ ./gradlew run -Dexec.args="placeholder arg1 arg2"
```
長くてめんどくさいね

=================

## Kotlin Machine Learning

### TL;DR
Kotlinを用いるデータ分析と、簡単な統計分析、一部の機械学習ができることを示します。  


### ラムダ式を通して任意の結果を得る

KotlinはApache SparkでのScalaのシンタックスの流れを汲んでおり、複雑なデータ操作が可能です　　

Apache SparkにはRDDというデータフレームがあって、データフレームを用いてデータオペレーションができるのですが、KotlinやRubyなどのListに対する操作と似た感覚で使うことができます　　

具体的には、RDDとKotlinにはこのようなList, Set, Mapなど代表t系なデータ型に対するメソッドが用意されており、SQLで操作できるデータオペレーションと同等かそれ以上のことが可能です　　
(詳細が知りたい方は、[こちらを参照](https://github.com/GINK03/data-analysis-design-patterns)していただけると幸いです)  

### よく使う関数

Kotlinでよく使う関数としてこのようなものがあります  
```console
filter: 条件を満たす要素のみ抽出する
map: 各要素に関数を適用して別の要素に変換する
flatMap: 各要素に関数を適用して別の要素に変換する。関数の返り値はイテレータ
distinct: 重複する要素を取り除く
reduce: 畳み込みを行うような計算をします。系列の和を求めたり、積を求めたりします
groupBy: 特定のキーにてデータを転置・直列化して変換する
```
データとそれを処理するラムダ式は、圏論という数学の一分野で扱うことが可能です  

例えば、[この文章を参考にする](http://choreographlife.jp/pdf/intro.pdf)と、型を導入したラムダ式は、カルテシアン閉圏と対応することがわかります  

完全に適応できなくても、始域や終域の制約を外した圏論の状態とも捉えられ、部分的な圏論の知識を用いることで、単射か、全射か、そのラムダ式の並びはモニックなのか、エピックなのかという視点を追加することで、一般的な理論に還元した状態で、データオペレーションを扱うことができます  

これらを使いこなせ、関数の特徴を理解すると便利であり、groupByなど一部処理でメモリがマシンから溢れることがわかるので、本当にどこでマシンをスケールアウトすれば良いのかわかりやすいのと、様々な細やかなオペレーションが可能です。　　

### データの永続化と、DSL SQLによるデータ操作
#### Kotlinx Exposed
計算した結果や、一次加工、二時加工したデータをどこかに保存しておくと、非常に便利なことがあります。　　
データを加工した内容を何らかの方法でディスクやSQLに書き出す必要がありますが、SQLを使うことが多い人がそれなりにいらっしゃるかと思います。　　

DSL（Domain Specific Language）か扱える[Kotlinx Exposed](https://github.com/JetBrains/Exposed)にて、KotlinやScalaはSQLのオペレーションがOR Mapperの操作を容易にしています。　　

例えば、Kotlinx ExposedというKotlinを作成したJetBrain社のライブラリで、SQLで特定のテーブルを作り、データを代入するにはこのようにすればいいので、わかりやすいです
```kotlin
// テーブルの構造をこのように定義して、プログラムの中で利用できる
object CinemaDataFrame : Table() {
  val id = integer("id").autoIncrement().primaryKey() // Column<Int>
  val someInt = integer("someInt") // Column<Int>
  val text = varchar("someText", 100000) // Column<String>
}
```
 最初にデータベースとJDBCとコネクトします  
 コネクトすると、このようにしてデータを書き込むことができます
```kotlin
 fun someFunction() {
  Database.connect("jdbc:mysql://localhost:3306/cinema", driver = "com.mysql.cj.jdbc.Driver", user = "root", password = "root")
  transaction { 
     (0..100).map { index ->
       CinemaDataFrame.insert {
         it[someInt] = index
         it[text] = index.toString()
       }
     }
   }
}
```
全ての要素を取り出すには、このようにすることで取り出すことができます　　
```kotlin
CinemaDataFrame.selectAll().map { 
  // enter some operation...
  it[id] // idのフィールドを取り出せる
  it[someInt] // someIntのフィールドを取り出せる
  it[text] // textのフィールドを取り出せる
}
```
また、プログラム中にロジックを書かなくても、SQL程度であれば、似た記述方法で同等の結果を得ることができます　　
```kotlin
CinemaDataFrame.selectAll().groupBy(CinemaDataFrame.someInt)).forEach {
   val cinemaText = it[CinemaDataFrame.text]
   val cinemaCount = it[CinemaDataFrame.id.count()]
   if (cinemaCount > 0) {
     println("$cinemasCount cinema(s) $cinemaText")
   } else {
     println("Noone in $cinemaText")
   }
}
```

 ### データ構造のシリアライズとデシリアライズ
 #### Kotlinx Serialization
 様々なデータオブジェクトのシリアライズした内容を保存しておくことで、あたかも一つのクラス・オブジェクトを平文にしてマシン間の転送や、先ほどのSQLを利用することで、オブジェクト丸ごとの永続化などを行うことができます　　
 
 JavaScriptのオブジェクトのシリアライザ・デシリアライザであるJSON.stringfy、JSON.parseというそのままの関数が使える便利な[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)というものがあります。  
 
 Kotlinx Serializationではdata classと呼ばれるセッターゲッターなどが色々まとまったclassの定義の上に@でノーテーションをつけることで、シリアライズ可能になります
```kotlin
import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

@Serializable
data class Data(val a: Int, @Optional val b: String = "42")

fun serialize() {
  println(JSON.stringify(Data(42))) // {"a": 42, "b": "42"}
}
```
 デシリアライズも逆の手順を踏むことで行えます
```kotlin
fun desrialize() {
  val obj = JSON.parse<Data>("""{"a":42}""") // Data(a=42, b="42")
 }
``` 
JSON以外にもKotlinx SerializationはGoogleのシリアライザなどであるProtocol Bufferなどをサポートしています　

#### Google Gson
もう一つの選択肢として、GoogleのシリアライザであるGsonなども便利です。  
GsonはKotlinx Serializationと異なりノーテーションがなくても、シリアライズとデシリアライズが可能であり、Map<String, Map<String, Any>>のような複雑な型にも適応可能であり、Kotlinxで用が足りない時には選択肢に入れると良いです　　
```kotlin
fun serialize() {
  val gson = Gson() 
  val map = mutableMapOf<Int, List<String>>( 0 to listOf("これは", "テスト"))
  val json = gson.toJson(map) 
}
```
デシリアライズには型パラメータのような変数を定義して、型情報を与えて任意の型にデシリアライズします  
```kotlin
fun deserialize(json: String) {
  val type = object : TypeToken<Map<Int, List<String>>>() {}.type 
  val recover:Map<String,List<String>> = gson.fromJson<Map<String, List<String>>>(json, type) 
  println( recover ) 
}
```

### PureJVM形態素解析
#### Kuromoji Neologd
幾人かの人が[形態素解析のライブラリ](https://github.com/atilika/kuromoji)を提供してくださっているおかげで、neologdなどの最新の辞書を追加した状態で、JVMのみで形態素解析が可能になりました  
githubから該当のプロジェクトをクローンしてmvn packageで一つにまとめたパッケージを作ることで簡単に再利用可能なjarファイルを得ることができます　　
これをKotlinをコンパイルする際のbuild.gradleなどに依存を追加することで、Kotlinでも利用可能です　　
```console
$ git clone https://github.com/atilika/kuromoji
$ cd kuromoji
$ mvn package
//  作成したjarファイルを利用することで、neologdの辞書を反映したモデルを構築可能
```
Kotlinで形態素解析する例です
```kotlin
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
```
実行すると、形態素解析できていることがわかります
```console
$ ./gradlew run -Dexec.args="neologd"
(一夜一夜に人見頃, 名詞,固有名詞,一般,*,*,*,一夜一夜に人見頃,ヒトヨヒトヨニヒトミゴロ,ヒトヨヒトヨニヒトミゴロ)

BUILD SUCCESSFUL
```

### mean, variance, mode,  normalize, chi square testなどを求める

Javaの統計ツールである、StatUtilsが用いられるのですがこのように用いることができます  

Chi Square Testなどは、[事象の発生回数をカウント](http://www.statisticshowto.com/probability-and-statistics/chi-square/)していけば、p-valueを返却してくれるので、この値が十分小さければ(例えば0.05以下)、有意であると言えそうです　　

試しに、[randomを二回重ねて、少しだけコクのある乱数](https://togetter.com/li/1044668)にすると、p-valueは0になり、ただのrandomとは異なり、別の事象であるということができそうです。
```kotlin
mport java.util.Random
import org.apache.commons.math3.stat.*
import org.apache.commons.math3.stat.inference.ChiSquareTest
object Stat {
  val random = Random()

  fun main( args : Array<String> ) {
    val nm = (0..10000).map { Math.random() }

    val mean = StatUtils.mean( nm.toDoubleArray() )
    println( "mean ${mean}" )
    val geometricMean = StatUtils.geometricMean( nm.toDoubleArray() )
    println( "geometricMean ${geometricMean}" )
    val populationVariance = StatUtils.populationVariance( nm.toDoubleArray() )
    println( "populationVariance ${populationVariance}")
    val variance = StatUtils.variance( nm.toDoubleArray() )
    println( "variance ${variance}" )

    val nn = (0..1000).map { 0L }.toMutableList()
    (0..1000000).map { (Math.random()*1000 + 0.5).toInt() }.map {
      nn[it] += 1L
    }
    val mm = (0..1000).map{ 0L }.toMutableList()
    (0..1000000).map { ( (Math.random() + Math.random())/2.0*1000 + 0.5).toInt() }.map {
      mm[it] += 1L
    }
    val oo = (0..1000).map { 0L }.toMutableList()
    (0..1000000).map { (Math.random()*1000 + 0.5).toInt() }.map {
      oo[it] += 1L
    }
    val t = ChiSquareTest()
    val pval = t.chiSquareTestDataSetsComparison(mm.toLongArray(), nn.toLongArray())
    println("p-value mm <-> nn ${pval}")
    val pval2 = t.chiSquareTestDataSetsComparison(oo.toLongArray(), nn.toLongArray())
    println("p-value oo <-> nn ${pval2}")
  }
}
```

### 機械学習ツールを使う
#### Liblinear-Javaを用いる
LiblinearもJavaに移植されていて、使うことができます。  
interfaceは当然、Javaなのですが、KotlinがJavaと互換性があるおかげで、Liblinear-Javaを実行することが可能です  

例えば、2クラス分類ではこのようにすることで行えます  
この例ではliblinear フォーマットに従った出力を/tmp/logregwrapperに出力して、それで学習しています
```kotlin
    PrintWriter("/tmp/logregwrapper").append(text).close()
    val prob = Problem.readFromFile( File("/tmp/logregwrapper"), -1.0)
    val solver = SolverType.MCSVM_CS // -s 0
          val C = 100.0 // cost of constraints violation
          val eps = 0.001 // stopping criteria
    val parameter = Parameter(solver, C, eps)
    val model:Model = Linear.train(prob, parameter)

    val featWeights:List<Double> = model.getFeatureWeights().toList()
```
